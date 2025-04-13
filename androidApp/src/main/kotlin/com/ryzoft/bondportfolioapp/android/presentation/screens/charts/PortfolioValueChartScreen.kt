package com.ryzoft.bondportfolioapp.android.presentation.screens.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.ryzoft.bondportfolioapp.shared.domain.model.TimeRange
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import java.text.NumberFormat
import java.util.Locale

/**
 * Screen for displaying the Portfolio Value Over Time chart
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioValueChartScreen(
    onBackClick: () -> Unit,
    viewModel: PortfolioValueChartViewModel = viewModel(
        factory = PortfolioValueChartViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portfolio Value Over Time") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Time range selection
            TimeRangeSelector(
                selectedTimeRange = uiState.selectedTimeRange,
                onTimeRangeSelected = { viewModel.setTimeRange(it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
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
                                text = uiState.error ?: "An error occurred",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    uiState.portfolioValues.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No portfolio value data available",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {
                        // Create chart entries from portfolio value data
                        val chartEntries = remember(uiState.portfolioValues) {
                            uiState.portfolioValues.mapIndexed { index, value ->
                                FloatEntry(
                                    x = index.toFloat(),
                                    y = value.totalValue.toFloat()
                                )
                            }
                        }
                        
                        // Create entry model
                        val entryModel = remember(chartEntries) {
                            entryModelOf(chartEntries)
                        }
                        
                        val currencyFormatter = remember {
                            NumberFormat.getCurrencyInstance(Locale.US)
                        }
                        
                        ProvideChartStyle(m3ChartStyle()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = 8.dp,
                                        top = 16.dp,
                                        end = 16.dp,
                                        bottom = 8.dp
                                    )
                            ) {
                                Chart(
                                    chart = lineChart(),
                                    model = entryModel,
                                    startAxis = rememberStartAxis(
                                        valueFormatter = { value, _ ->
                                            currencyFormatter.format(value)
                                        },
                                        title = "Portfolio Value"
                                    ),
                                    bottomAxis = rememberBottomAxis(
                                        valueFormatter = { value, _ ->
                                            val idx = value.toInt().coerceIn(0, uiState.portfolioValues.size - 1)
                                            formatDate(uiState.portfolioValues[idx].date)
                                        },
                                        title = "Date"
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Summary
            if (!uiState.isLoading && uiState.portfolioValues.isNotEmpty()) {
                val currentValue = uiState.portfolioValues.last().totalValue
                val initialValue = uiState.portfolioValues.first().totalValue
                val change = currentValue - initialValue
                val percentChange = (change / initialValue * 100)
                
                Column(
                    modifier = Modifier.fillMaxWidth()
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
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Current Value",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatCurrency(currentValue),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Change",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${formatCurrency(change)} (${String.format("%.2f", percentChange)}%)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (change >= 0) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeRangeSelector(
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Time Range",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeRange.values().forEach { timeRange ->
                FilterChip(
                    selected = timeRange == selectedTimeRange,
                    onClick = { onTimeRangeSelected(timeRange) },
                    label = { 
                        Text(timeRange.displayName)
                    }
                )
                
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

// Helper functions
private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}

private fun formatDate(date: LocalDate): String {
    return "${date.month.getShortName()} ${date.year}"
}

private fun Month.getShortName(): String {
    return when (this) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Feb"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Apr"
        Month.MAY -> "May"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Aug"
        Month.SEPTEMBER -> "Sep"
        Month.OCTOBER -> "Oct"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dec"
        else -> this.toString()
    }
}