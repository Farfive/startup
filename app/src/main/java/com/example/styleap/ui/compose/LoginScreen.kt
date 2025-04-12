package com.example.styleap.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.styleap.R // Adjust if R file is elsewhere

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    isLoading: Boolean = false
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center content vertically
    ) {
        Text(
            text = stringResource(id = R.string.login_welcome_back),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 48.dp) // Increased bottom padding
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.hint_email)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(id = R.string.hint_password)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { onLoginClick(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // Standard button height
            ) {
                Text(stringResource(id = R.string.action_login))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ClickableText(
            text = AnnotatedString(stringResource(id = R.string.prompt_register)),
            onClick = { offset ->
                if (!isLoading) onRegisterClick()
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Surface {
        LoginScreen(
            onLoginClick = { _, _ -> },
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    Surface {
        LoginScreen(
            onLoginClick = { _, _ -> },
            onRegisterClick = {},
            isLoading = true
        )
    }
} 