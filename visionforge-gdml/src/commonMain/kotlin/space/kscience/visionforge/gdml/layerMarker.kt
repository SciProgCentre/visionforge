package space.kscience.visionforge.gdml

import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.VisionGroup

private fun VisionGroup.countChildren(namePrefix: Name, cache: MutableMap<Name, Int> = hashMapOf()): Int {
    var counter = 0
    children.forEach { (token, child) ->
        if (child is VisionGroup) {
            counter += child.countChildren(namePrefix + token, cache)
        } else {
            counter++
        }
    }
    cache[namePrefix] = counter
    return counter
}


public fun VisionGroup.processLayers() {

}