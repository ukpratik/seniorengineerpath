plugins {
    id("seniorpath.jvm.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}

// The contract test parses the real export (npm run export:app), so the app never ships content it can't read.
tasks.withType<Test>().configureEach {
    val dir = rootProject.layout.projectDirectory.dir("app/src/main/assets/content")
    inputs.dir(dir).withPathSensitivity(PathSensitivity.RELATIVE).optional()
    systemProperty("contentDir", dir.asFile.path)
}
