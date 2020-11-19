package hep.dataforge.vision.html

import hep.dataforge.meta.configure
import hep.dataforge.meta.set
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionBase
import hep.dataforge.vision.VisionGroup
import kotlinx.html.*
import kotlin.test.Test

class HtmlTagTest {

    fun HtmlOutput<Vision>.vision(block: Vision.() -> Unit) =
        outputScope.renderVision(this, VisionBase().apply(block))

    val fragment = buildVisionFragment {
        div {
            h1 { +"Head" }
            visionOutput("ddd") {
                vision {
                    configure {
                        set("myProp", 82)
                    }
                }
            }
        }
    }

    val simpleVisionRenderer: HtmlVisionRenderer<Vision> = { vision ->
        div {
            h2 { +"Properties" }
            ul {
                vision.properties?.items?.forEach {
                    li {
                        a { +it.key.toString() }
                        p { +it.value.toString() }
                    }
                }
            }
        }
    }

    val groupRenderer: HtmlVisionRenderer<VisionGroup> = { group ->
        p { +"This is group" }
    }


    @Test
    fun testStringRender() {
        println(fragment.renderToString(simpleVisionRenderer))
    }
}