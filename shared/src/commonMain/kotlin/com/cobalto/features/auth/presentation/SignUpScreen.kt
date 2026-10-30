package com.cobalto.features.auth.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobalto.core.presentation.theme.*
import com.cobalto.core.presentation.util.AdaptivePreviews
import com.cobalto.features.user.domain.model.SubscriptionHistory
import com.cobalto.features.user.domain.model.UserPlan
import kotlin.time.Clock
import kotlin.time.Instant
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: SignUpViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSignUpSuccess()
            viewModel.onEvent(SignUpEvent.ClearSuccess)
        }
    }

    LaunchedEffect(uiState.isCancelled) {
        if (uiState.isCancelled) {
            onBackClick()
            viewModel.onEvent(SignUpEvent.ClearSuccess)
        }
    }

    SignUpScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick
    )

    if (uiState.showUserExistsDialog) {
        UserExistsDialog(
            email = uiState.email,
            subscription = uiState.existingSubscription,
            planName = uiState.existingPlanName,
            onDismiss = { viewModel.onEvent(SignUpEvent.OnDismissUserExistsDialog) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreenContent(
    uiState: SignUpUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (SignUpEvent) -> Unit,
    onBackClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var professionExpanded by remember { mutableStateOf(false) }
    
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isCompact = !adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(600)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = if (isCompact) 600.dp else 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = IndustrialOrange
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Crear Cuenta",
                        style = MaterialTheme.typography.headlineSmall,
                        color = IndustrialOrange,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.waitingForEmailConfirmation) {
                    EmailConfirmationCard(
                        email = uiState.email,
                        otpToken = uiState.otpToken,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onOtpChange = { onEvent(SignUpEvent.OnOtpTokenChanged(it)) },
                        onVerifyClick = { onEvent(SignUpEvent.OnVerifyOtpClicked) }
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = IndustrialShapes.medium,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.name,
                                    onValueChange = { onEvent(SignUpEvent.OnNameChanged(it)) },
                                    label = { Text("Nombre") },
                                    modifier = Modifier.weight(1f),
                                    isError = uiState.nameError != null,
                                    supportingText = if (uiState.nameError != null) {
                                        { Text(uiState.nameError, color = MaterialTheme.colorScheme.error) }
                                    } else null,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Next
                                    ),
                                    singleLine = true,
                                    shape = IndustrialShapes.small,
                                    colors = signUpTextFieldColors()
                                )
                                OutlinedTextField(
                                    value = uiState.lastName,
                                    onValueChange = { onEvent(SignUpEvent.OnLastNameChanged(it)) },
                                    label = { Text("Apellidos") },
                                    modifier = Modifier.weight(1f),
                                    isError = uiState.lastNameError != null,
                                    supportingText = if (uiState.lastNameError != null) {
                                        { Text(uiState.lastNameError, color = MaterialTheme.colorScheme.error) }
                                    } else null,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Next
                                    ),
                                    singleLine = true,
                                    shape = IndustrialShapes.small,
                                    colors = signUpTextFieldColors()
                                )
                            }

                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = { onEvent(SignUpEvent.OnEmailChanged(it.trim())) },
                                label = { Text("Correo Electrónico") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = uiState.emailError != null,
                                supportingText = if (uiState.emailError != null) {
                                    { Text(uiState.emailError, color = MaterialTheme.colorScheme.error) }
                                } else null,
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                singleLine = true,
                                shape = IndustrialShapes.small,
                                colors = signUpTextFieldColors()
                            )

                            OutlinedTextField(
                                value = uiState.cellphone,
                                onValueChange = { 
                                    val trimmed = it.trim()
                                    if (trimmed.length <= 8 && trimmed.all { char -> char.isDigit() }) {
                                        onEvent(SignUpEvent.OnCellphoneChanged(trimmed))
                                    }
                                },
                                label = { Text("Celular *") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = uiState.cellphoneError != null,
                                supportingText = if (uiState.cellphoneError != null) {
                                    { Text(uiState.cellphoneError, color = MaterialTheme.colorScheme.error) }
                                } else null,
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                singleLine = true,
                                shape = IndustrialShapes.small,
                                colors = signUpTextFieldColors()
                            )

                            ExposedDropdownMenuBox(
                                expanded = professionExpanded,
                                onExpandedChange = { professionExpanded = !professionExpanded }
                            ) {
                                val selectedProfession = uiState.professions.find { it.professionId == uiState.professionId }?.name ?: "Selecciona tu Profesión"
                                OutlinedTextField(
                                    value = selectedProfession,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Profesión") },
                                    isError = uiState.professionError != null,
                                    supportingText = if (uiState.professionError != null) {
                                        { Text(uiState.professionError, color = MaterialTheme.colorScheme.error) }
                                    } else null,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = professionExpanded) },
                                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                    shape = IndustrialShapes.small,
                                    colors = signUpTextFieldColors()
                                )
                                ExposedDropdownMenu(
                                    expanded = professionExpanded,
                                    onDismissRequest = { professionExpanded = false }
                                ) {
                                    uiState.professions.forEach { profession ->
                                        DropdownMenuItem(
                                            text = { Text(profession.name) },
                                            onClick = {
                                                profession.professionId?.let { onEvent(SignUpEvent.OnProfessionSelected(it)) }
                                                professionExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Selecciona tu Plan",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (uiState.planError != null) MaterialTheme.colorScheme.error else IndustrialOrange,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.planError != null) {
                                Text(
                                    text = uiState.planError,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            // Plans in 2 columns
                            uiState.activePlans.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { plan ->
                                        val isSelected = uiState.selectedPlanId == plan.planId
                                        Card(
                                            onClick = { plan.planId?.let { onEvent(SignUpEvent.OnPlanSelected(it)) } },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(80.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) 
                                                    IndustrialOrange.copy(alpha = 0.1f) 
                                                else 
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            ),
                                            border = if (isSelected)
                                                BorderStroke(2.dp, IndustrialOrange)
                                            else null,
                                            shape = IndustrialShapes.small
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = plan.name,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "Bs. ${plan.price}",
                                                    color = IndustrialOrange,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                                Text(
                                                    text = "${plan.durationDays} días",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = { onEvent(SignUpEvent.OnPasswordChanged(it)) },
                                label = { Text("Contraseña (mínimo 6 digitos)") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = uiState.passwordError != null,
                                supportingText = if (uiState.passwordError != null) {
                                    { Text(uiState.passwordError, color = MaterialTheme.colorScheme.error) }
                                } else null,
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                singleLine = true,
                                shape = IndustrialShapes.small,
                                colors = signUpTextFieldColors()
                            )

                            OutlinedTextField(
                                value = uiState.confirmPassword,
                                onValueChange = { onEvent(SignUpEvent.OnConfirmPasswordChanged(it)) },
                                label = { Text("Confirmar Contraseña") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = uiState.confirmPasswordError != null,
                                supportingText = if (uiState.confirmPasswordError != null) {
                                    { Text(uiState.confirmPasswordError, color = MaterialTheme.colorScheme.error) }
                                } else null,
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                singleLine = true,
                                shape = IndustrialShapes.small,
                                colors = signUpTextFieldColors()
                            )

                            if (uiState.error != null) {
                                Text(
                                    text = uiState.error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Button(
                                onClick = { onEvent(SignUpEvent.OnSignUpClicked) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = IndustrialShapes.small,
                                colors = ButtonDefaults.buttonColors(containerColor = IndustrialOrange),
                                enabled = !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("REGISTRARSE", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmailConfirmationCard(
    email: String,
    otpToken: String,
    isLoading: Boolean,
    error: String?,
    onOtpChange: (String) -> Unit,
    onVerifyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = IndustrialShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MarkEmailRead,
                contentDescription = null,
                tint = IndustrialOrange,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Verifica tu cuenta",
                style = MaterialTheme.typography.titleLarge,
                color = IndustrialOrange,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Hemos enviado un código de 6 dígitos a:",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = IndustrialOrange
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = otpToken,
                onValueChange = { 
                    val trimmed = it.trim()
                    if (trimmed.length <= 6) onOtpChange(trimmed) 
                },
                label = { Text("Código de 6 dígitos") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                shape = IndustrialShapes.small,
                colors = signUpTextFieldColors(),
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            )

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onVerifyClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = IndustrialShapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialOrange),
                enabled = otpToken.length == 6 && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("VERIFICAR CÓDIGO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UserExistsDialog(
    email: String,
    subscription: SubscriptionHistory?,
    planName: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Usuario ya registrado",
                fontWeight = FontWeight.ExtraBold,
                color = IndustrialOrange
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "El correo $email ya se encuentra registrado en nuestro sistema.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (subscription != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Detalles de su último plan:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = IndustrialOrange
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Plan: ${planName ?: "Desconocido"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Adquirido: ${subscription.startDate.take(10)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Vence: ${subscription.endDate.take(10)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    val isExpired = try {
                        val endDate = Instant.parse(subscription.endDate)
                        endDate < Clock.System.now()
                    } catch (e: Exception) {
                        false
                    }

                    if (isExpired) {
                        Text(
                            text = "Su plan ha expirado. Puede seleccionar uno de los planes disponibles para renovar su acceso.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = "Su cuenta ya tiene un plan activo. Por favor, inicie sesión.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialOrange)
            ) {
                Text("Aceptar")
            }
        }
    )
}

@Composable
fun signUpTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = IndustrialOrange,
    cursorColor = IndustrialOrange,
    focusedLabelColor = IndustrialOrange
)

@AdaptivePreviews
@Composable
private fun SignUpScreenPreview() {
    IndustrialTheme {
        SignUpScreenContent(
            uiState = SignUpUiState(
                name = "John",
                lastName = "Doe",
                email = "john@example.com"
            ),
            snackbarHostState = SnackbarHostState(),
            onEvent = {},
            onBackClick = {}
        )
    }
}
