package space.kscience.visionforge.jupyter

import kotlinx.html.*
import org.jetbrains.kotlinx.jupyter.api.KotlinKernelHost
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import org.jetbrains.kotlinx.jupyter.api.declare
import org.jetbrains.kotlinx.jupyter.api.libraries.JupyterIntegration
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.html.*
import kotlin.random.Random
import kotlin.random.nextUInt

/**
 * A base class for different Jupyter VF integrations
 */
public abstract class VisionForgeIntegration(
    public val visionManager: VisionManager,
) : JupyterIntegration(), ContextAware {

    override val context: Context get() = visionManager.context

    protected abstract fun Builder.afterLoaded(vf: VisionForge)

    final override fun Builder.onLoaded() {

        val vf: VisionForge = VisionForge(visionManager, notebook)

        onLoaded {
            val kernel: KotlinKernelHost = this
            declare("VisionForge" to vf, "vf" to vf)
            vf.startServer(kernel)
            vf.configuration.onChange(this) { name ->
                if (name.toString() == "visionforge.port") {
                    kernel.displayHtml {
                        p { +"Property 'visionforge.port' changed. Restarting server" }
                    }
                    vf.startServer(kernel)
                }
            }
        }


        onShutdown {
            vf.stopServer(this)
        }

        import(
            "kotlinx.html.*",
            "space.kscience.visionforge.html.*",
            "space.kscience.visionforge.jupyter.*"
        )
//
//        render<HtmlFragment> { fragment ->
//            HTML(fragment.renderToString())
//        }
//
//        render<HtmlVisionFragment> { fragment ->
//            handler.produceHtml(fragment = fragment)
//        }

        render<Vision> { vision ->
            vf.produceHtml {
                vision(vision)
            }
        }

        render<VisionPage> { page ->
            HTML(true) {
                head {
                    meta {
                        charset = "utf-8"
                    }
                    page.pageHeaders.values.forEach {
                        appendFragment(it)
                    }
                }
                body {
                    val id = "fragment[${page.hashCode()}/${Random.nextUInt()}]"
                    div {
                        this.id = id
                        visionFragment(visionManager, fragment = page.content)
                    }
                    with(vf) {
                        renderScriptForId(id)
                    }
                }
            }
        }

        render<HtmlFormFragment> { fragment ->
            vf.produceHtml {
                if (!vf.isServerRunning()) {
                    p {
                        style = "color: red;"
                        +"The server is not running. Forms are not interactive. Start server with `VisionForge.startServer()."
                    }
                }
                appendFragment(fragment.formBody)
                vision(fragment.vision)
            }
        }

        afterLoaded(vf)
    }
}


/**
 * Create a fragment without a head to be embedded in the page
 */
@Suppress("UnusedReceiverParameter")
public fun VisionForge.html(body: TagConsumer<*>.() -> Unit): MimeTypedResult = HTML(false, body)


/**
 * Create a fragment without a head to be embedded in the page
 */
public fun VisionForge.fragment(body: VisionTagConsumer<*>.() -> Unit): MimeTypedResult = produceHtml(false, body)


/**
 * Create a standalone page in the notebook
 */
public fun VisionForge.page(
    pageHeaders: Map<String, HtmlFragment> = emptyMap(),
    body: VisionTagConsumer<*>.() -> Unit,
): VisionPage = VisionPage(visionManager, pageHeaders, body)

