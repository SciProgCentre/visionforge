package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaSerializer
import space.kscience.dataforge.meta.isEmpty
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.parseAsName
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.html.VisionTagConsumer.Companion.DEFAULT_VISION_NAME
import space.kscience.visionforge.setAsRoot
import space.kscience.visionforge.visionManager

/**
 * Rendering context for visions in HTML
 */
public interface HtmlVisionContext : ContextAware {

    /**
     * Generate div id for vision div tag
     */
    public fun generateId(name: Name): String = "vision[$name]"

    /**
     * Render vision at given [DIV]
     */
    public fun DIV.renderVision(name: Name, vision: Vision, outputMeta: Meta)
}


public typealias HtmlVisionContextFragment = context(HtmlVisionContext) TagConsumer<*>.() -> Unit

//context(HtmlVisionContext)
//public fun HtmlVisionFragment(
//    content: TagConsumer<*>.() -> Unit,
//): HtmlVisionFragment = HtmlVisionFragment {  }

context(HtmlVisionContext)
private fun <T> TagConsumer<T>.vision(
    visionManager: VisionManager,
    name: Name,
    vision: Vision,
    outputMeta: Meta = Meta.EMPTY,
): T = div {
    id = generateId(name)
    classes = setOf(VisionTagConsumer.OUTPUT_CLASS)
    vision.setAsRoot(visionManager)
    attributes[VisionTagConsumer.OUTPUT_NAME_ATTRIBUTE] = name.toString()
    if (!outputMeta.isEmpty()) {
        //Hard-code output configuration
        script {
            attributes["class"] = VisionTagConsumer.OUTPUT_META_CLASS
            unsafe {
                +visionManager.jsonFormat.encodeToString(MetaSerializer, outputMeta)
            }
        }
    }
    renderVision(name, vision, outputMeta)
}

context(HtmlVisionContext)
private fun <T> TagConsumer<T>.vision(
    name: Name,
    vision: Vision,
    outputMeta: Meta = Meta.EMPTY,
): T = vision(context.visionManager, name, vision, outputMeta)

/**
 * Insert a vision in this HTML.
 */
context(HtmlVisionContext)
@VisionDSL
public fun <T> TagConsumer<T>.vision(
    name: Name? = null,
    visionProvider: VisionOutput.() -> Vision,
): T {
    val actualName = name ?: NameToken(DEFAULT_VISION_NAME, visionProvider.hashCode().toUInt().toString()).asName()
    val output = VisionOutput(context, actualName)
    val vision = output.visionProvider()
    return vision(output.visionManager, actualName, vision, output.meta)
}

/**
 * Insert a vision in this HTML.
 */
context(HtmlVisionContext)
@VisionDSL
public fun <T> TagConsumer<T>.vision(
    name: String?,
    visionProvider: VisionOutput.() -> Vision,
): T = vision(name?.parseAsName(), visionProvider)