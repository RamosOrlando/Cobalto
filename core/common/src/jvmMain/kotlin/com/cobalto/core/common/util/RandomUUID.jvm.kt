package com.cobalto.core.common.util

import java.util.UUID
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

actual fun randomUUID(): String = UUID.randomUUID().toString()

actual fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date())
}
