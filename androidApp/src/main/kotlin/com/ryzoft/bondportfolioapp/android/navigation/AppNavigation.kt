package com.ryzoft.bondportfolioapp.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ryzoft.bondportfolioapp.android.presentation.screens.addedit.AddEditBondScreen
import com.ryzoft.bondportfolioapp.android.presentation.screens.details.BondDetailsScreen
import com.ryzoft.bondportfolioapp.android.presentation.screens.interest.InterestScheduleScreen
import com.ryzoft.bondportfolioapp.android.presentation.screens.portfolio.PortfolioListScreen

/**
 * Defines the navigation routes for the Bond Portfolio app
 */
object Routes {
    const val PORTFOLIO_LIST = "portfolio_list"
    const val BOND_DETAILS = "bond_details"
    const val ADD_BOND = "add_bond"
    const val EDIT_BOND = "edit_bond"
    const val INTEREST_SCHEDULE = "interest_schedule"
    
    // Routes with arguments
    fun bondDetailsRoute(bondId: Long): String = "$BOND_DETAILS/$bondId"
    fun editBondRoute(bondId: Long): String = "$EDIT_BOND/$bondId"
}

/**
 * AppNavHost sets up the navigation graph for the app
 */
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.PORTFOLIO_LIST
    ) {
        // Portfolio list screen - the main/home screen
        composable(Routes.PORTFOLIO_LIST) {
            PortfolioListScreen(
                onBondClick = { bondId ->
                    navController.navigate(Routes.bondDetailsRoute(bondId))
                },
                onAddBondClick = {
                    navController.navigate(Routes.ADD_BOND)
                },
                onInterestScheduleClick = {
                    navController.navigate(Routes.INTEREST_SCHEDULE)
                }
            )
        }

        // Bond details screen - view details of a specific bond
        composable(
            route = "${Routes.BOND_DETAILS}/{bondId}",
            arguments = listOf(
                navArgument("bondId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val bondId = backStackEntry.arguments?.getLong("bondId") ?: return@composable
            BondDetailsScreen(
                bondId = bondId,
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = {
                    navController.navigate(Routes.editBondRoute(bondId))
                }
            )
        }

        // Add bond screen - create a new bond
        composable(Routes.ADD_BOND) {
            AddEditBondScreen(
                bondId = null, // null means we're adding a new bond
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveComplete = {
                    navController.popBackStack()
                }
            )
        }

        // Edit bond screen - edit an existing bond
        composable(
            route = "${Routes.EDIT_BOND}/{bondId}",
            arguments = listOf(
                navArgument("bondId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val bondId = backStackEntry.arguments?.getLong("bondId") ?: return@composable
            AddEditBondScreen(
                bondId = bondId,
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveComplete = {
                    // Pop back to the details screen
                    navController.popBackStack()
                }
            )
        }
        
        // Interest Schedule screen - view interest payments and summaries
        composable(Routes.INTEREST_SCHEDULE) {
            InterestScheduleScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
