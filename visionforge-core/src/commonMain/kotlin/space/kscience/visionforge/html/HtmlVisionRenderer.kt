package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager
import kotlin.random.Random
import kotlin.random.nextUInt

public typealias HtmlVisionFragment = VisionTagConsumer<*>.() -> Unit

@DFExperimental
public fun HtmlVisionFragment(content: VisionTagConsumer<*>.() -> Unit): HtmlVisionFragment = content


internal const val RENDER_FUNCTION_NAME = "renderAllVisionsById"


/**
 * Render a fragment in the given consumer and return a map of extracted visions
 * @param context a context used to create a vision fragment
 * @param embedData embed Vision initial state in the HTML
 * @param fetchDataUrl fetch data after first render from given url
 * @param fetchUpdatesUrl receive push updates from the server at given url
 * @param idPrefix a prefix to be used before vision ids
 * @param renderScript if true add rendering script after the fragment
 */
public fun TagConsumer<*>.visionFragment(
    context: Context = Global,
    embedData: Boolean = true,
    fetchDataUrl: String? = null,
    fetchUpdatesUrl: String? = null,
    idPrefix: String? = null,
    renderScript: Boolean = true,
    fragment: HtmlVisionFragment,
): Map<Name, Vision> {
    val visionMap = HashMap<Name, Vision>()
    val consumer = object : VisionTagConsumer<Any?>(this@visionFragment, context, idPrefix) {
        override fun DIV.renderVision(manager: VisionManager, name: Name, vision: Vision, outputMeta: Meta) {
            visionMap[name] = vision
            // Toggle update mode

            fetchUpdatesUrl?.let {
                attributes[OUTPUT_CONNECT_ATTRIBUTE] = it
            }

            fetchDataUrl?.let {
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
    if (renderScript) {
        val id = "fragment[${fragment.hashCode()}/${Random.nextUInt()}]"
        div {
            this.id = id
            fragment(consumer)
        }
        script {
            type = "text/javascript"
            unsafe { +"window.${RENDER_FUNCTION_NAME}(\"$id\");" }
        }
    } else {
        fragment(consumer)
    }
    return visionMap
}

public fun FlowContent.visionFragment(
    context: Context = Global,
    embedData: Boolean = true,
    fetchDataUrl: String? = null,
    fetchUpdatesUrl: String? = null,
    idPrefix: String? = null,
    renderScript: Boolean = true,
    fragment: HtmlVisionFragment,
): Map<Name, Vision> = consumer.visionFragment(
    context,
    embedData,
    fetchDataUrl,
    fetchUpdatesUrl,
    idPrefix,
    renderScript,
    fragment
)