package space.kscience.visionforge

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaBuilder
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.Specification
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

@VisionBuilder
public fun VisionGroup.style(
    styleKey: String? = null,
    builder: MetaBuilder.() -> Unit,
): ReadOnlyProperty<Any?, StyleReference> = ReadOnlyProperty { _, property ->
    val styleName = styleKey ?: property.name
    styleSheet.define(styleName, Meta(builder))
    StyleReference(this, styleName)
}

@VisionBuilder
public fun <T : Scheme> VisionGroup.style(
    spec: Specification<T>,
    styleKey: String? = null,
    builder: T.() -> Unit,
): ReadOnlyProperty<Any?, StyleReference> = ReadOnlyProperty { _, property ->
    val styleName = styleKey ?: property.name
    styleSheet.define(styleName, spec(builder).toMeta())
    StyleReference(this, styleName)
}