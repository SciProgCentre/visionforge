package space.kscience.visionforge.jupyter

import kotlinx.browser.window
import org.w3c.dom.Document
import org.w3c.dom.Element
import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.visionforge.html.JsVisionClient
import space.kscience.visionforge.html.renderAllVisions
import space.kscience.visionforge.html.renderAllVisionsById
import space.kscience.visionforge.html.renderAllVisionsIn

@JsExport
public class VFNotebookClient : AbstractPlugin() {
    private val client by require(JsVisionClient)

    public fun renderAllVisionsIn(element: Element) {
        client.renderAllVisionsIn(element)
    }

    public fun renderAllVisionsById(document: Document, id: String) {
        client.renderAllVisionsById(document, id)
    }

    public fun renderAllVisions() {
        client.renderAllVisions()
    }


    init {
        console.info("Loading VisionForge global hooks")
        //register VisionForge in the browser window
        window.parent.asDynamic().vf = this
        window.parent.asDynamic().VisionForge = this
    }

    @Suppress("NON_EXPORTABLE_TYPE")
    override val tag: PluginTag get() = Companion.tag

    @Suppress("NON_EXPORTABLE_TYPE")
    public companion object : PluginFactory<VFNotebookClient> {
        override fun build(context: Context, meta: Meta): VFNotebookClient = VFNotebookClient()

        override val tag: PluginTag = PluginTag(name = "vision.notebook", group = PluginTag.DATAFORGE_GROUP)
    }

}