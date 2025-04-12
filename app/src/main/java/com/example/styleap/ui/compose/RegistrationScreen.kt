package com.example.styleap.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.styleap.R // Adjust if R file is elsewhere

// Define user type options
enum class UserType { INDIVIDUAL, COMPANY }

@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    onRegisterClick: (String, String, String, UserType) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean = false
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var selectedUserType by rememberSaveable { mutableStateOf(UserType.INDIVIDUAL) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.registration_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 24.dp) // Added padding
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(id = R.string.hint_full_name)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            label = { Text(stringResource(id = R.string.hint_password_min_chars)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // User Type Selection
        Text(
            text = stringResource(id = R.string.label_register_as),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            UserTypeRadioButton(UserType.INDIVIDUAL, selectedUserType, !isLoading) { selectedUserType = it }
            Spacer(modifier = Modifier.width(16.dp))
            UserTypeRadioButton(UserType.COMPANY, selectedUserType, !isLoading) { selectedUserType = it }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { onRegisterClick(name, email, password, selectedUserType) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading
            ) {
                Text(stringResource(id = R.string.action_register))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ClickableText(
            text = AnnotatedString(stringResource(id = R.string.prompt_login)),
            onClick = { offset ->
                if (!isLoading) onLoginClick()
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(8.dp)
        )

        // Push content towards top by adding a flexible spacer at the bottom
        Spacer(modifier = Modifier.weight(1.0f))
    }
}

@Composable
private fun UserTypeRadioButton(
    userType: UserType,
    selectedType: UserType,
    enabled: Boolean,
    onClick: (UserType) -> Unit
) {
    val label = when (userType) {
        UserType.INDIVIDUAL -> stringResource(id = R.string.radio_individual_user)
        UserType.COMPANY -> stringResource(id = R.string.radio_company)
    }
    Row(
        modifier = Modifier
            .selectable(
                selected = (userType == selectedType),
                onClick = { onClick(userType) },
                role = Role.RadioButton,
                enabled = enabled
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = (userType == selectedType),
            onClick = null, // Recommended: null onClick for RadioButton when Row is selectable
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    Surface {
        RegistrationScreen(
            onRegisterClick = { _, _, _, _ -> },
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenLoadingPreview() {
    Surface {
        RegistrationScreen(
            onRegisterClick = { _, _, _, _ -> },
            onLoginClick = {},
            isLoading = true
        )
    }
} 