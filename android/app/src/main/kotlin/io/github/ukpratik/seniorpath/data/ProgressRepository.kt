package io.github.ukpratik.seniorpath.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant

private val Context.progressStore by preferencesDataStore(name = "progress")

/** Progress on this phone (US-009, US-010, US-018), kept as one JSON value in the website's shape (see [ProgressState]). */
class ProgressRepository(private val context: Context, private val clock: () -> Instant = Instant::now) {
    private val key = stringPreferencesKey("seniorpath:v1")

    val state: Flow<ProgressState> = context.progressStore.data.map { ProgressState.parse(it[key]) }

    suspend fun current(): ProgressState = state.first()

    suspend fun setActivePath(id: String) = update { it.copy(activePath = id) }

    suspend fun setMapAll(all: Boolean) = update { it.copy(mapAll = all) }

    /** Returns the new state, so the caller can tell how many lessons are done now. */
    suspend fun markDone(id: String): ProgressState = update { it.markDone(id, clock()) }

    suspend fun undoDone(id: String) = update { it.undoDone(id) }

    suspend fun setLastOpened(id: String) = update { it.opened(id, clock()) }

    suspend fun setExportPrompted() = update { it.copy(exportPrompted = true) }

    suspend fun exportJson(): String = current().exportJson(clock())

    suspend fun importJson(text: String, known: Set<String>): ImportResult {
        var result: ImportResult = ImportResult.Failed.INVALID
        update { s -> s.importJson(text, known).let { (r, next) -> result = r; next } }
        return result
    }

    /** Replaces everything; tests use it to start clean, since DataStore outlives one test. */
    suspend fun replace(s: ProgressState) = update { s }

    private suspend fun update(fn: (ProgressState) -> ProgressState): ProgressState {
        var out = ProgressState()
        context.progressStore.edit { prefs -> out = fn(ProgressState.parse(prefs[key])); prefs[key] = out.encode() }
        return out
    }
}
