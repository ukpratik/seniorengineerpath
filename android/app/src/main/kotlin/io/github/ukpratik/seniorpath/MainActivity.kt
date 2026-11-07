package io.github.ukpratik.seniorpath

import android.graphics.Color
import android.app.UiModeManager
import android.os.Build
import android.os.Bundle
import io.github.ukpratik.seniorpath.data.ThemeMode
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import io.github.ukpratik.seniorpath.ui.links.CustomTabsUriHandler
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ukpratik.seniorpath.nav.AppNav
import io.github.ukpratik.seniorpath.ui.theme.SeniorPathTheme
import io.github.ukpratik.seniorpath.ui.theme.isDark

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as SeniorPathApp).container
        setContent {
            // Until the saved theme loads (a few ms) only the window background shows, never a wrong-theme flash.
            val mode by container.settings.theme.collectAsStateWithLifecycle(initialValue = null)
            val m = mode ?: return@setContent
            val dark = isDark(m)
            LaunchedEffect(dark) { applySystemBars(dark) }
            LaunchedEffect(m) { applyNightMode(m) }
            SeniorPathTheme(dark) {
                val toolbar = MaterialTheme.colorScheme.surface.toArgb()
                val links = remember(dark, toolbar) { CustomTabsUriHandler(this, toolbar, dark) }
                CompositionLocalProvider(LocalUriHandler provides links) { AppNav(container) }
            }
        }
    }

    /**
     * Android 12+: tell the system the reader's theme choice, so the window background and the splash screen use it
     * from the very first frame of the next start (no light flash in a dark app). Earlier versions follow the phone.
     */
    private fun applyNightMode(mode: ThemeMode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val ui = getSystemService(UiModeManager::class.java) ?: return
        val want = when (mode) {
            ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
            ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
            ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
        }
        if (ui.nightMode != want) ui.setApplicationNightMode(want)
    }

    /** Status and navigation bar icons follow the app's theme, not the phone's, when the reader picks one. */
    private fun applySystemBars(dark: Boolean) {
        val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}
