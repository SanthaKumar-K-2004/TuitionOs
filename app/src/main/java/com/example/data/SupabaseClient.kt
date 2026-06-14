package com.example.data

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import com.example.BuildConfig

/**
 * Supabase Cloud Backend client for TuitionOS.
 *
 * HOW TO SET UP (free at supabase.com):
 * 1. Create a project at https://supabase.com
 * 2. Copy your Project URL and anon/public key
 * 3. Replace the values below (or use BuildConfig)
 *
 * Database tables are auto-created by Supabase when you first insert.
 * Enable Row Level Security (RLS) for production use.
 */
object SupabaseConfig {
    // Prefer BuildConfig fields (set via Gradle), then environment vars, then repo fallback.
    val SUPABASE_URL: String = (BuildConfig.SUPABASE_URL).takeIf { it.isNotBlank() }
        ?: System.getenv("SUPABASE_URL")
        ?: "https://mupyyohhtsmxqxrrzoea.supabase.co"

    val SUPABASE_KEY: String = (BuildConfig.SUPABASE_KEY).takeIf { it.isNotBlank() }
        ?: System.getenv("SUPABASE_KEY")
        ?: ""

    val isConfigured: Boolean
        get() = SUPABASE_URL.contains("supabase.co") && SUPABASE_KEY.startsWith("eyJ")

    init {
        Log.d("SupabaseConfig", "Supabase URL=$SUPABASE_URL configured=${isConfigured} keySet=${SUPABASE_KEY.isNotBlank()}")
    }
}

/**
 * Global Supabase client instance.
 * Initialized lazily - only created when Supabase is configured.
 */
val supabaseClient: SupabaseClient by lazy {
    createSupabaseClient(
        supabaseUrl = SupabaseConfig.SUPABASE_URL,
        supabaseKey = SupabaseConfig.SUPABASE_KEY
    ) {
        install(Auth) {
            alwaysAutoRefresh = true
            autoLoadFromStorage = true
        }
        install(Postgrest)
        install(Storage)
    }
}
