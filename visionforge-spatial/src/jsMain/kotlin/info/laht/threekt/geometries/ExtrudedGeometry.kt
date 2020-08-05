@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION"
)
@file:JsModule("three")
@file:JsNonModule

package info.laht.threekt.geometries

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Geometry
import info.laht.threekt.extras.core.Shape
import info.laht.threekt.math.Vector2

external interface ExtrudeGeometryOptions {
    var curveSegments: Number?
        get() = definedExternally
        set(value) = definedExternally
    var steps: Number?
        get() = definedExternally
        set(value) = definedExternally
    var depth: Number?
        get() = definedExternally
        set(value) = definedExternally
    var bevelEnabled: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var bevelThickness: Number?
        get() = definedExternally
        set(value) = definedExternally
    var bevelSize: Number?
        get() = definedExternally
        set(value) = definedExternally
    var bevelOffset: Number?
        get() = definedExternally
        set(value) = definedExternally
    var bevelSegments: Number?
        get() = definedExternally
        set(value) = definedExternally
    var extrudePath: Any?
        get() = definedExternally
        set(value) = definedExternally
    var UVGenerator: UVGenerator?
        get() = definedExternally
        set(value) = definedExternally
}

external interface UVGenerator {
    fun generateTopUV(
        geometry: ExtrudeBufferGeometry,
        vertices: Array<Number>,
        indexA: Number,
        indexB: Number,
        indexC: Number
    ): Array<Vector2>

    fun generateSideWallUV(
        geometry: ExtrudeBufferGeometry,
        vertices: Array<Number>,
        indexA: Number,
        indexB: Number,
        indexC: Number,
        indexD: Number
    ): Array<Vector2>
}

external open class ExtrudeBufferGeometry : BufferGeometry {
    constructor(shapes: Shape, options: ExtrudeGeometryOptions?)
    constructor(shapes: Array<Shape>, options: ExtrudeGeometryOptions?)

    open fun addShapeList(shapes: Array<Shape>, options: Any? = definedExternally)
    open fun addShape(shape: Shape, options: Any? = definedExternally)

    companion object {
        var WorldUVGenerator: UVGenerator
    }
}

external open class ExtrudeGeometry : Geometry {
    constructor(shapes: Shape, options: ExtrudeGeometryOptions?)
    constructor(shapes: Array<Shape>, options: ExtrudeGeometryOptions?)

    open fun addShapeList(shapes: Array<Shape>, options: Any? = definedExternally)
    open fun addShape(shape: Shape, options: Any? = definedExternally)

    companion object {
        var WorldUVGenerator: UVGenerator
    }
}