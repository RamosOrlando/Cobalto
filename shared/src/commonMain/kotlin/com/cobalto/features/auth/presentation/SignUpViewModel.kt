package com.cobalto.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobalto.core.domain.util.ResultState
import com.cobalto.features.auth.domain.repository.AuthRepository
import com.cobalto.features.auth.domain.util.AuthErrorMapper
import com.cobalto.features.user.domain.model.User
import com.cobalto.features.user.domain.model.UserProfession
import com.cobalto.features.user.domain.model.UserRole
import com.cobalto.features.user.domain.model.UserPlan
import com.cobalto.features.user.domain.model.SubscriptionHistory
import com.cobalto.features.user.domain.repository.UserRepository
import com.cobalto.core.common.util.getCurrentIsoDate
import com.cobalto.core.common.util.randomUUID
import com.cobalto.core.util.getDevicePhoneNumber
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class SignUpUiState(
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    val cellphone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val otpToken: String = "",
    val roleId: Int? = null,
    val professionId: Int? = null,
    val roles: List<UserRole> = emptyList(),
    val professions: List<UserProfession> = emptyList(),
    val activePlans: List<UserPlan> = emptyList(),
    val selectedPlanId: Int? = null,
    val existingSubscription: SubscriptionHistory? = null,
    val existingPlanName: String? = null,
    val showUserExistsDialog: Boolean = false,
    val waitingForEmailConfirmation: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isCancelled: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val cellphoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val professionError: String? = null,
    val planError: String? = null
)

sealed interface SignUpEvent {
    data class OnNameChanged(val name: String) : SignUpEvent
    data class OnLastNameChanged(val lastName: String) : SignUpEvent
    data class OnEmailChanged(val email: String) : SignUpEvent
    data class OnCellphoneChanged(val cellphone: String) : SignUpEvent
    data class OnPasswordChanged(val password: String) : SignUpEvent
    data class OnConfirmPasswordChanged(val confirmPassword: String) : SignUpEvent
    data class OnOtpTokenChanged(val token: String) : SignUpEvent
    data class OnRoleSelected(val roleId: Int) : SignUpEvent
    data class OnProfessionSelected(val professionId: Int) : SignUpEvent
    data class OnPlanSelected(val planId: Int) : SignUpEvent
    object OnCancelSignUp : SignUpEvent
    object OnSignUpClicked : SignUpEvent
    object OnVerifyOtpClicked : SignUpEvent
    object OnDismissUserExistsDialog : SignUpEvent
    object ClearSuccess : SignUpEvent
}

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    private var registeredUserId: String? = null

    init {
        try {
            val devicePhone = getDevicePhoneNumber()
            if (!devicePhone.isNullOrBlank() && devicePhone.length == 8) {
                _uiState.update { it.copy(cellphone = devicePhone) }
            }
        } catch (_: Exception) {}

        // Observar Roles
        userRepository.getRolesFlow()
            .onEach { res ->
                if (res is ResultState.Success<List<UserRole>>) {
                    _uiState.update { state ->
                        val roles = res.data
                        val defaultRoleId = if (state.roleId == null) {
                            roles.find { it.name.trim().equals("Cliente", ignoreCase = true) }?.roleId
                        } else state.roleId
                        state.copy(roles = roles, roleId = defaultRoleId)
                    }
                }
            }.launchIn(viewModelScope)

        // Observar Profesiones
        userRepository.getProfessionsFlow()
            .onEach { res ->
                if (res is ResultState.Success<List<UserProfession>>) {
                    _uiState.update { state ->
                        val professions = res.data
                        val defaultProfId = if (state.professionId == null) {
                            professions.find { it.name.contains("Ingeniero Civil", ignoreCase = true) }?.professionId
                        } else state.professionId
                        state.copy(professions = professions, professionId = defaultProfId)
                    }
                }
            }.launchIn(viewModelScope)

        // Observar Planes Activos
        userRepository.getActivePlansFlow()
            .onEach { res ->
                if (res is ResultState.Success<List<UserPlan>>) {
                    _uiState.update { it.copy(activePlans = res.data) }
                }
            }.launchIn(viewModelScope)

        // Disparar sincronización desde el servidor
        viewModelScope.launch {
            val result = userRepository.refreshMetadata()
            if (result is ResultState.Error) {
                _uiState.update { it.copy(error = "Error de sincronización: ${result.message}") }
            }
        }
    }

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.OnNameChanged -> {
                _uiState.update { it.copy(name = event.name, nameError = null, error = null) }
            }
            is SignUpEvent.OnLastNameChanged -> {
                _uiState.update { it.copy(lastName = event.lastName, lastNameError = null, error = null) }
            }
            is SignUpEvent.OnEmailChanged -> {
                _uiState.update { it.copy(email = event.email, emailError = null, error = null) }
            }
            is SignUpEvent.OnCellphoneChanged -> {
                _uiState.update { it.copy(cellphone = event.cellphone, cellphoneError = null, error = null) }
            }
            is SignUpEvent.OnPasswordChanged -> {
                _uiState.update { it.copy(password = event.password, passwordError = null, error = null) }
            }
            is SignUpEvent.OnConfirmPasswordChanged -> {
                _uiState.update { it.copy(confirmPassword = event.confirmPassword, confirmPasswordError = null, error = null) }
            }
            is SignUpEvent.OnOtpTokenChanged -> {
                _uiState.update { it.copy(otpToken = event.token, error = null) }
            }
            is SignUpEvent.OnRoleSelected -> {
                _uiState.update { it.copy(roleId = event.roleId, error = null) }
            }
            is SignUpEvent.OnProfessionSelected -> {
                _uiState.update { it.copy(professionId = event.professionId, professionError = null, error = null) }
            }
            is SignUpEvent.OnPlanSelected -> {
                _uiState.update { it.copy(selectedPlanId = event.planId, planError = null, error = null) }
            }
            SignUpEvent.OnCancelSignUp -> {
                _uiState.update { it.copy(isCancelled = true) }
            }
            SignUpEvent.OnSignUpClicked -> {
                signUp()
            }
            SignUpEvent.OnVerifyOtpClicked -> {
                verifyOtp()
            }
            SignUpEvent.OnDismissUserExistsDialog -> {
                _uiState.update { it.copy(showUserExistsDialog = false) }
            }
            SignUpEvent.ClearSuccess -> {
                _uiState.update { 
                    SignUpUiState(
                        roles = it.roles,
                        professions = it.professions,
                        activePlans = it.activePlans,
                        roleId = it.roles.find { r -> r.name.trim().equals("Cliente", ignoreCase = true) }?.roleId,
                        professionId = it.professions.find { p -> p.name.contains("Ingeniero Civil", ignoreCase = true) }?.professionId
                    )
                }
            }
        }
    }

    private fun signUp() {
        val state = uiState.value
        
        val trimmedName = state.name.trim()
        val trimmedLastName = state.lastName.trim()
        val trimmedEmail = state.email.trim()
        val trimmedCellphone = state.cellphone.trim()

        var nameError: String? = null
        var lastNameError: String? = null
        var emailError: String? = null
        var cellphoneError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null
        var professionError: String? = null
        var planError: String? = null

        var hasError = false

        if (trimmedName.isEmpty()) {
            nameError = "El nombre es obligatorio"
            hasError = true
        }
        if (trimmedLastName.isEmpty()) {
            lastNameError = "Los apellidos son obligatorios"
            hasError = true
        }
        if (trimmedEmail.isEmpty()) {
            emailError = "El correo es obligatorio"
            hasError = true
        } else if (!trimmedEmail.contains("@")) {
            emailError = "Formato de correo inválido"
            hasError = true
        }
        if (trimmedCellphone.isEmpty()) {
            cellphoneError = "El número de celular es obligatorio"
            hasError = true
        } else if (trimmedCellphone.toIntOrNull() == null || trimmedCellphone.length != 8) {
            cellphoneError = "El número de celular debe tener exactamente 8 dígitos"
            hasError = true
        }
        if (state.password.isEmpty()) {
            passwordError = "La contraseña es obligatoria"
            hasError = true
        } else if (state.password.length < 6) {
            passwordError = "Mínimo 6 caracteres"
            hasError = true
        }
        if (state.confirmPassword.isEmpty()) {
            confirmPasswordError = "Confirma tu contraseña"
            hasError = true
        } else if (state.password != state.confirmPassword) {
            confirmPasswordError = "Las contraseñas no coinciden"
            hasError = true
        }
        if (state.professionId == null) {
            professionError = "Selecciona una profesión"
            hasError = true
        }
        if (state.selectedPlanId == null) {
            planError = "Debes seleccionar un plan"
            hasError = true
        }

        if (hasError) {
            _uiState.update { it.copy(
                nameError = nameError,
                lastNameError = lastNameError,
                emailError = emailError,
                cellphoneError = cellphoneError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                professionError = professionError,
                planError = planError,
                error = "Por favor corrige los errores señalados"
            ) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 0. Check if user already exists in public.User table
            val existingUserResult = userRepository.getUserByEmail(trimmedEmail)
            
            if (existingUserResult is ResultState.Success && existingUserResult.data != null) {
                val existingUser = existingUserResult.data
                val userId = existingUser.userId
                
                // Try to fetch subscription history for the existing user
                userRepository.refreshSubscriptionHistory(userId)
                val historyResult = userRepository.getSubscriptionHistoryFlow(userId)
                    .filter { it !is ResultState.Loading }
                    .first()
                
                var latestSub: SubscriptionHistory? = null
                var planName: String? = null
                
                if (historyResult is ResultState.Success && historyResult.data.isNotEmpty()) {
                    latestSub = historyResult.data.maxByOrNull { it.startDate }
                    planName = state.activePlans.find { it.planId == latestSub?.planId }?.name
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        showUserExistsDialog = true,
                        existingSubscription = latestSub,
                        existingPlanName = planName
                    )
                }
                return@launch // Stop sign up process
            }

            // 1. Create Auth User
            val authResult = authRepository.signUpWithEmail(trimmedEmail, state.password, trimmedName)
            
            authResult.onSuccess { userId ->
                registeredUserId = userId
                _uiState.update { it.copy(isLoading = false, waitingForEmailConfirmation = true) }
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = AuthErrorMapper.mapThrowableToUserMessage(e)
                    ) 
                }
            }
        }
    }

    private fun verifyOtp() {
        val state = uiState.value
        if (state.otpToken.length != 6) {
            _uiState.update { it.copy(error = "El código de verificación debe ser de 6 dígitos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.verifyEmailOtp(state.email.trim(), state.otpToken)
            result.onSuccess {
                // Now authenticated, create the profile
                val newUser = User(
                    userId = authRepository.getCurrentUserId()!!,
                    name = state.name.trim(),
                    lastName = state.lastName.trim(),
                    email = state.email.trim(),
                    roleId = state.roleId!!,
                    professionId = state.professionId!!,
                    createdAt = getCurrentIsoDate(),
                    cellphone = state.cellphone.trim().toIntOrNull()
                )
                userRepository.saveUser(newUser)
                subscribeToPlan(state.selectedPlanId!!)
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = AuthErrorMapper.mapThrowableToUserMessage(e)
                    ) 
                }
            }
        }
    }

    private fun subscribeToPlan(planId: Int) {
        val plan = uiState.value.activePlans.find { it.planId == planId } ?: return
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Calcular fecha de fin basada en durationDays
            val now = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val endDate = now.plus(plan.durationDays, DateTimeUnit.DAY, timeZone)

            val history = SubscriptionHistory(
                subHistoryId = randomUUID(),
                userId = userId,
                planId = planId,
                startDate = now.toString(),
                endDate = endDate.toString(),
                state = "ACTIVE",
                pricePaid = plan.price,
                discountAmount = 0f
            )

            val result = userRepository.saveSubscriptionHistory(history)
            if (result is ResultState.Success<*>) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Error al suscribirse: ${(result as ResultState.Error).message}") }
            }
        }
    }
}
