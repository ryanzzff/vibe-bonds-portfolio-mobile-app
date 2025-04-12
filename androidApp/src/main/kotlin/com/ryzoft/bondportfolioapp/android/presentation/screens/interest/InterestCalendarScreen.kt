package com.ryzoft.bondportfolioapp.android.presentation.screens.interest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.ryzoft.bondportfolioapp.shared.domain.model.InterestPayment
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Screen for displaying a calendar with interest payment highlights
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestCalendarScreen(
    onBackClick: () -> Unit,
    viewModel: InterestCalendarViewModel = viewModel(
        factory = InterestCalendarViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Month start and end for calendar
    val currentDate = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    
    var currentMonth by remember { 
        mutableStateOf(YearMonth.of(currentDate.year, currentDate.month.value)) 
    }
    
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    
    val daysOfWeek = remember { daysOfWeek() }
    
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeekFromLocale()
    )
    
    val monthTitleFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    
    val visibleMonth by derivedStateOf { 
        calendarState.firstVisibleMonth.yearMonth
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interest Calendar") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("backButton")
                    ) {
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
        ) {
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
                    // Calendar header with navigation buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentMonth = currentMonth.minusMonths(1)
                                coroutineScope.launch {
                                    calendarState.animateScrollToMonth(currentMonth)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                                contentDescription = "Previous Month"
                            )
                        }
                        
                        Text(
                            text = visibleMonth.format(monthTitleFormatter),
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        IconButton(
                            onClick = {
                                currentMonth = currentMonth.plusMonths(1)
                                coroutineScope.launch {
                                    calendarState.animateScrollToMonth(currentMonth)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                                contentDescription = "Next Month"
                            )
                        }
                    }
                    
                    // Days of week header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (dayOfWeek in daysOfWeek) {
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp),
                                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Calendar
                    HorizontalCalendar(
                        state = calendarState,
                        dayContent = { day ->
                            Day(
                                day = day,
                                hasPayment = uiState.paymentsByDate.keys.any { 
                                    it.year == day.date.year && 
                                    it.monthNumber == day.date.monthValue && 
                                    it.dayOfMonth == day.date.dayOfMonth 
                                },
                                isSelected = uiState.selectedDate?.let {
                                    it.year == day.date.year && 
                                    it.monthNumber == day.date.monthValue && 
                                    it.dayOfMonth == day.date.dayOfMonth
                                } ?: false,
                                onClick = {
                                    val selectedDate = LocalDate(
                                        day.date.year,
                                        day.date.monthValue,
                                        day.date.dayOfMonth
                                    )
                                    viewModel.selectDate(selectedDate)
                                }
                            )
                        },
                        monthHeader = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selected date payments
                    if (uiState.selectedDate != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Payments on ${formatDate(uiState.selectedDate)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (uiState.selectedDatePayments.isEmpty()) {
                                Text(
                                    text = "No payments on this date",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                LazyColumn {
                                    items(uiState.selectedDatePayments) { payment ->
                                        CalendarPaymentCard(payment)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Day cell composable for the calendar
 */
@Composable
fun Day(
    day: CalendarDay,
    hasPayment: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    day.position == DayPosition.MonthDate -> Color.Transparent
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    day.position != DayPosition.MonthDate -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            // Indicator for days with payments
            if (hasPayment && day.position == DayPosition.MonthDate) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

/**
 * Card to display an upcoming interest payment in calendar view
 */
@Composable
fun CalendarPaymentCard(payment: InterestPayment) {
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
                text = "Bond ID: ${payment.bondId}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Helper functions
private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}

private fun formatDate(date: LocalDate?): String {
    if (date == null) return ""
    return "${date.month.name.take(3)} ${date.dayOfMonth}, ${date.year}"
}