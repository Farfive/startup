package com.example.styleap.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Correct import for LazyColumn items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.styleap.R // Adjust if R file is elsewhere
import com.example.styleap.data.model.EmployeeInfo // Import the correct model

@Composable
fun CompanyProfileScreen(
    modifier: Modifier = Modifier,
    companyName: String,
    companyType: String,
    companyPoints: Int,
    employees: List<EmployeeInfo>, // Use the correct EmployeeInfo model
    onAddPhotoClick: () -> Unit,
    onSetHoursClick: () -> Unit,
    onAddPriceListClick: () -> Unit,
    onEmployeeClick: (EmployeeInfo) -> Unit // Use the correct EmployeeInfo model
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            // Consider using AsyncImage for network logos
            painter = painterResource(id = R.drawable.ic_business),
            contentDescription = stringResource(id = R.string.desc_company_logo),
            modifier = Modifier
                .size(80.dp)
                .padding(top = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = companyName,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = companyType,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            // Assuming a similar format string exists or just display points
            text = "$companyPoints points", // TODO: Use string resource (e.g., R.string.company_profile_points_format)
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        ProfileActionButton(
            text = stringResource(id = R.string.action_add_photo),
            icon = Icons.Filled.AddAPhoto,
            onClick = onAddPhotoClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        ProfileActionButton(
            text = stringResource(id = R.string.action_set_hours),
            icon = Icons.Filled.Schedule,
            onClick = onSetHoursClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        ProfileActionButton(
            text = stringResource(id = R.string.action_add_price_list),
            icon = Icons.Filled.ListAlt,
            onClick = onAddPriceListClick
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Employees List
        Text(
            text = stringResource(id = R.string.header_employees),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) { // Make list take remaining space
            items(employees, key = { it.id }) { employee ->
                EmployeeItem(
                    employeeName = employee.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEmployeeClick(employee) } // Add clickable here
                )
                Divider() // Optional divider between items
            }
        }
    }
}

@Composable
private fun ProfileActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(
            icon,
            contentDescription = null, // Button text describes action
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun CompanyProfileScreenPreview() {
    val sampleEmployees = listOf(
        EmployeeInfo(id = "1", name = "Alice Smith"),
        EmployeeInfo(id = "2", name = "Bob Johnson"),
        EmployeeInfo(id = "3", name = "Charlie Brown")
    )
    Surface {
        CompanyProfileScreen(
            companyName = "Vibe Coding",
            companyType = "Tech Company",
            companyPoints = 500,
            employees = sampleEmployees,
            onAddPhotoClick = {},
            onSetHoursClick = {},
            onAddPriceListClick = {},
            onEmployeeClick = {}
        )
    }
} 