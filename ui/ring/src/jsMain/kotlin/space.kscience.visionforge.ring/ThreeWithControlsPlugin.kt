package space.kscience.visionforge.ring

import kotlinx.coroutines.async
import org.w3c.dom.Element
import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.ElementVisionRenderer
import space.kscience.visionforge.Vision
import space.kscience.visionforge.react.render
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.solid.three.ThreePlugin

public class ThreeWithControlsPlugin : AbstractPlugin(), ElementVisionRenderer {
    public val three: ThreePlugin by require(ThreePlugin)

    override val tag: PluginTag get() = Companion.tag

    override fun rateVision(vision: Vision): Int =
        if (vision is Solid) ElementVisionRenderer.DEFAULT_RATING * 2 else ElementVisionRenderer.ZERO_RATING

    override fun render(element: Element, name: Name, vision: Vision, meta: Meta) {
        if(meta["controls.enabled"].boolean == false){
            three.render(element, name, vision, meta)
        } else {
            space.kscience.visionforge.react.createRoot(element).render {
                child(ThreeCanvasWithControls) {
                    attrs {
                        this.solids = three.solids
                        this.options = Canvas3DOptions.read(meta)
                        this.builderOfSolid = context.async { vision as Solid }
                    }
                }
            }
        }
    }

    override fun content(target: String): Map<Name, Any> {
        return when (target) {
            ElementVisionRenderer.TYPE -> mapOf("three.withControls".asName() to this)
            else -> super.content(target)
        }
    }

    public companion object : PluginFactory<ThreeWithControlsPlugin> {
        override val tag: PluginTag = PluginTag("vision.threejs.withControls", PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): ThreeWithControlsPlugin = ThreeWithControlsPlugin()
    }
}