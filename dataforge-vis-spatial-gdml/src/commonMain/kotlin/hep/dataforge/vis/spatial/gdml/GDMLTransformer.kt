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
    val proto by lazy { VisualGroup3D() }
    private val styleCache = HashMap<Name, Meta>()

    var lUnit: LUnit = LUnit.MM

    var solidAction: (GDMLSolid) -> Action = { Action.CACHE }
    var volumeAction: (GDMLGroup) -> Action = { Action.CACHE }


    var solidConfiguration: VisualObject3D.(parent: GDMLVolume, solid: GDMLSolid) -> Unit = { _, _ -> }

    fun VisualObject.useStyle(name: String, builder: MetaBuilder.() -> Unit) {
        styleCache.getOrPut(name.toName()){
            buildMeta(builder)
        }
        applyStyle(name)
    }

    internal fun configureSolid(obj: VisualObject3D, parent: GDMLVolume, solid: GDMLSolid) {
        val material = parent.materialref.resolve(root) ?: GDMLElement(parent.materialref.ref)

        val styleName = "material[${material.name}]"

        obj.useStyle(styleName){
            COLOR_KEY to Colors.rgbToString(random.nextInt(0, Int.MAX_VALUE))
            "gdml.material" put material.name
        }

        obj.solidConfiguration(parent, solid)
    }
//
//    internal fun solidAdded(solid: GDMLSolid) {
//        solidCounter[solid.name] = (solidCounter[solid.name] ?: 0) + 1
//    }

    var onFinish: GDMLTransformer.() -> Unit = {}

    internal fun finalize(final: VisualGroup3D): VisualGroup3D {
        final.prototypes = proto
        styleCache.forEach {
            final.addStyle(it.key, it.value, false)
        }
        final.rotationOrder = RotationOrder.ZXY
        onFinish(this@GDMLTransformer)
        return final
    }

}
