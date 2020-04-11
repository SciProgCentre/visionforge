package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.toName
import hep.dataforge.vis.spatial.*
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_COLOR_KEY
import hep.dataforge.vis.useStyle
import scientifik.gdml.*
import kotlin.random.Random

class GDMLTransformer(val root: GDML) {
    //private val materialCache = HashMap<GDMLMaterial, Meta>()
    private val random = Random(222)

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


    var solidConfiguration: VisualObject3D.(parent: GDMLVolume, solid: GDMLSolid) -> Unit = { parent, _ ->
        lUnit = LUnit.CM
        if (parent.physVolumes.isNotEmpty()) {
            useStyle("opaque") {
                Material3D.MATERIAL_OPACITY_KEY put 0.3
            }
        }
    }

    fun VisualObject3D.useStyle(name: String, builder: MetaBuilder.() -> Unit) {
        styleCache.getOrPut(name.toName()) {
            Meta(builder)
        }
        useStyle(name)
    }

    internal fun configureSolid(obj: VisualObject3D, parent: GDMLVolume, solid: GDMLSolid) {
        val material = parent.materialref.resolve(root) ?: GDMLElement(parent.materialref.ref)

        val styleName = "material[${material.name}]"

        obj.useStyle(styleName) {
            MATERIAL_COLOR_KEY put random.nextInt(16777216)
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
        //final.prototypes = proto
        final.prototypes {
            proto.children.forEach { (token, item) ->
                item.parent = null
                set(token.asName(), item)
            }
        }
        styleCache.forEach {
            final.styleSheet {
                define(it.key.toString(), it.value)
            }
        }
        final.rotationOrder = RotationOrder.ZXY
        onFinish(this@GDMLTransformer)
        return final
    }

}