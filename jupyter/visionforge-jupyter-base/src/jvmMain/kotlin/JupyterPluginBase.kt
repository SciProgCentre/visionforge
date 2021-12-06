package space.kscience.visionforge.jupyter

import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import kotlinx.html.stream.createHTML
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import org.jetbrains.kotlinx.jupyter.api.libraries.resources
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.Vision
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.Page
import space.kscience.visionforge.html.embedAndRenderVisionFragment
import space.kscience.visionforge.three.server.VisionServer
import space.kscience.visionforge.three.server.visionServer
import space.kscience.visionforge.visionManager

private const val DEFAULT_VISIONFORGE_PORT = 88898

@DFExperimental
public abstract class JupyterPluginBase(
    override val context: Context,
) : JupyterIntegration(), ContextAware {

    private var counter = 0

    private fun produceHtmlVisionString(fragment: HtmlVisionFragment) = createHTML().apply {
        embedAndRenderVisionFragment(context.visionManager, counter++, fragment = fragment)
    }.finalize()

    private var engine: ApplicationEngine? = null
    private var server: VisionServer? = null

    override fun Builder.onLoaded() {

        onLoaded {
            val host = context.properties["visionforge.host"].string ?: "localhost"
            val port = context.properties["visionforge.port"].int ?: DEFAULT_VISIONFORGE_PORT
            engine = context.embeddedServer(CIO, port, host) {
                server = visionServer(context)
            }.start()
        }

        onShutdown {
            engine?.stop(1000, 1000)
            engine = null
            server = null
        }

        resources {
            js("three") {
                classPath("js/gdml-jupyter.js")
            }
        }

        import(
            "kotlinx.html.*",
            "space.kscience.visionforge.html.Page",
            "space.kscience.visionforge.html.page",
        )

        render<Vision> { vision ->
            val server = this@JupyterPluginBase.server
            if (server == null) {
                HTML(produceHtmlVisionString { vision(vision) })
            } else {
                val route = "route.${counter++}"
                HTML(server.createHtmlAndServe(route,route, emptyList()){
                    vision(vision)
                })
            }
        }

        render<Page> { page ->
            //HTML(page.render(createHTML()), true)
        }
    }
}
