package com.example.data

import android.content.Context
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalAuthManager(private val context: Context) {

    private val db = TuitionDatabase.getInstance(context)
    private val userDao = db.userDao()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    val isLoggedIn: Boolean
        get() = _currentUser.value != null

    private val sessionPrefs
        get() = context.getSharedPreferences("tuition_os_auth", Context.MODE_PRIVATE)

    /**
     * Restore a previously logged-in session on app startup.
     */
    suspend fun restoreSession() {
        val userId = sessionPrefs.getInt("logged_in_user_id", -1)
        if (userId != -1) {
            val user = userDao.findById(userId)
            _currentUser.value = user
        }
    }

    /**
     * Create a new local account with BCrypt-hashed password.
     */
    suspend fun signUp(email: String, password: String, displayName: String): Result<UserEntity> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email cannot be empty"))
        if (password.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))

        val existing = userDao.findByEmail(email.trim().lowercase())
        if (existing != null) return Result.failure(IllegalStateException("An account with this email already exists"))

        val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        val user = UserEntity(
            email = email.trim().lowercase(),
            passwordHash = hash,
            displayName = displayName.ifBlank { email.substringBefore("@") },
            createdAt = System.currentTimeMillis()
        )
        val id = userDao.insert(user).toInt()
        val savedUser = user.copy(userId = id)
        setSession(savedUser)
        return Result.success(savedUser)
    }

    /**
     * Sign in with email and password with rate limiting.
     */
    suspend fun signIn(email: String, password: String): Result<UserEntity> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Please fill in all fields"))
        }

        // Rate limiting check
        val lockoutRemaining = getLockoutRemaining()
        if (lockoutRemaining > 0) {
            return Result.failure(IllegalArgumentException("Too many failed attempts. Please wait ${lockoutRemaining}s."))
        }

        val user = userDao.findByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("No account found with this email"))

        val result = BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash)
        if (!result.verified) {
            recordFailedAttempt()
            val attempts = getFailedAttempts()
            val remaining = if (attempts >= 10) "5 minutes" else if (attempts >= 5) "30 seconds" else "${5 - attempts} more attempts"
            return Result.failure(IllegalArgumentException("Incorrect password. ${if (attempts >= 5) "Account locked for $remaining." else "$remaining remaining."}"))
        }

        // Reset failed attempts on successful login
        resetFailedAttempts()
        setSession(user)
        return Result.success(user)
    }

    /**
     * Sign out and clear the session.
     */
    fun signOut() {
        _currentUser.value = null
        sessionPrefs.edit().clear().apply()
    }

    /**
     * Change the current user's password.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        val user = _currentUser.value
            ?: return Result.failure(IllegalStateException("Not logged in"))

        val verifyResult = BCrypt.verifyer().verify(currentPassword.toCharArray(), user.passwordHash)
        if (!verifyResult.verified) {
            return Result.failure(IllegalArgumentException("Current password is incorrect"))
        }

        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("New password must be at least 6 characters"))
        }

        val newHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray())
        val updated = user.copy(passwordHash = newHash)
        userDao.update(updated)
        _currentUser.value = updated
        return Result.success(Unit)
    }

    private fun setSession(user: UserEntity) {
        _currentUser.value = user
        sessionPrefs.edit()
            .putInt("logged_in_user_id", user.userId)
            .putString("logged_in_email", user.email)
            .apply()
    }

    // === Rate Limiting ===
    private val rateLimitPrefs
        get() = context.getSharedPreferences("tuition_os_rate_limit", Context.MODE_PRIVATE)

    private fun getFailedAttempts(): Int {
        return rateLimitPrefs.getInt("failed_attempts", 0)
    }

    private fun recordFailedAttempt() {
        val attempts = getFailedAttempts() + 1
        rateLimitPrefs.edit()
            .putInt("failed_attempts", attempts)
            .putLong("last_failed_time", System.currentTimeMillis())
            .apply()
    }

    private fun resetFailedAttempts() {
        rateLimitPrefs.edit()
            .putInt("failed_attempts", 0)
            .putLong("last_failed_time", 0)
            .apply()
    }

    fun getLockoutRemaining(): Long {
        val attempts = getFailedAttempts()
        if (attempts < 5) return 0
        val lastFailed = rateLimitPrefs.getLong("last_failed_time", 0)
        val lockoutMs = if (attempts >= 10) 5 * 60 * 1000L else 30 * 1000L
        val elapsed = System.currentTimeMillis() - lastFailed
        return if (elapsed < lockoutMs) (lockoutMs - elapsed) / 1000 else 0
    }
}
