package io.github.ukpratik.seniorpath.data

import io.github.ukpratik.seniorpath.content.LearningPath
import io.github.ukpratik.seniorpath.content.Manifest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The reader's progress, in the same shape as the website's `seniorpath:v1` (src/scripts/store.ts), so A5 can move it
 * between web and app without a translation step. Unknown fields are ignored; anything unreadable starts empty, as on the web.
 */
@Serializable
data class ProgressState(
    val v: Int = 1,
    val done: Map<String, String> = emptyMap(), // lesson id → ISO time it was marked done
    val ever: List<String> = emptyList(),
    val lastOpened: String? = null,
    val lastOpenedAt: String? = null,
    val activePath: String? = null,
    val theme: String? = null,
    val exportPrompted: Boolean? = null,
    val mapAll: Boolean? = null, // map shows coming-soon topics too
) {
    fun encode(): String = json.encodeToString(serializer(), this)

    companion object {
        // The web only accepts a value with `v` and `done`, so defaults are written; unset optional fields are left out.
        private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }

        fun parse(raw: String?): ProgressState {
            if (raw.isNullOrBlank()) return ProgressState()
            val s = runCatching { json.decodeFromString(serializer(), raw) }.getOrNull() ?: return ProgressState()
            return if (s.v == 1) s else ProgressState()
        }
    }
}

/** How a topic shows on the map and in paths. */
enum class TopicStatus { DONE, HERE, WRITTEN, SOON }

fun ProgressState.statusOf(id: String, manifest: Manifest): TopicStatus = when {
    manifest.topics[id]?.written != true -> TopicStatus.SOON
    id in done -> TopicStatus.DONE
    id == lastOpened -> TopicStatus.HERE
    else -> TopicStatus.WRITTEN
}

/** The reader's chosen path, or the default one if none is chosen (or the chosen one was removed). */
fun ProgressState.activePath(manifest: Manifest): LearningPath? =
    manifest.paths.firstOrNull { it.id == activePath } ?: manifest.paths.firstOrNull { it.default } ?: manifest.paths.firstOrNull()

/** The first written step of [path] not done yet, with its 0-based position; null when all written steps are done. */
fun ProgressState.nextStep(path: LearningPath, manifest: Manifest): IndexedValue<String>? =
    path.steps.withIndex().firstOrNull { (_, id) -> manifest.topics[id]?.written == true && id !in done }

/** What the map's "Continue" button opens: the lesson in progress, else the next lesson on the active path. */
fun ProgressState.continueTarget(manifest: Manifest): String? =
    lastOpened?.takeIf { manifest.topics[it]?.written == true && it !in done }
        ?: activePath(manifest)?.let { nextStep(it, manifest)?.value }
