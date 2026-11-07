// Generates the app's Baseline Profile and measures cold start (A8). Needs a device or emulator (API 33+);
// never part of the unit-test run or CI.
//   Generate: ./gradlew :app:generateBaselineProfile
//   Measure:  ./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest
//             (on an emulator add -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR)
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "io.github.ukpratik.seniorpath.baselineprofile"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 28
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":app"
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro)
}
