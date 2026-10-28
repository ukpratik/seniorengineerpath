package io.github.ukpratik.seniorpath.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Context.settingsStore by preferencesDataStore(name = "settings")

/** Reader settings (US-021). Stored in DataStore, so they go to backup with the progress (data_extraction_rules.xml). */
class SettingsRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme")

    val theme: Flow<ThemeMode> = context.settingsStore.data.map { prefs ->
        prefs[themeKey]?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } } ?: ThemeMode.SYSTEM
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.settingsStore.edit { it[themeKey] = mode.name }
    }
}
