@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION"
)

package hep.dataforge.vis.spatial.editor

import org.w3c.dom.HTMLElement

external interface DropTargetValidator {
    @nativeInvoke
    operator fun invoke(dragNode: TreeNode, targetNode: TreeNode): Boolean
}

external interface DragAndDrop {
    var enabled: Boolean? get() = definedExternally; set(value) = definedExternally
    var validateOn: String? get() = definedExternally; set(value) = definedExternally
    var validate: DropTargetValidator
}

external interface DomConfig {
    var autoLoadMore: Boolean? get() = definedExternally; set(value) = definedExternally
    var deferredRendering: Boolean? get() = definedExternally; set(value) = definedExternally
    var dragAndDrop: DragAndDrop? get() = definedExternally; set(value) = definedExternally
    var nodeHeight: Number? get() = definedExternally; set(value) = definedExternally
    var showCheckboxes: Boolean? get() = definedExternally; set(value) = definedExternally
    var dragTargets: Array<String>? get() = definedExternally; set(value) = definedExternally
    var tabindex: Number? get() = definedExternally; set(value) = definedExternally
    var target: HTMLElement
}


@JsModule("inspire-tree-dom")
@JsNonModule
open external class InspireTreeDOM(tree: Any, opts: DomConfig)