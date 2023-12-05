package space.kscience.visionforge.gdml

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.names.Name
import space.kscience.gdml.*
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.useStyle
import kotlin.random.Random

public class GdmlLoaderOptions {

    public enum class Action {
        ADD,
        REJECT,
        PROTOTYPE
    }

    public var lUnit: LUnit = LUnit.MM
    public var aUnit: AUnit = AUnit.RADIAN

    public var solidAction: (GdmlSolid) -> Action = { Action.PROTOTYPE }
    public var volumeAction: (GdmlGroup) -> Action = { Action.PROTOTYPE }

    internal val styleCache = HashMap<Name, Meta>()

    public fun Solid.registerAndUseStyle(name: String, builder: MutableMeta.() -> Unit) {
        styleCache.getOrPut(Name.parse(name)) {
            Meta(builder)
        }
        useStyle(name, false)
    }

    public fun Solid.transparent() {
        registerAndUseStyle("transparent") {
            SolidMaterial.MATERIAL_OPACITY_KEY put 0.3
            "edges.enabled" put true
        }
    }

    /**
     * Configure paint for given solid with given [GdmlMaterial]
     */
    public var configurePaint: SolidMaterial.(material: GdmlMaterial, solid: GdmlSolid) -> Unit =
        { material, _ -> color(randomColor(material)) }
        private set

    public fun paint(block: SolidMaterial.(material: GdmlMaterial, solid: GdmlSolid) -> Unit) {
        configurePaint = block
    }

    /**
     * Configure given solid
     */
    public var configureSolid: Solid.(parent: GdmlVolume, solid: GdmlSolid, material: GdmlMaterial) -> Unit =
        { parent, solid, material ->
            val styleName = "materials.${material.name}"

            if (parent.physVolumes.isNotEmpty()) transparent()

            registerAndUseStyle(styleName) {
                val vfMaterial = SolidMaterial().apply {
                    configurePaint(material, solid)
                }
                SolidMaterial.MATERIAL_KEY put vfMaterial.toMeta()
                "Gdml.material" put material.name
            }
        }
        private set

    public fun solids(block: Solid.(parent: GdmlVolume, solid: GdmlSolid, material: GdmlMaterial) -> Unit) {
        val oldConfigure = configureSolid
        configureSolid = { parent: GdmlVolume, solid: GdmlSolid, material: GdmlMaterial ->
            oldConfigure(parent, solid, material)
            block(parent, solid, material)
        }
    }


    public var light: LightSource? = AmbientLightSource()

    public companion object {
        private val random: Random = Random(222)

        private val colorCache = HashMap<GdmlMaterial, Int>()

        /**
         * Use random color and cache it based on the material. Meaning that colors are random, but always the same for the
         * same material.
         */
        public fun randomColor(material: GdmlMaterial): Int {
            return colorCache.getOrPut(material) { random.nextInt(16777216) }
        }
    }
}