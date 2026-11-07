package io.github.ukpratik.seniorpath.feature.diagram

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGExternalFileResolver
import io.github.ukpratik.seniorpath.R
import io.github.ukpratik.seniorpath.content.Diagram
import io.github.ukpratik.seniorpath.content.DiagramStyle
import io.github.ukpratik.seniorpath.content.cssFor
import java.util.concurrent.ConcurrentHashMap

/**
 * Draws one frame of a diagram with AndroidSVG: the exported SVG plus the theme CSS and [frameCss]
 * (architecture.md §5). Text uses Plus Jakarta Sans, as on the site; glyphs it lacks (such as ✓) fall back to the system font.
 */
class DiagramRenderer(context: Context, private val style: DiagramStyle) {
    private val parsed = ConcurrentHashMap<String, SVG>()

    init {
        installFont(context.applicationContext)
    }

    /**
     * Frame [k] (1-based) at [widthPx] wide, on the diagram background colour, or null if AndroidSVG can't draw this
     * diagram (the viewer then shows the steps as text instead of crashing). Call off the main thread.
     */
    fun renderOrNull(d: Diagram, k: Int, dark: Boolean, widthPx: Int): Bitmap? = runCatching { render(d, k, dark, widthPx) }
        .onFailure { Log.w("DiagramRenderer", "Can't draw ${d.id} step $k", it) }
        .getOrNull()

    /** As [renderOrNull], but throws; tests use it to find diagrams the app can't draw. */
    fun render(d: Diagram, k: Int, dark: Boolean, widthPx: Int): Bitmap {
        val (_, _, vw, vh) = d.viewBox.trim().split(Regex("[ ,]+")).map { it.toFloat() }
        val w = widthPx.coerceIn(1, MAX_PX)
        val h = (w * vh / vw).toInt().coerceAtLeast(1)
        val bitmap = createBitmap(w, h)
        val canvas = Canvas(bitmap)
        canvas.drawColor(style.palette.getValue(if (dark) "dark" else "light").getValue("bg").toColorInt())
        val svg = parsed.getOrPut(d.id) { SVG.getFromString(d.svg) }
        // One SVG object per diagram; AndroidSVG isn't safe to render from two threads at once.
        synchronized(svg) {
            svg.renderToCanvas(canvas, RenderOptions().css(style.cssFor(d, k, dark)).viewPort(0f, 0f, w.toFloat(), h.toFloat()))
        }
        return bitmap
    }

    private companion object {
        const val MAX_PX = 2400

        @Volatile var fontInstalled = false

        fun installFont(context: Context) {
            if (fontInstalled) return
            val base = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans) ?: return
            SVG.registerExternalFileResolver(object : SVGExternalFileResolver() {
                override fun resolveFont(fontFamily: String, fontWeight: Int, fontStyle: String): Typeface? {
                    if (!fontFamily.contains("Jakarta", ignoreCase = true)) return null
                    val italic = fontStyle == "italic"
                    return if (Build.VERSION.SDK_INT >= 28) {
                        Typeface.create(base, fontWeight, italic)
                    } else {
                        Typeface.create(base, if (fontWeight >= 600) Typeface.BOLD else Typeface.NORMAL)
                    }
                }
            })
            fontInstalled = true
        }
    }
}
