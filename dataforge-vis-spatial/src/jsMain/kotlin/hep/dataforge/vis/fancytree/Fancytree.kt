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

import js.externals.jquery.JQuery
import js.externals.jquery.JQueryEventObject
import js.externals.jquery.JQueryPromise
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableRowElement
import org.w3c.dom.events.Event

external fun createTree(id: String, options: FancytreeOptions = definedExternally /* null */): Fancytree

external interface Fancytree {
    var `$div`: JQuery<*>
    var widget: Any
    var rootNode: FancytreeNode
    var `$container`: JQuery<*>
    var focusNode: FancytreeNode
    var options: FancytreeOptions
    fun activateKey(key: String): FancytreeNode
    fun activateKey(key: Boolean): FancytreeNode
    fun applyPatch(patchList: Array<NodePatch>): JQueryPromise<Any>
    fun changeRefKey(oldRefKey: String, newRefKey: String)
    fun clearCookies()
    fun clearFilter()
    fun count(): Number
    fun debug(msg: Any)
    fun filterBranches(filter: String): Number
    fun filterBranches(filter: (node: FancytreeNode) -> Boolean): Number
    fun filterNodes(filter: String, leavesOnly: Boolean? = definedExternally /* null */): Number
    fun filterNodes(
        filter: (node: FancytreeNode) -> Boolean,
        leavesOnly: Boolean? = definedExternally /* null */
    ): Number

    fun findNextNode(match: String, startNode: FancytreeNode? = definedExternally /* null */): FancytreeNode
    fun findNextNode(
        match: (node: FancytreeNode) -> Boolean,
        startNode: FancytreeNode? = definedExternally /* null */
    ): FancytreeNode

    fun findAll(match: String): Array<FancytreeNode>
    fun findAll(match: (node: FancytreeNode) -> Boolean?): Array<FancytreeNode>
    fun generateFormElements(
        selected: Boolean? = definedExternally /* null */,
        active: Boolean? = definedExternally /* null */
    )

    fun getActiveNode(): FancytreeNode
    fun getFirstChild(): FancytreeNode
    fun getFocusNode(ifTreeHasFocus: Boolean? = definedExternally /* null */): FancytreeNode
    fun getNodeByKey(key: String, searchRoot: FancytreeNode? = definedExternally /* null */): FancytreeNode
    fun getNodesByRef(refKey: String, rootNode: FancytreeNode? = definedExternally /* null */): Array<FancytreeNode>
    fun getPersistData()
    fun getRootNode(): FancytreeNode
    fun getSelectedNodes(stopOnParents: Boolean? = definedExternally /* null */): Array<FancytreeNode>
    fun hasFocus(): Boolean
    fun info(msg: Any)
    fun isEditing(): FancytreeNode
    fun loadKeyPath(
        keyPathList: Array<String>,
        callback: (node: FancytreeNode, status: String) -> Unit
    ): JQueryPromise<Any>

    fun loadKeyPath(keyPath: String, callback: (node: FancytreeNode, status: String) -> Unit): JQueryPromise<Any>
    fun reactivate()
    fun reload(source: Any? = definedExternally /* null */): JQueryPromise<Any>
    fun render(force: Boolean? = definedExternally /* null */, deep: Boolean? = definedExternally /* null */)
    fun setFocus(flag: Boolean? = definedExternally /* null */)
    fun toDict(
        includeRoot: Boolean? = definedExternally /* null */,
        callback: ((node: FancytreeNode) -> Unit)? = definedExternally /* null */
    ): Any

    fun visit(fn: (node: FancytreeNode) -> Any): Boolean
    fun warn(msg: Any)
    fun enableUpdate(enabled: Boolean)
}

external interface FancytreeNode {
    var tree: Fancytree
    var parent: FancytreeNode
    var key: String
    var title: String
    var data: Any
    var children: Array<FancytreeNode>
    var expanded: Boolean
    var extraClasses: String
    var folder: Boolean
    var statusNodeType: String
    var lazy: Boolean
    var tooltip: String
    var span: HTMLElement
    var tr: HTMLTableRowElement
    fun addChildren(
        children: Array<NodeData>,
        insertBefore: FancytreeNode? = definedExternally /* null */
    ): FancytreeNode

    fun addChildren(children: Array<NodeData>, insertBefore: String? = definedExternally /* null */): FancytreeNode
    fun addChildren(children: Array<NodeData>, insertBefore: Number? = definedExternally /* null */): FancytreeNode
    fun addChildren(child: NodeData, insertBefore: FancytreeNode? = definedExternally /* null */): FancytreeNode
    fun addChildren(child: NodeData, insertBefore: String? = definedExternally /* null */): FancytreeNode
    fun addChildren(child: NodeData, insertBefore: Number? = definedExternally /* null */): FancytreeNode
    fun addClass(className: String)
    fun addNode(node: NodeData, mode: String? = definedExternally /* null */): FancytreeNode
    fun applyPatch(patch: NodePatch): JQueryPromise<Any>
    fun collapseSiblings(): JQueryPromise<Any>
    fun copyTo(
        node: FancytreeNode,
        mode: String? = definedExternally /* null */,
        map: ((node: NodeData) -> Unit)? = definedExternally /* null */
    ): FancytreeNode

    fun countChildren(deep: Boolean? = definedExternally /* null */): Number
    fun debug(msg: Any)
    fun editCreateNode(mode: String? = definedExternally /* null */, init: Any? = definedExternally /* null */)
    fun editEnd(applyChanges: Boolean)
    fun editStart()
    fun findAll(match: String): Array<FancytreeNode>
    fun findAll(match: (node: FancytreeNode) -> Boolean): Array<FancytreeNode>
    fun findFirst(match: String): FancytreeNode
    fun findFirst(match: (node: FancytreeNode) -> Boolean): FancytreeNode
    fun fixSelection3AfterClick()
    fun fixSelection3FromEndNodes()
    fun fromDict(dict: NodeData)
    fun getChildren(): Array<FancytreeNode>
    fun getCloneList(includeSelf: Boolean? = definedExternally /* null */): Array<FancytreeNode>
    fun getFirstChild(): FancytreeNode
    fun getIndex(): Number
    fun getIndexHier(): String
    fun getKeyPath(excludeSelf: Boolean): String
    fun getLastChild(): FancytreeNode
    fun getLevel(): Number
    fun getNextSibling(): FancytreeNode
    fun getParent(): FancytreeNode
    fun getParentList(includeRoot: Boolean, includeSelf: Boolean): Array<FancytreeNode>
    fun getPrevSibling(): FancytreeNode
    fun hasChildren(): Boolean
    fun hasFocus(): Boolean
    fun info(msg: String)
    fun isActive(): Boolean
    fun isChildOf(otherNode: FancytreeNode): Boolean
    fun isClone(): Boolean
    fun isDescendantOf(otherNode: FancytreeNode): Boolean
    fun isEditing(): Boolean
    fun isExpanded(): Boolean
    fun isFirstSibling(): Boolean
    fun isFolder(): Boolean
    fun isLastSibling(): Boolean
    fun isLazy(): Boolean
    fun isLoaded(): Boolean
    fun isLoading(): Boolean
    fun isRootNode(): Boolean
    fun isSelected(): Boolean
    fun isStatusNode(): Boolean
    fun isTopLevel(): Boolean
    fun isUndefined(): Boolean
    fun isVisible(): Boolean
    fun load(forceReload: Boolean? = definedExternally /* null */): JQueryPromise<Any>
    fun makeVisible(opts: Any? = definedExternally /* null */): JQueryPromise<Any>
    fun moveTo(
        targetNode: FancytreeNode,
        mode: String,
        map: ((node: FancytreeNode) -> Unit)? = definedExternally /* null */
    )

    fun navigate(where: Number, activate: Boolean? = definedExternally /* null */): JQueryPromise<Any>
    fun remove()
    fun removeChild(childNode: FancytreeNode)
    fun removeChildren()
    fun removeClass(className: String)
    fun render(force: Boolean? = definedExternally /* null */, deep: Boolean? = definedExternally /* null */)
    fun renderStatus()
    fun renderTitle()
    fun reRegister(key: String, refKey: String): Boolean
    fun resetLazy()
    fun scheduleAction(mode: String, ms: Number)
    fun scrollIntoView(
        effects: Boolean? = definedExternally /* null */,
        options: Any? = definedExternally /* null */
    ): JQueryPromise<Any>

    fun scrollIntoView(
        effects: Any? = definedExternally /* null */,
        options: Any? = definedExternally /* null */
    ): JQueryPromise<Any>

    fun setActive(
        flag: Boolean? = definedExternally /* null */,
        opts: Any? = definedExternally /* null */
    ): JQueryPromise<Any>

    fun setExpanded(
        flag: Boolean? = definedExternally /* null */,
        opts: Any? = definedExternally /* null */
    ): JQueryPromise<Any>

    fun setFocus(flag: Boolean? = definedExternally /* null */)
    fun setSelected(flag: Boolean? = definedExternally /* null */)
    fun setStatus(
        status: String,
        message: String? = definedExternally /* null */,
        details: String? = definedExternally /* null */
    )

    fun setTitle(title: String)
    fun sortChildren(
        cmp: ((a: FancytreeNode, b: FancytreeNode) -> Number)? = definedExternally /* null */,
        deep: Boolean? = definedExternally /* null */
    )

    fun toDict(
        recursive: Boolean? = definedExternally /* null */,
        callback: ((dict: NodeData) -> Unit)? = definedExternally /* null */
    ): NodeData

    fun toggleClass(className: String, flag: Boolean? = definedExternally /* null */): Boolean
    fun toggleExpanded()
    fun toggleSelected()
    fun visit(fn: (node: FancytreeNode) -> Any, includeSelf: Boolean? = definedExternally /* null */): Boolean
    fun visitAndLoad(
        fn: (node: FancytreeNode) -> Any,
        includeSelf: Boolean? = definedExternally /* null */
    ): JQueryPromise<Any>

    fun visitParents(fn: (node: FancytreeNode) -> Any, includeSelf: Boolean? = definedExternally /* null */): Boolean
    fun warn(msg: Any)
    fun addChildren(children: Array<NodeData>): FancytreeNode
    fun addChildren(child: NodeData): FancytreeNode
    fun scrollIntoView(): JQueryPromise<Any>
}

external enum class FancytreeClickFolderMode {
    activate /* = 1 */,
    expand /* = 2 */,
    activate_and_expand /* = 3 */,
    activate_dblclick_expands /* = 4 */
}

external enum class FancytreeSelectMode {
    single /* = 1 */,
    multi /* = 2 */,
    mutlti_hier /* = 3 */
}

external interface EventData {
    var tree: Fancytree
    var widget: Any
    var options: FancytreeOptions
    var originalEvent: JQueryEventObject
    var node: FancytreeNode
    var result: Any
    var targetType: String
    var response: Any
}

external interface FancytreeEvents {
    val activate: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val beforeActivate: ((event: JQueryEventObject, data: EventData) -> Boolean)? get() = definedExternally
    val beforeExpand: ((event: JQueryEventObject, data: EventData) -> Boolean)? get() = definedExternally
    val beforeSelect: ((event: JQueryEventObject, data: EventData) -> Boolean)? get() = definedExternally
    val blur: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val blurTree: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val click: ((event: JQueryEventObject, data: EventData) -> Boolean)? get() = definedExternally
    val collapse: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val create: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val createNode: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val dblclick: ((event: JQueryEventObject, data: EventData) -> Boolean)? get() = definedExternally
    val deactivate: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val expand: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val focus: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val focusTree: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val init: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val keydown: ((event: JQueryEventObject, data: EventData) -> Boolean)? get() = definedExternally
    val keypress: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val lazyLoad: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val loadChildren: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val loadError: ((event: JQueryEventObject, data: EventData) -> Boolean)? get() = definedExternally
    val postProcess: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val removeNode: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val renderColumns: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val renderNode: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val renderTitle: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val restore: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    val select: ((event: JQueryEventObject, data: EventData) -> Unit)? get() = definedExternally
    var rtl: Boolean? get() = definedExternally; set(value) = definedExternally
}

external interface `T$0` {
    var type: String
    var cache: Boolean
    var dataType: String
}

external interface `T$1` {
    var top: Number
    var bottom: Number
}

external interface FancytreeOptions : FancytreeEvents {
    var activeVisible: Boolean? get() = definedExternally; set(value) = definedExternally
    var ajax: `T$0`? get() = definedExternally; set(value) = definedExternally
    var aria: Boolean? get() = definedExternally; set(value) = definedExternally
    var autoActivate: Boolean? get() = definedExternally; set(value) = definedExternally
    var autoCollapse: Boolean? get() = definedExternally; set(value) = definedExternally
    var autoScroll: Boolean? get() = definedExternally; set(value) = definedExternally
    var checkbox: dynamic /* Boolean | String | (event: JQueryEventObject, data: EventData) -> Boolean */
    var clickFolderMode: FancytreeClickFolderMode? get() = definedExternally; set(value) = definedExternally
    var debugLevel: dynamic /* 0 | 1 | 2 | 3 | 4 */
    var defaultKey: ((node: FancytreeNode) -> String)? get() = definedExternally; set(value) = definedExternally
    var enableAspx: Boolean? get() = definedExternally; set(value) = definedExternally
    var enableTitles: Boolean? get() = definedExternally; set(value) = definedExternally
    var extensions: Array<dynamic /* Any | String */>? get() = definedExternally; set(value) = definedExternally
    var focusOnSelect: Boolean? get() = definedExternally; set(value) = definedExternally
    var generateIds: Boolean? get() = definedExternally; set(value) = definedExternally
    var icon: dynamic /* Boolean | String */
    var idPrefix: String? get() = definedExternally; set(value) = definedExternally
    var imagePath: String? get() = definedExternally; set(value) = definedExternally
    var keyboard: Boolean? get() = definedExternally; set(value) = definedExternally
    var keyPathSeparator: String? get() = definedExternally; set(value) = definedExternally
    var minExpandLevel: Number? get() = definedExternally; set(value) = definedExternally
    var quicksearch: Boolean? get() = definedExternally; set(value) = definedExternally
    var scrollOfs: `T$1`? get() = definedExternally; set(value) = definedExternally
    var scrollParent: JQuery<*>? get() = definedExternally; set(value) = definedExternally
    var selectMode: FancytreeSelectMode? get() = definedExternally; set(value) = definedExternally
    var source: dynamic /* Array<Any> | Any */
    var strings: TranslationTable? get() = definedExternally; set(value) = definedExternally
    var tabbable: Boolean? get() = definedExternally; set(value) = definedExternally
    var titlesTabbable: Boolean? get() = definedExternally; set(value) = definedExternally
    //var toggleEffect: EffectOptions? get() = definedExternally; set(value) = definedExternally
    var tooltip: Boolean? get() = definedExternally; set(value) = definedExternally
    var unselectable: dynamic /* Boolean | (event: JQueryEventObject, data: Fancytree.EventData) -> Boolean? */
    var unselectableIgnore: dynamic /* Boolean | (event: JQueryEventObject, data: Fancytree.EventData) -> Boolean? */
    var unselectableStatus: dynamic /* Boolean | (event: JQueryEventObject, data: Fancytree.EventData) -> Boolean? */
    var dnd5: DragAndDrop5
    var filter: Filter
    var table: Table
    @nativeGetter
    operator fun get(extension: String): Any?

    @nativeSetter
    operator fun set(extension: String, value: Any)
}

external interface TranslationTable {
    var loading: String
    var loadError: String
    var moreData: String
    var noData: String
}

external interface NodeData {
    var title: String
    var icon: dynamic /* Boolean | String */
    var key: String? get() = definedExternally; set(value) = definedExternally
    var refKey: String? get() = definedExternally; set(value) = definedExternally
    var expanded: Boolean? get() = definedExternally; set(value) = definedExternally
    var active: Boolean? get() = definedExternally; set(value) = definedExternally
    var focus: Boolean? get() = definedExternally; set(value) = definedExternally
    var folder: Boolean? get() = definedExternally; set(value) = definedExternally
    var hideCheckbox: Boolean? get() = definedExternally; set(value) = definedExternally
    var lazy: Boolean? get() = definedExternally; set(value) = definedExternally
    var selected: Boolean? get() = definedExternally; set(value) = definedExternally
    var unselectable: Boolean? get() = definedExternally; set(value) = definedExternally
    var children: Array<NodeData>? get() = definedExternally; set(value) = definedExternally
    var tooltip: String? get() = definedExternally; set(value) = definedExternally
    var extraClasses: String? get() = definedExternally; set(value) = definedExternally
    var data: Any? get() = definedExternally; set(value) = definedExternally
    var iconTooltip: String? get() = definedExternally; set(value) = definedExternally
    var statusNodeType: String? get() = definedExternally; set(value) = definedExternally
    var type: String? get() = definedExternally; set(value) = definedExternally
    var unselectableIgnore: Boolean? get() = definedExternally; set(value) = definedExternally
    var unselectableStatus: Boolean? get() = definedExternally; set(value) = definedExternally
}

external interface NodePatch {
    var appendChildren: NodeData? get() = definedExternally; set(value) = definedExternally
    var replaceChildren: NodeData? get() = definedExternally; set(value) = definedExternally
    var insertChildren: NodeData? get() = definedExternally; set(value) = definedExternally
}

external interface TreePatch {
    @nativeGetter
    operator fun get(key: String): NodePatch?

    @nativeSetter
    operator fun set(key: String, value: NodePatch)
}

external object FancytreeStatic {
    var buildType: String
    var debugLevel: Number
    var version: String
    fun assert(cond: Boolean, msg: String)
    fun <T : (args: Any) -> Unit> debounce(
        timeout: Number,
        fn: T,
        invokeAsap: Boolean? = definedExternally /* null */,
        ctx: Any? = definedExternally /* null */
    ): T

    fun debug(msg: String)
    fun error(msg: String)
    fun escapeHtml(s: String): String
    fun getEventTarget(event: Event): Any
    fun getEventTargetType(event: Event): String
    fun getNode(el: JQuery<*>): FancytreeNode
    fun getNode(el: Event): FancytreeNode
    fun getNode(el: Element): FancytreeNode
    fun info(msg: String)
    fun keyEventToString(event: Event): String
    fun parseHtml(`$ul`: JQuery<*>): Array<NodeData>
    fun registerExtension(definition: Any)
    fun unescapeHtml(s: String): String
    fun warn(msg: String)
}