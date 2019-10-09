@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION"
)
@file:JsModule("dat.gui")
@file:JsNonModule

package hep.dataforge.vis.spatial.editor

import org.w3c.dom.HTMLElement

external interface GUIParams {
    var autoPlace: Boolean? get() = definedExternally; set(value) = definedExternally
    var closed: Boolean? get() = definedExternally; set(value) = definedExternally
    var closeOnTop: Boolean? get() = definedExternally; set(value) = definedExternally
    var hideable: Boolean? get() = definedExternally; set(value) = definedExternally
    var load: Any? get() = definedExternally; set(value) = definedExternally
    var name: String? get() = definedExternally; set(value) = definedExternally
    var preset: String? get() = definedExternally; set(value) = definedExternally
    var width: Number? get() = definedExternally; set(value) = definedExternally
}

external open class GUI(option: GUIParams? = definedExternally /* null */) {
    open var __controllers: Array<GUIController>
    open var __folders: Array<GUI>
    open var domElement: HTMLElement
    open fun add(
        target: Any,
        propName: String,
        min: Number? = definedExternally /* null */,
        max: Number? = definedExternally /* null */,
        step: Number? = definedExternally /* null */
    ): GUIController

    open fun add(target: Any, propName: String, status: Boolean): GUIController
    open fun add(target: Any, propName: String, items: Array<String>): GUIController
    open fun add(target: Any, propName: String, items: Array<Number>): GUIController
    open fun add(target: Any, propName: String, items: Any): GUIController
    open fun addColor(target: Any, propName: String): GUIController
    open fun remove(controller: GUIController)
    open fun destroy()
    open fun addFolder(propName: String): GUI
    open fun removeFolder(subFolder: GUI)
    open fun open()
    open fun close()
    open fun hide()
    open fun show()
    open fun remember(target: Any, vararg additionalTargets: Any)
    open fun getRoot(): GUI
    open fun getSaveObject(): Any
    open fun save()
    open fun saveAs(presetName: String)
    open fun revert(gui: GUI)
    open fun listen(controller: GUIController)
    open fun updateDisplay()
    open var parent: GUI
    open var scrollable: Boolean
    open var autoPlace: Boolean
    open var preset: String
    open var width: Number
    open var name: String
    open var closed: Boolean
    open var load: Any
    open var useLocalStorage: Boolean

    companion object {
        var CLASS_AUTO_PLACE: String
        var CLASS_AUTO_PLACE_CONTAINER: String
        var CLASS_MAIN: String
        var CLASS_CONTROLLER_ROW: String
        var CLASS_TOO_TALL: String
        var CLASS_CLOSED: String
        var CLASS_CLOSE_BUTTON: String
        var CLASS_CLOSE_TOP: String
        var CLASS_CLOSE_BOTTOM: String
        var CLASS_DRAG: String
        var DEFAULT_WIDTH: Number
        var TEXT_CLOSED: String
        var TEXT_OPEN: String
    }
}

external open class GUIController {
    open fun destroy()
    open var onChange: (value: Any? /* = null */) -> GUIController
    open var onFinishChange: (value: Any? /* = null */) -> GUIController
    open fun setValue(value: Any): GUIController
    open fun getValue(): Any
    open fun updateDisplay(): GUIController
    open fun isModified(): Boolean
    open fun min(n: Number): GUIController
    open fun max(n: Number): GUIController
    open fun step(n: Number): GUIController
    open fun fire(): GUIController
    open fun options(option: Any): GUIController
    open fun name(s: String): GUIController
    open fun listen(): GUIController
    open fun remove(): GUIController
}