package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.Meta
import hep.dataforge.meta.buildMeta
import hep.dataforge.meta.builder
import hep.dataforge.vis.spatial.VisualGroup3D
import scientifik.gdml.GDML
import scientifik.gdml.GDMLGroup
import scientifik.gdml.GDMLMaterial
import scientifik.gdml.GDMLSolid
import kotlin.random.Random

class GDMLTransformer(val root: GDML) {
    private val materialCache = HashMap<GDMLMaterial, Meta>()
    private val random = Random(111)

    enum class Action{
        ACCEPT,
        REJECT,
        CACHE
    }

    /**
     * A special group for local templates
     */
    val templates by lazy { VisualGroup3D() }

    var lUnit: LUnit = LUnit.MM
    var resolveColor: ColorResolver = { material, _ ->
        val materialColor = materialCache.getOrPut(material) {
            buildMeta {
                "color" to random.nextInt(0, Int.MAX_VALUE)
            }
        }

        if (this?.physVolumes?.isEmpty() != false) {
            materialColor
        } else {
            materialColor.builder().apply { "opacity" to 0.5 }
        }
    }

    var solidAction: (GDMLSolid) -> Action = { Action.CACHE }
    var volumeAction: (GDMLGroup) -> Action = { Action.ACCEPT }

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