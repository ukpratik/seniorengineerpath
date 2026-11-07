package io.github.ukpratik.seniorpath.sims

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.Test
import java.io.File

/** The Kotlin models give exactly the TypeScript models' outputs (tests/fixtures/sims/cases.json, sims-fixture.test.ts). */
class SimsFixtureTest {
    private val root: JsonObject = Json.parseToJsonElement(
        listOf("../tests/fixtures/sims/cases.json", "../../tests/fixtures/sims/cases.json").map(::File).first { it.exists() }.readText(),
    ).jsonObject

    private fun cases(name: String) = root.getValue(name).jsonArray.map { it.jsonObject }
    private val JsonElement.i get() = jsonPrimitive.int
    private fun JsonObject.p(k: String) = getValue("params").jsonObject.getValue(k)

    @Test fun `cache stampede`() {
        for (c in cases("stampede")) {
            val p = StampedeParams(c.p("ticks").i, c.p("requestsPerTick").i, c.p("expireAt").i, c.p("recomputeTicks").i, c.p("coalesce").jsonPrimitive.boolean)
            val ticks = simulateStampede(p)
            val want = c.getValue("ticks").jsonArray.map { it.jsonObject }
            assertThat(ticks.map { listOf(it.t, it.hits, it.dbQueries, it.waiting, it.inFlight) })
                .isEqualTo(want.map { w -> listOf("t", "hits", "dbQueries", "waiting", "inFlight").map { w.getValue(it).i } })
            val s = c.getValue("summary").jsonObject
            assertThat(summary(ticks)).isEqualTo(StampedeSummary(s.getValue("totalQueries").i, s.getValue("peakInFlight").i))
        }
    }

    @Test fun `replication lag`() {
        for (c in cases("lag")) {
            val r = simulateRead(LagParams(c.p("lagMs").i, c.p("readAfterMs").i, c.p("readYourWrites").jsonPrimitive.boolean, c.p("rywWindowMs").i))
            val w = c.getValue("result").jsonObject
            assertThat(r).isEqualTo(LagResult(w.getValue("readFrom").jsonPrimitive.content, w.getValue("value").jsonPrimitive.content, w.getValue("stale").jsonPrimitive.boolean))
        }
    }

    @Test fun rollout() {
        for (c in cases("rollout")) {
            val r = simulateRollout(
                RolloutParams(
                    staged = c.p("strategy").jsonPrimitive.content == "staged",
                    stages = c.p("stages").jsonArray.map { it.i },
                    bakeMinutes = c.p("bakeMinutes").i,
                    detectMinutes = c.p("detectMinutes").i,
                    rollbackMinutes = c.p("rollbackMinutes").i,
                    minutes = c.p("minutes").i,
                ),
            )
            val w = c.getValue("result").jsonObject
            assertThat(r.timeline).isEqualTo(w.getValue("timeline").jsonArray.map { it.jsonObject.getValue("affectedPct").i })
            assertThat(listOf(r.peakPct, r.userMinutes, r.detectedAt, r.fixedAt))
                .isEqualTo(listOf("peakPct", "userMinutes", "detectedAt", "fixedAt").map { w.getValue(it).i })
        }
    }

    @Test fun `token bucket`() {
        for (c in cases("bucket")) {
            val arrivals = c.p("arrivals").jsonArray.map { it.i }
            assertThat(arrivals).isEqualTo(burstTraffic(30, 3, 10, arrivals[10] - 3))
            val ticks = simulateBucket(c.p("capacity").i, c.p("refillPerSec").i, arrivals)
            val want = c.getValue("ticks").jsonArray.map { it.jsonObject }
            assertThat(ticks.map { listOf(it.t.toDouble(), it.allowed.toDouble(), it.rejected.toDouble(), it.tokens) })
                .isEqualTo(want.map { w -> listOf("t", "allowed", "rejected", "tokens").map { w.getValue(it).jsonPrimitive.double } })
        }
    }

    @Test fun `hash and hash ring`() {
        for (h in root.getValue("hashes").jsonArray.map { it.jsonObject }) {
            assertThat(hash(h.getValue("s").jsonPrimitive.content)).isEqualTo(h.getValue("h").jsonPrimitive.long)
        }
        for (c in cases("ring")) {
            val n = c.p("servers").i
            val v = c.p("vnodes").i
            val ks = keys(c.p("keys").i)
            val before = nodeNames(n)
            val after = nodeNames(n + 1)
            assertThat(Math.round(movedShare(modN(ks, before), modN(ks, after)) * ks.size).toInt()).isEqualTo(c.getValue("modMoved").i)
            assertThat(Math.round(movedShare(ring(ks, before, v), ring(ks, after, v)) * ks.size).toInt()).isEqualTo(c.getValue("ringMoved").i)
            assertThat(loadCounts(ring(ks, after, v), after)).isEqualTo(c.getValue("ringCounts").jsonArray.map { it.i })
        }
    }

    @Test fun `raft election`() {
        for (c in cases("raft")) {
            val r = simulateRaft(RaftParams(c.p("nodes").i, c.p("stopped").jsonArray.map { it.i }, c.p("stopAt").i, c.p("untilMs").i, c.p("seed").i))
            val w = c.getValue("result").jsonObject
            val events = w.getValue("events").jsonArray.map { it.jsonObject }.map { e ->
                RaftEvent(
                    e.getValue("t").i, e.getValue("node").i, RaftEventKind.valueOf(e.getValue("kind").jsonPrimitive.content.uppercase()), e.getValue("term").i,
                    votes = e["votes"]?.i, forNode = e["for"]?.i,
                )
            }
            assertThat(r.events).isEqualTo(events)
            assertThat(r.leader).isEqualTo(w.getValue("leader").takeUnless { it is JsonNull }?.i)
            assertThat(r.term).isEqualTo(w.getValue("term").i)
            assertThat(r.majority).isEqualTo(w.getValue("majority").i)
        }
    }

}
