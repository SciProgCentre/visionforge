@file:JsModule("three-csg-ts")
@file:JsNonModule
@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION",
    "NESTED_CLASS_IN_EXTERNAL_INTERFACE"
)

package hep.dataforge.vision.solid.three


import info.laht.threekt.math.Matrix4
import info.laht.threekt.math.Vector3
import info.laht.threekt.objects.Mesh

/**
 * Constructive Solid Geometry
 */
open external class CSG {
    open var polygons: Array<Polygon>
    open fun clone(): CSG
    open fun toPolygons(): Array<Polygon>
    open fun union(csg: CSG): CSG
    open fun subtract(csg: CSG): CSG
    open fun intersect(csg: CSG): CSG
    open fun inverse(): CSG

    companion object {
        fun fromPolygons(polygons: Array<Polygon>): CSG
        fun fromGeometry(geom: Any): CSG
        fun fromMesh(mesh: Mesh): CSG
        fun toMesh(csg: CSG, toMatrix: Matrix4): Mesh
        fun iEval(tokens: Mesh, index: Number? = definedExternally /* null */): Unit
        fun eval(tokens: Mesh, doRemove: Boolean): Mesh
        var _tmpm3: Any
        var doRemove: Any
        var currentOp: Any
        var currentPrim: Any
        var nextPrim: Any
        var sourceMesh: Any
    }
}

open external class Vector(x: Number, y: Number, z: Number): Vector3 {
    open fun negated(): Vector
    open fun plus(a: Vector): Vector
    open fun minus(a: Vector): Vector
    open fun times(a: Number): Vector
    open fun dividedBy(a: Number): Vector
    open fun lerp(a: Vector, t: Number): Any
    open fun unit(): Vector
    open fun cross(a: Vector): Any
}

external interface IVector {
    var x: Number
    var y: Number
    var z: Number
}

open external class Vertex(pos: IVector, normal: IVector, uv: IVector) {
    open var pos: Vector
    open var normal: Vector
    open var uv: Vector
    open fun clone(): Vertex
    open fun flip(): Unit
    open fun interpolate(other: Vertex, t: Number): Vertex
}

open external class Plane(normal: Vector, w: Number) {
    open var normal: Vector
    open var w: Number
    open fun clone(): Plane
    open fun flip(): Unit
    open fun splitPolygon(
        polygon: Polygon,
        coplanarFront: Array<Polygon>,
        coplanarBack: Array<Polygon>,
        front: Array<Polygon>,
        back: Array<Polygon>
    ): Unit

    companion object {
        fun fromPoints(a: Vector, b: Vector, c: Vector): Plane
        var EPSILON: Any
    }
}

open external class Polygon(vertices: Array<Vertex>, shared: Any? = definedExternally /* null */) {
    open var plane: Plane
    open var vertices: Array<Vertex>
    open var shared: Any
    open fun clone(): Polygon
    open fun flip(): Unit
}