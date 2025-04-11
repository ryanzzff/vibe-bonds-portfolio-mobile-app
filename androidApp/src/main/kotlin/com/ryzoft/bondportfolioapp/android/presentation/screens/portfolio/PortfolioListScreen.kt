package com.ryzoft.bondportfolioapp.android.presentation.screens.portfolio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryzoft.bondportfolioapp.android.di.UseCaseProvider
import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Portfolio List Screen - Displays a list of bonds in the user's portfolio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioListScreen(
    onBondClick: (bondId: Long) -> Unit,
    onAddBondClick: () -> Unit,
    viewModel: PortfolioListViewModel = createViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedBondType by remember { mutableStateOf<BondType?>(null) }
    
    val filteredBonds = if (selectedBondType != null) {
        uiState.bonds.filter { it.bondType == selectedBondType }
    } else {
        uiState.bonds
    }
    
    // Calculate portfolio summary values
    val totalInvestment = filteredBonds.sumOf { it.purchasePrice * it.quantityPurchased }
    val totalFaceValue = filteredBonds.sumOf { it.faceValuePerBond * it.quantityPurchased }
    val averageCouponRate = if (filteredBonds.isNotEmpty()) {
        filteredBonds.sumOf { it.couponRate * it.faceValuePerBond * it.quantityPurchased } / totalFaceValue
    } else 0.0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bond Portfolio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBondClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Bond"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Portfolio Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Portfolio Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SummaryItem(
                            label = "Total Investment",
                            value = formatCurrency(totalInvestment),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryItem(
                            label = "Face Value",
                            value = formatCurrency(totalFaceValue),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SummaryItem(
                        label = "Average Coupon Rate",
                        value = "${String.format("%.2f", averageCouponRate)}%"
                    )
                }
            }
            
            // Filter Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filter by Type:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                BondTypeFilter(
                    selectedType = selectedBondType,
                    onTypeSelected = { selectedBondType = it }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bond List
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "An unknown error occurred",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                filteredBonds.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedBondType != null) 
                                "No bonds of type ${selectedBondType?.name ?: ""} found" 
                            else 
                                "No bonds in your portfolio yet",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredBonds) { bond ->
                            BondListItem(
                                bond = bond,
                                onBondClick = { onBondClick(bond.id) }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper function to create the ViewModel with proper dependencies
 */
@Composable
private fun createViewModel(): PortfolioListViewModel {
    val context = LocalContext.current
    val getBondsUseCase = UseCaseProvider.provideGetBondsUseCase(context)
    val factory = PortfolioListViewModelFactory(getBondsUseCase)
    return viewModel(factory = factory)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BondTypeFilter(
    selectedType: BondType?,
    onTypeSelected: (BondType?) -> Unit
) {
    Row {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text("All") },
            modifier = Modifier.padding(end = 4.dp)
        )
        
        BondType.values().forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type.name.lowercase().capitalize()) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BondListItem(
    bond: Bond,
    onBondClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onBondClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = Color.Transparent
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bond.name ?: bond.issuerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "${String.format("%.2f", bond.couponRate)}% | ${bond.bondType.name.lowercase().capitalize()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatCurrency(bond.faceValuePerBond * bond.quantityPurchased),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Matures: ${formatDate(bond.maturityDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Helper functions
private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}

private fun formatDate(date: kotlinx.datetime.LocalDate): String {
    return "${date.month.name.take(3)} ${date.year}"
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
    }
}
