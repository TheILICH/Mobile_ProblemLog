@file:OptIn(ExperimentalLayoutApi::class)

package com.example.problemlog.ui.navigation

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.problemlog.ui.ProblemsViewModel
import com.example.problemlog.ui.screens.EditProblemRoute
import com.example.problemlog.ui.screens.ProblemDetailRoute
import com.example.problemlog.ui.screens.ProblemListRoute
import com.example.problemlog.ui.screens.ReviewRoute
import com.example.problemlog.ui.screens.StatsRoute

object Routes {
    const val PROBLEMS = "problems"
    const val REVIEW = "review"
    const val STATS = "stats"
    const val DETAIL = "problem/{id}"
    const val EDIT = "edit?id={id}"

    fun detail(id: String) = "problem/$id"
    fun edit(id: String? = null) = if (id == null) "edit" else "edit?id=$id"
}

private data class TopLevelItem(val route: String, val label: String, val icon: ImageVector)

private val topLevelItems = listOf(
    TopLevelItem(Routes.PROBLEMS, "Problems", Icons.AutoMirrored.Filled.List),
    TopLevelItem(Routes.REVIEW, "Review", Icons.Default.Refresh),
    TopLevelItem(Routes.STATS, "Stats", Icons.Default.Star)
)

@Composable
fun ProblemLogApp() {
    val navController = rememberNavController()
    // created here so it belongs to the activity and all screens share it
    val viewModel: ProblemsViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = topLevelItems.any { it.route == currentRoute }

    Scaffold(
        // inner screens have their own scaffold, so outer one adds no insets
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                BottomBar(currentRoute) { route -> navController.navigateTopLevel(route) }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.PROBLEMS,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Routes.PROBLEMS) {
                ProblemListRoute(
                    viewModel = viewModel,
                    onOpenProblem = { id -> navController.navigate(Routes.detail(id)) },
                    onAddProblem = { navController.navigate(Routes.edit()) }
                )
            }
            composable(Routes.REVIEW) {
                ReviewRoute(
                    viewModel = viewModel,
                    onOpenProblem = { id -> navController.navigate(Routes.detail(id)) }
                )
            }
            composable(Routes.STATS) {
                StatsRoute(viewModel = viewModel)
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                ProblemDetailRoute(
                    viewModel = viewModel,
                    problemId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.edit(id)) }
                )
            }
            composable(
                route = Routes.EDIT,
                arguments = listOf(navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { entry ->
                EditProblemRoute(
                    viewModel = viewModel,
                    editingId = entry.arguments?.getString("id"),
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}

// standard bottom nav: keep one copy of each tab and restore its scroll state
private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BottomBar(currentRoute: String?, onSelect: (String) -> Unit) {
    NavigationBar {
        topLevelItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onSelect(item.route) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) }
            )
        }
    }
}
