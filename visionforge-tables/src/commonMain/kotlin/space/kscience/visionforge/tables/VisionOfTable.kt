package space.kscience.visionforge.tables

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.values.Null
import space.kscience.dataforge.values.Value
import space.kscience.dataforge.values.asValue
import space.kscience.tables.*
import space.kscience.visionforge.VisionBase
import space.kscience.visionforge.html.VisionOutput
import kotlin.jvm.JvmName
import kotlin.reflect.typeOf

internal object ColumnHeaderSerializer : KSerializer<ColumnHeader<Value>> {

    override val descriptor: SerialDescriptor get() = MetaSerializer.descriptor

    override fun deserialize(decoder: Decoder): ColumnHeader<Value> {
        val meta = decoder.decodeSerializableValue(MetaSerializer)
        return SimpleColumnHeader(meta["name"].string!!, typeOf<Value>(), meta["meta"] ?: Meta.EMPTY)
    }

    override fun serialize(encoder: Encoder, value: ColumnHeader<Value>) {
        val meta = Meta {
            "name" put value.name
            "meta" put value.meta
        }
        encoder.encodeSerializableValue(MetaSerializer, meta)
    }
}

public val ColumnHeader<Value>.properties: ValueColumnScheme get() = ValueColumnScheme.read(meta)

@Serializable
@SerialName("vision.table")
public class VisionOfTable(
    override val headers: List<@Serializable(ColumnHeaderSerializer::class) ColumnHeader<Value>>,
) : VisionBase(), Rows<Value> {

    public var data: List<Meta>
        get() = meta.getIndexed("rows").entries.sortedBy { it.key?.toInt() }.map { it.value }
        set(value) {
            meta["rows"] = value
        }

    public val rows: List<MetaRow> get() = data.map(::MetaRow)

    override fun rowSequence(): Sequence<Row<Value>> = rows.asSequence()
}

/**
 * Convert a table to a serializable vision
 */
@Suppress("UNCHECKED_CAST")
public fun <T> Table<T>.toVision(
    converter: (T?) -> Value,
): VisionOfTable = VisionOfTable(headers as TableHeader<Value>).also { vision ->
    vision.data = rows.map { row ->
        if (row is MetaRow) {
            row.meta
        } else {
            Meta {
                headers.forEach {
                    it.name put converter(row[it.name])
                }
            }
        }
    }
}

@JvmName("valueTableToVision")
public fun Table<Value>.toVision(): VisionOfTable = toVision { it ?: Null }

@JvmName("stringTableToVision")
public fun Table<String>.toVision(): VisionOfTable = toVision { (it ?: "").asValue() }

@JvmName("numberTableToVision")
public fun Table<Number>.toVision(): VisionOfTable = toVision { (it ?: Double.NaN).asValue() }

@DFExperimental
public inline fun VisionOutput.table(
    vararg headers: ColumnHeader<Value>,
    block: MutableRowTable<Value>.() -> Unit,
): VisionOfTable = RowTable(*headers, block = block).toVision()

@DFExperimental
public inline fun VisionOutput.columnTable(
    columnSize: UInt,
    block: MutableColumnTable<Value>.() -> Unit,
): VisionOfTable = ColumnTable(columnSize, block).toVision()

@DFExperimental
public fun VisionOutput.columnTable(
    vararg dataAndHeaders: Pair<ColumnHeader<Value>, List<Any?>>,
): VisionOfTable {
    val columns = dataAndHeaders.map { (header, data) ->
        ListColumn(header, data.map { Value.of(it) })
    }
    return ColumnTable(columns).toVision()
}

//public val tabulatorCssHader: HtmlFragment = {
//    link {
//        href = "https://unpkg.com/tabulator-tables@5.0.10/dist/css/tabulator.min.css"
//        rel = "stylesheet"
//    }
//}