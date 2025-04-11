package com.ryzoft.bondportfolioapp.android.presentation.screens.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Bond Details Screen - Displays the detailed information about a specific bond
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BondDetailsScreen(
    bondId: Long,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    // The ViewModel will be injected later
) {
    // This variable will be handled by the ViewModel later
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bond Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Edit button
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Bond"
                        )
                    }
                    
                    // Delete button
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Bond"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // This is a placeholder. In the actual implementation, we'll observe the ViewModel's state
        // and display the actual bond details
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Bond Details (ID: $bondId)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Placeholder content - will be replaced with actual bond details
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // In the final implementation, these will display actual bond data
                        DetailItem(label = "Bond Name", value = "Example Bond")
                        DetailItem(label = "ISIN", value = "US123456AB12")
                        DetailItem(label = "Type", value = "Corporate Bond")
                        DetailItem(label = "Face Value", value = "$10,000")
                        DetailItem(label = "Quantity", value = "5")
                        DetailItem(label = "Purchase Price", value = "$9,800")
                        DetailItem(label = "Coupon Rate", value = "5.5%")
                        DetailItem(label = "Payment Frequency", value = "Semi-Annual")
                        DetailItem(label = "Issue Date", value = "Jan 15, 2020")
                        DetailItem(label = "Maturity Date", value = "Dec 31, 2030")
                        DetailItem(label = "Total Investment", value = "$49,000")
                        DetailItem(label = "Current Yield", value = "5.61%")
                    }
                }
            }
        }
        
        // Delete Confirmation Dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Bond") },
                text = { Text("Are you sure you want to delete this bond from your portfolio?") },
                confirmButton = {
                    Button(
                        onClick = {
                            // Will be implemented with ViewModel later
                            showDeleteConfirmation = false
                            // Navigate back after deletion
                            onBackClick()
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmation = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}
