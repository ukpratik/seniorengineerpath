@file:OptIn(ExperimentalSerializationApi::class)

package io.github.ukpratik.seniorpath.content

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

// Kotlin side of the export in src/content-tools/app-export.ts (schema APP_SCHEMA). Change both together.

@Serializable
data class Manifest(
    val schema: Int,
    val contentHash: String,
    val lessonCount: Int,
    val lessons: List<String>,
    val tracks: List<Track>,
    val topics: Map<String, Topic>,
    val paths: List<LearningPath>,
    val anims: List<String>,
)

@Serializable
data class Track(val id: String, val title: String, val short: String, val index: Int, val modules: List<Module>)

@Serializable
data class Module(val id: String, val title: String, val topics: List<String>)

@Serializable
data class TopicLink(val title: String, val url: String)

@Serializable
data class Topic(
    val slug: String,
    val title: String,
    val oneLiner: String,
    val level: String,
    val track: String,
    val module: String,
    val prereqs: List<String> = emptyList(),
    val link: TopicLink? = null,
    val written: Boolean,
    val subtitle: String? = null,
    val readingMinutes: Int? = null,
)

@Serializable
data class LearningPath(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val default: Boolean = false,
    val steps: List<String>,
)

@Serializable
data class Lesson(
    val id: String,
    val slug: String,
    val title: String,
    val subtitle: String,
    val level: String,
    val track: String,
    val module: String,
    val readingMinutes: Int,
    val illustrativeStory: Boolean = false,
    val reviewedBy: String? = null,
    val reviewedOn: String? = null,
    val majorRevisedOn: String? = null,
    val sections: List<Section>,
    val quiz: List<QuizQuestion>,
    val sources: List<Source>,
    val incidents: List<String>,
    val cases: List<String>,
    val related: List<String>,
    val glossaryUsed: List<String>,
)

@Serializable
data class Section(val n: Int, val name: String, val title: String, val blocks: List<Block>)

@Serializable
data class QuizQuestion(val q: List<Inline>, val options: List<List<Inline>>, val answer: Int, val why: List<Inline>)

@Serializable
data class Source(val id: String, val title: String, val url: String? = null, val where: String? = null, val note: String? = null)

@Serializable
@JsonClassDiscriminator("t")
sealed interface Inline {
    @Serializable @SerialName("text") data class Text(val v: String) : Inline
    @Serializable @SerialName("strong") data class Strong(val c: List<Inline>) : Inline
    @Serializable @SerialName("em") data class Em(val c: List<Inline>) : Inline
    @Serializable @SerialName("code") data class Code(val v: String) : Inline
    @Serializable @SerialName("link") data class Link(val url: String, val c: List<Inline>) : Inline
    @Serializable @SerialName("term") data class Term(val id: String, val v: String) : Inline
    @Serializable @SerialName("cite") data class Cite(val id: String, val n: Int) : Inline
    @Serializable @SerialName("br") data object Break : Inline
}

@Serializable
data class Cell(val c: List<Inline>, val mark: String? = null)

@Serializable
data class AiColumn(val kind: String, val title: List<Inline>, val blocks: List<Block>)

@Serializable
@JsonClassDiscriminator("t")
sealed interface Block {
    @Serializable @SerialName("p") data class Paragraph(val c: List<Inline>) : Block
    @Serializable @SerialName("h") data class Heading(val depth: Int, val c: List<Inline>) : Block
    @Serializable @SerialName("list") data class ListBlock(val ordered: Boolean, val start: Int? = null, val items: List<List<Block>>) : Block
    @Serializable @SerialName("code") data class Code(val lang: String? = null, val v: String) : Block
    @Serializable @SerialName("table") data class Table(val head: List<List<Inline>>, val rows: List<List<Cell>>) : Block
    @Serializable @SerialName("callout") data class Callout(val kind: String, val blocks: List<Block>) : Block
    @Serializable @SerialName("quote") data class Quote(val blocks: List<Block>) : Block
    @Serializable @SerialName("diagram") data class Diagram(val id: String) : Block
    @Serializable @SerialName("anim") data class Anim(val id: String) : Block
    @Serializable @SerialName("prompt") data class Prompt(val c: List<Inline>) : Block
    @Serializable @SerialName("checks") data class Checks(val items: List<List<Block>>) : Block
    @Serializable @SerialName("ai") data class Ai(val cols: List<AiColumn>) : Block
}

@Serializable
data class GlossaryEntry(val id: String, val term: String, val forms: List<String> = emptyList(), val short: String)

@Serializable
data class Incident(
    val id: String,
    val title: String,
    val date: String,
    val company: String,
    val broke: String,
    val why: String,
    val lesson: String,
    val url: String,
    val tags: List<String>,
    val lessons: List<String> = emptyList(),
    val verified: Boolean = false,
)

@Serializable
data class CaseStudy(
    val id: String,
    val company: String,
    val title: String,
    val lesson: String,
    val url: String,
    val tags: List<String>,
    val lessons: List<String> = emptyList(),
    val verified: Boolean = false,
)

@Serializable
data class Diagram(val id: String, val title: String, val viewBox: String, val frames: Int, val captions: List<String>, val svg: String)

@Serializable
data class DiagramStyle(val light: String, val dark: String, val palette: Map<String, Map<String, String>>)

@Serializable
data class SearchEntry(val type: String, val id: String, val title: String, val text: String, val body: String? = null)
