package space.kscience.visionforge.gdml

import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.info
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.length
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.SolidReference
import space.kscience.visionforge.solid.layer


private class VisionCounterTree(
    val name: Name,
    val vision: Solid,
    val prototypes: HashMap<Name, VisionCounterTree>
) {

    // self count for prototypes
    var selfCount = 1

    val children: Map<NameToken, VisionCounterTree> by lazy {
        (vision as? SolidGroup)?.items?.mapValues { (key, vision) ->
            if (vision is SolidReference) {
                prototypes.getOrPut(vision.prototypeName) {
                    VisionCounterTree(vision.prototypeName, vision.prototype, prototypes)
                }.apply {
                    selfCount += 1
                }
            } else {
                VisionCounterTree(name + key, vision, prototypes)
            }
        } ?: emptyMap()
    }

    val childrenCount: Int by lazy {
        children.values.sumOf { it.childrenCount + 1 }
    }

}


private fun VisionCounterTree.topToBottom(): Sequence<VisionCounterTree> = sequence {
    yield(this@topToBottom)
    children.values.forEach {
        yieldAll(it.topToBottom())
    }
}

public fun SolidGroup.markLayers(thresholds: List<Int> = listOf(500, 1000, 20000, 50000)) {
    val logger = manager?.context?.logger ?: Global.logger
    val counterTree = VisionCounterTree(Name.EMPTY, this, hashMapOf())
    val totalCount = counterTree.childrenCount
    if (totalCount > (thresholds.firstOrNull() ?: 0)) {
        val allNodes = counterTree.topToBottom().distinct().toMutableList()
        //println("tree construction finished")
        allNodes.sortWith(
            compareBy<VisionCounterTree>(
                { it.name.length },
                { (it.children.size + 1) * it.selfCount }
            ).reversed()
        )

        //mark layers
        var remaining = totalCount

        for (node in allNodes) {
            val layerIndex = if (remaining > thresholds.last())
                thresholds.size
            else
                thresholds.indexOfLast { remaining < it }

            if (layerIndex == 0) break

            node.vision.layer = layerIndex
            remaining -= node.selfCount * (node.children.size + 1)
            logger.run {
                if (node.selfCount > 1) {
                    info { "Prototype with name ${node.name} moved to layer $layerIndex. $remaining nodes remains" }
                } else {
                    info { "Vision with name ${node.name} moved to layer $layerIndex. $remaining nodes remains" }
                }
            }
        }
    }
}