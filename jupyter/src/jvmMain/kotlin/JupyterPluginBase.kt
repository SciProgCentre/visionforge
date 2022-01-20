package space.kscience.visionforge.jupyter

import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.declare
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.Vision
import space.kscience.visionforge.html.*

@DFExperimental
public abstract class JupyterPluginBase(final override val context: Context) : JupyterIntegration(), ContextAware {

    protected val handler: VisionForgeForNotebook = VisionForgeForNotebook(context)

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
            "space.kscience.visionforge.html.*"
        )

        render<HtmlFragment> { fragment ->
            handler.produceHtml(fragment = fragment)
        }

        render<HtmlVisionFragment> { fragment ->
            handler.produceHtml(fragment = fragment)
        }

        render<Vision> { vision ->
            handler.produceHtml {
                vision { vision }
            }

        }

        render<Page> { page ->
            HTML(page.render(createHTML()), true)
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
                vision { fragment.vision }
            }
        }

        afterLoaded()
    }
}