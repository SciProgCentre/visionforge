@file:Suppress("NO_EXPLICIT_VISIBILITY_IN_API_MODE_WARNING")

package tabulator

import org.w3c.dom.events.UIEvent

@Suppress("UNUSED_TYPEALIAS_PARAMETER")
internal typealias Pick<T, K> = Any

@Suppress("UNUSED_TYPEALIAS_PARAMETER")
internal typealias Record<K, T> = Any

internal typealias FilterFunction = (field: String, type: String /* "=" | "!=" | "like" | "<" | ">" | "<=" | ">=" | "in" | "regex" | "starts" | "ends" */, value: Any, filterParams: Tabulator.FilterParams) -> Unit

internal typealias GroupValuesArg = Array<Array<Any>>

internal typealias CustomMutator = (value: Any, data: Any, type: String /* "data" | "edit" */, mutatorParams: Any, cell: Tabulator.CellComponent) -> Any

internal typealias CustomAccessor = (value: Any, data: Any, type: String /* "data" | "download" | "clipboard" */, AccessorParams: Any, column: Tabulator.ColumnComponent, row: Tabulator.RowComponent) -> Any

internal typealias ColumnCalcParams = (values: Any, data: Any) -> Any

internal typealias ValueStringCallback = (value: Any) -> String

internal typealias ValueBooleanCallback = (value: Any) -> Boolean

internal typealias ValueVoidCallback = (value: Any) -> Unit

internal typealias EmptyCallback = (callback: () -> Unit) -> Unit

internal typealias CellEventCallback = (e: UIEvent, cell: Tabulator.CellComponent) -> Unit

internal typealias CellEditEventCallback = (cell: Tabulator.CellComponent) -> Unit

internal typealias ColumnEventCallback = (e: UIEvent, column: Tabulator.ColumnComponent) -> Unit

internal typealias RowEventCallback = (e: UIEvent, row: Tabulator.RowComponent) -> Unit

internal typealias RowChangedCallback = (row: Tabulator.RowComponent) -> Unit

internal typealias GroupEventCallback = (e: UIEvent, group: Tabulator.GroupComponent) -> Unit

internal typealias JSONRecord = Record<String, dynamic /* String | Number | Boolean */>

internal typealias ColumnSorterParamLookupFunction = (column: Tabulator.ColumnComponent, dir: String /* "asc" | "desc" */) -> Any