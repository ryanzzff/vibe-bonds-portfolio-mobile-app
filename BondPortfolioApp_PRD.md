# Product Requirements Document: Vibe Bond Portfolio Tracker App

**Version:** 1.0
**Date:** 2025-04-12

## 1. Introduction

This document outlines the requirements for the Vibe Bond Portfolio Tracker mobile application. The primary goal is to allow users to manually input and track their bond investments (initially USD-denominated), monitor interest payments, and calculate portfolio yields. The application will initially target Android using Kotlin Multiplatform for future iOS compatibility, with data stored locally on the device.

## 2. Goals

*   Provide a simple interface for users to manage their bond portfolio.
*   Accurately calculate and display upcoming and historical interest payments.
*   Calculate and display various average portfolio yield metrics.
*   Allow filtering and organization of bonds.
*   Ensure data persistence locally on the user's device.
*   Build with Kotlin Multiplatform to facilitate future iOS development.

## 3. User Stories

*   As a bond investor, I want to add my bond purchases with details like coupon rate, maturity, price, etc., so I can track them in one place.
*   As a bond investor, I want to see a list of all my bonds with key summary information.
*   As a bond investor, I want to edit or delete bond entries if I make a mistake or sell the bond.
*   As a bond investor, I want to know the exact dates and amounts of my upcoming interest payments so I can manage my cash flow.
*   As a bond investor, I want to see a summary of my total interest income per month and per year.
*   As a bond investor, I want to see a calendar view highlighting interest payment dates.
*   As a bond investor, I want to know the average yield of my portfolio (Coupon Rate, YTM, Current Yield based on purchase price) so I can assess performance.
*   As a bond investor, I want to filter my bonds by type (e.g., Treasury, Company) to focus on specific segments of my portfolio.
*   As a bond investor, I want my data saved securely on my device so I don't have to re-enter it every time.

## 4. Functional Requirements

### 4.1. Bond Management (CRUD)

*   **FR1.1:** Users must be able to add a new bond entry.
*   **FR1.2:** Mandatory fields for adding a bond:
    *   `bond_type` (Text: e.g., 'Treasury', 'Company', 'Municipal', 'Agency' - dropdown/selection)
    *   `issuer_name` (Text) - *Added based on context*
    *   `coupon_rate` (Decimal % - e.g., 2.5)
    *   `maturity_date` (Date)
    *   `face_value_per_bond` (Decimal - e.g., 1000)
    *   `purchase_date` (Date)
    *   `purchase_price` (Decimal - price paid per 100 face value, e.g., 99.5 or 101.2)
    *   `payment_frequency` (Selection: 'Semi-Annual', 'Annual', 'Quarterly', 'Monthly', 'ZeroCoupon')
    *   `quantity_purchased` (Integer - number of bonds)
    *   `currency` (Text - Default to 'USD', non-editable initially)
*   **FR1.3:** Optional fields:
    *   `name` (Text - User-defined nickname)
    *   `isin_cusip` (Text)
    *   `notes` (Text)
*   **FR1.4:** Users must be able to view the details of a selected bond.
*   **FR1.5:** Users must be able to edit the details of an existing bond entry.
*   **FR1.6:** Users must be able to delete a bond entry from their portfolio.

### 4.2. Portfolio Overview

*   **FR2.1:** Display a list/dashboard summarizing all added bonds. Key info per bond (e.g., Name/Issuer, Quantity, Maturity Date, Coupon Rate).
*   **FR2.2:** Calculate and display the **Total Portfolio Value (Initial Cost)**: Sum of (`purchase_price` / 100 * `face_value_per_bond` * `quantity_purchased`) for all bonds.
*   **FR2.3:** Calculate and display the **Average Portfolio Yield**. Provide user selection for:
    *   *Average Coupon Rate:* Weighted average of coupon rates based on initial investment cost or face value.
    *   *Average Yield to Maturity (YTM):* Weighted average YTM calculated for each bond based on purchase price, coupon, face value, and time to maturity. (Requires complex calculation, potentially iterative or using approximation).
    *   *Average Current Yield:* Weighted average of (Annual Coupon Payment / Initial Investment Cost) for each bond.
*   **FR2.4:** Clearly label which yield calculation is being displayed.

### 4.3. Interest Payment Tracking

*   **FR3.1:** For each bond (excluding ZeroCoupon), calculate and display the date and amount of the *next* upcoming interest payment.
*   **FR3.2:** Provide a view listing *all* future calculated interest payments for the entire portfolio, sorted by date.
*   **FR3.3:** Provide aggregated views summarizing total calculated interest income:
    *   Per Month (for the next 12 months and historical).
    *   Per Year (for the lifetime of the bonds held and historical).
*   **FR3.4:** Implement a calendar view highlighting dates with scheduled interest payments. Tapping a date shows payments due.

### 4.4. Filtering

*   **FR4.1:** Allow users to filter the main bond list view by `bond_type`. Initial types: 'Treasury', 'Company'. Design should allow easy addition of more types (e.g., 'Municipal', 'Agency').
*   **FR4.2:** Filter controls should be easily accessible from the portfolio overview screen.

### 4.5. Data Storage

*   **FR5.1:** All user-entered bond data must be persisted locally on the device.
*   **FR5.2:** Data must survive application restarts and device reboots.
*   **FR5.3:** Use SQLite via SQLDelight within the KMP shared module.

### 4.6. Currency

*   **FR6.1:** The application will initially only support USD. Currency field should exist but be fixed/non-editable.

## 5. Non-Functional Requirements

*   **NFR1:** **Platform:** Android (Initial), iOS (Future).
*   **NFR2:** **Technology:** Kotlin Multiplatform (Shared Logic: Kotlin, Android UI: Jetpack Compose, iOS UI: SwiftUI).
*   **NFR3:** **Architecture:** Clean Architecture (Domain, Data, Presentation layers), MVVM/MVI in presentation layers.
*   **NFR4:** **Performance:** UI should be responsive. Calculations (especially YTM) should be efficient and not block the UI thread.
*   **NFR5:** **Usability:** Interface should be intuitive and easy to navigate for non-expert users.
*   **NFR6:** **Data Safety:** Local data storage should be implemented robustly.

## 6. Design & UI/UX (High-Level)

*   **Navigation:** Bottom Navigation Bar likely suitable. Tabs: Portfolio, Interest Schedule, Calendar (or integrate Calendar into Interest Schedule), Add Bond (+ Button).
*   **Screens:**
    *   `Portfolio List`: Main dashboard. List of bonds, summary cards for Total Value / Avg. Yield. Filter options accessible.
    *   `Bond Detail`: View all info for one bond, upcoming payments for *this* bond, Edit/Delete actions.
    *   `Add/Edit Bond Form`: Clean form with appropriate input types (date pickers, number pads, dropdowns).
    *   `Interest Schedule`: List of all upcoming payments, Monthly/Yearly summary toggles/tabs.
    *   `Interest Calendar`: Visual calendar highlighting payment dates.
*   **Theme:** Clean, modern, finance-appropriate aesthetic.

## 7. Future Considerations

*   Integration with external APIs to fetch current market prices for bonds.
*   Calculation of portfolio value based on market prices.
*   User accounts and cloud synchronization/backup (Firebase, AWS Amplify).
*   Push notifications for upcoming interest payments.
*   Advanced charting/visualizations for portfolio allocation and interest trends.
*   Support for multiple currencies.
*   Handling bond calls or sales before maturity.

## 8. Open Questions/Assumptions

*   **YTM Calculation Precision:** How precise does the YTM calculation need to be? Standard financial formulas can be complex. Assume standard approximation methods are acceptable.
*   **Error Handling:** Assume standard input validation (non-negative numbers, valid dates, etc.). Specific error message details TBD.
*   **Bond Type List:** Start with 'Treasury', 'Company'. Are 'Municipal', 'Agency' needed for V1? (Assuming No for MVP, but easy to add).
