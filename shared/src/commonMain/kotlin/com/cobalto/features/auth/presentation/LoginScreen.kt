package com.cobalto.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobalto.core.presentation.theme.*
import com.cobalto.core.presentation.util.AdaptivePreviews
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
            viewModel.onEvent(LoginEvent.ClearSuccess)
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSignUp = onNavigateToSignUp
    )

    if (uiState.showForgotPasswordDialog) {
        ForgotPasswordDialog(
            uiState = uiState,
            onEvent = { event ->
                if (event is LoginEvent.OnDismissForgotPassword && uiState.forgotPasswordSuccess) {
                    onLoginSuccess()
                }
                viewModel.onEvent(event)
            }
        )
    }
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isCompact = !adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(600)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = if (isCompact) 600.dp else 480.dp)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Cobalto",
                style = MaterialTheme.typography.headlineLarge,
                color = IndustrialOrange,
                fontWeight = FontWeight.ExtraBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Gestión Industrial de Materiales",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

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
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { onEvent(LoginEvent.OnEmailChanged(it.trim())) },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = IndustrialShapes.small,
                        colors = loginTextFieldColors()
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { onEvent(LoginEvent.OnPasswordChanged(it)) },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        shape = IndustrialShapes.small,
                        colors = loginTextFieldColors()
                    )

                    TextButton(
                        onClick = { onEvent(LoginEvent.OnForgotPasswordClicked) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            style = MaterialTheme.typography.labelLarge,
                            color = IndustrialOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (uiState.error != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                            shape = IndustrialShapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = uiState.error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onEvent(LoginEvent.OnSignInClicked) },
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
                            Text("ENTRAR", fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(
                        onClick = onNavigateToSignUp,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        Text(
                            "¿No tienes cuenta? Regístrate",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    uiState: LoginUiState,
    onEvent: (LoginEvent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(LoginEvent.OnDismissForgotPassword) },
        title = {
            Text(
                text = "Recuperar Contraseña",
                fontWeight = FontWeight.ExtraBold,
                color = IndustrialOrange
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (uiState.forgotPasswordSuccess) {
                    Text(
                        text = "¡Contraseña actualizada con éxito! Ya puedes iniciar sesión con tu nueva contraseña.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )
                } else {
                    when (uiState.forgotPasswordStep) {
                        ForgotPasswordStep.EMAIL -> {
                            Text(
                                text = "Ingresa tu correo electrónico para recibir un código de verificación.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = uiState.forgotPasswordEmail,
                                onValueChange = { onEvent(LoginEvent.OnForgotPasswordEmailChanged(it.trim())) },
                                label = { Text("Correo Electrónico") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = IndustrialOrange) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                                singleLine = true,
                                shape = IndustrialShapes.small,
                                colors = loginTextFieldColors()
                            )
                        }
                        ForgotPasswordStep.OTP -> {
                            Text(
                                text = "Hemos enviado un código de 6 dígitos a ${uiState.forgotPasswordEmail}.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedTextField(
                                value = uiState.forgotPasswordOtp,
                                onValueChange = { 
                                    val trimmed = it.trim()
                                    if (trimmed.length <= 6) onEvent(LoginEvent.OnForgotPasswordOtpChanged(trimmed)) 
                                },
                                label = { Text("Código de 6 dígitos") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = IndustrialOrange) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                singleLine = true,
                                shape = IndustrialShapes.small,
                                colors = loginTextFieldColors(),
                                textStyle = TextStyle(
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 4.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        ForgotPasswordStep.NEW_PASSWORD -> {
                            Text(
                                text = "Ingresa tu nueva contraseña.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            var passVisible by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = uiState.forgotPasswordNewPassword,
                                onValueChange = { onEvent(LoginEvent.OnForgotPasswordNewPasswordChanged(it)) },
                                label = { Text("Nueva Contraseña") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IndustrialOrange) },
                                trailingIcon = {
                                    IconButton(onClick = { passVisible = !passVisible }) {
                                        Icon(imageVector = if (passVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                                    }
                                },
                                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                singleLine = true,
                                shape = IndustrialShapes.small,
                                colors = loginTextFieldColors()
                            )
                            OutlinedTextField(
                                value = uiState.forgotPasswordConfirmPassword,
                                onValueChange = { onEvent(LoginEvent.OnForgotPasswordConfirmPasswordChanged(it)) },
                                label = { Text("Confirmar Contraseña") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IndustrialOrange) },
                                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                singleLine = true,
                                shape = IndustrialShapes.small,
                                colors = loginTextFieldColors()
                            )
                        }
                    }
                }

                if (uiState.forgotPasswordError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                        shape = IndustrialShapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.forgotPasswordError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (uiState.forgotPasswordSuccess) {
                Button(
                    onClick = { onEvent(LoginEvent.OnDismissForgotPassword) },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialOrange)
                ) {
                    Text("Cerrar")
                }
            } else {
                Button(
                    onClick = {
                        when (uiState.forgotPasswordStep) {
                            ForgotPasswordStep.EMAIL -> onEvent(LoginEvent.OnSendForgotPasswordEmailClicked)
                            ForgotPasswordStep.OTP -> onEvent(LoginEvent.OnVerifyForgotPasswordOtpClicked)
                            ForgotPasswordStep.NEW_PASSWORD -> onEvent(LoginEvent.OnResetPasswordClicked)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialOrange),
                    enabled = !uiState.forgotPasswordLoading && (uiState.forgotPasswordStep != ForgotPasswordStep.OTP || uiState.forgotPasswordOtp.length == 6)
                ) {
                    if (uiState.forgotPasswordLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        val text = when (uiState.forgotPasswordStep) {
                            ForgotPasswordStep.EMAIL -> "Enviar Código"
                            ForgotPasswordStep.OTP -> "Verificar Código"
                            ForgotPasswordStep.NEW_PASSWORD -> "Cambiar Contraseña"
                        }
                        Text(text)
                    }
                }
            }
        },
        dismissButton = {
            if (!uiState.forgotPasswordSuccess) {
                TextButton(onClick = { onEvent(LoginEvent.OnDismissForgotPassword) }) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
fun loginTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = IndustrialOrange,
    cursorColor = IndustrialOrange,
    focusedLabelColor = IndustrialOrange
)

@AdaptivePreviews
@Composable
private fun LoginScreenPreview() {
    IndustrialTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                email = "industrial@material.com",
                password = "password123"
            ),
            onEvent = {},
            onNavigateToSignUp = {}
        )
    }
}
