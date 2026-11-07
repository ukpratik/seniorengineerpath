package io.github.ukpratik.seniorpath.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PACKAGE = "io.github.ukpratik.seniorpath"
private const val TIMEOUT_MS = 10_000L

/** Records launch to Home, opening the first lesson and scrolling it, and the Map tab, into the shipped profile. */
@RunWith(AndroidJUnit4::class)
class StartupProfileGenerator {
    @get:Rule val rule = BaselineProfileRule()

    @Test fun generate() = rule.collect(packageName = PACKAGE, includeInStartupProfile = true) {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.text("All learning paths")), TIMEOUT_MS)
        device.findObject(By.textStartsWith("Lesson 1"))?.click()
        device.wait(Until.hasObject(By.desc("Contents")), TIMEOUT_MS)
        device.findObject(By.scrollable(true))?.let { list -> repeat(3) { list.fling(Direction.DOWN) } }
        device.pressBack()
        device.wait(Until.hasObject(By.text("Map")), TIMEOUT_MS)
        device.findObject(By.text("Map"))?.click()
        device.waitForIdle()
    }
}

/** A-001 / NFR: cold start to Home. Compares no profile with the shipped Baseline Profile. */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test fun startupWithoutProfile() = startup(CompilationMode.None())

    @Test fun startupWithBaselineProfile() = startup(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun startup(mode: CompilationMode) = rule.measureRepeated(
        packageName = PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = mode,
        startupMode = StartupMode.COLD,
        iterations = 8,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
        device.wait(Until.hasObject(By.text("All learning paths")), TIMEOUT_MS)
    }
}
