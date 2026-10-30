package com.cobalto.features.auth.presentation

enum class ForgotPasswordStep {
    EMAIL, OTP, NEW_PASSWORD
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    
    // Forgot Password State
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordStep: ForgotPasswordStep = ForgotPasswordStep.EMAIL,
    val forgotPasswordEmail: String = "",
    val forgotPasswordOtp: String = "",
    val forgotPasswordNewPassword: String = "",
    val forgotPasswordConfirmPassword: String = "",
    val forgotPasswordLoading: Boolean = false,
    val forgotPasswordError: String? = null,
    val forgotPasswordSuccess: Boolean = false,
)

sealed interface LoginEvent {
    data class OnEmailChanged(val email: String) : LoginEvent
    data class OnPasswordChanged(val password: String) : LoginEvent
    data object OnSignInClicked : LoginEvent
    data object OnSignUpClicked : LoginEvent
    data object ClearSuccess : LoginEvent

    // Forgot Password Events
    data object OnForgotPasswordClicked : LoginEvent
    data class OnForgotPasswordEmailChanged(val email: String) : LoginEvent
    data class OnForgotPasswordOtpChanged(val otp: String) : LoginEvent
    data class OnForgotPasswordNewPasswordChanged(val pass: String) : LoginEvent
    data class OnForgotPasswordConfirmPasswordChanged(val pass: String) : LoginEvent
    data object OnSendForgotPasswordEmailClicked : LoginEvent
    data object OnVerifyForgotPasswordOtpClicked : LoginEvent
    data object OnResetPasswordClicked : LoginEvent
    data object OnDismissForgotPassword : LoginEvent
}
