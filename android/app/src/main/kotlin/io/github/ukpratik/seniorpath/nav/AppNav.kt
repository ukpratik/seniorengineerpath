package io.github.ukpratik.seniorpath.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.feature.about.LicencesScreen
import io.github.ukpratik.seniorpath.feature.home.HomeScreen
import io.github.ukpratik.seniorpath.feature.map.MapScreen
import io.github.ukpratik.seniorpath.feature.progress.ProgressScreen
import io.github.ukpratik.seniorpath.feature.search.SearchScreen
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute
@Serializable data object MapRoute
@Serializable data object SearchRoute
@Serializable data object ProgressRoute
@Serializable data object LicencesRoute

private data class Tab(val route: Any, @param:StringRes val label: Int, @param:DrawableRes val icon: Int)

private val tabs = listOf(
    Tab(HomeRoute, R.string.tab_home, R.drawable.ic_home),
    Tab(MapRoute, R.string.tab_map, R.drawable.ic_map),
    Tab(SearchRoute, R.string.tab_search, R.drawable.ic_search),
    Tab(ProgressRoute, R.string.tab_progress, R.drawable.ic_check_circle),
)

/** Bottom bar on the four top-level screens (A-002); other screens stack on top and use system back. */
@Composable
fun AppNav(container: AppContainer) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val destination = entry?.destination
    val onTab = tabs.any { t -> destination?.hasRoute(t.route::class) == true }
    Scaffold(
        bottomBar = {
            if (onTab) {
                NavigationBar {
                    for (t in tabs) {
                        NavigationBarItem(
                            selected = destination?.hierarchy?.any { it.hasRoute(t.route::class) } == true,
                            onClick = {
                                nav.navigate(t.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(painterResource(t.icon), contentDescription = null) },
                            label = { Text(stringResource(t.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = HomeRoute, modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable<HomeRoute> { HomeScreen(container) }
            composable<MapRoute> { MapScreen(container) }
            composable<SearchRoute> { SearchScreen() }
            composable<ProgressRoute> { ProgressScreen(container, onLicences = { nav.navigate(LicencesRoute) }) }
            composable<LicencesRoute> { LicencesScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
