package io.github.ukpratik.seniorpath.sims

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

// Kotlin port of the six simulation models in src/scripts/sims/*.ts (SPEC.md there). Pure and deterministic.
// tests/fixtures/sims/cases.json holds the TypeScript models' outputs; SimsFixtureTest checks these give the same.

// ---- cache-stampede ----
data class StampedeParams(val ticks: Int, val requestsPerTick: Int, val expireAt: Int, val recomputeTicks: Int, val coalesce: Boolean)
data class StampedeTick(val t: Int, val hits: Int, val dbQueries: Int, val waiting: Int, val inFlight: Int)
data class StampedeSummary(val totalQueries: Int, val peakInFlight: Int)

fun simulateStampede(p: StampedeParams): List<StampedeTick> {
    val out = ArrayList<StampedeTick>(p.ticks)
    var cachedUntil = p.expireAt.toDouble() // value is valid for t < cachedUntil
    val finishing = ArrayList<Int>() // end ticks of running queries
    var waiting = 0
    for (t in 0 until p.ticks) {
        if (finishing.any { it == t }) {
            cachedUntil = Double.POSITIVE_INFINITY
            waiting = 0
        }
        finishing.removeAll { it <= t }
        var hits = 0
        var dbQueries = 0
        if (t < cachedUntil) {
            hits = p.requestsPerTick
        } else if (p.coalesce) {
            if (finishing.isEmpty()) {
                dbQueries = 1
                finishing += t + p.recomputeTicks
            }
            waiting += p.requestsPerTick - dbQueries
        } else {
            dbQueries = p.requestsPerTick
            repeat(dbQueries) { finishing += t + p.recomputeTicks }
        }
        out += StampedeTick(t, hits, dbQueries, if (p.coalesce) waiting else 0, finishing.size)
    }
    return out
}

fun summary(ticks: List<StampedeTick>) = StampedeSummary(ticks.sumOf { it.dbQueries }, max(0, ticks.maxOfOrNull { it.inFlight } ?: 0))

// ---- replication-lag ----
data class LagParams(val lagMs: Int, val readAfterMs: Int, val readYourWrites: Boolean, val rywWindowMs: Int)
data class LagResult(val readFrom: String, val value: String, val stale: Boolean)

fun simulateRead(p: LagParams): LagResult {
    if (p.readYourWrites && p.readAfterMs < p.rywWindowMs) return LagResult("leader", "new", false)
    val applied = p.readAfterMs >= p.lagMs
    return LagResult("follower", if (applied) "new" else "old", !applied)
}

// ---- rollout ----
data class RolloutParams(val staged: Boolean, val stages: List<Int>, val bakeMinutes: Int, val detectMinutes: Int, val rollbackMinutes: Int, val minutes: Int)
data class RolloutResult(val timeline: List<Int>, val peakPct: Int, val userMinutes: Int, val detectedAt: Int, val fixedAt: Int)

fun simulateRollout(p: RolloutParams): RolloutResult {
    val detectedAt = p.detectMinutes
    val fixedAt = detectedAt + p.rollbackMinutes
    val timeline = (0 until p.minutes).map { m ->
        val reach = if (!p.staged) 100 else {
            val k = min(p.stages.size - 1, m / p.bakeMinutes)
            val lastStarted = min(k, max(0, detectedAt - 1) / p.bakeMinutes)
            p.stages[if (m >= detectedAt) lastStarted else k]
        }
        if (m < fixedAt) reach else 0
    }
    return RolloutResult(timeline, timeline.max(), timeline.sum(), detectedAt, fixedAt)
}

// ---- token-bucket ----
data class BucketTick(val t: Int, val allowed: Int, val rejected: Int, val tokens: Double)

fun simulateBucket(capacity: Int, refillPerSec: Int, arrivals: List<Int>): List<BucketTick> {
    var tokens = capacity.toDouble() // starts full
    return arrivals.mapIndexed { t, n ->
        if (t > 0) tokens = min(capacity.toDouble(), tokens + refillPerSec / 10.0)
        val allowed = min(n, floor(tokens).toInt())
        tokens -= allowed
        BucketTick(t, allowed, n - allowed, jsRound(tokens * 100) / 100.0)
    }
}

/** Steady traffic with one burst in the middle. */
fun burstTraffic(ticks: Int, steadyPerTick: Int, burstAt: Int, burstSize: Int): List<Int> =
    List(ticks) { t -> steadyPerTick + if (t == burstAt) burstSize else 0 }

/** JavaScript's Math.round: halves go up (towards +∞). */
private fun jsRound(x: Double): Double = floor(x + 0.5)

// ---- hash-ring ----
/** FNV-1a 32-bit with murmur3's fmix32, over UTF-16 code units, exactly as hash() in hash-ring.ts. Unsigned, as a Long. */
fun hash(s: String): Long {
    var h = 0x811c9dc5.toInt()
    for (c in s) {
        h = h xor c.code
        h *= 0x01000193
    }
    h = h xor (h ushr 16)
    h *= 0x85ebca6b.toInt()
    h = h xor (h ushr 13)
    h *= 0xc2b2ae35.toInt()
    h = h xor (h ushr 16)
    return h.toLong() and 0xffffffffL
}

fun nodeNames(n: Int): List<String> = List(n) { "server-${it + 1}" }

fun keys(n: Int): List<String> = List(n) { "user:$it" }

fun modN(ks: List<String>, nodes: List<String>): Map<String, String> = ks.associateWith { nodes[(hash(it) % nodes.size).toInt()] }

fun ring(ks: List<String>, nodes: List<String>, vnodes: Int): Map<String, String> {
    val points = nodes.flatMap { n -> List(vnodes) { v -> hash("$n#$v") to n } }.sortedBy { it.first }
    fun owner(h: Long): String {
        var lo = 0
        var hi = points.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (points[mid].first < h) lo = mid + 1 else hi = mid
        }
        return points[lo % points.size].second
    }
    return ks.associateWith { owner(hash(it)) }
}

fun movedShare(before: Map<String, String>, after: Map<String, String>): Double =
    if (before.isEmpty()) 0.0 else before.count { (k, n) -> after[k] != n }.toDouble() / before.size

/** Keys per server, in [nodes] order. */
fun loadCounts(assign: Map<String, String>, nodes: List<String>): List<Int> {
    val counts = assign.values.groupingBy { it }.eachCount()
    return nodes.map { counts[it] ?: 0 }
}

// ---- raft-election ----
data class RaftParams(
    val nodes: Int,
    val stopped: List<Int>,
    val stopAt: Int,
    val untilMs: Int,
    val seed: Int,
    val minTimeout: Int = 150,
    val maxTimeout: Int = 300,
    val heartbeatMs: Int = 50,
    val latencyMs: Int = 10,
)

enum class RaftEventKind { STOPPED, TIMEOUT, VOTE, LEADER }
data class RaftEvent(val t: Int, val node: Int, val kind: RaftEventKind, val term: Int, val votes: Int? = null, val forNode: Int? = null)
data class RaftResult(val events: List<RaftEvent>, val leader: Int?, val term: Int, val majority: Int)

/** mulberry32, as in raft-election.ts. */
private fun rng(seed: Int): () -> Double {
    var a = seed
    return {
        a += 0x6d2b79f5
        var t = a
        t = (t xor (t ushr 15)) * (t or 1)
        t = t xor (t + (t xor (t ushr 7)) * (t or 61))
        ((t xor (t ushr 14)).toLong() and 0xffffffffL) / 4294967296.0
    }
}

private enum class Role { FOLLOWER, CANDIDATE, LEADER }
private enum class MsgType { HEARTBEAT, REQUEST_VOTE, GRANT }
private class Msg(val at: Int, val to: Int, val from: Int, val type: MsgType, val term: Int)

fun simulateRaft(p: RaftParams): RaftResult {
    val rand = rng(p.seed)
    val majority = p.nodes / 2 + 1
    val timeout = { p.minTimeout + floor(rand() * (p.maxTimeout - p.minTimeout + 1)).toInt() }
    val term = IntArray(p.nodes) { 1 }
    val role = Array(p.nodes) { Role.FOLLOWER }.also { it[0] = Role.LEADER }
    val votedFor = arrayOfNulls<Int>(p.nodes).also { it[0] = 0 }
    val votes = IntArray(p.nodes)
    val alive = BooleanArray(p.nodes) { true }
    val deadline = IntArray(p.nodes) { timeout() }
    var nextHeartbeat = 0
    val queue = ArrayList<Msg>()
    val events = ArrayList<RaftEvent>()
    fun send(from: Int, to: Int, type: MsgType, t: Int, tm: Int) {
        if (to != from) queue += Msg(t + p.latencyMs, to, from, type, tm)
    }

    for (t in 0..p.untilMs) {
        if (t == p.stopAt) for (n in p.stopped) {
            alive[n] = false
            events += RaftEvent(t, n, RaftEventKind.STOPPED, term[n])
        }
        // Deliver messages due now, newest first as the TypeScript loop does (messages to stopped nodes are lost).
        var i = queue.size - 1
        while (i >= 0) {
            val m = queue[i]
            if (m.at != t) { i--; continue }
            queue.removeAt(i)
            i--
            val n = m.to
            if (!alive[n]) continue
            if (m.term > term[n]) {
                term[n] = m.term
                role[n] = Role.FOLLOWER
                votedFor[n] = null
            }
            if (m.type == MsgType.HEARTBEAT && m.term >= term[n]) {
                role[n] = Role.FOLLOWER
                deadline[n] = t + timeout()
            } else if (m.type == MsgType.REQUEST_VOTE && m.term == term[n] && (votedFor[n] == null || votedFor[n] == m.from)) {
                votedFor[n] = m.from
                deadline[n] = t + timeout()
                events += RaftEvent(t, n, RaftEventKind.VOTE, term[n], forNode = m.from)
                send(n, m.from, MsgType.GRANT, t, term[n])
            } else if (m.type == MsgType.GRANT && role[n] == Role.CANDIDATE && m.term == term[n]) {
                votes[n]++
                if (votes[n] >= majority) {
                    role[n] = Role.LEADER
                    events += RaftEvent(t, n, RaftEventKind.LEADER, term[n], votes = votes[n])
                    nextHeartbeat = t
                }
            }
        }
        for (n in 0 until p.nodes) {
            if (!alive[n]) continue
            if (role[n] == Role.LEADER) {
                if (t >= nextHeartbeat) for (o in 0 until p.nodes) send(n, o, MsgType.HEARTBEAT, t, term[n])
                continue
            }
            if (t >= deadline[n]) {
                term[n]++
                role[n] = Role.CANDIDATE
                votedFor[n] = n
                votes[n] = 1
                deadline[n] = t + timeout()
                events += RaftEvent(t, n, RaftEventKind.TIMEOUT, term[n])
                for (o in 0 until p.nodes) send(n, o, MsgType.REQUEST_VOTE, t, term[n])
            }
        }
        if ((0 until p.nodes).any { role[it] == Role.LEADER && alive[it] } && t >= nextHeartbeat) nextHeartbeat = t + p.heartbeatMs
    }
    val leaders = (0 until p.nodes).filter { role[it] == Role.LEADER && alive[it] }
    return RaftResult(events, leaders.lastOrNull(), term.max(), majority)
}

