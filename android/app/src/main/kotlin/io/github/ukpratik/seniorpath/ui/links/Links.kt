package io.github.ukpratik.seniorpath.ui.links

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import io.github.ukpratik.seniorpath.BuildConfig
import io.github.ukpratik.seniorpath.R

/**
 * Opens web links (sources, postmortems, report a mistake) in a Custom Tab in the app's colours (US-013, US-014).
 * Custom Tabs need no INTERNET permission in this app; with no browser at all, it says so instead of crashing.
 */
class CustomTabsUriHandler(private val context: Context, private val toolbar: Int, private val dark: Boolean) : UriHandler {
    override fun openUri(uri: String) {
        val u = uri.toUri()
        val colors = CustomTabColorSchemeParams.Builder().setToolbarColor(toolbar).build()
        val tab = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setColorScheme(if (dark) CustomTabsIntent.COLOR_SCHEME_DARK else CustomTabsIntent.COLOR_SCHEME_LIGHT)
            .setDefaultColorSchemeParams(colors)
            .build()
        try {
            tab.launchUrl(context, u)
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, u).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, R.string.no_browser, Toast.LENGTH_LONG).show()
            }
        }
    }
}

/** The lesson's page on the website. */
fun lessonUrl(slug: String): String = "${BuildConfig.SITE_URL}/learn/$slug"

/** The GitHub issue form for a content mistake, filled in as the site's issueUrl() does (src/config.ts). */
fun issueUrl(lessonId: String, pageUrl: String): String {
    val body = "Lesson: $lessonId\nPage: $pageUrl\n\nWhat is wrong:\n\nWhat it should say (with a source, if you have one):\n"
    return Uri.Builder().scheme("https").authority("github.com").path("${BuildConfig.REPO}/issues/new")
        .appendQueryParameter("title", "Mistake in lesson: $lessonId")
        .appendQueryParameter("body", body)
        .appendQueryParameter("labels", "content-mistake")
        .build().toString()
}

/** The share sheet with the lesson's title and link (US-015). */
fun shareLesson(context: Context, title: String, slug: String, chooser: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, "$title\n${lessonUrl(slug)}")
    }
    context.startActivity(Intent.createChooser(send, chooser))
}
