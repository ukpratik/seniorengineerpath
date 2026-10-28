package io.github.ukpratik.seniorpath.content

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

/** The export schema this app understands (APP_SCHEMA in src/content-tools/app-export.ts). */
const val SUPPORTED_SCHEMA = 1

class UnsupportedContentException(message: String) : IllegalStateException(message)

/**
 * Read-only access to the bundled content. [read] returns the text of a file under `assets/content/`
 * (the app passes an AssetManager reader; tests read the export folder). Files are parsed once and kept in memory.
 * Call from a background thread: the first read of each file does I/O.
 */
class ContentRepository(
    private val read: (path: String) -> String,
    private val json: Json = lenient,
) {
    val manifest: Manifest by lazy {
        val m = decode("manifest.json", Manifest.serializer())
        if (m.schema != SUPPORTED_SCHEMA) throw UnsupportedContentException("Content schema ${m.schema}, app reads $SUPPORTED_SCHEMA")
        m
    }

    val glossary: List<GlossaryEntry> by lazy { decode("glossary.json", ListSerializer(GlossaryEntry.serializer())) }
    val glossaryById: Map<String, GlossaryEntry> by lazy { glossary.associateBy { it.id } }
    val incidents: List<Incident> by lazy { decode("incidents.json", ListSerializer(Incident.serializer())) }
    val cases: List<CaseStudy> by lazy { decode("cases.json", ListSerializer(CaseStudy.serializer())) }
    val search: List<SearchEntry> by lazy { decode("search.json", ListSerializer(SearchEntry.serializer())) }
    val diagramStyle: DiagramStyle by lazy { decode("diagram-style.json", DiagramStyle.serializer()) }

    private val lessons = ConcurrentHashMap<String, Lesson>()
    private val diagrams = ConcurrentHashMap<String, Diagram>()

    fun topic(id: String): Topic? = manifest.topics[id]

    /** The topic whose `slug` (the site's URL part) matches; slugs and ids differ for a few topics. */
    fun topicBySlug(slug: String): Pair<String, Topic>? = manifest.topics.entries.firstOrNull { it.value.slug == slug }?.toPair()

    fun lesson(id: String): Lesson = lessons.getOrPut(id) { decode("lessons/$id.json", Lesson.serializer()) }

    fun diagram(id: String): Diagram = diagrams.getOrPut(id) { decode("diagrams/$id.json", Diagram.serializer()) }

    fun defaultPath(): LearningPath? = manifest.paths.firstOrNull { it.default } ?: manifest.paths.firstOrNull()

    private fun <T> decode(path: String, strategy: DeserializationStrategy<T>): T = json.decodeFromString(strategy, read(path))

    companion object {
        /** The app's parser: tolerates fields a newer export adds within the same schema. */
        val lenient = Json { ignoreUnknownKeys = true }

        /** The contract test's parser: any field the models don't know is an error. */
        val strict = Json { ignoreUnknownKeys = false }
    }
}
