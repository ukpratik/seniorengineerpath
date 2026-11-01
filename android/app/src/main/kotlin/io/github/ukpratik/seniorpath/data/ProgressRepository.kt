package io.github.ukpratik.seniorpath.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressStore by preferencesDataStore(name = "progress")

/**
 * Progress on this phone (US-009, US-018), kept as one JSON value in the website's shape (see [ProgressState]).
 * A2 writes the active path and the map filter; marking done, "last opened" and export/import come in A5.
 */
class ProgressRepository(private val context: Context) {
    private val key = stringPreferencesKey("seniorpath:v1")

    val state: Flow<ProgressState> = context.progressStore.data.map { ProgressState.parse(it[key]) }

    suspend fun setActivePath(id: String) = update { it.copy(activePath = id) }

    suspend fun setMapAll(all: Boolean) = update { it.copy(mapAll = all) }

    private suspend fun update(fn: (ProgressState) -> ProgressState) {
        context.progressStore.edit { prefs -> prefs[key] = fn(ProgressState.parse(prefs[key])).encode() }
    }
}
