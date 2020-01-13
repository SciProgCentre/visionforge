@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION",
    "NESTED_CLASS_IN_EXTERNAL_INTERFACE"
)
@file:JsModule("three/examples/jsm/controls/TrackballControls.js")
@file:JsNonModule

package info.laht.threekt.external.controls

import info.laht.threekt.cameras.Camera
import info.laht.threekt.core.EventDispatcher
import info.laht.threekt.math.Vector3
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

external interface `T$0` {
    var left: Number
    var top: Number
    var width: Number
    var height: Number
}

external open class TrackballControls(
    `object`: Camera,
    domElement: Node = definedExternally /* null */
) : EventDispatcher {
    open var `object`: Camera
    open var domElement: HTMLElement
    open var enabled: Boolean
    open var screen: `T$0`
    open var rotateSpeed: Number
    open var zoomSpeed: Number
    open var panSpeed: Number
    open var noRotate: Boolean
    open var noZoom: Boolean
    open var noPan: Boolean
    open var noRoll: Boolean
    open var staticMoving: Boolean
    open var dynamicDampingFactor: Number
    open var minDistance: Number
    open var maxDistance: Number
    open var keys: Array<Number>
    open var target: Vector3
    open var position0: Vector3
    open var target0: Vector3
    open var up0: Vector3
    open fun update(): Unit
    open fun reset(): Unit
    open fun dispose(): Unit
    open fun checkDistances(): Unit
    open fun zoomCamera(): Unit
    open fun panCamera(): Unit
    open fun rotateCamera(): Unit
    open fun handleResize(): Unit
    open fun handleEvent(event: Any): Unit
}