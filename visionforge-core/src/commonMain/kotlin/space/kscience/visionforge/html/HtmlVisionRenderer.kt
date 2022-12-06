package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager

public typealias HtmlVisionFragment = VisionTagConsumer<*>.() -> Unit

@DFExperimental
public fun HtmlVisionFragment(content: VisionTagConsumer<*>.() -> Unit): HtmlVisionFragment = content


/**
 * Render a fragment in the given consumer and return a map of extracted visions
 * @param context a context used to create a vision fragment
 * @param embedData embed Vision initial state in the HTML
 * @param fetchDataUrl fetch data after first render from given url
 * @param updatesUrl receive push updates from the server at given url
 * @param idPrefix a prefix to be used before vision ids
 */
public fun TagConsumer<*>.visionFragment(
    visionManager: VisionManager,
    embedData: Boolean = true,
    fetchDataUrl: String? = null,
    updatesUrl: String? = null,
    idPrefix: String? = null,
    onVisionRendered: (Name, Vision) -> Unit = { _, _ -> },
    fragment: HtmlVisionFragment,
) {

    val collector: MutableMap<Name, Pair<VisionOutput, Vision>> = mutableMapOf()

    val consumer = object : VisionTagConsumer<Any?>(this@visionFragment, visionManager, idPrefix) {

        override fun <T> TagConsumer<T>.vision(name: Name?, buildOutput: VisionOutput.() -> Vision): T {
            //Avoid re-creating cached visions
            val actualName = name ?: NameToken(
                DEFAULT_VISION_NAME,
                buildOutput.hashCode().toUInt().toString()
            ).asName()

            val (output, vision) = collector.getOrPut(actualName) {
                val output = VisionOutput(context, actualName)
                val vision = output.buildOutput()
                onVisionRendered(actualName, vision)
                output to vision
            }

            return addVision(actualName, output.visionManager, vision, output.meta)
        }

        override fun DIV.renderVision(manager: VisionManager, name: Name, vision: Vision, outputMeta: Meta) {

            val (_, actualVision) = collector.getOrPut(name) {
                val output = VisionOutput(context, name)
                onVisionRendered(name, vision)
                output to vision
            }


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
                        +"\n${manager.encodeToString(actualVision)}\n"
                    }
                }
            }
        }
    }

    fragment(consumer)
}

public fun FlowContent.visionFragment(
    visionManager: VisionManager,
    embedData: Boolean = true,
    fetchDataUrl: String? = null,
    updatesUrl: String? = null,
    onVisionRendered: (Name, Vision) -> Unit = { _, _ -> },
    idPrefix: String? = null,

    fragment: HtmlVisionFragment,
): Unit = consumer.visionFragment(
    visionManager = visionManager,
    embedData = embedData,
    fetchDataUrl = fetchDataUrl,
    updatesUrl = updatesUrl,
    idPrefix = idPrefix,
    onVisionRendered = onVisionRendered,
    fragment = fragment
)