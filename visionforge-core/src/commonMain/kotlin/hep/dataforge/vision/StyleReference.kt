package hep.dataforge.vision

import hep.dataforge.meta.DFExperimental
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import kotlin.properties.ReadOnlyProperty

/**
 * A reference to a style defined in a specific container
 */
public class StyleReference(public val owner: VisionGroup, public val name: String)

private tailrec fun styleIsDefined(vision: Vision, reference: StyleReference): Boolean = when {
    reference.owner === vision -> true
    vision.parent == null -> false
    else -> styleIsDefined(vision.parent!!, reference)
}

@VisionBuilder
public fun Vision.useStyle(reference: StyleReference) {
    //check that style is defined in a parent
    //check(styleIsDefined(this, reference)) { "Style reference does not belong to a Vision parent" }
    useStyle(reference.name)
}

@DFExperimental
@VisionBuilder
public fun VisionGroup.style(builder: MetaBuilder.() -> Unit): ReadOnlyProperty<Any?, StyleReference> =
    ReadOnlyProperty { _, property ->
        val styleName = property.name
        styleSheet.define(styleName, Meta(builder))
        StyleReference(this, styleName)
    }