package io.github.ukpratik.seniorpath.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// Marking done and moving progress between the website and the app (US-009, US-010). A Kotlin port of
// src/scripts/store.ts: same file, same checks, same merge rules. Pinned by tests/fixtures/progress/*.json on both sides.

const val PROGRESS_SCHEMA_VERSION = 1
const val MAX_IMPORT_BYTES = 100_000

private val idPattern = Regex("^[a-z0-9-]+$")

/** The time format JavaScript's `toISOString()` writes (always milliseconds, always UTC), so string order is time order. */
private val isoMillis = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)

fun isoTime(at: Instant): String = isoMillis.format(at)

/** The moment a stored time means, or null. Accepts what the web writes plus plain dates, as `Date.parse` does. */
fun parseTime(v: String): Instant? = runCatching { Instant.parse(v) }.getOrNull()
    ?: runCatching { OffsetDateTime.parse(v).toInstant() }.getOrNull()
    ?: runCatching { LocalDateTime.parse(v).toInstant(ZoneOffset.UTC) }.getOrNull()
    ?: runCatching { LocalDate.parse(v).atStartOfDay().toInstant(ZoneOffset.UTC) }.getOrNull()

fun ProgressState.markDone(id: String, now: Instant): ProgressState =
    copy(done = done + (id to isoTime(now)), ever = if (id in ever) ever else ever + id)

fun ProgressState.undoDone(id: String): ProgressState = copy(done = done - id)

fun ProgressState.opened(id: String, now: Instant): ProgressState = copy(lastOpened = id, lastOpenedAt = isoTime(now))

private val pretty = Json { prettyPrint = true; prettyPrintIndent = "  " }

/** The export file, as the website's "Export progress (.json)" writes it. */
fun ProgressState.exportJson(now: Instant): String = pretty.encodeToString(
    JsonObject.serializer(),
    buildJsonObject {
        put("schema_version", PROGRESS_SCHEMA_VERSION)
        put("exported_at", isoTime(now))
        putJsonObject("done") { for ((k, v) in done) put(k, v) }
        put("last_opened", lastOpened?.let(::JsonPrimitive) ?: JsonNull)
        put("last_opened_at", lastOpenedAt?.let(::JsonPrimitive) ?: JsonNull)
    },
)

sealed interface ImportResult {
    data class Ok(val added: Int, val ignored: Int) : ImportResult
    enum class Failed : ImportResult { INVALID, NEWER, TOO_BIG }
}

/**
 * Merges an export file into this progress: every lesson done in either place stays done, the earlier finish date
 * wins, and the newer "last opened" wins. Ids not in [known] (renamed or removed lessons) are skipped and counted.
 */
fun ProgressState.importJson(text: String, known: Set<String>? = null): Pair<ImportResult, ProgressState> {
    if (text.length > MAX_IMPORT_BYTES) return ImportResult.Failed.TOO_BIG to this
    val data = runCatching { Json.parseToJsonElement(text) }.getOrNull() as? JsonObject ?: return ImportResult.Failed.INVALID to this
    val version = (data["schema_version"] as? JsonPrimitive)?.takeIf { !it.isString }?.doubleOrNull ?: return ImportResult.Failed.INVALID to this
    if (version > PROGRESS_SCHEMA_VERSION) return ImportResult.Failed.NEWER to this
    val doneIn = data["done"] as? JsonObject ?: return ImportResult.Failed.INVALID to this
    val valid = doneIn.mapNotNull { (k, v) ->
        val at = (v as? JsonPrimitive)?.takeIf { it.isString }?.content
        if (idPattern.matches(k) && at != null && parseTime(at) != null) k to at else null
    }
    val incoming = if (known != null) valid.filter { it.first in known } else valid
    var added = 0
    val merged = done.toMutableMap()
    val everNow = ever.toMutableList()
    for ((id, at) in incoming) {
        val have = merged[id]
        if (have == null) added++
        if (have == null || at < have) merged[id] = at
        if (id !in everNow) everNow += id
    }
    var next = copy(done = merged, ever = everNow)
    val lastId = (data["last_opened"] as? JsonPrimitive)?.takeIf { it.isString }?.content
    val lastAt = (data["last_opened_at"] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull.orEmpty()
    if (lastId != null && idPattern.matches(lastId) && (lastOpenedAt == null || lastAt > lastOpenedAt)) {
        next = next.copy(lastOpened = lastId, lastOpenedAt = lastAt.ifEmpty { lastOpenedAt })
    }
    return ImportResult.Ok(added, valid.size - incoming.size) to next
}

