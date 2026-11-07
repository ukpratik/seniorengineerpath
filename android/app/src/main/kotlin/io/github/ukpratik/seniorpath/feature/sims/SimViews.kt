package io.github.ukpratik.seniorpath.feature.sims

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.sims.LagParams
import io.github.ukpratik.seniorpath.sims.RaftEventKind
import io.github.ukpratik.seniorpath.sims.RaftParams
import io.github.ukpratik.seniorpath.sims.RolloutParams
import io.github.ukpratik.seniorpath.sims.StampedeParams
import io.github.ukpratik.seniorpath.sims.burstTraffic
import io.github.ukpratik.seniorpath.sims.keys
import io.github.ukpratik.seniorpath.sims.loadCounts
import io.github.ukpratik.seniorpath.sims.modN
import io.github.ukpratik.seniorpath.sims.movedShare
import io.github.ukpratik.seniorpath.sims.nodeNames
import io.github.ukpratik.seniorpath.sims.ring
import io.github.ukpratik.seniorpath.sims.simulateBucket
import io.github.ukpratik.seniorpath.sims.simulateRaft
import io.github.ukpratik.seniorpath.sims.simulateRead
import io.github.ukpratik.seniorpath.sims.simulateRollout
import io.github.ukpratik.seniorpath.sims.simulateStampede
import io.github.ukpratik.seniorpath.sims.summary
import java.util.Locale
import kotlin.math.roundToInt

/** The ids the app can draw (ANIMS in src/lib/anims.ts). */
val SIM_IDS = listOf("cache-stampede", "replication-lag", "rollout", "token-bucket", "hash-ring", "raft-election")

/**
 * One of the six interactive animations (US-007). As on the site, nothing plays by itself: the reader moves sliders and
 * switches and the picture and sentence update at once, so "Remove animations" readers get the same thing.
 * The texts are the site's (src/scripts/anims.ts); keep them in step.
 */
@Composable
fun SimView(id: String, modifier: Modifier = Modifier) {
    val title = when (id) {
        "cache-stampede" -> R.string.sim_title_stampede
        "replication-lag" -> R.string.sim_title_lag
        "rollout" -> R.string.sim_title_rollout
        "token-bucket" -> R.string.sim_title_bucket
        "hash-ring" -> R.string.sim_title_ring
        "raft-election" -> R.string.sim_title_raft
        else -> null
    }
    Column(
        modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(stringResource(R.string.sim_eyebrow), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        if (title != null) Text(stringResource(title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        when (id) {
            "cache-stampede" -> CacheStampede()
            "replication-lag" -> ReplicationLag()
            "rollout" -> Rollout()
            "token-bucket" -> TokenBucket()
            "hash-ring" -> HashRing()
            "raft-election" -> RaftElection()
            else -> Text(stringResource(R.string.lesson_anim_soon), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun grouped(n: Int) = String.format(Locale.US, "%,d", n)

@Composable
private fun CacheStampede() {
    var rate by rememberSaveable { mutableIntStateOf(40) }
    var coalesce by rememberSaveable { mutableStateOf(false) }
    SimSlider("Requests to this server every 100 ms:", rate, 5..100, 5, "requests") { rate = it }
    SimSwitch("Request coalescing (one query per key, on this server)", coalesce) { coalesce = it }
    val ticks = remember(rate, coalesce) { simulateStampede(StampedeParams(30, rate, 8, 6, coalesce)) }
    val s = summary(ticks)
    BarChart(ticks.map { it.inFlight }, 600.0, "Queries from one app server (3 seconds)", "peak: ${s.peakInFlight} at once", if (coalesce) BarTone.OK else BarTone.BAD, 8, "key expires")
    SimResult(
        if (coalesce) "With coalescing: 1 database query from this server. ${rate * 6 - 1} requests wait a moment for its answer. (With many servers, each sends one; a lease can make it one in total.)"
        else "Without coalescing: ${s.totalQueries} database queries for the same value, up to ${s.peakInFlight} at once.",
    )
}

@Composable
private fun ReplicationLag() {
    var lag by rememberSaveable { mutableIntStateOf(800) }
    var after by rememberSaveable { mutableIntStateOf(300) }
    var ryw by rememberSaveable { mutableStateOf(false) }
    SimSlider("Replication lag:", lag, 0..2000, 100, "ms") { lag = it }
    SimSlider("You read again after:", after, 0..2000, 100, "ms") { after = it }
    SimSwitch("Read-your-writes (send your reads to the leader for 1 s)", ryw) { ryw = it }
    val r = simulateRead(LagParams(lag, after, ryw, 1000))
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.clearAndSetSemantics { }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Lane("Leader", 0f, "write: name = \"Asha\"", scheme.primary)
        Lane("Follower", lag / 2000f, "gets the write", scheme.secondary)
        Lane("You", after / 2000f, "read from ${r.readFrom}", if (r.stale) scheme.error else scheme.tertiary)
    }
    SimResult(
        if (r.stale) "Stale read: the follower still shows the old name. You just changed it, so it looks like your change was lost."
        else "Fresh read from the ${r.readFrom}: you see \"Asha\".",
        bad = r.stale,
    )
}

/** One timeline lane. The label sits at its time and shifts left by that share of its own width, so it always fits (BUG-016). */
@Composable
private fun Lane(who: String, at: Float, label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(who, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(64.dp))
        Box(Modifier.weight(1f).height(30.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.surface,
                maxLines = 1,
                modifier = Modifier.align(Alignment.CenterStart)
                    .layout { m, c ->
                        val p = m.measure(c.copy(minWidth = 0))
                        layout(c.maxWidth, p.height) {
                            val x = (c.maxWidth * at - p.width * at).roundToInt().coerceIn(0, (c.maxWidth - p.width).coerceAtLeast(0))
                            p.place(x, 0)
                        }
                    }
                    .background(color, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun Rollout() {
    var staged by rememberSaveable { mutableStateOf(false) }
    var detect by rememberSaveable { mutableIntStateOf(5) }
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = !staged, onClick = { staged = false }, label = { Text("Everywhere at once") })
        FilterChip(selected = staged, onClick = { staged = true }, label = { Text("In stages: 1%, 10%, 50%, 100%") })
    }
    SimSlider("Alerts notice errors after:", detect, 1..30, 1, "min") { detect = it }
    val r = simulateRollout(RolloutParams(staged, listOf(1, 10, 50, 100), 10, detect, 5, 60))
    BarChart(r.timeline, 100.0, "Users getting errors, per minute (1 hour)", "peak: ${r.peakPct}% of users", if (staged) BarTone.OK else BarTone.BAD, r.fixedAt, "fixed")
    SimResult("Peak: ${r.peakPct}% of users get errors. Total harm: ${grouped(r.userMinutes)} “user-percent minutes”. Fixed at minute ${r.fixedAt}.")
}

@Composable
private fun TokenBucket() {
    var cap by rememberSaveable { mutableIntStateOf(20) }
    var rate by rememberSaveable { mutableIntStateOf(50) }
    var burst by rememberSaveable { mutableIntStateOf(80) }
    SimSlider("Bucket size (biggest burst allowed):", cap, 5..100, 5, "tokens") { cap = it }
    SimSlider("Refill rate:", rate, 10..200, 10, "per second") { rate = it }
    SimSlider("Burst size at 1 second:", burst, 0..200, 10, "requests") { burst = it }
    // 3 seconds of steady traffic (3 requests per 100 ms = 30/s) with one burst.
    val arrivals = burstTraffic(30, 3, 10, burst)
    val t = simulateBucket(cap, rate, arrivals)
    val rejected = t.sumOf { it.rejected }
    val allowed = t.sumOf { it.allowed }
    BarChart(t.map { it.rejected }, maxOf(10, arrivals.max()).toDouble(), "Rejected requests (429), per 100 ms", "$rejected rejected", BarTone.BAD, 10, "burst")
    BarChart(t.map { it.tokens }, cap.toDouble(), "Tokens left in the bucket", "size $cap", BarTone.OK)
    SimResult(
        when {
            rejected > 0 -> "$allowed requests allowed, $rejected rejected with “429 Too Many Requests”. A bigger bucket allows bigger bursts; a faster refill allows more requests over time."
            burst > 0 -> "All $allowed requests allowed. The bucket was big enough for the burst."
            else -> "All $allowed requests allowed. Steady traffic stays under the refill rate."
        },
        bad = rejected > 0,
    )
}

private val ringKeys by lazy { keys(4000) }

@Composable
private fun HashRing() {
    var n by rememberSaveable { mutableIntStateOf(4) }
    var v by rememberSaveable { mutableIntStateOf(100) }
    SimSlider("Servers now:", n, 2..12, 1, "servers") { n = it }
    SimSlider("Virtual nodes per server (ring only):", v, 1..200, 1, "points") { v = it }
    val before = nodeNames(n)
    val after = nodeNames(n + 1)
    val ks = ringKeys
    val mod = remember(n) { movedShare(modN(ks, before), modN(ks, after)) }
    val placed = remember(n, v) { ring(ks, after, v) }
    val rng = remember(n, v) { movedShare(ring(ks, before, v), placed) }
    val shares = remember(n, v) { loadCounts(placed, after).map { it * 100.0 / ks.size } }
    val pct = { x: Double -> "${(x * 100).roundToInt()}%" }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Compare(pct(mod), "of keys move with hash mod N", bad = true, Modifier.weight(1f))
        Compare(pct(rng), "of keys move with a hash ring", bad = false, Modifier.weight(1f))
    }
    BarChart(
        shares, shares.max() * 1.1, "Share of keys per server on the ring (${after.size} servers)",
        "${pct(shares.min() / 100)}–${pct(shares.max() / 100)}", BarTone.PLAIN,
    )
    SimResult(
        "Adding server ${after.size}: the ring moved ${pct(rng)} of the keys, all to the new server (about 1 in ${after.size} on average). " +
            if (v < 10) "With few virtual nodes, shares and moves are uneven." else "With many virtual nodes, each server gets a similar share.",
    )
}

@Composable
private fun Compare(big: String, text: String, bad: Boolean, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier.border(1.dp, if (bad) scheme.error else scheme.tertiary, RoundedCornerShape(10.dp)).padding(10.dp),
    ) {
        Text(big, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if (bad) scheme.error else scheme.tertiary)
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RaftElection() {
    var nodes by rememberSaveable { mutableIntStateOf(5) }
    var stoppedCount by rememberSaveable { mutableIntStateOf(0) }
    var seed by rememberSaveable { mutableIntStateOf(1) }
    val stopped = List(stoppedCount) { it }
    val name = { i: Int -> "S${i + 1}" }
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (size in listOf(3, 5)) FilterChip(selected = nodes == size, onClick = { nodes = size; stoppedCount = 0 }, label = { Text("$size servers") })
    }
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Button(onClick = { stoppedCount = 1; seed++ }, enabled = stoppedCount == 0) { Text("Stop the leader") }
        OutlinedButton(onClick = { stoppedCount++; seed++ }, enabled = stoppedCount in 1 until nodes - 1) { Text("Stop one more server") }
        TextButton(onClick = { stoppedCount = 0 }) { Text("Reset") }
    }
    val r = remember(nodes, stoppedCount, seed) { simulateRaft(RaftParams(nodes, stopped, 100, 2500, seed)) }
    val scheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clearAndSetSemantics { }, horizontalArrangement = Arrangement.SpaceEvenly) {
        for (i in 0 until nodes) {
            val off = i in stopped
            val lead = !off && (if (stopped.isNotEmpty()) r.leader == i else i == 0)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(44.dp).background(
                        when { off -> scheme.surface; lead -> scheme.primary; else -> scheme.secondaryContainer }, CircleShape,
                    ).border(1.dp, if (off) scheme.outline else scheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(name(i), fontWeight = FontWeight.Bold, color = when { off -> scheme.outline; lead -> scheme.onPrimary; else -> scheme.onSecondaryContainer })
                }
                Text(if (off) "stopped" else if (lead) "leader" else "follower", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            }
        }
    }
    SimResult(
        when {
            stopped.isEmpty() -> "${name(0)} is the leader of term 1. It sends a heartbeat every 50 ms, so nobody starts an election."
            r.leader == null -> "No leader. Only ${nodes - stopped.size} of $nodes servers are running, and a leader needs ${r.majority} votes. Elections keep failing (term ${r.term}), so the cluster cannot accept writes."
            else -> "New leader: ${name(r.leader!!)} in term ${r.term}. ${nodes - stopped.size} of $nodes servers are running, enough for a majority of ${r.majority}."
        },
        bad = stopped.isNotEmpty() && r.leader == null,
    )
    val shown = r.events.filter { it.kind != RaftEventKind.VOTE || r.events.size < 14 }.take(12)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        shown.forEachIndexed { i, e ->
            val at = "${e.t} ms"
            Text(
                "${i + 1}. " + when (e.kind) {
                    RaftEventKind.STOPPED -> "$at: ${name(e.node)} stops."
                    RaftEventKind.TIMEOUT -> "$at: ${name(e.node)} hears nothing, starts an election for term ${e.term} and votes for itself."
                    RaftEventKind.VOTE -> "$at: ${name(e.node)} votes for ${name(e.forNode!!)} in term ${e.term}."
                    RaftEventKind.LEADER -> "$at: ${name(e.node)} has ${e.votes} of $nodes votes (a majority) and becomes leader of term ${e.term}."
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (e.kind == RaftEventKind.LEADER) FontWeight.Bold else null,
            )
        }
    }
}
