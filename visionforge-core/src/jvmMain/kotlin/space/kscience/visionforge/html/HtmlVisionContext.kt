package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaSerializer
import space.kscience.dataforge.meta.isEmpty
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.parseAsName
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager
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

context(HtmlVisionContext)
        public fun HtmlVisionContextFragment(content: TagConsumer<*>.() -> Unit): HtmlVisionFragment = content

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
        @DFExperimental
        @VisionDSL
        public fun <T> TagConsumer<T>.vision(
    name: Name? = null,
    visionProvider: VisionOutput.() -> Vision,
): T {
    val output = VisionOutput(context, name)
    val vision = output.visionProvider()
    val actualName =
        name ?: NameToken(VisionTagConsumer.DEFAULT_VISION_NAME, vision.hashCode().toUInt().toString()).asName()
    return vision(output.buildVisionManager(), actualName, vision, output.meta)
}

/**
 * Insert a vision in this HTML.
 */
context(HtmlVisionContext)
        @DFExperimental
        @VisionDSL
        public fun <T> TagConsumer<T>.vision(
    name: String?,
    visionProvider: VisionOutput.() -> Vision,
): T = vision(name?.parseAsName(), visionProvider)