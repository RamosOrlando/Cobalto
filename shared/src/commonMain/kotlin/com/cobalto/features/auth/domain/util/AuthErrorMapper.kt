package com.cobalto.features.auth.domain.util

object AuthErrorMapper {

    private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    }

    /**
     * Convierte una excepción técnica de autenticación en un mensaje claro,
     * profesional y amigable para el usuario final.
     */
    fun mapThrowableToUserMessage(throwable: Throwable?): String {
        if (throwable == null) return "Ha ocurrido un error inesperado. Por favor intenta nuevamente."
        return mapErrorMessageToUserMessage(throwable.message)
    }

    fun mapErrorMessageToUserMessage(message: String?): String {
        if (message.isNullOrBlank()) return "Ha ocurrido un error inesperado. Por favor intenta nuevamente."

        val lowerMessage = message.lowercase()

        return when {
            // Credenciales incorrectas (Correo o contraseña erróneos)
            lowerMessage.contains("invalid login credentials") ||
            lowerMessage.contains("invalid_credentials") ||
            lowerMessage.contains("invalid credentials") ||
            lowerMessage.contains("wrong password") ||
            lowerMessage.contains("invalid password") -> {
                "El correo electrónico o la contraseña son incorrectos. Verifícalos e intenta de nuevo."
            }

            // Correo no confirmado
            lowerMessage.contains("email not confirmed") ||
            lowerMessage.contains("email_not_confirmed") -> {
                "Tu correo electrónico aún no ha sido confirmado. Revisa tu bandeja de entrada o spam."
            }

            // Usuario no encontrado
            lowerMessage.contains("user not found") ||
            lowerMessage.contains("user_not_found") -> {
                "No existe una cuenta registrada con este correo electrónico."
            }

            // Correo no válido / formato incorrecto
            lowerMessage.contains("invalid email") ||
            lowerMessage.contains("invalid_email") ||
            lowerMessage.contains("unable to validate email") ||
            lowerMessage.contains("format error") -> {
                "El correo electrónico ingresado no tiene un formato válido (ej. usuario@dominio.com)."
            }

            // Usuario ya registrado (SignUp)
            lowerMessage.contains("user already registered") ||
            lowerMessage.contains("user_already_exists") ||
            lowerMessage.contains("already exists") -> {
                "Este correo electrónico ya se encuentra registrado. Intenta iniciar sesión."
            }

            // Contraseña débil / demasiado corta
            lowerMessage.contains("password should be at least") ||
            lowerMessage.contains("weak password") ||
            lowerMessage.contains("password_too_short") -> {
                "La contraseña debe tener al menos 6 caracteres."
            }

            // Límite de intentos / Rate limit
            lowerMessage.contains("rate limit") ||
            lowerMessage.contains("too many requests") ||
            lowerMessage.contains("over_email_send_rate_limit") -> {
                "Demasiados intentos fallidos. Por favor, espera unos minutos antes de reintentar."
            }

            // Token OTP inválido o expirado
            lowerMessage.contains("token has expired") ||
            lowerMessage.contains("invalid token") ||
            lowerMessage.contains("invalid otp") ||
            lowerMessage.contains("otp_expired") ||
            lowerMessage.contains("invalid_grant") -> {
                "El código de verificación es incorrecto o ha expirado."
            }

            // Problemas de Red / Conexión
            lowerMessage.contains("unable to resolve host") ||
            lowerMessage.contains("failed to connect") ||
            lowerMessage.contains("network") ||
            lowerMessage.contains("connectexception") ||
            lowerMessage.contains("unknownhostexception") ||
            lowerMessage.contains("sockettimeoutexception") ||
            lowerMessage.contains("timeout") ||
            lowerMessage.contains("httprequestexception") -> {
                "No se pudo conectar al servidor. Revisa tu conexión a internet e intenta de nuevo."
            }

            // Fallback general amigable
            else -> {
                "Ocurrió un inconveniente al procesar tu solicitud. Por favor intenta de nuevo."
            }
        }
    }
}
