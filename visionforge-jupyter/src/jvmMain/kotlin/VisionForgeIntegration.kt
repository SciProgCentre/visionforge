package space.kscience.visionforge.jupyter

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.declare
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.html.*
import kotlin.random.Random
import kotlin.random.nextUInt

/**
 * A base class for different Jupyter VF integrations
 */
@DFExperimental
public abstract class VisionForgeIntegration(
    public val visionManager: VisionManager,
) : JupyterIntegration(), ContextAware {

    override val context: Context get() = visionManager.context
    protected val handler: VisionForge = VisionForge(visionManager)

    protected abstract fun Builder.afterLoaded()

    final override fun Builder.onLoaded() {

        onLoaded {
            declare("VisionForge" to handler, "vf" to handler)
        }


        onShutdown {
            handler.stopServer()
        }

        import(
            "kotlinx.html.*",
            "space.kscience.visionforge.html.*",
            "space.kscience.visionforge.jupyter.*"
        )

        render<HtmlFragment> { fragment ->
            handler.produceHtml(fragment = fragment)
        }

        render<HtmlVisionFragment> { fragment ->
            handler.produceHtml(fragment = fragment)
        }

        render<Vision> { vision ->
            handler.produceHtml {
                vision(vision)
            }
        }

        render<VisionPage> { page ->
            HTML(createHTML().apply {
                head {
                    meta {
                        charset = "utf-8"
                    }
                    page.pageHeaders.values.forEach {
                        fragment(it)
                    }
                }
                body {
                    val id = "fragment[${page.hashCode()}/${Random.nextUInt()}]"
                    div {
                        this.id = id
                        visionFragment(visionManager, fragment = page.content)
                    }
                    renderScriptForId(id)
                }
            }.finalize(), true)
        }

        render<HtmlFormFragment> { fragment ->
            handler.produceHtml {
                if (!handler.isServerRunning()) {
                    p {
                        style = "color: red;"
                        +"The server is not running. Forms are not interactive. Start server with `VisionForge.startServer()."
                    }
                }
                fragment(fragment.formBody)
                vision(fragment.vision)
            }
        }

        afterLoaded()
    }
}

/**
 * Create a standalone page in the notebook
 */
public fun VisionForge.page(
    pageHeaders: Map<String, HtmlFragment> = emptyMap(),
    content: HtmlVisionFragment
): VisionPage = VisionPage(visionManager, pageHeaders, content)