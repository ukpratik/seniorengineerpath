package io.github.ukpratik.seniorpath.content

/**
 * Frame [k] of [n] (1-based) as CSS: later frames hidden, frame k's arrows and labels highlighted.
 * Port of `frameCss` in src/content-tools/app-export.ts; both tests pin the same output, so keep them identical.
 * [palette] is one theme's entry of [DiagramStyle.palette].
 */
fun frameCss(k: Int, n: Int, palette: Map<String, String>): String {
    require(k in 1..n) { "frame $k of $n" }
    val active = palette.getValue("arrowActive")
    val label = palette.getValue("labelActive")
    val hide = (k + 1..n).joinToString(",") { ".f$it" }
    return listOfNotNull(
        if (hide.isNotEmpty()) "$hide{display:none}" else null,
        ".f$k .ln{stroke:$active;stroke-width:3.5;marker-end:url(#ahA)}",
        ".f$k .lb{fill:$label}",
    ).joinToString("\n")
}

/** The full stylesheet for drawing frame [k] of [diagram] in the light or dark theme. */
fun DiagramStyle.cssFor(diagram: Diagram, k: Int, dark: Boolean): String {
    val theme = if (dark) "dark" else "light"
    return (if (dark) this.dark else light) + "\n" + frameCss(k, diagram.frames, palette.getValue(theme))
}
