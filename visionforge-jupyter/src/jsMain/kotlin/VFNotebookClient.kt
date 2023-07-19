package space.kscience.visionforge.jupyter

import kotlinx.browser.window
import org.w3c.dom.Element
import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.visionforge.VisionClient
import space.kscience.visionforge.renderAllVisions
import space.kscience.visionforge.renderAllVisionsById
import space.kscience.visionforge.renderAllVisionsIn

@JsExport
public class VFNotebookClient : AbstractPlugin() {
    private val client by require(VisionClient)

    public fun renderAllVisionsIn(element: Element) {
        client.renderAllVisionsIn(element)
    }

    public fun renderAllVisionsById(id: String) {
        client.renderAllVisionsById(id)
    }

    public fun renderAllVisions() {
        client.renderAllVisions()
    }


    init {
        //register VisionForge in the browser window
        window.asDynamic().vf = this
        window.asDynamic().VisionForge = this
    }

    @Suppress("NON_EXPORTABLE_TYPE")
    override val tag: PluginTag get() = Companion.tag

    @Suppress("NON_EXPORTABLE_TYPE")
    public companion object : PluginFactory<VFNotebookClient> {
        override fun build(context: Context, meta: Meta): VFNotebookClient = VFNotebookClient()

        override val tag: PluginTag = PluginTag(name = "vision.notebook", group = PluginTag.DATAFORGE_GROUP)
    }

}