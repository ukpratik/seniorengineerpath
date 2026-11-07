package io.github.ukpratik.seniorpath.content

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DiagramCssTest {
    private val light = mapOf("arrowActive" to "#4F46E5", "labelActive" to "#3730A3")

    @Test
    fun `matches the site export's frameCss exactly`() {
        // Same case and string as tests/unit/app-export.test.ts.
        assertThat(frameCss(2, 4, light)).isEqualTo(
            ".f3,.f4{display:none}\n.f2 .ln{stroke:#4F46E5;stroke-width:3.5;marker-end:url(#ahA)}\n.f2 .lb{fill:#3730A3}",
        )
    }

    @Test
    fun `the last frame hides nothing`() {
        assertThat(frameCss(3, 3, light)).isEqualTo(".f3 .ln{stroke:#4F46E5;stroke-width:3.5;marker-end:url(#ahA)}\n.f3 .lb{fill:#3730A3}")
    }
}
