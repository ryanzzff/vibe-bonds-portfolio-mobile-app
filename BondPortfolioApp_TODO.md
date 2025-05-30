# Bond Portfolio Tracker App - Development TODO List

This list outlines the steps to build the Bond Portfolio Tracker app using Kotlin Multiplatform (KMP) with local storage (SQLDelight) and Jetpack Compose for the Android UI.

## Phase 0: Project Setup & Configuration

*   [x] 1. Set up a new Kotlin Multiplatform Mobile project (e.g., using IntelliJ IDEA wizard or KMP template).
*   [x] 2. Configure `build.gradle.kts` files (root, shared, androidApp).
*   [x] 3. Add KMP dependencies to `shared` module:
    *   [x] SQLDelight (runtime, drivers - native, android)
    *   [x] Kotlinx Coroutines
    *   [x] Kotlinx Datetime
    *   [ ] Potentially Ktor client & serialization (for future API)
    *   [ ] Potentially KMP ViewModel library (e.g., Moko MVVM, KMM-ViewModel)
*   [x] 4. Configure SQLDelight Gradle plugin: Define package name, database name (`BondPortfolio.db`).
*   [x] 5. Add Android dependencies to `androidApp` module:
    *   [x] Jetpack Compose (UI, Navigation, ViewModel)
    *   [x] Material Design 3 Components
    *   [x] AndroidX Lifecycle/ViewModel KTX
    *   [ ] Coil (for potential future image loading, optional now)
*   [x] 6. Set up basic project structure (folders for `domain`, `data`, `presentation` within `shared` and `androidApp`).

## Phase 1: Core Data & Persistence (Shared Module)

*   [x] 1. **Domain Layer:** Define core data classes (`Bond`, `InterestPayment`, `BondType` enum, `PaymentFrequency` enum) in `shared/src/commonMain/kotlin/.../domain/model`.
*   [x] 2. **Domain Layer:** Define `BondRepository` interface in `shared/src/commonMain/kotlin/.../domain/repository`. Include methods like `addBond`, `getBondById`, `getAllBonds`, `updateBond`, `deleteBond`.
*   [x] 3. **Data Layer:** Define SQLDelight schema (`.sq` files) in `shared/src/commonMain/sqldelight/.../Bond.sq`.
    *   [x] Create `Bonds` table schema matching `Bond` data class fields (FR1.2, FR1.3). Handle date storage (ISO String or Timestamp). Handle enums (store as Text).
*   [x] 4. **Data Layer:** Implement `BondRepository` in `shared/src/commonMain/kotlin/.../data/repository`.
    *   [x] Inject SQLDelight database instance (platform-specific drivers needed).
    *   [x] Write repository implementation using generated SQLDelight queries.
*   [x] 5. **Data Layer:** Set up platform-specific SQLDelight driver creation (e.g., in `androidMain` and `iosMain` - *defer iOS driver setup if not building immediately*).
*   [x] 6. **Domain Layer:** Create initial Use Cases:
    *   [x] `AddBondUseCase(repository: BondRepository)`
    *   [x] `GetBondsUseCase(repository: BondRepository)`
    *   [x] `GetBondDetailsUseCase(repository: BondRepository)`
    *   [x] `UpdateBondUseCase(repository: BondRepository)`
    *   [x] `DeleteBondUseCase(repository: BondRepository)`

## Phase 2: Android UI - Bond Management (androidApp Module)

*   [x] 1. **Navigation:** Set up basic Jetpack Compose Navigation Host with initial screens (PortfolioList, AddBond, BondDetails).
*   [x] 2. **Portfolio List Screen:**
    *   [x] Create `PortfolioListViewModel` (Android ViewModel). Inject `GetBondsUseCase`.
    *   [x] Expose bond list state (e.g., using `StateFlow<List<Bond>>`).
    *   [x] Create `PortfolioListScreen` Composable. Observe ViewModel state.
    *   [x] Display bonds in a `LazyColumn`. Show key info (FR2.1).
    *   [x] Add a Floating Action Button (FAB) to navigate to Add Bond screen.
    *   [x] Implement item click navigation to Bond Details screen.
    *   [x] Implement flat design style with minimal elevation, clean typography, and simple UI elements.
    *   [x] Add portfolio summary section with total investment, face value, and average coupon rate.
    *   [x] Add bond type filtering functionality.
    *   [x] Create comprehensive unit and UI tests.
*   [x] 3. **Add/Edit Bond Screen:**
    *   [x] Create `AddEditBondViewModel`. Inject `AddBondUseCase`, `UpdateBondUseCase`, `GetBondDetailsUseCase` (for editing).
    *   [x] Manage form input state (consider a `BondDraft` state class).
    *   [x] Create `AddEditBondScreen` Composable.
    *   [x] Implement form fields for all mandatory/optional bond details (FR1.2, FR1.3). Use `TextField`, `DatePickerDialog`, `DropdownMenu`, etc.
    *   [x] Implement input validation.
    *   [x] Implement Save button logic calling the appropriate UseCase.
    *   [x] Handle navigation back after save/cancel.
    *   [x] Adapt screen for both Add (new bond) and Edit (load existing bond) modes.
*   [x] 4. **Bond Details Screen:**
    *   [x] Create `BondDetailsViewModel`. Inject `GetBondDetailsUseCase`, `DeleteBondUseCase`.
    *   [x] Load bond details based on passed ID. Expose state.
    *   [x] Create `BondDetailsScreen` Composable. Display all bond fields.
    *   [x] Add "Edit" button navigating to `AddEditBondScreen` in edit mode.
    *   [x] Add "Delete" button with confirmation dialog, calling `DeleteBondUseCase`.
    *   [x] Create comprehensive unit and UI tests.

## Phase 3: Core Logic - Interest & Yield Calculations (Shared Module)

*   [x] 1. **Interest Calculation Logic:**
    *   [x] Create `InterestCalculator` utility class or functions in `shared/commonMain`.
    *   [x] Implement logic to calculate payment dates based on `maturity_date`, `payment_frequency`, and `purchase_date` (or first payment date if known).
    *   [x] Implement logic to calculate payment amount (`face_value_per_bond * quantity_purchased * coupon_rate / payments_per_year`).
    *   [x] Handle different frequencies (Semi-Annual, Annual, Quarterly, Monthly).
    *   [x] Handle ZeroCoupon bonds (no payments).
*   [x] 2. **Domain Layer:** Create Use Cases:
    *   [x] `GetNextInterestPaymentUseCase(bond: Bond)` -> `InterestPayment?`
    *   [x] `GetAllFutureInterestPaymentsUseCase(bond: Bond)` -> `List<InterestPayment>`
    *   [x] `GetPortfolioInterestScheduleUseCase(repository: BondRepository)` -> `List<InterestPayment>` (combines payments from all bonds, sorted)
    *   [x] `GetMonthlyInterestSummaryUseCase(...)` -> `Map<YearMonth, Double>`
    *   [x] `GetYearlyInterestSummaryUseCase(...)` -> `Map<Int, Double>`
*   [x] 3. **Yield Calculation Logic:**
    *   [x] Create `YieldCalculator` utility class/functions in `shared/commonMain`.
    *   [x] Implement Average Coupon Rate calculation (weighted).
    *   [x] Implement Average Current Yield calculation (weighted, based on purchase price).
    *   [x] Research and implement Average YTM calculation (this is complex - consider using a library if available or a standard iterative formula). Decide on weighting method (initial cost vs. face value).
*   [x] 4. **Domain Layer:** Create Use Cases:
    *   [x] `CalculateAverageYieldUseCase(repository: BondRepository, type: YieldType)` -> `Double`

## Phase 4: Android UI - Portfolio Overview & Interest Tracking

*   [x] 1. **Portfolio List Screen Enhancements:**
    *   [x] Integrate `CalculateAverageYieldUseCase` into `PortfolioListViewModel`.
    *   [x] Add UI elements (e.g., Cards) to display Total Portfolio Value (Initial Cost) and Average Yield (with selection dropdown - FR2.3).
    *   [x] Implement Filter UI (e.g., Chips, Dropdown) for Bond Type (FR4.1). Update ViewModel and UseCase calls to support filtering.
*   [x] 2. **Interest Schedule Screen:**
    *   [x] Create `InterestScheduleViewModel`. Inject summary/schedule UseCases.
    *   [x] Create `InterestScheduleScreen` Composable.
    *   [x] Add Tabs/Toggles for "Upcoming Payments", "Monthly Summary", "Yearly Summary".
    *   [x] Display list of all upcoming portfolio payments (FR3.2).
    *   [x] Display monthly/yearly summaries (FR3.3).
*   [x] 3. **Interest Calendar Screen:**
    *   [x] Find/choose a KMP-compatible or Android-specific Calendar Compose library.
    *   [x] Create `InterestCalendarViewModel`. Inject `GetPortfolioInterestScheduleUseCase`.
    *   [x] Create `InterestCalendarScreen` Composable.
    *   [x] Fetch all interest payments, process dates.
    *   [x] Display calendar, highlighting dates with payments (FR3.4).
    *   [x] Implement date selection to show payments due on that day.

## Phase 5: Refinement & Testing

*   [x] 1. Implement comprehensive Unit Tests for Use Cases and ViewModels
*   [x] 2. Implement Integration and UI Tests for Android screens
*   [ ] 3. Code cleanup, documentation, and linting.
*   [ ] 4. UI Polish: Improve layouts, add animations/transitions, refine themes.
*   [ ] 5. Thorough manual testing on different Android devices/emulators.

## Phase 6: Future (Post-MVP)

*   [ ] 1. iOS App (`iosApp` module setup, SwiftUI UI implementation).
*   [ ] 2. Market Price API Integration (e.g., using Ktor in `shared` data layer).
*   [ ] 3. Cloud Sync/Backup.
*   [ ] 4. Notifications.
*   [ ] 5. Charts/Visualizations.

## Phase 7: Charts and Visualizations

*   [ ] 1. **Portfolio Value Over Time Chart:**
    *   [ ] Create a `PortfolioValueChartViewModel` to fetch and process historical portfolio value data.
    *   [ ] Implement a `PortfolioValueChartScreen` Composable using a charting library (e.g., MPAndroidChart or Compose Charts).
    *   [ ] Allow users to select time ranges (e.g., 1 month, 6 months, 1 year, all time).
    *   [ ] Add navigation to this screen from the Portfolio Overview.

*   [ ] 2. **Upcoming Income/Coupon Payments Chart:**
    *   [ ] Create a `IncomeProjectionChartViewModel` to fetch and process upcoming income data.
    *   [ ] Implement a `IncomeProjectionChartScreen` Composable using a bar chart.
    *   [ ] Display projected income for the next few months or quarters.
    *   [ ] Add navigation to this screen from the Interest Schedule.

*   [ ] 3. **Maturity Distribution Chart:**
    *   [ ] Create a `MaturityDistributionChartViewModel` to fetch and process bond maturity data.
    *   [ ] Implement a `MaturityDistributionChartScreen` Composable using a bar chart.
    *   [ ] Group bonds into time buckets (e.g., <1 year, 1-3 years, 3-5 years, >5 years).
    *   [ ] Add navigation to this screen from the Portfolio Overview.
