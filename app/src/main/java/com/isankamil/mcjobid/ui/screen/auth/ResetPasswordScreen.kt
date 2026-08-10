package com.isankamil.mcjobid.ui.screen.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.ui.components.McJobIdLogo
import com.isankamil.mcjobid.ui.theme.*

@Composable
fun ResetPasswordScreen(
    oobCode: String,
    viewModel: AuthViewModel,
    onResetSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val confirmResetStatus by viewModel.confirmResetStatus.collectAsState()

    var newPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val hasUpper = newPassword.any { it.isUpperCase() }
    val hasLower = newPassword.any { it.isLowerCase() }
    val hasDigit = newPassword.any { it.isDigit() }
    val isPasswordValid = hasUpper && hasLower && hasDigit && newPassword.length >= 6

    val isSuccess = confirmResetStatus?.isSuccess == true

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearError()
            viewModel.clearConfirmResetStatus()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo
                McJobIdLogo(iconSize = 48.dp, showWordmark = true)

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Primary,
                            modifier = Modifier.width(60.dp).height(4.dp)
                        ) {}

                        Spacer(modifier = Modifier.height(20.dp))

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.size(64.dp),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.LockReset,
                                    contentDescription = null,
                                    tint = if (isSuccess) Success else Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isSuccess) "Kata Sandi Diperbarui!" else "Buat Kata Sandi Baru",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isSuccess) 
                                "Kata sandi Anda telah berhasil diubah. Silakan login kembali dengan kata sandi baru." 
                                else "Masukkan kata sandi baru untuk mengamankan akun mcjob.id Anda.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (isSuccess) {
                            Button(
                                onClick = onResetSuccess,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Text("Masuk ke Halaman Login", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        } else {
                            // Error banner
                            errorMessage?.let { error ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Error.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, Error.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                ) {
                                    Text(
                                        text = error,
                                        color = Error,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(10.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Password Input
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Kata Sandi Baru",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { 
                                        newPassword = it
                                        if (errorMessage != null) viewModel.clearError()
                                    },
                                    placeholder = { Text("••••••••", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                                    },
                                    trailingIcon = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color.White,
                                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                                modifier = Modifier.padding(end = 4.dp)
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                                        val lowercase = "abcdefghijklmnopqrstuvwxyz"
                                                        val digits = "0123456789"
                                                        val specials = "!@#"
                                                        val mandatory = listOf(uppercase.random(), lowercase.random(), digits.random(), specials.random())
                                                        val rest = (1..6).map { (uppercase + lowercase + digits + specials).random() }
                                                        newPassword = (mandatory + rest).shuffled().joinToString("")
                                                        if (errorMessage != null) viewModel.clearError()
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Generate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Primary)
                                                }
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = Color(0xFFE2E8F0),
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Minimal 6 karakter dengan kombinasi kapital, huruf kecil, dan angka.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (!isPasswordValid) {
                                        viewModel.loginError("Password harus mengandung minimal Kapital (A-Z), Huruf Kecil (a-z), dan Angka (0-9).")
                                    } else {
                                        viewModel.confirmPasswordReset(oobCode, newPassword)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Simpan Kata Sandi Baru", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
