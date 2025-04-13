package com.ryzoft.bondportfolioapp.android.presentation.screens.interest

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import com.ryzoft.bondportfolioapp.shared.domain.usecase.YearMonth
import kotlinx.datetime.Month
import java.text.NumberFormat
import java.util.Locale

/**
 * Screen for displaying interest payment schedule and summaries
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestScheduleScreen(
    onBackClick: () -> Unit,
    onCalendarClick: () -> Unit,
    viewModel: InterestScheduleViewModel = viewModel(
        factory = InterestScheduleViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interest Schedule") },
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
                },
                actions = {
                    // Remove the calendar IconButton
                    /* IconButton(onClick = onCalendarClick) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar View",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } */
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = uiState.activeTab.ordinal) {
                InterestScheduleTab.values().forEachIndexed { index, tab ->
                    Tab(
                        selected = uiState.activeTab.ordinal == index,
                        onClick = { viewModel.setActiveTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    InterestScheduleTab.UPCOMING_PAYMENTS -> "Upcoming"
                                    InterestScheduleTab.MONTHLY_SUMMARY -> "Monthly"
                                    InterestScheduleTab.YEARLY_SUMMARY -> "Yearly"
                                }
                            )
                        }
                    )
                }
            }
            
            // Content based on the selected tab
            when {
                uiState.isLoading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(uiState.error!!)
                    }
                }
                else -> {
                    when (uiState.activeTab) {
                        InterestScheduleTab.UPCOMING_PAYMENTS -> UpcomingPaymentsTab(uiState.upcomingPayments)
                        InterestScheduleTab.MONTHLY_SUMMARY -> MonthlySummaryTab(uiState.monthlySummary)
                        InterestScheduleTab.YEARLY_SUMMARY -> YearlySummaryTab(uiState.yearlySummary)
                    }
                }
            }
        }
    }
}

/**
 * Tab content for displaying upcoming interest payments
 */
@Composable
fun UpcomingPaymentsTab(payments: List<InterestPayment>) {
    if (payments.isEmpty()) {
        EmptyState("No upcoming interest payments scheduled")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            items(payments) { payment ->
                PaymentCard(payment)
            }
        }
    }
}

/**
 * Tab content for displaying monthly interest summary
 */
@Composable
fun MonthlySummaryTab(monthlySummary: Map<YearMonth, Double>) {
    if (monthlySummary.isEmpty()) {
        EmptyState("No monthly interest summary available")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            items(monthlySummary.entries.toList()) { (yearMonth, amount) ->
                MonthlySummaryCard(yearMonth, amount)
            }
        }
    }
}

/**
 * Tab content for displaying yearly interest summary
 */
@Composable
fun YearlySummaryTab(yearlySummary: Map<Int, Double>) {
    if (yearlySummary.isEmpty()) {
        EmptyState("No yearly interest summary available")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            items(yearlySummary.entries.toList()) { (year, amount) ->
                YearlySummaryCard(year, amount)
            }
        }
    }
}

/**
 * Card to display an upcoming interest payment
 */
@Composable
fun PaymentCard(payment: InterestPayment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Payment: ${formatCurrency(payment.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Date: ${formatDate(payment.paymentDate)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Bond: ${payment.bondName}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Card to display monthly interest summary
 */
@Composable
fun MonthlySummaryCard(yearMonth: YearMonth, amount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${formatMonth(yearMonth.second)} ${yearMonth.first}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Card to display yearly interest summary
 */
@Composable
fun YearlySummaryCard(year: Int, amount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Empty state display when there's no data to show
 */
@Composable
fun EmptyState(message: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(message)
    }
}

// Helper functions
private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}

private fun formatDate(date: kotlinx.datetime.LocalDate): String {
    return "${date.month.name.take(3)} ${date.dayOfMonth}, ${date.year}"
}

private fun formatMonth(month: Month): String {
    return month.name.lowercase().capitalize()
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
    }
}