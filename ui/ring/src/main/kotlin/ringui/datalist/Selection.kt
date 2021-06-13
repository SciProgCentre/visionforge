@file:JsModule("@jetbrains/ring-ui/components/data-list/selection")
package ringui.datalist

public external interface SelectionProperties<T> {
    public var data: Array<dynamic>
    public var selected: Set<T>
    public var focused: T?
    public var getKey: (T) -> dynamic
    public var getChildren: (T) -> Array<T>
    public var isItemSelectable: (T) -> Boolean
}

@JsName("default")
public external class Selection<T>(args: SelectionProperties<T> = definedExternally) {
    public fun select(item: T = definedExternally)
    public fun deselect(item: T = definedExternally)
    public fun toggleSelection(item: T = definedExternally)
    public fun selectAll()
    public fun resetFocus()
    public fun resetSelection()
    public fun reset()
    public fun isFocused(value: T): Boolean
    public fun isSelected(value: T): Boolean
    public fun getFocused(): T
    public fun getSelected(): Set<T>
    public fun getActive(): Set<T>
}