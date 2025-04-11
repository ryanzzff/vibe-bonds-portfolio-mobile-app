package com.ryzoft.bondportfolioapp.android.presentation.screens.portfolio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Portfolio List Screen - Displays a list of bonds in the user's portfolio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioListScreen(
    onBondClick: (bondId: Long) -> Unit,
    onAddBondClick: () -> Unit,
    // The ViewModel will be injected later
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bond Portfolio") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBondClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Bond"
                )
            }
        }
    ) { paddingValues ->
        // This is a placeholder. In the actual implementation, we'll observe the ViewModel's state
        // and display the actual list of bonds
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder content - will be replaced with actual bond list
            // Simply showing a message for now since we haven't implemented the ViewModel yet
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Portfolio List Screen",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Text(
                    text = "This screen will display all bonds in your portfolio",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                // Example card to demonstrate how bonds will be displayed
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onBondClick(1) } // Using placeholder ID 1
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Example Bond",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "5.5% Coupon | Matures Dec 2030",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Face Value: $10,000",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
