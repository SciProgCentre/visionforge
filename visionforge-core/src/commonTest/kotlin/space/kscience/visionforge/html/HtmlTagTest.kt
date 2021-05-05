package space.kscience.visionforge.html

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import space.kscience.dataforge.meta.set
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.VisionBase
import space.kscience.visionforge.configure
import space.kscience.visionforge.meta
import kotlin.test.Test

@DFExperimental
class HtmlTagTest {

    fun VisionOutput.base(block: VisionBase.() -> Unit) =
        VisionBase().apply(block)

     val fragment: HtmlVisionFragment = {
        div {
            h1 { +"Head" }
            vision("ddd") {
                meta {
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

    val simpleVisionRenderer: HtmlVisionRenderer = { _, vision, _ ->
        div {
            h2 { +"Properties" }
            ul {
                (vision as? VisionBase)?.meta?.items?.forEach {
                    li {
                        a { +it.key.toString() }
                        p { +it.value.toString() }
                    }
                }
            }
        }
    }

    val groupRenderer: HtmlVisionRenderer = { _, _, _ ->
        p { +"This is group" }
    }


    @Test
    fun testStringRender() {
        println(
            createHTML().div {
                renderVisionFragment(simpleVisionRenderer, fragment = fragment)
            }
        )
    }
}