package com.cobalto.features.auth.data.repository

import java.io.File
import java.util.prefs.Preferences

actual suspend fun checkFreshInstallAndClearSessionIfNeeded(signOutAction: suspend () -> Unit) {
    try {
        val userDir = System.getProperty("user.dir") ?: return
        val markerFile = File(userDir, ".materials_install_marker")
        if (!markerFile.exists()) {
            // Fresh install detected on desktop! Clear session/preferences
            try {
                signOutAction()
            } catch (_: Exception) {}
            try {
                Preferences.userNodeForPackage(AuthRepositoryImpl::class.java).clear()
            } catch (_: Exception) {}

            try {
                markerFile.createNewFile()
            } catch (_: Exception) {}
        }
    } catch (_: Exception) {}
}
