package com.yizuka17.dailylife.feature.transaction.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yizuka17.dailylife.feature.transaction.categorysettings.ui.CategorySettingsScreen
import com.yizuka17.dailylife.feature.transaction.details.ui.TransactionDetailsScreen
import com.yizuka17.dailylife.feature.transaction.editor.ui.TransactionEditorScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.transactionNavGraph(
    navController: NavHostController,
) {
    composable(
        route = TransactionRoute.ADD_EDIT_TRANSACTION,
        arguments = listOf(
            navArgument("transactionId") {
                type = NavType.IntType
                defaultValue = -1 // -1 表示新建
            },
            navArgument("categoryId") {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            },
            navArgument("isExpense") {
                type = NavType.BoolType
                defaultValue = true
            }
        )
    ) {
        TransactionEditorScreen(navController = navController)
    }
    composable(
        route = TransactionRoute.TRANSACTION_DETAILS,
        arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
    ) {
        TransactionDetailsScreen(navController = navController)
    }
    composable(TransactionRoute.CATEGORY_SETTINGS) {
        CategorySettingsScreen(navController = navController)
    }
}
