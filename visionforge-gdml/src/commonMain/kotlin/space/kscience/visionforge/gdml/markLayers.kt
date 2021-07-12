package space.kscience.visionforge.gdml

import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.length
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.VisionGroup
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.layer

private class VisionCounterTree(
    val name: Name,
    val vision: Solid,
) {
    val children: Map<NameToken, VisionCounterTree> =
        (vision as? VisionGroup)?.children?.mapValues {
            VisionCounterTree(name + it.key, it.value as Solid)
        } ?: emptyMap()

//
//    val directChildrenCount: Int by lazy {
//        children.size
//    }

    val childrenCount: Int =
        children.values.sumOf { it.childrenCount + 1 }

}


private fun VisionCounterTree.topToBottom(): Sequence<VisionCounterTree> = sequence {
    yield(this@topToBottom)
    children.values.forEach {
        yieldAll(it.topToBottom())
    }
}

public fun SolidGroup.markLayers(thresholds: List<Int> = listOf(1000, 20000, 100000)) {
    val counterTree = VisionCounterTree(Name.EMPTY, this)
    val totalCount = counterTree.childrenCount
    if (totalCount > thresholds.firstOrNull() ?: 0) {
        val allNodes = counterTree.topToBottom().toMutableList()
        //println("tree construction finished")
        allNodes.sortWith(compareBy<VisionCounterTree>({ it.name.length }, { it.childrenCount }).reversed())

        //mark layers
        var removed = 0
        var thresholdIndex = thresholds.indexOfLast { it < totalCount }

        for (node in allNodes) {
            node.vision.layer = thresholdIndex + 1
            removed++
            if (totalCount - removed < thresholds[thresholdIndex]) {
                thresholdIndex--
            }
            if (thresholdIndex < 0) break
        }


    }
}