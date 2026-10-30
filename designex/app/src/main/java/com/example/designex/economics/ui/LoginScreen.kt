package com.example.designex.economics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.economics.data.AccountStore
import com.example.designex.ui.theme.Accent
import com.example.designex.ui.theme.Coral
import com.example.designex.ui.theme.DeepNavy
import com.example.designex.ui.theme.Glass
import com.example.designex.ui.theme.GlassBorder
import com.example.designex.ui.theme.TextFaint
import com.example.designex.ui.theme.TextHi
import com.example.designex.ui.theme.TextLo

@Composable
fun glassFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Accent,
    unfocusedBorderColor = GlassBorder,
    focusedLabelColor = Accent,
    unfocusedLabelColor = TextLo,
    focusedTextColor = TextHi,
    unfocusedTextColor = TextHi,
    cursorColor = Accent,
    focusedContainerColor = DeepNavy.copy(alpha = 0.35f),
    unfocusedContainerColor = DeepNavy.copy(alpha = 0.25f),
)

/** Log in or create an account. Accounts live on this phone only. */
@Composable
fun LoginScreen(accounts: AccountStore, onSignedIn: (name: String, email: String) -> Unit) {
    var signUp by remember { mutableStateOf(accounts.lastEmail.isEmpty()) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(accounts.lastEmail) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        val result = if (signUp) accounts.signUp(name, email, password) else accounts.logIn(email, password)
        when (result) {
            is AccountStore.Result.Ok -> onSignedIn(result.name, result.email)
            is AccountStore.Result.Error -> error = when (result.reason) {
                AccountStore.Reason.BAD_EMAIL -> "Enter a valid email address."
                AccountStore.Reason.SHORT_PASSWORD -> "Use at least 6 characters for the password."
                AccountStore.Reason.MISSING_NAME -> "Tell us your name."
                AccountStore.Reason.EXISTS -> "An account with this email already exists. Log in instead."
                AccountStore.Reason.NOT_FOUND -> "No account with this email on this phone. Create one first."
                AccountStore.Reason.WRONG_PASSWORD -> "That password doesn't match."
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        SceneArt(Scenes.Night, 11, Modifier.fillMaxSize(), horizonAt = 0.5f, glowAt = androidx.compose.ui.geometry.Offset(0.8f, 0.1f))
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.42f to Color.Transparent,
                        0.62f to DeepNavy.copy(alpha = 0.7f),
                        1f to DeepNavy.copy(alpha = 0.95f),
                    )
                )
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .statusBarsPadding()
                .padding(horizontal = 22.dp),
        ) {
            Spacer(Modifier.height(56.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Logo(size = 50.sp)
                Text("Learn your syllabus through music", color = TextHi, fontSize = 15.sp)
                Spacer(Modifier.height(6.dp))
                Text("Economics · Chemistry · Calculus", color = TextLo, fontSize = 13.sp)
            }
            Spacer(Modifier.height(170.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(DeepNavy.copy(alpha = 0.55f))
                    .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(Glass)
                        .padding(4.dp),
                ) {
                    listOf(false to "Log in", true to "Create account").forEach { (isSignUp, label) ->
                        val active = signUp == isSignUp
                        Text(
                            label,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (active) Accent else Color.Transparent)
                                .clickable { signUp = isSignUp; error = null }
                                .padding(vertical = 10.dp),
                            color = if (active) DeepNavy else TextLo,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(if (signUp) "Create your student account" else "Welcome back", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = TextHi)
                Text(
                    if (signUp) "Your progress and songs are saved on this phone." else "Log in to pick up where you left off.",
                    color = TextLo,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (signUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; error = null },
                            label = { Text("Your name") },
                            singleLine = true,
                            colors = glassFieldColors(),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; error = null },
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = glassFieldColors(),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            Text(
                                if (showPassword) "Hide" else "Show",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showPassword = !showPassword }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Accent,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                            )
                        },
                        colors = glassFieldColors(),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                error?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(it, color = Coral, fontSize = 14.sp)
                }
                Spacer(Modifier.height(18.dp))
                PrimaryButton(if (signUp) "Create account" else "Log in", onClick = ::submit)
                Spacer(Modifier.height(12.dp))
                Text(
                    if (signUp) "Already have an account? Log in" else "New student? Create an account",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { signUp = !signUp; error = null }
                        .padding(8.dp),
                    color = Accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
