package ru.mipt.npm.root

import kotlinx.serialization.json.Json
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import kotlin.properties.ReadOnlyProperty

public typealias RefCache = List<Meta>

public interface ObjectRef<T : TObjectScheme> {
    public fun resolve(refCache: RefCache): T?
}

private class ChildObjectRef<T : TObjectScheme>(
    val spec: Specification<T>,
    val metaProvider: () -> Meta?
) : ObjectRef<T> {
    override fun resolve(refCache: RefCache): T? {
        val meta = metaProvider() ?: return null
        meta["\$ref"]?.int?.let { refId ->
            return spec.read(refCache[refId])
        }
        return spec.read(meta)
    }
}

public fun <T: TObjectScheme> List<ObjectRef<T>>.resolve(refCache: RefCache): List<T> = map { it.resolve(refCache)!! }


public open class TObjectScheme : Scheme() {

    public val typename: String by string(key = "_typename".asName()) { error("Type is not defined") }

    internal fun <T : TObjectScheme> tObjectArray(
        spec: Specification<T>
    ): ReadOnlyProperty<Any?, List<ObjectRef<T>>> = ReadOnlyProperty { _, property ->
        meta.getIndexed(Name.of(property.name, "arr")).values.map { ChildObjectRef(spec){it} }
    }

    internal fun <T : TObjectScheme> refSpec(
        spec: Specification<T>,
        key: Name? = null
    ): ReadOnlyProperty<Any?, ObjectRef<T>> = ReadOnlyProperty { _, property ->
        ChildObjectRef(spec) { meta[key ?: property.name.asName()] }
    }

    public companion object : SchemeSpec<TObjectScheme>(::TObjectScheme)
}

public open class TNamedScheme : TObjectScheme() {
    public val fName: String by string("")
    public val fTitle: String by string("")

    public companion object : SchemeSpec<TNamedScheme>(::TNamedScheme)
}

public class TGeoMaterialScheme : TNamedScheme() {

    public companion object : SchemeSpec<TGeoMaterialScheme>(::TGeoMaterialScheme)
}

public class TGeoMediumScheme : TNamedScheme() {
    public val fMaterial: ObjectRef<TGeoMaterialScheme> by refSpec(TGeoMaterialScheme)
    public val fParams: DoubleArray by doubleArray()

    public companion object : SchemeSpec<TGeoMediumScheme>(::TGeoMediumScheme)
}

public class TGeoShapeScheme : TNamedScheme() {
    public val fDX: Double by double(0.0)
    public val fDY: Double by double(0.0)
    public val fDZ: Double by double(0.0)

    public companion object : SchemeSpec<TGeoShapeScheme>(::TGeoShapeScheme)
}

public class TGeoVolumeScheme : TNamedScheme() {
    public val fNodes: List<ObjectRef<TGeoNodeScheme>> by tObjectArray(TGeoNodeScheme)
    public val fShape: ObjectRef<TGeoShapeScheme> by refSpec(TGeoShapeScheme)
    public val fMedium: ObjectRef<TGeoMediumScheme> by refSpec(TGeoMediumScheme)

    public companion object : SchemeSpec<TGeoVolumeScheme>(::TGeoVolumeScheme)
}

public class TGeoNodeScheme : TNamedScheme() {
    public val fVolume: ObjectRef<TGeoVolumeScheme> by refSpec(TGeoVolumeScheme)

    public companion object : SchemeSpec<TGeoNodeScheme>(::TGeoNodeScheme)
}

public class TGeoMatrixScheme : TNamedScheme() {
    public companion object : SchemeSpec<TGeoMatrixScheme>(::TGeoMatrixScheme)
}


public class TGeoBoolNodeScheme : TObjectScheme() {
    public val fLeft: ObjectRef<TGeoShapeScheme> by refSpec(TGeoShapeScheme)
    public val fLeftMat: ObjectRef<TGeoMatrixScheme> by refSpec(TGeoMatrixScheme)

    public val fRight: ObjectRef<TGeoShapeScheme> by refSpec(TGeoShapeScheme)
    public val fRightMat: ObjectRef<TGeoMatrixScheme> by refSpec(TGeoMatrixScheme)

    public companion object : SchemeSpec<TGeoBoolNodeScheme>(::TGeoBoolNodeScheme)
}


public class TGeoManagerScheme : TNamedScheme() {
    public val fMatrices: List<ObjectRef<TGeoMatrixScheme>> by tObjectArray(TGeoMatrixScheme)

    public val fShapes: List<ObjectRef<TGeoShapeScheme>> by tObjectArray(TGeoShapeScheme)

    public val fVolumes: List<ObjectRef<TGeoVolumeScheme>> by tObjectArray(TGeoVolumeScheme)

    public val fNodes: List<ObjectRef<TGeoNodeScheme>> by tObjectArray(TGeoNodeScheme)

    public val refCache: List<Meta> by lazy {
        val res = ArrayList<Meta>(4096)
        fun fillCache(element: Meta) {
            if(element["\$ref"] == null) {
                res.add(element)
                element.items.values.forEach {
                    if (!it.isLeaf) {
                        fillCache(it)
                    }
                }
            }
        }
        fillCache(meta)
        res
    }

    public companion object : SchemeSpec<TGeoManagerScheme>(::TGeoManagerScheme) {

        public fun parse(string: String): TGeoManagerScheme {
            val meta = Json.decodeFromString(MetaSerializer, string)
            return read(meta)
        }
    }
}
