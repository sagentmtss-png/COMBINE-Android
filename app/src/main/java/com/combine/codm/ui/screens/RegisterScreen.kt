package com.combine.codm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.combine.codm.data.SupabaseManager
import com.combine.codm.ui.components.CombineLogo
import com.combine.codm.ui.theme.*
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CombineDarkNavy, CombineBlack, CombineBlack)))
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CombineLogo(showSubtitle = true)
            Spacer(modifier = Modifier.height(20.dp))
            Text("Create Account", color = CombineWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            RegField(username, { username = it }, "Username", Icons.Default.Person)
            Spacer(modifier = Modifier.height(12.dp))
            RegField(phone, { phone = it }, "Phone Number / Email", Icons.Default.Phone)
            Spacer(modifier = Modifier.height(12.dp))
            RegPassField(password, { password = it }, "Password", showPass) { showPass = !showPass }
            Spacer(modifier = Modifier.height(12.dp))
            RegPassField(confirmPassword, { confirmPassword = it }, "Confirm Password", showPass) { showPass = !showPass }

            statusMessage?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(it, color = CombineYellow, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (username.isBlank() || phone.isBlank() || password.length < 6) {
                        statusMessage = "Jaza taarifa zote (nenosiri angalau herufi 6)."
                        return@Button
                    }
                    if (password != confirmPassword) {
                        statusMessage = "Nenosiri hazifanani."
                        return@Button
                    }
                    if (!SupabaseManager.isConfigured()) {
                        statusMessage = "Seva ya Supabase haijawekwa URL/Key bado."
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        statusMessage = null
                        try {
                            val regEmail = if (phone.contains("@")) phone.trim()
                            else "${phone.trim().replace(" ", "")}@combine.codm"
                            SupabaseManager.client.auth.signUpWith(Email) {
                                email = regEmail
                                this.password = password
                                data = buildJsonObject {
                                    put("username", username.trim())
                                    put("phone", phone.trim())
                                }
                            }
                            onRegisterSuccess()
                        } catch (e: Exception) {
                            statusMessage = "Usajili umeshindikana: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CombineYellow, contentColor = CombineBlack),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = CombineBlack, modifier = Modifier.size(22.dp))
                } else {
                    Icon(Icons.Default.PersonAdd, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row {
                Text("Already have an account? ", color = CombineGrayText, fontSize = 14.sp)
                Text(
                    "Login",
                    color = CombineYellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Composable
private fun RegField(valText: String, onChange: (String) -> Unit, hint: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OutlinedTextField(
        value = valText, onValueChange = onChange, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(hint, color = CombineGrayText) },
        leadingIcon = { Icon(icon, null, tint = CombineElectricBlue) },
        singleLine = true, shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CombineInputBg, unfocusedContainerColor = CombineInputBg,
            focusedBorderColor = CombineElectricBlue, unfocusedBorderColor = CombineCardBg,
            focusedTextColor = CombineWhite, unfocusedTextColor = CombineWhite
        )
    )
}

@Composable
private fun RegPassField(valText: String, onChange: (String) -> Unit, hint: String, visible: Boolean, onToggle: () -> Unit) {
    OutlinedTextField(
        value = valText, onValueChange = onChange, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(hint, color = CombineGrayText) },
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = CombineElectricBlue) },
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = CombineGrayText)
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true, shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CombineInputBg, unfocusedContainerColor = CombineInputBg,
            focusedBorderColor = CombineElectricBlue, unfocusedBorderColor = CombineCardBg,
            focusedTextColor = CombineWhite, unfocusedTextColor = CombineWhite
        )
    )
}
