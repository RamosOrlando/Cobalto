package com.cobalto.core.network

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SettingsSessionManager
import com.russhwolf.settings.Settings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer

object SupabaseClient {
    // Pre-populated with your project ID from the provided URL
    const val SUPABASE_URL = "https://qewwdpltnibjdkmxkksk.supabase.co"
    
    // IMPORTANT: Replace this with your actual Supabase Anon / Public Key
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFld3dkcGx0bmliamRrbXhra3NrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODExODQ2NTIsImV4cCI6MjA5Njc2MDY1Mn0.2G76RZftmJ2b_p93Gg_oW499vROEqBlo24XD1md41UQ"

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            defaultSerializer = KotlinXSerializer()
            install(Postgrest) {
                // Enable logging for Postgrest to debug fetch issues
            }
            install(Auth) {
                sessionManager = SettingsSessionManager(Settings())
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
            }
            install(Realtime)
        }
    }
}
