package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager

public fun TagConsumer<*>.embedVisionFragment(
    manager: VisionManager,
    embedData: Boolean = true,
    fetchData: String? = null,
    fetchUpdates: String? = null,
    idPrefix: String? = null,
    fragment: HtmlVisionFragment,
): Map<Name, Vision> {
    val visionMap = HashMap<Name, Vision>()
    val consumer = object : VisionTagConsumer<Any?>(this@embedVisionFragment, manager, idPrefix) {
        override fun DIV.renderVision(name: Name, vision: Vision, outputMeta: Meta) {
            visionMap[name] = vision
            // Toggle update mode

            fetchUpdates?.let {
                attributes[OUTPUT_CONNECT_ATTRIBUTE] = it
            }

            fetchData?.let {
                attributes[OUTPUT_FETCH_ATTRIBUTE] = it
            }

            if (embedData) {
                script {
                    type = "text/json"
                    attributes["class"] = OUTPUT_DATA_CLASS
                    unsafe {
                        +"\n${manager.encodeToString(vision)}\n"
                    }
                }
            }
        }
    }
    fragment(consumer)
    return visionMap
}

public fun FlowContent.embedVisionFragment(
    manager: VisionManager,
    embedData: Boolean = true,
    fetchDataUrl: String? = null,
    fetchUpdatesUrl: String? = null,
    idPrefix: String? = null,
    fragment: HtmlVisionFragment,
): Map<Name, Vision> = consumer.embedVisionFragment(manager, embedData, fetchDataUrl, fetchUpdatesUrl, idPrefix, fragment)


internal const val RENDER_FUNCTION_NAME = "renderAllVisionsById"

public fun TagConsumer<*>.embedAndRenderVisionFragment(
    manager: VisionManager,
    id: Any,
    embedData: Boolean = true,
    fetchData: String? = null,
    fetchUpdates: String? = null,
    idPrefix: String? = null,
    fragment: HtmlVisionFragment,
) {
    div {
        div {
            this.id = id.toString()
            embedVisionFragment(manager, embedData, fetchData, fetchUpdates, idPrefix, fragment)
        }
        script {
            type = "text/javascript"
            unsafe { +"window.${RENDER_FUNCTION_NAME}(\"$id\");" }
        }
    }
}