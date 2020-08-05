package hep.dataforge.vision.spatial.transform

import hep.dataforge.vision.Vision

/**
 * A root class for [Vision] tree optimization
 */
abstract class VisualTreeTransform<T : Vision> {
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

fun <T : Vision> T.transform(vararg transform: VisualTreeTransform<T>): T {
    var res = this
    transform.forEach {
        res = it(res)
    }
    return res
}

fun <T : Vision> T.transformInPlace(vararg transform: VisualTreeTransform<in T>) {
    transform.forEach { it(this) }
}