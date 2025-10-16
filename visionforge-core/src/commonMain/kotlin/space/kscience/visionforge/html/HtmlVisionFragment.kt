package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager

public fun interface HtmlVisionFragment {
    context(htmlContext: HtmlVisionContext) public fun TagConsumer<*>.append()
}

context(scope: HtmlVisionContext)
public fun HtmlVisionFragment.appendTo(consumer: TagConsumer<*>): Unit = consumer.append()

public data class VisionDisplay(val visionManager: VisionManager, val vision: Vision, val meta: Meta)

/**
 * Render a fragment in the given consumer and return a map of extracted visions
 * @param visionManager a context plugin used to create a vision fragment
 * @param embedData embed Vision initial state in the HTML
 * @param fetchDataUrl fetch data after first render from given url
 * @param updatesUrl receive push updates from the server at given url
 * @param idPrefix a prefix to be used before vision ids
 * @param displayCache external cache for Vision displays. It is required to avoid re-creating visions on page update
 * @param fragment the fragment to render
 */
public fun TagConsumer<*>.visionFragment(
    visionManager: VisionManager,
    embedData: Boolean = true,
    fetchDataUrl: String? = null,
    updatesUrl: String? = null,
    idPrefix: String? = null,
    displayCache: MutableMap<Name, VisionDisplay> = mutableMapOf(),
    fragment: HtmlVisionFragment,
) {

    val consumer = object : HtmlVisionContext(visionManager.context, idPrefix) {

        override fun DIV.renderVision(manager: VisionManager, name: Name, vision: Vision, outputMeta: Meta) {

            displayCache[name] = VisionDisplay(manager, vision, outputMeta)

            // Toggle update mode
            updatesUrl?.let {
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

    with(consumer) {
        fragment.appendTo(this@visionFragment)
    }
}

public fun FlowContent.visionFragment(
    visionManager: VisionManager,
    embedData: Boolean = true,
    fetchDataUrl: String? = null,
    updatesUrl: String? = null,
    idPrefix: String? = null,
    displayCache: MutableMap<Name, VisionDisplay> = mutableMapOf(),
    fragment: HtmlVisionFragment,
): Unit = consumer.visionFragment(
    visionManager = visionManager,
    embedData = embedData,
    fetchDataUrl = fetchDataUrl,
    updatesUrl = updatesUrl,
    idPrefix = idPrefix,
    displayCache = displayCache,
    fragment = fragment
)