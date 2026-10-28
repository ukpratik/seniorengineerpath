package io.github.ukpratik.seniorpath.content

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * Parses every file of the real export with the strict parser, so a change to app-export.ts that the
 * Kotlin models don't follow fails here, not on a reader's phone.
 */
class ExportContractTest {
    private val dir = File(System.getProperty("contentDir") ?: "app/src/main/assets/content")
    private val repo by lazy { ContentRepository({ File(dir, it).readText() }, ContentRepository.strict) }

    private fun requireExport() {
        val present = File(dir, "manifest.json").exists()
        // CI runs `npm run export:app` first; locally, say what to do instead of failing with a stack trace.
        if (System.getenv("CI") != null) check(present) { "No export at $dir. Run `npm run export:app` in the repo root." }
        assumeTrue("No export at $dir. Run `npm run export:app` in the repo root.", present)
    }

    @Test
    fun `manifest is the supported schema and lists every lesson file`() {
        requireExport()
        val m = repo.manifest
        assertThat(m.schema).isEqualTo(SUPPORTED_SCHEMA)
        assertThat(m.lessons).hasSize(m.lessonCount)
        val files = File(dir, "lessons").list()!!.map { it.removeSuffix(".json") }
        assertThat(files).containsExactlyElementsIn(m.lessons)
        assertThat(m.topics.values.count { it.written }).isEqualTo(m.lessonCount)
    }

    @Test
    fun `every lesson parses and only references things that exist`() {
        requireExport()
        val m = repo.manifest
        val glossary = repo.glossaryById.keys
        val incidents = repo.incidents.map { it.id }.toSet()
        val cases = repo.cases.map { it.id }.toSet()
        for (id in m.lessons) {
            val l = repo.lesson(id)
            assertThat(l.id).isEqualTo(id)
            assertThat(l.sections.map { it.name }).containsExactly(
                "What broke", "The idea in simple words", "How it works", "Trade-offs",
                "At work", "With AI", "Check yourself", "Go deeper",
            ).inOrder()
            assertThat(glossary).containsAtLeastElementsIn(l.glossaryUsed)
            assertThat(incidents).containsAtLeastElementsIn(l.incidents)
            assertThat(cases).containsAtLeastElementsIn(l.cases)
            assertThat(m.topics.keys).containsAtLeastElementsIn(l.related)
            for (q in l.quiz) assertThat(q.answer).isIn(q.options.indices)
            val sourceIds = l.sources.map { it.id }
            forEachBlock(l.sections.flatMap { it.blocks }) { b ->
                when (b) {
                    is Block.Diagram -> assertThat(repo.diagram(b.id).frames).isEqualTo(repo.diagram(b.id).captions.size)
                    is Block.Anim -> assertThat(m.anims).contains(b.id)
                    else -> Unit
                }
                inlinesOf(b).forEach { i ->
                    if (i is Inline.Cite) assertThat(sourceIds.indexOf(i.id) + 1).isEqualTo(i.n)
                    if (i is Inline.Term) assertThat(glossary).contains(i.id)
                }
            }
        }
    }

    @Test
    fun `libraries, search and diagram style parse`() {
        requireExport()
        assertThat(repo.glossary).isNotEmpty()
        assertThat(repo.incidents).isNotEmpty()
        assertThat(repo.cases).isNotEmpty()
        assertThat(repo.search.map { it.type }.toSet()).containsAtLeast("lesson", "topic", "glossary", "incident", "case")
        assertThat(repo.diagramStyle.palette.keys).containsExactly("light", "dark")
        assertThat(repo.defaultPath()).isNotNull()
        for (t in repo.manifest.tracks) for (mod in t.modules) assertThat(repo.manifest.topics.keys).containsAtLeastElementsIn(mod.topics)
    }

    @Test
    fun `a newer schema is refused with a clear error`() {
        val repo = ContentRepository({
            """{"schema":99,"contentHash":"x","lessonCount":0,"lessons":[],"tracks":[],"topics":{},"paths":[],"anims":[]}"""
        })
        val e = assertThrows(UnsupportedContentException::class.java) { repo.manifest }
        assertThat(e).hasMessageThat().contains("schema 99")
    }

    private fun forEachBlock(blocks: List<Block>, f: (Block) -> Unit) {
        for (b in blocks) {
            f(b)
            when (b) {
                is Block.ListBlock -> b.items.forEach { forEachBlock(it, f) }
                is Block.Checks -> b.items.forEach { forEachBlock(it, f) }
                is Block.Callout -> forEachBlock(b.blocks, f)
                is Block.Quote -> forEachBlock(b.blocks, f)
                is Block.Ai -> b.cols.forEach { forEachBlock(it.blocks, f) }
                else -> Unit
            }
        }
    }

    private fun inlinesOf(b: Block): List<Inline> {
        fun flat(xs: List<Inline>): List<Inline> = xs.flatMap {
            when (it) {
                is Inline.Strong -> listOf(it) + flat(it.c)
                is Inline.Em -> listOf(it) + flat(it.c)
                is Inline.Link -> listOf(it) + flat(it.c)
                else -> listOf(it)
            }
        }
        return when (b) {
            is Block.Paragraph -> flat(b.c)
            is Block.Heading -> flat(b.c)
            is Block.Prompt -> flat(b.c)
            is Block.Table -> b.head.flatMap(::flat) + b.rows.flatten().flatMap { flat(it.c) }
            is Block.Ai -> b.cols.flatMap { flat(it.title) }
            else -> emptyList()
        }
    }
}
