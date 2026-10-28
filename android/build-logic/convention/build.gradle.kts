plugins {
    `kotlin-dsl`
}

group = "io.github.ukpratik.seniorpath.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "seniorpath.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidCompose") {
            id = "seniorpath.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id = "seniorpath.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
