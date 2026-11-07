package io.github.ukpratik.seniorpath.content

import java.text.Normalizer

/** One search hit: the entry, its score, the text to show under the title, and where the query words are in it. */
data class SearchHit(val entry: SearchEntry, val score: Int, val snippet: String, val marks: List<IntRange>, val titleMarks: List<IntRange>)

/**
 * On-device search over lessons, coming-soon topics, glossary terms, incidents and case studies (US-012).
 * Every query word must match the start of a word in the entry (so "idem" finds "idempotency"); words of 5+ letters
 * also match with one typo. Ranking follows the site: the title first (exact, starts with, contains), then where the
 * words were found. A few hundred entries and ~600 KB of lesson text build in well under 100 ms.
 */
class SearchIndex(entries: List<SearchEntry>) {
    private class Doc(val entry: SearchEntry, val title: String, val titleWords: Set<String>, val textWords: Set<String>, val bodyWords: Set<String>)

    private val docs = entries.map { e ->
        val title = norm(e.title)
        Doc(e, title, wordSet(title), wordSet(norm(e.text)), wordSet(norm(e.body.orEmpty())))
    }

    fun search(query: String, limit: Int = 60): List<SearchHit> {
        val q = norm(query).trim()
        val qWords = words(q).distinct()
        if (q.length < 2 || qWords.isEmpty()) return emptyList()
        val hits = docs.mapNotNull { d -> score(d, q, qWords)?.let { d to it } }
            .sortedWith(compareByDescending<Pair<Doc, Int>> { it.second }.thenBy { TYPE_ORDER.indexOf(it.first.entry.type) }.thenBy { it.first.title })
            .take(limit)
        return hits.map { (d, s) ->
            val (snippet, marks) = snippet(d.entry, qWords)
            SearchHit(d.entry, s, snippet, marks, find(d.entry.title, qWords))
        }
    }

    private fun score(d: Doc, q: String, qWords: List<String>): Int? {
        var total = when {
            d.title == q -> 1000
            d.title.startsWith(q) -> 500
            d.title.contains(q) -> 250
            else -> 0
        }
        for (w in qWords) {
            val ws = stem(w)
            val hit = { words: Set<String> -> words.any { it.startsWith(w) || it.startsWith(ws) } }
            // A typo match always ranks below a real one.
            total += when {
                hit(d.titleWords) -> 60
                hit(d.textWords) -> 20
                hit(d.bodyWords) -> 5
                w.length >= 5 && d.titleWords.any { close(it, w) } -> 2
                w.length >= 5 && (d.textWords.any { close(it, w) } || d.bodyWords.any { close(it, w) }) -> 1
                else -> return null
            }
        }
        // Lessons are what most readers want; glossary entries are many and short.
        return total + when (d.entry.type) { "lesson" -> 8; "incident", "case" -> 4; "glossary" -> 2; else -> 0 }
    }

    /** The entry's short text, or for a lesson whose match is only in the body, ~160 characters around the first match. */
    private fun snippet(e: SearchEntry, qWords: List<String>): Pair<String, List<IntRange>> {
        val inText = find(e.text, qWords)
        val body = e.body
        if (inText.isNotEmpty() || body == null) return e.text to inText
        val first = find(body, qWords).firstOrNull() ?: return e.text to inText
        var start = (first.first - 60).coerceAtLeast(0)
        while (start > 0 && !body[start - 1].isWhitespace()) start--
        var end = (start + 160).coerceAtMost(body.length)
        while (end < body.length && !body[end].isWhitespace()) end++
        val cut = (if (start > 0) "…" else "") + body.substring(start, end).trim() + (if (end < body.length) "…" else "")
        return cut to find(cut, qWords)
    }

    companion object {
        val TYPE_ORDER = listOf("lesson", "glossary", "incident", "case", "topic")

        private val marks = Regex("\\p{M}+")

        /** Lower case without accents, so "café" and "cafe" match. Plain ASCII (almost all text) skips the slow path. */
        fun norm(s: String): String {
            val lower = s.lowercase()
            return if (lower.all { it.code < 128 }) lower else Normalizer.normalize(lower, Normalizer.Form.NFD).replace(marks, "")
        }

        /** Runs of letters and digits, by a plain loop: a regex over ~600 KB of lessons is slow on a phone. */
        fun words(s: String): List<String> = buildList { eachWord(s) { start, end -> add(s.substring(start, end)) } }

        /** Every word, plus its plain form ("retries" → "retry"), so either form of a word finds the other. */
        private fun wordSet(s: String): Set<String> = HashSet<String>().also { set ->
            eachWord(s) { a, b -> val w = s.substring(a, b); set.add(w); set.add(stem(w)) }
        }

        /** A very small English stemmer: enough for plurals and -ing/-ed forms of the words lessons use. */
        fun stem(w: String): String = when {
            w.length > 4 && (w.endsWith("ies") || w.endsWith("ied")) -> w.dropLast(3) + "y"
            w.length > 5 && w.endsWith("ing") -> w.dropLast(3)
            w.length > 4 && w.endsWith("ed") -> w.dropLast(2)
            w.length > 3 && w.endsWith("s") && !w.endsWith("ss") -> w.dropLast(1)
            else -> w
        }

        private inline fun eachWord(s: String, f: (Int, Int) -> Unit) {
            var start = -1
            for (i in s.indices) {
                if (s[i].isLetterOrDigit()) {
                    if (start < 0) start = i
                } else if (start >= 0) {
                    f(start, i); start = -1
                }
            }
            if (start >= 0) f(start, s.length)
        }

        /** Where words starting with a query word appear in [text], for highlighting. Same length as the original text. */
        fun find(text: String, qWords: List<String>): List<IntRange> {
            val n = norm(text)
            // NFD can change length for accented letters; fall back to no highlights then rather than mark the wrong letters.
            if (n.length != text.length) return emptyList()
            return buildList {
                eachWord(n) { a, b ->
                    val word = n.substring(a, b)
                    val w = qWords.filter { word.startsWith(it) }.maxByOrNull { it.length }
                    when {
                        w != null -> add(a until a + w.length)
                        qWords.any { stem(word).startsWith(stem(it)) } -> add(a until b) // "retries" for "retry"
                    }
                }
            }
        }

        /** One edit apart (a typo), comparing the query word with the start of a word of similar length. */
        fun close(word: String, q: String): Boolean {
            if (word.length < q.length - 1) return false
            val w = word.take(q.length + 1)
            return editDistanceAtMostOne(w.take(q.length), q) || editDistanceAtMostOne(w, q)
        }

        private fun editDistanceAtMostOne(a: String, b: String): Boolean {
            if (kotlin.math.abs(a.length - b.length) > 1) return false
            var i = 0
            var j = 0
            var edits = 0
            while (i < a.length && j < b.length) {
                if (a[i] == b[j]) { i++; j++; continue }
                if (++edits > 1) return false
                when {
                    a.length > b.length -> i++
                    a.length < b.length -> j++
                    else -> { i++; j++ }
                }
            }
            return edits + (a.length - i) + (b.length - j) <= 1
        }
    }
}
