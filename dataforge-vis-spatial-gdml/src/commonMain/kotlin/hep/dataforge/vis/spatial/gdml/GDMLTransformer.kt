package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.Meta
import hep.dataforge.meta.buildMeta
import hep.dataforge.meta.builder
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.material
import scientifik.gdml.*
import kotlin.random.Random

class GDMLTransformer(val root: GDML) {
    private val materialCache = HashMap<GDMLMaterial, Meta>()
    private val random = Random(111)

    enum class Action {
        ACCEPT,
        REJECT,
        CACHE
    }

    /**
     * A special group for local templates
     */
    val templates by lazy { VisualGroup3D() }

    var lUnit: LUnit = LUnit.MM

    var solidAction: (GDMLSolid) -> Action = { Action.CACHE }
    var volumeAction: (GDMLGroup) -> Action = { Action.ACCEPT }


    var transparent: GDMLVolume.(GDMLSolid?) -> Boolean = { !physVolumes.isEmpty() }

    internal fun configureSolid(obj: VisualObject3D, parent: GDMLVolume, solid: GDMLSolid?) {
        val material = parent.materialref.resolve(root) ?: GDMLElement(parent.materialref.ref)

        val materialColor = materialCache.getOrPut(material) {
            buildMeta {
                "color" to random.nextInt(0, Int.MAX_VALUE)
            }
        }

        obj.material = if (parent.transparent(solid)) {
            materialColor.builder().apply { "opacity" to 0.5 }
        } else {
            materialColor
        }
    }

    fun printStatistics() {
        println("Solids:")
        solidCounter.entries.sortedByDescending { it.value }.forEach {
            println("\t$it")
        }
        println("Solids total: ${solidCounter.values.sum()}")
    }

    private val solidCounter = HashMap<String, Int>()

    internal fun solidAdded(solid: GDMLSolid) {
        solidCounter[solid.name] = (solidCounter[solid.name] ?: 0) + 1
    }

    var onFinish: GDMLTransformer.() -> Unit = {}

    internal fun finished(final: VisualGroup3D) {
        final.templates = templates
        onFinish(this@GDMLTransformer)
    }

}