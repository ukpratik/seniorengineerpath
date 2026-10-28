package io.github.ukpratik.seniorpath

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
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
            SeniorPathTheme(dark) { AppNav(container) }
        }
    }

    /** Status and navigation bar icons follow the app's theme, not the phone's, when the reader picks one. */
    private fun applySystemBars(dark: Boolean) {
        val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}
