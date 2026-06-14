package com.example.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferencesManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        "tuition_os_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getGeminiApiKey(): String =
        securePrefs.getString("gemini_api_key", "") ?: ""

    fun saveGeminiApiKey(key: String) {
        securePrefs.edit().putString("gemini_api_key", key.trim()).apply()
    }

    fun getGroqApiKey(): String =
        securePrefs.getString("groq_api_key", "") ?: ""

    fun saveGroqApiKey(key: String) {
        securePrefs.edit().putString("groq_api_key", key.trim()).apply()
    }

    fun getActiveAiService(): String =
        securePrefs.getString("active_ai_service", "Gemini") ?: "Gemini"

    fun saveActiveAiService(service: String) {
        securePrefs.edit().putString("active_ai_service", service).apply()
    }

    /**
     * One-time migration from plain SharedPreferences to EncryptedSharedPreferences.
     * Copies any existing API keys and clears the plain prefs afterwards.
     */
    fun migrateFromPlainPrefs(context: Context) {
        val plainPrefs = context.getSharedPreferences("tuition_os_prefs", Context.MODE_PRIVATE)
        val geminiKey = plainPrefs.getString("gemini_api_key", null)
        val groqKey = plainPrefs.getString("groq_api_key", null)
        val activeService = plainPrefs.getString("active_ai_service", null)

        val editor = securePrefs.edit()
        geminiKey?.let { if (it.isNotEmpty()) editor.putString("gemini_api_key", it) }
        groqKey?.let { if (it.isNotEmpty()) editor.putString("groq_api_key", it) }
        activeService?.let { if (it.isNotEmpty()) editor.putString("active_ai_service", it) }
        editor.apply()

        // Clear plain prefs to avoid stale unencrypted data
        plainPrefs.edit().clear().apply()
    }
}
