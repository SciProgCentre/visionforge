@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION"
)
@file:JsModule("eventemitter2")
@file: JsNonModule

package hep.dataforge.vis.spatial.tree

import kotlin.js.Promise

//typealias eventNS = Array<String>

external interface ConstructorOptions {
    var wildcard: Boolean? get() = definedExternally; set(value) = definedExternally
    var delimiter: String? get() = definedExternally; set(value) = definedExternally
    var newListener: Boolean? get() = definedExternally; set(value) = definedExternally
    var maxListeners: Number? get() = definedExternally; set(value) = definedExternally
    var verboseMemoryLeak: Boolean? get() = definedExternally; set(value) = definedExternally
}

external interface EventAndListener {
    @nativeInvoke
    operator fun invoke(event: String, vararg values: Any)

    @nativeInvoke
    operator fun invoke(event: Array<String>, vararg values: Any)
}

external open class EventEmitter2(options: ConstructorOptions? = definedExternally /* null */) {
    open fun emit(event: String, vararg values: Any): Boolean
    open fun emit(event: Array<String>, vararg values: Any): Boolean
    open fun emitAsync(event: String, vararg values: Any): Promise<Array<Any>>
    open fun emitAsync(event: Array<String>, vararg values: Any): Promise<Array<Any>>
    open fun addListener(event: String, listener: Function<*>): EventEmitter2 /* this */
    open fun on(event: String, listener: Function<*>): EventEmitter2 /* this */
    open fun on(event: Array<String>, listener: Function<*>): EventEmitter2 /* this */
    open fun prependListener(event: String, listener: Function<*>): EventEmitter2 /* this */
    open fun prependListener(event: Array<String>, listener: Function<*>): EventEmitter2 /* this */
    open fun once(event: String, listener: Function<*>): EventEmitter2 /* this */
    open fun once(event: Array<String>, listener: Function<*>): EventEmitter2 /* this */
    open fun prependOnceListener(event: String, listener: Function<*>): EventEmitter2 /* this */
    open fun prependOnceListener(event: Array<String>, listener: Function<*>): EventEmitter2 /* this */
    open fun many(event: String, timesToListen: Number, listener: Function<*>): EventEmitter2 /* this */
    open fun many(event: Array<String>, timesToListen: Number, listener: Function<*>): EventEmitter2 /* this */
    open fun prependMany(event: String, timesToListen: Number, listener: Function<*>): EventEmitter2 /* this */
    open fun prependMany(event: Array<String>, timesToListen: Number, listener: Function<*>): EventEmitter2 /* this */
    open fun onAny(listener: EventAndListener): EventEmitter2 /* this */
    open fun prependAny(listener: EventAndListener): EventEmitter2 /* this */
    open fun offAny(listener: Function<*>): EventEmitter2 /* this */
    open fun removeListener(event: String, listener: Function<*>): EventEmitter2 /* this */
    open fun removeListener(event: Array<String>, listener: Function<*>): EventEmitter2 /* this */
    open fun off(event: String, listener: Function<*>): EventEmitter2 /* this */
    open fun removeAllListeners(event: String? = definedExternally /* null */): EventEmitter2 /* this */
    open fun removeAllListeners(event: Array<String>? = definedExternally /* null */): EventEmitter2 /* this */
    open fun setMaxListeners(n: Number)
    open fun eventNames(): Array<String>
    open fun listeners(event: String): Array<Function<*>>
    open fun listeners(event: Array<String>): Array<Function<*>>
    open fun listenersAny(): Array<Function<*>>
    open fun removeAllListeners(): EventEmitter2 /* this */
}