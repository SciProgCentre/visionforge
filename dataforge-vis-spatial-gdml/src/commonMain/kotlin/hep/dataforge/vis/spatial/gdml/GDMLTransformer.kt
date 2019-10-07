package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.buildMeta
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.applyStyle
import hep.dataforge.vis.spatial.Material3D.Companion.COLOR_KEY
import hep.dataforge.vis.spatial.RotationOrder
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.rotationOrder
import scientifik.gdml.*
import kotlin.collections.set
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
    private val styles = HashMap<Name, Meta>()

    var lUnit: LUnit = LUnit.MM

    var solidAction: (GDMLSolid) -> Action = { Action.CACHE }
    var volumeAction: (GDMLGroup) -> Action = { Action.CACHE }


    var solidConfiguration: VisualObject3D.(parent: GDMLVolume, solid: GDMLSolid) -> Unit = { _, _ -> }

    fun VisualObject.useStyle(name: String, builder: MetaBuilder.() -> Unit) {
        styles.getOrPut(name.toName()){
            buildMeta(builder)
        }
        applyStyle(name)
    }

    internal fun configureSolid(obj: VisualObject3D, parent: GDMLVolume, solid: GDMLSolid) {
        val material = parent.materialref.resolve(root) ?: GDMLElement(parent.materialref.ref)

        val styleName = "material[${material.name}]"

        obj.useStyle(styleName){
            COLOR_KEY to Colors.rgbToString(random.nextInt(0, Int.MAX_VALUE))
            "gdml.material" to material.name
        }

        obj.solidConfiguration(parent, solid)
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

    var optimizeSingleChild = false

    //var optimizations: List<GDMLOptimization> = emptyList()

    internal fun finalize(final: VisualGroup3D): VisualGroup3D {
//        var res = final
//        optimizations.forEach {
//            res = it(res)
//        }
        final.templates = templates
        styles.forEach {
            final.setStyle(it.key, it.value)
        }
        final.rotationOrder = RotationOrder.ZXY
        onFinish(this@GDMLTransformer)
        return final
    }

}
