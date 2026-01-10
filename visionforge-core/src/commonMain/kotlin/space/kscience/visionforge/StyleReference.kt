package space.kscience.visionforge

import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import kotlin.properties.ReadOnlyProperty

/**
 * A reference to a style defined in a specific container
 */
public class StyleReference(public val owner: Vision, public val name: String)

private tailrec fun styleIsDefined(vision: Vision, reference: StyleReference): Boolean = when {
    reference.owner === vision -> true
    vision.parent == null -> false
    else -> styleIsDefined(vision.parent!!, reference)
}

public fun MutableVision.useStyle(reference: StyleReference) {
    //check that style is defined in a parent
    //check(styleIsDefined(this, reference)) { "Style reference does not belong to a Vision parent" }
    useStyle(reference.name)
}

public fun MutableVision.style(
    styleKey: String? = null,
    builder: MutableMeta.() -> Unit,
): ReadOnlyProperty<Any?, StyleReference> = ReadOnlyProperty { _, property ->
    val styleName = styleKey ?: property.name
    updateStyle(styleName, builder)
    StyleReference(this, styleName)
}

public fun <T : Scheme> MutableVision.style(
    spec: SchemeSpec<T>,
    styleKey: String? = null,
    builder: T.() -> Unit,
): ReadOnlyProperty<Any?, StyleReference> = ReadOnlyProperty { _, property ->
    val styleName = styleKey ?: property.name
    setStyle(styleName,  spec.invoke(builder).toMeta())
    StyleReference(this, styleName)
}