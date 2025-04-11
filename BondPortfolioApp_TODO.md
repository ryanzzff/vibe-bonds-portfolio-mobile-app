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
*   [ ] 2. **Portfolio List Screen:**
    *   [ ] Create `PortfolioListViewModel` (Android ViewModel). Inject `GetBondsUseCase`.
    *   [ ] Expose bond list state (e.g., using `StateFlow<List<Bond>>`).
    *   [ ] Create `PortfolioListScreen` Composable. Observe ViewModel state.
    *   [ ] Display bonds in a `LazyColumn`. Show key info (FR2.1).
    *   [ ] Add a Floating Action Button (FAB) to navigate to Add Bond screen.
    *   [ ] Implement item click navigation to Bond Details screen.
*   [ ] 3. **Add/Edit Bond Screen:**
    *   [ ] Create `AddEditBondViewModel`. Inject `AddBondUseCase`, `UpdateBondUseCase`, `GetBondDetailsUseCase` (for editing).
    *   [ ] Manage form input state (consider a `BondDraft` state class).
    *   [ ] Create `AddEditBondScreen` Composable.
    *   [ ] Implement form fields for all mandatory/optional bond details (FR1.2, FR1.3). Use `TextField`, `DatePickerDialog`, `DropdownMenu`, etc.
    *   [ ] Implement input validation.
    *   [ ] Implement Save button logic calling the appropriate UseCase.
    *   [ ] Handle navigation back after save/cancel.
    *   [ ] Adapt screen for both Add (new bond) and Edit (load existing bond) modes.
*   [ ] 4. **Bond Details Screen:**
    *   [ ] Create `BondDetailsViewModel`. Inject `GetBondDetailsUseCase`, `DeleteBondUseCase`.
    *   [ ] Load bond details based on passed ID. Expose state.
    *   [ ] Create `BondDetailsScreen` Composable. Display all bond fields.
    *   [ ] Add "Edit" button navigating to `AddEditBondScreen` in edit mode.
    *   [ ] Add "Delete" button with confirmation dialog, calling `DeleteBondUseCase`.

## Phase 3: Core Logic - Interest & Yield Calculations (Shared Module)

*   [ ] 1. **Interest Calculation Logic:**
    *   [ ] Create `InterestCalculator` utility class or functions in `shared/commonMain`.
    *   [ ] Implement logic to calculate payment dates based on `maturity_date`, `payment_frequency`, and `purchase_date` (or first payment date if known).
    *   [ ] Implement logic to calculate payment amount (`face_value_per_bond * quantity_purchased * coupon_rate / payments_per_year`).
    *   [ ] Handle different frequencies (Semi-Annual, Annual, Quarterly, Monthly).
    *   [ ] Handle ZeroCoupon bonds (no payments).
*   [ ] 2. **Domain Layer:** Create Use Cases:
    *   [ ] `GetNextInterestPaymentUseCase(bond: Bond)` -> `InterestPayment?`
    *   [ ] `GetAllFutureInterestPaymentsUseCase(bond: Bond)` -> `List<InterestPayment>`
    *   [ ] `GetPortfolioInterestScheduleUseCase(repository: BondRepository)` -> `List<InterestPayment>` (combines payments from all bonds, sorted)
    *   [ ] `GetMonthlyInterestSummaryUseCase(...)` -> `Map<YearMonth, Double>`
    *   [ ] `GetYearlyInterestSummaryUseCase(...)` -> `Map<Int, Double>`
*   [ ] 3. **Yield Calculation Logic:**
    *   [ ] Create `YieldCalculator` utility class/functions in `shared/commonMain`.
    *   [ ] Implement Average Coupon Rate calculation (weighted).
    *   [ ] Implement Average Current Yield calculation (weighted, based on purchase price).
    *   [ ] Research and implement Average YTM calculation (this is complex - consider using a library if available or a standard iterative formula). Decide on weighting method (initial cost vs. face value).
*   [ ] 4. **Domain Layer:** Create Use Cases:
    *   [ ] `CalculateAverageYieldUseCase(repository: BondRepository, type: YieldType)` -> `Double`

## Phase 4: Android UI - Portfolio Overview & Interest Tracking

*   [ ] 1. **Portfolio List Screen Enhancements:**
    *   [ ] Integrate `CalculateAverageYieldUseCase` into `PortfolioListViewModel`.
    *   [ ] Add UI elements (e.g., Cards) to display Total Portfolio Value (Initial Cost) and Average Yield (with selection dropdown - FR2.3).
    *   [ ] Implement Filter UI (e.g., Chips, Dropdown) for Bond Type (FR4.1). Update ViewModel and UseCase calls to support filtering.
*   [ ] 2. **Interest Schedule Screen:**
    *   [ ] Create `InterestScheduleViewModel`. Inject summary/schedule UseCases.
    *   [ ] Create `InterestScheduleScreen` Composable.
    *   [ ] Add Tabs/Toggles for "Upcoming Payments", "Monthly Summary", "Yearly Summary".
    *   [ ] Display list of all upcoming portfolio payments (FR3.2).
    *   [ ] Display monthly/yearly summaries (FR3.3).
*   [ ] 3. **Interest Calendar Screen:**
    *   [ ] Find/choose a KMP-compatible or Android-specific Calendar Compose library.
    *   [ ] Create `InterestCalendarViewModel`. Inject `GetPortfolioInterestScheduleUseCase`.
    *   [ ] Create `InterestCalendarScreen` Composable.
    *   [ ] Fetch all interest payments, process dates.
    *   [ ] Display calendar, highlighting dates with payments (FR3.4).
    *   [ ] Implement date selection to show payments due on that day.

## Phase 5: Refinement & Testing

*   [ ] 1. Implement comprehensive Unit Tests for Use Cases and Calculators in `shared/commonTest`.
*   [ ] 2. Implement Integration Tests for Repository layer in `shared` (using in-memory SQLDelight driver for tests).
*   [ ] 3. Implement UI Tests for Android screens (`androidApp/androidTest`).
*   [ ] 4. Code cleanup, documentation, and linting.
*   [ ] 5. UI Polish: Improve layouts, add animations/transitions, refine themes.
*   [ ] 6. Thorough manual testing on different Android devices/emulators.

## Phase 6: Future (Post-MVP)

*   [ ] 1. iOS App (`iosApp` module setup, SwiftUI UI implementation).
*   [ ] 2. Market Price API Integration (e.g., using Ktor in `shared` data layer).
*   [ ] 3. Cloud Sync/Backup.
*   [ ] 4. Notifications.
*   [ ] 5. Charts/Visualizations.
