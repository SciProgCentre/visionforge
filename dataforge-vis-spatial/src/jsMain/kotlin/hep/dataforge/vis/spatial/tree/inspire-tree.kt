@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION",
    "unused"
)

package hep.dataforge.vis.spatial.tree

import kotlin.js.Promise
import kotlin.js.RegExp

external interface NodeIteratee {
    @nativeInvoke
    operator fun invoke(node: TreeNode): Any
}

external interface MatchProcessor {
    @nativeInvoke
    operator fun invoke(matches: TreeNodes): Any
}

external interface SearchMatcher {
    @nativeInvoke
    operator fun invoke(query: String, resolve: Any, reject: Any): Any
}

external interface `T$0` {
    var add: Boolean? get() = definedExternally; set(value) = definedExternally
    var edit: Boolean? get() = definedExternally; set(value) = definedExternally
    var remove: Boolean? get() = definedExternally; set(value) = definedExternally
}

external interface `T$1` {
    var resetStateOnRestore: Boolean? get() = definedExternally; set(value) = definedExternally
}

external interface `T$2` {
    var limit: Number? get() = definedExternally; set(value) = definedExternally
}

external interface `T$3` {
    var matcher: SearchMatcher
    var matchProcess: MatchProcessor
}

external interface Selection {
    var allow: NodeIteratee? get() = definedExternally; set(value) = definedExternally
    var autoDeselect: Boolean? get() = definedExternally; set(value) = definedExternally
    var autoSelectChildren: Boolean? get() = definedExternally; set(value) = definedExternally
    var disableDirectDeselection: Boolean? get() = definedExternally; set(value) = definedExternally
    var mode: String? get() = definedExternally; set(value) = definedExternally
    var multiple: Boolean? get() = definedExternally; set(value) = definedExternally
    var require: Boolean? get() = definedExternally; set(value) = definedExternally
}

external interface Config {
    var allowLoadEvents: Array<String>? get() = definedExternally; set(value) = definedExternally
//    var checkbox: dynamic get() = definedExternally; set(value) = definedExternally
    var contextMenu: Boolean? get() = definedExternally; set(value) = definedExternally
    val data: ((node: TreeNode, resolve: (nodes: Array<NodeConfig>) -> Any, reject: (err: Error) -> Any) -> dynamic)? get() = definedExternally
    var deferredLoading: Boolean? get() = definedExternally; set(value) = definedExternally
    var editable: Boolean? get() = definedExternally; set(value) = definedExternally
    var editing: `T$0`? get() = definedExternally; set(value) = definedExternally
    var nodes: `T$1`? get() = definedExternally; set(value) = definedExternally
    var pagination: `T$2`? get() = definedExternally; set(value) = definedExternally
    var renderer: Any? get() = definedExternally; set(value) = definedExternally
    var search: `T$3`? get() = definedExternally; set(value) = definedExternally
    var selection: Selection? get() = definedExternally; set(value) = definedExternally
    var sort: String? get() = definedExternally; set(value) = definedExternally
    var multiselect: Boolean? get() = definedExternally; set(value) = definedExternally
}

external interface State {
    var checked: Boolean? get() = definedExternally; set(value) = definedExternally
    var collapsed: Boolean? get() = definedExternally; set(value) = definedExternally
    var draggable: Boolean? get() = definedExternally; set(value) = definedExternally
    //var `drop-target`: Boolean? get() = definedExternally; set(value) = definedExternally
    var editable: Boolean? get() = definedExternally; set(value) = definedExternally
    var focused: Boolean? get() = definedExternally; set(value) = definedExternally
    var hidden: Boolean? get() = definedExternally; set(value) = definedExternally
    var indeterminate: Boolean? get() = definedExternally; set(value) = definedExternally
    var loading: Boolean? get() = definedExternally; set(value) = definedExternally
    var matched: Boolean? get() = definedExternally; set(value) = definedExternally
    var removed: Boolean? get() = definedExternally; set(value) = definedExternally
    var rendered: Boolean? get() = definedExternally; set(value) = definedExternally
    var selectable: Boolean? get() = definedExternally; set(value) = definedExternally
    var selected: Boolean? get() = definedExternally; set(value) = definedExternally
}

external interface InspireTag {
    var attributes: Any? get() = definedExternally; set(value) = definedExternally
}

external interface ITree {
    var a: InspireTag? get() = definedExternally; set(value) = definedExternally
    var icon: String? get() = definedExternally; set(value) = definedExternally
    var li: InspireTag? get() = definedExternally; set(value) = definedExternally
    var state: State? get() = definedExternally; set(value) = definedExternally
}

external interface NodeConfig {
    var children: dynamic /* Array<NodeConfig> | true */
    var id: String? get() = definedExternally; set(value) = definedExternally
    var text: String
    @JsName("itree")
    var itree: ITree? get() = definedExternally; set(value) = definedExternally
}

external interface Pagination {
    var limit: Number
    var total: Number
}

@JsModule("inspire-tree")
@JsNonModule
open external class InspireTree(opts: Config) : EventEmitter2 {
    constructor(tree: InspireTree)
    constructor(tree: InspireTree, array: Array<Any>)
    constructor(tree: InspireTree, array: TreeNodes)

    open fun addNode(node: NodeConfig): TreeNode
    open fun addNodes(node: Array<NodeConfig>): TreeNodes
    open fun available(): TreeNodes
    open fun blur(): TreeNodes
    open fun blurDeep(): TreeNodes
    open fun boundingNodes(): Array<TreeNode>
    open fun canAutoDeselect(): Boolean
    open fun checked(): TreeNodes
    open fun clean(): TreeNodes
    open fun clearSearch(): InspireTree
    open fun clone(): TreeNodes
    open fun collapse(): TreeNodes
    open fun collapsed(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun collapseDeep(): TreeNodes
    open fun context(): TreeNode
    open fun copy(
        dest: InspireTree,
        hierarchy: Boolean? = definedExternally /* null */,
        includeState: Boolean? = definedExternally /* null */
    ): TreeNodes

    open fun createNode(obj: Any): TreeNode
    open fun deepest(): TreeNodes
    open fun deselect(): TreeNodes
    open fun deselectDeep(): TreeNodes
    open fun disableDeselection(): InspireTree
    open fun each(iteratee: NodeIteratee): TreeNodes
    open fun editable(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun editing(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun enableDeselection(): InspireTree
    open fun expand(): Promise<TreeNodes>
    open fun expandDeep(): TreeNodes
    open fun expanded(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun expandParents(): TreeNodes
    open fun extract(predicate: String): TreeNodes
    open fun extract(predicate: NodeIteratee): TreeNodes
    open fun filterBy(predicate: String): TreeNodes
    open fun filterBy(predicate: NodeIteratee): TreeNodes
    open fun find(
        predicate: (node: TreeNode, index: Number? /* = null */, obj: Array<TreeNode>? /* = null */) -> Boolean,
        thisArg: Any? = definedExternally /* null */
    ): TreeNode

    open fun first(predicate: (node: TreeNode) -> Boolean): TreeNode
    open fun flatten(predicate: String): TreeNodes
    open fun flatten(predicate: NodeIteratee): TreeNodes
    open fun focused(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun get(index: Number): TreeNode
    open fun hidden(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun hide(): TreeNodes
    open fun hideDeep(): TreeNodes
    open var id: dynamic /* String | Number */
    open var config: Config
    open var preventDeselection: Boolean
    open fun indeterminate(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun insertAt(index: Number, `object`: Any): TreeNode
    open fun invoke(methods: String): TreeNodes
    open fun invoke(methods: Array<String>): TreeNodes
    open fun invokeDeep(methods: String): TreeNodes
    open fun invokeDeep(methods: Array<String>): TreeNodes
    open fun isEventMuted(eventName: String): Boolean
    open fun last(predicate: (node: TreeNode) -> Boolean): TreeNode
    open fun lastSelectedNode(): TreeNode
    open fun load(loader: Promise<TreeNodes>): Promise<TreeNodes>
    open fun loading(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun matched(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun move(index: Number, newIndex: Number, target: TreeNodes): TreeNode
    open fun mute(events: Array<String>): InspireTree
    open fun muted(): Boolean
    open fun node(id: String): TreeNode
    open fun node(id: Number): TreeNode
    open fun nodes(ids: Array<String>? = definedExternally /* null */): TreeNodes
    open fun nodes(ids: Array<Number>? = definedExternally /* null */): TreeNodes
    open fun pagination(): Pagination
    open fun recurseDown(iteratee: NodeIteratee): TreeNodes
    open fun reload(): Promise<TreeNodes>
    open fun removeAll(): InspireTree
    open fun removed(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun restore(): TreeNodes
    open fun restoreDeep(): TreeNodes
    open fun search(query: String): Promise<TreeNodes>
    open fun search(query: RegExp): Promise<TreeNodes>
    open fun search(query: NodeIteratee): Promise<TreeNodes>
    open fun select(): TreeNodes
    open fun selectable(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun selectBetween(start: TreeNode, end: TreeNode): InspireTree
    open fun selectDeep(): TreeNodes
    open fun selected(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun selectFirstAvailableNode(): TreeNode
    open fun show(): TreeNodes
    open fun showDeep(): TreeNodes
    open fun softRemove(): TreeNodes
    open fun sortBy(sorter: String): TreeNodes
    open fun sortBy(sorter: NodeIteratee): TreeNodes
    open fun state(key: String, `val`: Boolean): TreeNodes
    open fun stateDeep(key: String, `val`: Boolean): TreeNodes
    open fun swap(node1: TreeNode, node2: TreeNode): TreeNodes
    open fun toArray(): Array<Any>
    open fun toArray(): Array<Any>
    open fun tree(): InspireTree
    open fun unmute(events: Array<String>): InspireTree
    open fun visible(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun nodes(): TreeNodes

    companion object {
        fun isTreeNode(`object`: Any): Boolean
        fun isTreeNodes(`object`: Any): Boolean
    }
}

open external class TreeNodes(tree: InspireTree) {
    constructor(tree: InspireTree, array: Array<Any>)
    constructor(tree: InspireTree, array: TreeNodes)

    open fun addNode(node: NodeConfig): TreeNode
    open fun available(): TreeNodes
    open fun blur(): TreeNodes
    open fun blurDeep(): TreeNodes
    open fun checked(): TreeNodes
    open fun clean(): TreeNodes
    open fun clone(): TreeNodes
    open fun collapse(): TreeNodes
    open fun collapsed(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun collapseDeep(): TreeNodes
    open fun context(): TreeNode
    open fun copy(
        dest: InspireTree,
        hierarchy: Boolean? = definedExternally /* null */,
        includeState: Boolean? = definedExternally /* null */
    ): TreeNodes

    open fun deepest(): TreeNodes
    open fun deselect(): TreeNodes
    open fun deselectDeep(): TreeNodes
    open fun each(iteratee: NodeIteratee): TreeNodes
    open fun editable(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun editing(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun expand(): TreeNodes
    open fun expandDeep(): Promise<TreeNodes>
    open fun expanded(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun expandParents(): TreeNodes
    open fun extract(predicate: String): TreeNodes
    open fun extract(predicate: NodeIteratee): TreeNodes
    open fun filterBy(predicate: String): TreeNodes
    open fun filterBy(predicate: NodeIteratee): TreeNodes
    open fun find(
        predicate: (node: TreeNode, index: Number? /* = null */, obj: Array<TreeNode>? /* = null */) -> Boolean,
        thisArg: Any? = definedExternally /* null */
    ): TreeNode

    open fun flatten(predicate: String): TreeNodes
    open fun flatten(predicate: NodeIteratee): TreeNodes
    open fun focused(full: Boolean? = definedExternally /* null */): TreeNodes
    operator fun get(index: Number): TreeNode
    open fun hidden(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun hide(): TreeNodes
    open fun hideDeep(): TreeNodes
    open fun indeterminate(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun insertAt(index: Number, `object`: Any): TreeNode
    open fun invoke(methods: String): TreeNodes
    open fun invoke(methods: Array<String>): TreeNodes
    open fun invokeDeep(methods: String): TreeNodes
    open fun invokeDeep(methods: Array<String>): TreeNodes
    open fun loading(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun matched(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun move(index: Number, newIndex: Number, target: TreeNodes): TreeNode
    open fun node(id: String): TreeNode
    open fun node(id: Number): TreeNode
    open fun nodes(ids: Array<String>? = definedExternally /* null */): TreeNodes
    open fun nodes(ids: Array<Number>? = definedExternally /* null */): TreeNodes
    open fun pagination(): Pagination
    open fun recurseDown(iteratee: NodeIteratee): TreeNodes
    open fun removed(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun restore(): TreeNodes
    open fun restoreDeep(): TreeNodes
    open fun select(): TreeNodes
    open fun selectable(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun selectBetween(start: TreeNode, end: TreeNode): InspireTree
    open fun selectDeep(): TreeNodes
    open fun selected(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun show(): TreeNodes
    open fun showDeep(): TreeNodes
    open fun softRemove(): TreeNodes
    open fun sortBy(sorter: String): TreeNodes
    open fun sortBy(sorter: NodeIteratee): TreeNodes
    open fun state(key: String, `val`: Boolean): TreeNodes
    open fun stateDeep(key: String, `val`: Boolean): TreeNodes
    open fun swap(node1: TreeNode, node2: TreeNode): TreeNodes
    open fun toArray(): Array<Any>
    open fun tree(): InspireTree
    open fun visible(full: Boolean? = definedExternally /* null */): TreeNodes
    open fun nodes(): TreeNodes
}

open external class TreeNode(tree: InspireTree) {
    constructor(tree: InspireTree, source: Any, excludeKeys: Array<String>)
    constructor(tree: InspireTree, source: TreeNode, excludeKeys: Array<String>)
    constructor(tree: InspireTree, source: Any)
    constructor(tree: InspireTree, source: TreeNode)

    open fun addChild(node: NodeConfig): TreeNode
    open fun addChildren(nodes: Array<NodeConfig>): TreeNodes
    open fun assign(vararg sources: Any?): TreeNode
    open fun available(): Boolean
    open fun blur(): TreeNode
    open fun check(shallow: Boolean? = definedExternally /* null */): TreeNode
    open fun checked(): Boolean
    open fun clean(): TreeNode
    open fun clone(excludeKeys: Array<String>? = definedExternally /* null */): TreeNode
    open fun collapse(): TreeNode
    open fun collapsed(): Boolean
    open var text: String
    open var id: String
    open var itree: Any
    open fun context(): TreeNodes
    open fun copy(
        dest: InspireTree,
        hierarchy: Boolean? = definedExternally /* null */,
        includeState: Boolean? = definedExternally /* null */
    ): TreeNode

    open fun copyHierarchy(
        excludeNode: Boolean? = definedExternally /* null */,
        includeState: Boolean? = definedExternally /* null */
    ): TreeNode

    open fun deselect(shallow: Boolean? = definedExternally /* null */): TreeNode
    open fun editable(): Boolean
    open fun editing(): Boolean
    open fun expand(): Promise<TreeNode>
    open fun expanded(): Boolean
    open fun expandParents(): TreeNode
    open fun focus(): TreeNode
    open fun focused(): Boolean
    open fun getChildren(): TreeNodes
    open fun getParent(): TreeNode
    open fun getParents(): TreeNodes
    open fun getTextualHierarchy(): Array<String>
    open fun hasAncestor(): Boolean
    open fun hasChildren(): Boolean
    open fun hasOrWillHaveChildren(): Boolean
    open fun hasParent(): Boolean
    open fun hasVisibleChildren(): Boolean
    open fun hidden(): Boolean
    open fun hide(): TreeNode
    open fun indeterminate(): Boolean
    open fun indexPath(): String
    open fun isFirstRenderable(): Boolean
    open fun isLastRenderable(): Boolean
    open fun lastDeepestVisibleChild(): TreeNode
    open fun loadChildren(): Promise<TreeNodes>
    open fun loading(): Boolean
    open fun markDirty(): TreeNode
    open fun matched(): TreeNodes
    open fun nextVisibleAncestralSiblingNode(): TreeNode
    open fun nextVisibleChildNode(): TreeNode
    open fun nextVisibleNode(): TreeNode
    open fun nextVisibleSiblingNode(): TreeNode
    open fun pagination(): Pagination
    open fun previousVisibleNode(): TreeNode
    open fun previousVisibleSiblingNode(): TreeNode
    open fun recurseDown(iteratee: NodeIteratee): TreeNode
    open fun recurseUp(iteratee: NodeIteratee): TreeNode
    open fun refreshIndeterminateState(): TreeNode
    open fun reload(): Promise<TreeNodes>
    open fun remove(includeState: Boolean? = definedExternally /* null */): Any
    open fun removed(): Boolean
    open fun renderable(): Boolean
    open fun rendered(): Boolean
    open fun restore(): TreeNode
    open fun select(shallow: Boolean? = definedExternally /* null */): TreeNode
    open fun selectable(): Boolean
    open fun selected(): Boolean
    open fun set(key: Number, `val`: Any): TreeNode
    open fun set(key: String, `val`: Any): TreeNode
    open fun show(): TreeNode
    open fun softRemove(): TreeNode
    open fun state(key: Any?, `val`: Boolean? = definedExternally /* null */): dynamic /* Boolean | Any? */
    open fun state(key: String, `val`: Boolean? = definedExternally /* null */): dynamic /* Boolean | Any? */
    open fun states(keys: Array<String>, `val`: Boolean): Boolean
    open fun toggleCheck(): TreeNode
    open fun toggleCollapse(): TreeNode
    open fun toggleEditing(): TreeNode
    open fun toggleSelect(): TreeNode
    open fun toObject(
        excludeChildren: Boolean? = definedExternally /* null */,
        includeState: Boolean? = definedExternally /* null */
    ): Any

    override fun toString(): String
    open fun tree(): InspireTree
    open fun uncheck(shallow: Boolean? = definedExternally /* null */): TreeNode
    open fun visible(): Boolean
}