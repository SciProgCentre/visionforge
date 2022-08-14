@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION",
    "NO_EXPLICIT_VISIBILITY_IN_API_MODE_WARNING"
)

@file:JsModule("three-csg-ts")
@file:JsNonModule

import three.core.BufferGeometry
import three.math.Matrix4
import three.math.Vector3
import three.objects.Mesh

public external class CSG {
    public fun clone(): CSG
    public fun toPolygons(): Array<Polygon>
    public fun toGeometry(toMatrix: Matrix4): BufferGeometry
    public fun union(csg: CSG): CSG
    public fun subtract(csg: CSG): CSG
    public fun intersect(csg: CSG): CSG
    public fun inverse(): CSG


    public companion object {
        fun fromPolygons(polygons: Array<Polygon>): CSG
        fun fromGeometry(geom: BufferGeometry, objectIndex: dynamic = definedExternally): CSG
        fun fromMesh(mesh: Mesh, objectIndex: dynamic = definedExternally): CSG
        fun toGeometry(csg: CSG, toMatrix: Matrix4): BufferGeometry
        fun toMesh(csg: CSG, toMatrix: Matrix4): Mesh
        fun iEval(tokens: Mesh, index: Number? = definedExternally)
        fun eval(tokens: Mesh, doRemove: Boolean): Mesh
        fun union(meshA: Mesh, meshB: Mesh): Mesh
        fun subtract(meshA: Mesh, meshB: Mesh): Mesh
        fun intersect(meshA: Mesh, meshB: Mesh): Mesh
    }
}

external class Vector(x: Number, y: Number, z: Number) : Vector3 {
    fun negated(): Vector
    fun plus(a: Vector): Vector
    fun minus(a: Vector): Vector
    fun times(a: Number): Vector
    fun dividedBy(a: Number): Vector
    fun lerp(a: Vector, t: Number): Any
    fun unit(): Vector
    fun cross(a: Vector): Any
}

external interface IVector {
    var x: Number
    var y: Number
    var z: Number
}

external class Vertex(pos: IVector, normal: IVector, uv: IVector? = definedExternally) {
    var pos: Vector
    var normal: Vector
    var uv: Vector
    fun clone(): Vertex
    fun flip()
    fun interpolate(other: Vertex, t: Number): Vertex
}

external class Plane(normal: Vector, w: Number) {
    var normal: Vector
    var w: Number
    fun clone(): Plane
    fun flip()
    fun splitPolygon(
        polygon: Polygon,
        coplanarFront: Array<Polygon>,
        coplanarBack: Array<Polygon>,
        front: Array<Polygon>,
        back: Array<Polygon>,
    )

    companion object {
        fun fromPoints(a: Vector, b: Vector, c: Vector): Plane
        var EPSILON: Any
    }
}

external class Polygon(vertices: Array<Vertex>, shared: Any? = definedExternally) {
    var plane: Plane
    var vertices: Array<Vertex>
    var shared: Any
    fun clone(): Polygon
    fun flip()
}