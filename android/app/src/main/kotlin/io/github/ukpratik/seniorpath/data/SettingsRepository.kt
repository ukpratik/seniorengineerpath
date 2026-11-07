package io.github.ukpratik.seniorpath.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Where the reader was in a lesson (US-002): the list item and the pixel offset inside it. */
data class ReadingSpot(val lesson: String, val index: Int, val offset: Int)

private val Context.settingsStore by preferencesDataStore(name = "settings")

/**
 * Reader settings (US-021) and app-only state that the website file doesn't carry: the reading position and the
 * lessons the reader has been told about (A-004). Stored in DataStore, so they go to backup with the progress.
 */
class SettingsRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme")
    private val spotKey = stringPreferencesKey("reading_spot")
    private val seenKey = stringSetPreferencesKey("seen_lessons")

    val theme: Flow<ThemeMode> = context.settingsStore.data.map { prefs ->
        prefs[themeKey]?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } } ?: ThemeMode.SYSTEM
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.settingsStore.edit { it[themeKey] = mode.name }
    }

    suspend fun readingSpot(): ReadingSpot? = context.settingsStore.data.first()[spotKey]?.split('|')?.let { p ->
        if (p.size == 3) ReadingSpot(p[0], p[1].toIntOrNull() ?: 0, p[2].toIntOrNull() ?: 0) else null
    }

    suspend fun clearReadingSpot() {
        context.settingsStore.edit { it.remove(spotKey) }
    }

    suspend fun setReadingSpot(spot: ReadingSpot) {
        context.settingsStore.edit { it[spotKey] = "${spot.lesson}|${spot.index}|${spot.offset}" }
    }

    /**
     * What's new (A-004): the lessons in [current] the reader hasn't been told about. On the first run everything
     * counts as seen, so a new install shows no card.
     */
    suspend fun unseenLessons(current: List<String>): List<String> {
        val seen = context.settingsStore.data.first()[seenKey]
        if (seen == null) {
            markLessonsSeen(current)
            return emptyList()
        }
        return current.filter { it !in seen }
    }

    suspend fun markLessonsSeen(current: List<String>) {
        context.settingsStore.edit { prefs -> prefs[seenKey] = current.toSet() }
    }
}
