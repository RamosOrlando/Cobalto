package com.cobalto.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager
import org.koin.core.context.GlobalContext

@SuppressLint("MissingPermission", "HardwareIds")
actual fun getDevicePhoneNumber(): String? {
    return try {
        val context = GlobalContext.get().get<Context>() ?: return null
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return null
        val number = telephonyManager.line1Number ?: return null
        val cleaned = number.replace(Regex("[^0-9]"), "")
        if (cleaned.length >= 8) {
            cleaned.takeLast(8)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
