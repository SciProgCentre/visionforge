package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.context.Context
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

public typealias VisionCollector = MutableMap<Name, Pair<VisionOutput, Vision>>


/**
 * Render a fragment in the given consumer and return a map of extracted visions
 * @param context a context used to create a vision fragment
 * @param embedData embed Vision initial state in the HTML
 * @param fetchDataUrl fetch data after first render from given url
 * @param updatesUrl receive push updates from the server at given url
 * @param idPrefix a prefix to be used before vision ids
 */
public fun TagConsumer<*>.visionFragment(
    context: Context,
    embedData: Boolean = true,
    fetchDataUrl: String? = null,
    updatesUrl: String? = null,
    idPrefix: String? = null,
    collector: VisionCollector = mutableMapOf(),
    fragment: HtmlVisionFragment,
) {
    val consumer = object : VisionTagConsumer<Any?>(this@visionFragment, context, idPrefix) {

        override fun <T> TagConsumer<T>.vision(name: Name?, buildOutput: VisionOutput.() -> Vision): T {
            //Avoid re-creating cached visions
            val actualName = name ?: NameToken(
                DEFAULT_VISION_NAME,
                buildOutput.hashCode().toUInt().toString()
            ).asName()

            val (output, vision) = collector.getOrPut(actualName) {
                val output = VisionOutput(context, actualName)
                val vision = output.buildOutput()
                output to vision
            }

            return addVision(actualName, output.visionManager, vision, output.meta)
        }

        override fun DIV.renderVision(manager: VisionManager, name: Name, vision: Vision, outputMeta: Meta) {
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

    fragment(consumer)
}

public fun FlowContent.visionFragment(
    context: Context,
    embedData: Boolean = true,
    fetchDataUrl: String? = null,
    updatesUrl: String? = null,
    idPrefix: String? = null,
    visionCache: VisionCollector = mutableMapOf(),
    fragment: HtmlVisionFragment,
): Unit = consumer.visionFragment(
    context,
    embedData,
    fetchDataUrl,
    updatesUrl,
    idPrefix,
    visionCache,
    fragment = fragment
)