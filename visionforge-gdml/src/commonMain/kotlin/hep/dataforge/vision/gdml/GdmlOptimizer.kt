package hep.dataforge.vision.gdml

import hep.dataforge.meta.DFExperimental
import hep.dataforge.meta.sequence
import hep.dataforge.meta.set
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.*
import hep.dataforge.vision.solid.*
import hep.dataforge.vision.visitor.VisionVisitor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging

expect class Counter() {
    fun get(): Int
    fun incrementAndGet(): Int
}

private fun Point3D?.safePlus(other: Point3D?): Point3D? = if (this == null && other == null) {
    null
} else {
    (this ?: Point3D(0, 0, 0)) + (other ?: Point3D(0, 0, 0))
}

internal fun Vision.updateFrom(other: Vision): Vision {
    if (this is Solid && other is Solid) {
        position = position.safePlus(other.position)
        rotation = rotation.safePlus(other.rotation)
        if (this.scale != null || other.scale != null) {
            scaleX = scaleX.toDouble() * other.scaleX.toDouble()
            scaleY = scaleY.toDouble() * other.scaleY.toDouble()
            scaleZ = scaleZ.toDouble() * other.scaleZ.toDouble()
        }
        other.properties?.sequence()?.forEach { (name, item) ->
            if (properties?.getItem(name) == null) {
                config[name] = item
            }
        }
    }
    return this
}
//
//@DFExperimental
//private class GdmlOptimizer() : VisionVisitor {
//    val logger = KotlinLogging.logger("SingleChildReducer")
//
//    private val depthCount = HashMap<Int, Counter>()
//
//    override suspend fun visit(name: Name, vision: Vision) {
//        val depth = name.length
//        depthCount.getOrPut(depth) { Counter() }.incrementAndGet()
//    }
//
//    override fun skip(name: Name, vision: Vision): Boolean = vision is Proxy.ProxyChild
//
//    override suspend fun visitChildren(name: Name, group: VisionGroup) {
//        if (name == "volumes".toName()) return
//        if (group !is MutableVisionGroup) return
//
//        val newChildren = group.children.entries.associate { (visionToken, vision) ->
//            //Reduce single child groups
//            if (vision is VisionGroup && vision !is Proxy && vision.children.size == 1) {
//                val (token, child) = vision.children.entries.first()
//                child.parent = null
//                if (token != visionToken) {
//                    child.config["solidName"] = token.toString()
//                }
//                visionToken to child.updateFrom(vision)
//            } else {
//                visionToken to vision
//            }
//        }
//        if (newChildren != group.children) {
//            group.removeAll()
//            newChildren.forEach { (token, child) ->
//                group[token] = child
//            }
//        }
//    }
//}
//
//@DFExperimental
//suspend fun SolidGroup.optimizeGdml(): Job = coroutineScope {
//    prototypes?.let {
//        VisionVisitor.visitTree(GdmlOptimizer(), this, it)
//    } ?: CompletableDeferred(Unit)
//}