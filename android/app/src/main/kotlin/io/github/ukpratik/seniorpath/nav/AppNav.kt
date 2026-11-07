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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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
import androidx.navigation.toRoute
import io.github.ukpratik.seniorpath.AppContainer
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.feature.about.LicencesScreen
import io.github.ukpratik.seniorpath.feature.home.HomeScreen
import io.github.ukpratik.seniorpath.feature.lesson.LessonScreen
import io.github.ukpratik.seniorpath.feature.map.MapScreen
import io.github.ukpratik.seniorpath.feature.paths.PathScreen
import io.github.ukpratik.seniorpath.feature.paths.PathsScreen
import io.github.ukpratik.seniorpath.feature.progress.ProgressScreen
import io.github.ukpratik.seniorpath.feature.search.SearchScreen
import io.github.ukpratik.seniorpath.feature.search.SearchTargets
import io.github.ukpratik.seniorpath.feature.library.CasesScreen
import io.github.ukpratik.seniorpath.feature.library.GlossaryScreen
import io.github.ukpratik.seniorpath.feature.library.IncidentsScreen
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute
@Serializable data object MapRoute
@Serializable data object SearchRoute
@Serializable data object ProgressRoute
@Serializable data object LicencesRoute
@Serializable data object PathsRoute
@Serializable data class PathRoute(val id: String)
@Serializable data class LessonRoute(val id: String)
@Serializable data class IncidentsRoute(val focus: String? = null)
@Serializable data class CasesRoute(val focus: String? = null)
@Serializable data object GlossaryRoute

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
    // Build the search index in the background once the first screen is up, so the Search tab answers at once.
    LaunchedEffect(Unit) {
        delay(1500)
        withContext(Dispatchers.Default) { runCatching { container.content.searchIndex } }
    }
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
            val lesson = { id: String -> nav.navigate(LessonRoute(id)) }
            val path = { id: String -> nav.navigate(PathRoute(id)) }
            val back: () -> Unit = { nav.popBackStack() }
            composable<HomeRoute> { HomeScreen(container, onLesson = lesson, onPath = path, onPaths = { nav.navigate(PathsRoute) }) }
            composable<MapRoute> { MapScreen(container, onLesson = lesson, onPath = path) }
            composable<SearchRoute> {
                SearchScreen(container, SearchTargets(
                    onLesson = lesson,
                    onIncident = { nav.navigate(IncidentsRoute(it)) },
                    onCase = { nav.navigate(CasesRoute(it)) },
                    onGlossary = { nav.navigate(GlossaryRoute) },
                ))
            }
            composable<IncidentsRoute> { IncidentsScreen(container, it.toRoute<IncidentsRoute>().focus, onLesson = lesson, onBack = back) }
            composable<CasesRoute> { CasesScreen(container, it.toRoute<CasesRoute>().focus, onLesson = lesson, onBack = back) }
            composable<GlossaryRoute> { GlossaryScreen(container, onBack = back) }
            composable<ProgressRoute> { ProgressScreen(container, onLicences = { nav.navigate(LicencesRoute) }, onLesson = lesson) }
            composable<LicencesRoute> { LicencesScreen(onBack = back) }
            composable<PathsRoute> { PathsScreen(container, onPath = path, onBack = back) }
            composable<PathRoute> { PathScreen(container, it.toRoute<PathRoute>().id, onLesson = lesson, onBack = back) }
            composable<LessonRoute> {
                // Previous / Next and related topics replace the lesson on screen, so Back returns to where reading began.
                LessonScreen(container, it.toRoute<LessonRoute>().id, onLesson = { id ->
                    nav.navigate(LessonRoute(id)) { popUpTo<LessonRoute> { inclusive = true } }
                }, onBack = back, onIncident = { nav.navigate(IncidentsRoute(it)) }, onCase = { nav.navigate(CasesRoute(it)) }, onProgress = {
                    nav.navigate(ProgressRoute) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                    }
                })
            }
        }
    }
}
