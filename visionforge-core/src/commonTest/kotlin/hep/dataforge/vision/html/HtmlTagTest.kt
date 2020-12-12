package hep.dataforge.vision.html

import hep.dataforge.meta.DFExperimental
import hep.dataforge.meta.configure
import hep.dataforge.meta.set
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionBase
import hep.dataforge.vision.VisionGroup
import kotlinx.html.*
import kotlin.test.Test

class HtmlTagTest {

    @OptIn(DFExperimental::class)
    fun VisionOutput.base(block: VisionBase.() -> Unit) =
        VisionBase().apply(block)

    val fragment = buildVisionFragment {
        div {
            h1 { +"Head" }
            vision("ddd") {
                meta{
                    "metaProperty" put 87
                }
                base {
                    configure {
                        set("myProp", 82)
                        set("otherProp", false)
                    }
                }
            }
        }
    }

    val simpleVisionRenderer: HtmlVisionRenderer<Vision> = { vision, _ ->
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

    val groupRenderer: HtmlVisionRenderer<VisionGroup> = { group, _ ->
        p { +"This is group" }
    }


    @Test
    fun testStringRender() {
        println(fragment.renderToString(simpleVisionRenderer))
    }
}