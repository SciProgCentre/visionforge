@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION"
)

package hep.dataforge.vis.js.editor

import org.w3c.dom.HTMLElement

external interface Node {
    var field: String
    var value: String? get() = definedExternally; set(value) = definedExternally
    var path: dynamic
}

external interface NodeName {
    var path: Array<String>
    var type: dynamic /* 'object' | 'array' */
    var size: Number
}

external interface ValidationError {
    var path: dynamic
    var message: String
}

external interface Template {
    var text: String
    var title: String
    var className: String? get() = definedExternally; set(value) = definedExternally
    var field: String
    var value: Any
}

external interface `T$6` {
    var startFrom: Number
    var options: Array<String>
}

external interface AutoCompleteOptions {
    var confirmKeys: Array<Number>? get() = definedExternally; set(value) = definedExternally
    var caseSensitive: Boolean? get() = definedExternally; set(value) = definedExternally
//    var getOptions: AutoCompleteOptionsGetter? get() = definedExternally; set(value) = definedExternally
}

external interface SelectionPosition {
    var row: Number
    var column: Number
}

external interface SerializableNode {
    var value: Any
    var path: dynamic
}

external interface Color {
    var rgba: Array<Number>
    var hsla: Array<Number>
    var rgbString: String
    var rgbaString: String
    var hslString: String
    var hslaString: String
    var hex: String
}

//external interface `T$0` {
//    var field: Boolean
//    var value: Boolean
//}
//
//external interface `T$1` {
//    @nativeGetter
//    operator fun get(key: String): String?
//
//    @nativeSetter
//    operator fun set(key: String, value: String)
//}

//external interface Languages {
//    @nativeGetter
//    operator fun get(lang: String): `T$1`?
//
//    @nativeSetter
//    operator fun set(lang: String, value: `T$1`)
//}

external interface JSONEditorOptions {
//    var ace: AceAjax.Ace? get() = definedExternally; set(value) = definedExternally
//    var ajv: Ajv? get() = definedExternally; set(value) = definedExternally
    var onChange: (() -> Unit)? get() = definedExternally; set(value) = definedExternally
    var onChangeJSON: ((json: Any) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var onChangeText: ((jsonString: String) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var onEditable: ((node: Node) -> dynamic)? get() = definedExternally; set(value) = definedExternally
    var onError: ((error: Error) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var onModeChange: ((newMode: dynamic /* 'tree' | 'view' | 'form' | 'code' | 'text' */, oldMode: dynamic /* 'tree' | 'view' | 'form' | 'code' | 'text' */) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var onNodeName: ((nodeName: NodeName) -> String?)? get() = definedExternally; set(value) = definedExternally
    var onValidate: ((json: Any) -> dynamic)? get() = definedExternally; set(value) = definedExternally
    var escapeUnicode: Boolean? get() = definedExternally; set(value) = definedExternally
    var sortObjectKeys: Boolean? get() = definedExternally; set(value) = definedExternally
    var history: Boolean? get() = definedExternally; set(value) = definedExternally
    var mode: dynamic /* 'tree' | 'view' | 'form' | 'code' | 'text' */
    var modes: Array<dynamic /* 'tree' | 'view' | 'form' | 'code' | 'text' */>? get() = definedExternally; set(value) = definedExternally
    var name: String? get() = definedExternally; set(value) = definedExternally
    var schema: Any? get() = definedExternally; set(value) = definedExternally
    var schemaRefs: Any? get() = definedExternally; set(value) = definedExternally
    var search: Boolean? get() = definedExternally; set(value) = definedExternally
    var indentation: Number? get() = definedExternally; set(value) = definedExternally
    var theme: String? get() = definedExternally; set(value) = definedExternally
    var templates: Array<Template>? get() = definedExternally; set(value) = definedExternally
    var autocomplete: AutoCompleteOptions? get() = definedExternally; set(value) = definedExternally
    var mainMenuBar: Boolean? get() = definedExternally; set(value) = definedExternally
    var navigationBar: Boolean? get() = definedExternally; set(value) = definedExternally
    var statusBar: Boolean? get() = definedExternally; set(value) = definedExternally
    var onTextSelectionChange: ((start: SelectionPosition, end: SelectionPosition, text: String) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var onSelectionChange: ((start: SerializableNode, end: SerializableNode) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var onEvent: ((node: Node, event: String) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var colorPicker: Boolean? get() = definedExternally; set(value) = definedExternally
    var onColorPicker: ((parent: HTMLElement, color: String, onChange: (color: Color) -> Unit) -> Unit)? get() = definedExternally; set(value) = definedExternally
    var timestampTag: Boolean? get() = definedExternally; set(value) = definedExternally
    var language: String? get() = definedExternally; set(value) = definedExternally
    //var languages: Languages? get() = definedExternally; set(value) = definedExternally
    var modalAnchor: HTMLElement? get() = definedExternally; set(value) = definedExternally
    var enableSort: Boolean? get() = definedExternally; set(value) = definedExternally
    var enableTransform: Boolean? get() = definedExternally; set(value) = definedExternally
    var maxVisibleChilds: Number? get() = definedExternally; set(value) = definedExternally
}

external interface JsonPath {
    var path: dynamic
}

external interface EditorSelection {
    var start: SerializableNode
    var end: SerializableNode
}

external interface TextSelection {
    var start: SelectionPosition
    var end: SelectionPosition
    var text: String
}

@JsModule("jsoneditor")
@JsNonModule
external open class JSONEditor(
    container: HTMLElement,
    options: JSONEditorOptions? = definedExternally /* null */,
    json: dynamic = definedExternally /* null */
) {
    open fun collapseAll()
    open fun destroy()
    open fun expandAll()
    open fun focus()
    open fun get(): Any
    open fun getMode(): dynamic /* 'tree' | 'view' | 'form' | 'code' | 'text' */
    open fun getName(): String?
    open fun getNodesByRange(start: JsonPath, end: JsonPath): Array<SerializableNode>
    open fun getSelection(): EditorSelection
    open fun getText(): String
    open fun getTextSelection(): TextSelection
    open fun refresh()
    open fun set(json: Any)
    open fun setMode(mode: String /* 'tree' */)
    open fun setMode(mode: String /* 'view' */)
    open fun setMode(mode: String /* 'form' */)
    open fun setMode(mode: String /* 'code' */)
    open fun setMode(mode: String /* 'text' */)
    open fun setName(name: String? = definedExternally /* null */)
    open fun setSchema(schema: Any?, schemaRefs: Any? = definedExternally /* null */)
    open fun setSelection(start: JsonPath, end: JsonPath)
    open fun setText(jsonString: String)
    open fun setTextSelection(start: SelectionPosition, end: SelectionPosition)
    open fun update(json: Any)
    open fun updateText(jsonString: String)

    companion object {
        var VALID_OPTIONS: Array<String>
//        var ace: AceAjax.Ace
//        var Ajv: Ajv
        var VanillaPicker: Any
    }
}