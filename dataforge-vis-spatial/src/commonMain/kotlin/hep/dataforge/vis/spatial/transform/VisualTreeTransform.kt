package hep.dataforge.vis.spatial.transform

import hep.dataforge.vis.common.VisualObject

/**
 * A root class for [VisualObject] tree optimization
 */
abstract class VisualTreeTransform<T : VisualObject> {
    protected abstract fun T.transformInPlace()
    protected abstract fun T.clone(): T

    operator fun invoke(source: T, inPlace: Boolean = true): T {
        val newSource = if (inPlace) {
            source
        } else {
            source.clone()
        }
        newSource.transformInPlace()

        return newSource
    }
}

fun <T : VisualObject> T.transform(vararg transform: VisualTreeTransform<T>): T {
    var res = this
    transform.forEach {
        res = it(res)
    }
    return res
}

fun <T : VisualObject> T.transformInPlace(vararg transform: VisualTreeTransform<in T>) {
    transform.forEach { it(this) }
}