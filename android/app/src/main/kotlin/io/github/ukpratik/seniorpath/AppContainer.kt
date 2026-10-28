package io.github.ukpratik.seniorpath

import android.content.Context
import io.github.ukpratik.seniorpath.content.ContentRepository
import io.github.ukpratik.seniorpath.data.SettingsRepository

/** Manual wiring (architecture.md §4): the app is small enough not to need a DI framework. */
class AppContainer(context: Context) {
    private val assets = context.applicationContext.assets

    /** The bundled export (`assets/content/`, from `npm run export:app`). */
    val content = ContentRepository({ path -> assets.open("content/$path").bufferedReader().use { it.readText() } })

    val settings = SettingsRepository(context.applicationContext)
}
