package com.example.data

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Cloud-based authentication manager using Supabase Auth.
 * Replaces LocalAuthManager for online mode.
 *
 * Features:
 * - Email/password sign up and sign in
 * - Session persistence (auto-restored on app restart)
 * - Password change
 * - Sign out
 *
 * The local LocalAuthManager is still used as fallback when Supabase is not configured.
 */
class CloudAuthManager {

    companion object {
        private const val TAG = "CloudAuth"
    }

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: Flow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: Flow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: Flow<String?> = _currentUserId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()

    private suspend fun ensureAuthReady() {
        try {
            supabaseClient.auth.awaitInitialization()
            if (supabaseClient.auth.currentSessionOrNull() == null) {
                Log.d(TAG, "No existing session found; loading from storage")
                runCatching {
                    supabaseClient.auth.loadFromStorage()
                }.onFailure {
                    Log.w(TAG, "Failed to load auth session from storage", it)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Auth plugin initialization failed", e)
        }
    }

    /**
     * Check if user has an existing Supabase session (auto-restored).
     */
    suspend fun restoreSession(): Result<Unit> {
        if (!SupabaseConfig.isConfigured) {
            Log.w(TAG, "Supabase not configured, skipping session restore")
            return Result.failure(Exception("Supabase not configured"))
        }
        return try {
            _isLoading.value = true
            ensureAuthReady()
            val user = supabaseClient.auth.currentUserOrNull()
            if (user != null) {
                _isAuthenticated.value = true
                _currentUserEmail.value = user.email
                _currentUserId.value = user.id
                Log.d(TAG, "Session restored for: ${user.email}")
                Result.success(Unit)
            } else {
                _isAuthenticated.value = false
                Result.failure(Exception("No active session"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Session restore failed", e)
            _isAuthenticated.value = false
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Sign up a new user with email and password.
     */
    suspend fun signUp(email: String, password: String, displayName: String): Result<String> {
        if (!SupabaseConfig.isConfigured) {
            return Result.failure(Exception("Cloud backend not configured. Please set up Supabase first."))
        }
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email cannot be empty"))
        if (password.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))

        return try {
            _isLoading.value = true
            ensureAuthReady()
            supabaseClient.auth.signUpWith(Email) {
                this.email = email.trim().lowercase()
                this.password = password
                data = buildJsonObject {
                    put("display_name", displayName.ifBlank { email.substringBefore("@") })
                    put("role", "user")
                }
            }
            val user = supabaseClient.auth.currentUserOrNull()
            if (user == null) {
                Log.e(TAG, "Sign up succeeded but no user session was found")
                return Result.failure(Exception("Sign up succeeded but no session was created"))
            }
            _isAuthenticated.value = true
            _currentUserEmail.value = user.email
            _currentUserId.value = user.id
            Log.d(TAG, "Sign up successful: $email")
            Result.success(user.id)
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Sign in with email and password.
     */
    suspend fun signIn(email: String, password: String): Result<String> {
        if (!SupabaseConfig.isConfigured) {
            return Result.failure(Exception("Cloud backend not configured. Please set up Supabase first."))
        }
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Please fill in all fields"))
        }

        return try {
            _isLoading.value = true
            ensureAuthReady()
            supabaseClient.auth.signInWith(Email) {
                this.email = email.trim().lowercase()
                this.password = password
            }
            val user = supabaseClient.auth.currentUserOrNull()
            if (user == null) {
                Log.e(TAG, "Sign in succeeded but no user session was found")
                return Result.failure(Exception("Sign in succeeded but no active session was found"))
            }
            _isAuthenticated.value = true
            _currentUserEmail.value = user.email
            _currentUserId.value = user.id
            Log.d(TAG, "Sign in successful: $email")
            Result.success(user.id)
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Sign out and clear session.
     */
    suspend fun signOut() {
        try {
            if (SupabaseConfig.isConfigured) {
                supabaseClient.auth.signOut()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error", e)
        } finally {
            _isAuthenticated.value = false
            _currentUserEmail.value = null
            _currentUserId.value = null
        }
    }

    /**
     * Change password for current user.
     */
    suspend fun changePassword(newPassword: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) {
            return Result.failure(Exception("Cloud backend not configured"))
        }
        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return try {
            _isLoading.value = true
            ensureAuthReady()
            supabaseClient.auth.updateUser {
                this.password = newPassword
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password change failed", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Send password reset email.
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        if (!SupabaseConfig.isConfigured) {
            return Result.failure(Exception("Cloud backend not configured"))
        }
        return try {
            ensureAuthReady()
            supabaseClient.auth.resetPasswordForEmail(email.trim().lowercase())
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset failed", e)
            Result.failure(e)
        }
    }
}
