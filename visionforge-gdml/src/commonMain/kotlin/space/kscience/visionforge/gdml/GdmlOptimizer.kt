package space.kscience.visionforge.gdml

import space.kscience.dataforge.meta.itemSequence
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.Vision
import space.kscience.visionforge.meta
import space.kscience.visionforge.solid.*

public expect class Counter() {
    public fun get(): Int
    public fun incrementAndGet(): Int
}

@DFExperimental
internal fun Vision.updateFrom(other: Vision): Vision {
    if (this is Solid && other is Solid) {
        position += other.position
        rotation += other.rotation
        scaleX = scaleX.toDouble() * other.scaleX.toDouble()
        scaleY = scaleY.toDouble() * other.scaleY.toDouble()
        scaleZ = scaleZ.toDouble() * other.scaleZ.toDouble()
        other.meta.itemSequence().forEach { (name, item) ->
            if (getProperty(name) == null) {
                setProperty(name, item)
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