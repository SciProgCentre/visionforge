package ru.mipt.npm.root

import kotlinx.serialization.json.Json
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.misc.Named
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.values.doubleArray
import kotlin.properties.ReadOnlyProperty

public fun MetaProvider.doubleArray(
    vararg default: Double,
    key: Name? = null,
): ReadOnlyProperty<Any?, DoubleArray> = value(key) {
    it?.doubleArray ?: doubleArrayOf(*default)
}

public class DObjectCache(private val cache: List<Meta>, public val refStack: List<Int> = emptyList()) {
    public operator fun get(index: Int): Meta = cache[index]

    public fun stack(ref: Int): DObjectCache = DObjectCache(cache, refStack + ref)

    public companion object {
        public val empty: DObjectCache = DObjectCache(emptyList(), emptyList())
    }
}

public open class DObject(public val meta: Meta, public val refCache: DObjectCache) {

    public val typename: String by meta.string(key = "_typename".asName()) {
        error("Type is not defined")
    }

    private fun <T : DObject> resolve(builder: (Meta, DObjectCache) -> T, meta: Meta): T? {
        meta["\$ref"]?.int?.let { refId ->
            if (refCache.refStack.contains(refId)) {
                println("Circular reference $refId in stack ${refCache.refStack}")
                return null
            }
            return builder(refCache[refId], refCache.stack(refId))
        }
        return builder(meta, refCache)
    }

    internal fun <T : DObject> tObjectArray(
        builder: (Meta, DObjectCache) -> T
    ): ReadOnlyProperty<Any?, List<T>> = ReadOnlyProperty { _, property ->
        meta.getIndexed(Name.of(property.name, "arr")).values.mapNotNull {
            resolve(builder, it)
        }
    }

    internal fun <T : DObject> dObject(
        builder: (Meta, DObjectCache) -> T,
        key: Name? = null
    ): ReadOnlyProperty<Any?, T?> = ReadOnlyProperty { _, property ->
        meta[key ?: property.name.asName()]?.let { resolve(builder, it) }
    }
}

public open class DNamed(meta: Meta, refCache: DObjectCache) : DObject(meta, refCache) {
    public val fName: String by meta.string("")
    public val fTitle: String by meta.string("")
}

public class DGeoMaterial(meta: Meta, refCache: DObjectCache) : DNamed(meta, refCache) {
}

public class DGeoMedium(meta: Meta, refCache: DObjectCache) : DNamed(meta, refCache) {
    public val fMaterial: DGeoMaterial? by dObject(::DGeoMaterial)
    public val fParams: DoubleArray by meta.doubleArray()
}

public class DGeoShape(meta: Meta, refCache: DObjectCache) : DNamed(meta, refCache) {
    public val fDX: Double by meta.double(0.0)
    public val fDY: Double by meta.double(0.0)
    public val fDZ: Double by meta.double(0.0)
}

public class DGeoVolume(meta: Meta, refCache: DObjectCache) : DNamed(meta, refCache), Named {
    public val fNodes: List<DGeoNode> by tObjectArray(::DGeoNode)
    public val fShape: DGeoShape? by dObject(::DGeoShape)
    public val fMedium: DGeoMedium? by dObject(::DGeoMedium)

    public val fFillColor: Int? by meta.int()

    override val name: Name by lazy { Name.parse(fName.ifEmpty { "volume[${meta.hashCode().toUInt()}]" }) }
}

public class DGeoNode(meta: Meta, refCache: DObjectCache) : DNamed(meta, refCache) {
    public val fVolume: DGeoVolume? by dObject(::DGeoVolume)
}

public open class DGeoMatrix(meta: Meta, refCache: DObjectCache) : DNamed(meta, refCache)

public open class DGeoScale(meta: Meta, refCache: DObjectCache) : DGeoMatrix(meta, refCache) {
    public val fScale: DoubleArray by meta.doubleArray(1.0, 1.0, 1.0)
    public val x: Double get() = fScale[0]
    public val y: Double get() = fScale[1]
    public val z: Double get() = fScale[2]
}


public class DGeoBoolNode(meta: Meta, refCache: DObjectCache) : DObject(meta, refCache) {
    public val fLeft: DGeoShape? by dObject(::DGeoShape)
    public val fLeftMat: DGeoMatrix? by dObject(::DGeoMatrix)

    public val fRight: DGeoShape? by dObject(::DGeoShape)
    public val fRightMat: DGeoMatrix? by dObject(::DGeoMatrix)
}


public class DGeoManager(meta: Meta, refCache: DObjectCache) : DNamed(meta, refCache) {
    public val fMatrices: List<DGeoMatrix> by tObjectArray(::DGeoMatrix)

    public val fShapes: List<DGeoShape> by tObjectArray(::DGeoShape)

    public val fVolumes: List<DGeoVolume> by tObjectArray(::DGeoVolume)

    public val fNodes: List<DGeoNode> by tObjectArray(::DGeoNode)

    public companion object {

        public fun parse(string: String): DGeoManager {
            val meta = Json.decodeFromString(MetaSerializer, string)
            val res = ArrayList<Meta>(4096)

            fun fillCache(element: Meta) {
                if (element["\$ref"] == null) {
                    res.add(element)
                    element.items.values.forEach {
                        if (!it.isLeaf) {
                            fillCache(it)
                        }
                    }
                }
            }

            fillCache(meta)

            val refCache = DObjectCache(res)
            return DGeoManager(meta, refCache)
        }
    }
}
