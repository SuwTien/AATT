package fr.bdst.aatt.ui.navigation

/**
 * Routes de navigation pour l'application
 */
sealed class NavRoutes(val route: String) {
    object Main : NavRoutes("main")
    object Edit : NavRoutes("edit")
    object Stats : NavRoutes("stats")
}