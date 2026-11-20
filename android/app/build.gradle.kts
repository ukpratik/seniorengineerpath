plugins {
    id("seniorpath.android.application")
    id("seniorpath.android.compose")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutlibraries.android)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "io.github.ukpratik.seniorpath"

    defaultConfig {
        applicationId = "io.github.ukpratik.seniorpath" // F-Droid and sideload; the Play build adds ".play" (D-041)
        // versionCode = MAJOR × 10000 + MINOR × 100 + PATCH, as in the other apps.
        versionCode = 10010
        versionName = "1.0.10"
        // Lesson share links and "Report a mistake" (src/config.ts SITE.url and SITE.repo).
        buildConfigField("String", "SITE_URL", "\"https://seniorpath.dev\"")
        buildConfigField("String", "REPO", "\"ukpratik/seniorengineerpath\"")
    }

    // Keys live in ~/.android-keys/, passwords in ~/.gradle/gradle.properties, never in the repo.
    // The config exists only when its properties do; otherwise release builds stay unsigned (CI).
    signingConfigs {
        val storeFilePath = providers.gradleProperty("seniorpath.upload.storeFile").orNull
        if (storeFilePath != null) {
            create("upload") {
                storeFile = file(storeFilePath)
                storePassword = providers.gradleProperty("seniorpath.upload.storePassword").get()
                keyAlias = providers.gradleProperty("seniorpath.upload.keyAlias").get()
                keyPassword = providers.gradleProperty("seniorpath.upload.keyPassword").get()
            }
        }
        // Google Play has its own package name and upload key, so it never collides with the F-Droid build (D-041).
        val playStoreFile = providers.gradleProperty("seniorpath.play.storeFile").orNull
        if (playStoreFile != null) {
            create("play") {
                storeFile = file(playStoreFile)
                storePassword = providers.gradleProperty("seniorpath.play.storePassword").get()
                keyAlias = providers.gradleProperty("seniorpath.play.keyAlias").get()
                keyPassword = providers.gradleProperty("seniorpath.play.keyPassword").get()
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("upload")?.let { signingConfig = it }
        }
        // Google Play build: `./gradlew :app:bundlePlay`. Same code and content as release, separate package name
        // (io.github.ukpratik.seniorpath.play) and key, so the F-Droid build (assembleRelease) is unchanged.
        create("play") {
            initWith(getByName("release"))
            applicationIdSuffix = ".play"
            matchingFallbacks += "release"
            signingConfig = signingConfigs.findByName("play")
        }
    }

    buildFeatures.buildConfig = true

    // Robolectric tests need the merged resources and the content assets.
    testOptions.unitTests.isIncludeAndroidResources = true
}

// The content comes from the site repo (`npm run export:app`, ADR-007). Fail early with the fix, not with a crash at start-up.
val checkContent = tasks.register("checkContentExport") {
    val manifest = layout.projectDirectory.file("src/main/assets/content/manifest.json")
    inputs.files(manifest).optional()
    doLast {
        check(manifest.asFile.exists()) { "No app content. Run `npm run export:app` in the repo root first." }
    }
}
tasks.named("preBuild") { dependsOn(checkContent) }

// A8: the start-up Baseline Profile (generated on a device by :baselineprofile) ships in the APK.
baselineProfile {
    mergeIntoMain = true
}

// Licence screen data, generated offline at build time. Strict mode fails the build on any licence outside the allowlist.
aboutLibraries {
    offlineMode = true
    collect {
        configPath = file("aboutlibraries")
        fetchRemoteLicense = false
        fetchRemoteFunding = false
        filterVariants.addAll("release")
    }
    export {
        excludeFields.addAll("generated", "funding", "developers")
    }
    license {
        strictMode = com.mikepenz.aboutlibraries.plugin.StrictMode.FAIL
        allowedLicenses.addAll("Apache-2.0", "MIT", "BSD-2-Clause", "BSD-3-Clause", "OFL-1.1", "ISC")
    }
}

dependencies {
    implementation(project(":content"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidsvg)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
