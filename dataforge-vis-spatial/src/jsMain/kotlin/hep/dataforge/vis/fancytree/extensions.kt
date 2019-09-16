@file:JsModule("jquery.fancytree")
@file:JsNonModule
@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION"
)
package ru.mipt.npm.fancytreekt

external interface List {
    var dnd5: DragAndDrop5? get() = definedExternally; set(value) = definedExternally
    var filter: Filter? get() = definedExternally; set(value) = definedExternally
    var table: Table? get() = definedExternally; set(value) = definedExternally
    @nativeGetter
    operator fun get(extension: String): Any?

    @nativeSetter
    operator fun set(extension: String, value: Any)
}

external interface DragAndDrop5 {
    var autoExpandMS: Number? get() = definedExternally; set(value) = definedExternally
    var dropMarkerOffsetX: Number? get() = definedExternally; set(value) = definedExternally
    var dropMarkerInsertOffsetX: Number? get() = definedExternally; set(value) = definedExternally
    var multiSource: Boolean? get() = definedExternally; set(value) = definedExternally
    var preventForeignNodes: Boolean? get() = definedExternally; set(value) = definedExternally
    var preventNonNodes: Boolean? get() = definedExternally; set(value) = definedExternally
    var preventRecursiveMoves: Boolean? get() = definedExternally; set(value) = definedExternally
    var preventVoidMoves: Boolean? get() = definedExternally; set(value) = definedExternally
    var scroll: Boolean? get() = definedExternally; set(value) = definedExternally
    var scrollSensitivity: Number? get() = definedExternally; set(value) = definedExternally
    var scrollSpeed: Number? get() = definedExternally; set(value) = definedExternally
    var setTextTypeJson: Boolean? get() = definedExternally; set(value) = definedExternally
    var dragStart: ((sourceNode: FancytreeNode, data: Any) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var dragDrag: ((sourceNode: FancytreeNode, data: Any) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var dragEnd: ((sourceNode: FancytreeNode, data: Any) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var dragEnter: ((targetNode: FancytreeNode, data: Any) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var dragOver: ((targetNode: FancytreeNode, data: Any) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var dragExpand: ((targetNode: FancytreeNode, data: Any) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var dragDrop: ((node: FancytreeNode, data: Any) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var dragLeave: ((targetNode: FancytreeNode, data: Any) -> Unit)? get() = definedExternally; set(value) = definedExternally
    @nativeGetter
    operator fun get(key: String): Any?

    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface Filter {
    var autoApply: Boolean
    var autoExpand: Boolean
    var counter: Boolean
    var fuzzy: Boolean
    var hideExpandedCounter: Boolean
    var hideExpanders: Boolean
    var highlight: Boolean
    var leavesOnly: Boolean
    var nodata: Boolean
    var mode: dynamic /* 'dimm' | 'string' */
    @nativeGetter
    operator fun get(key: String): Any?

    @nativeSetter
    operator fun set(key: String, value: Any)
}

external interface Table {
    var checkboxColumnIdx: Any
    var indentation: Number
    var nodeColumnIdx: Number
    @nativeGetter
    operator fun get(key: String): Any?

    @nativeSetter
    operator fun set(key: String, value: Any)
}