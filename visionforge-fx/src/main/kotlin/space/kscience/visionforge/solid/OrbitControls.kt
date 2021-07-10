package space.kscience.visionforge.solid

import javafx.beans.InvalidationListener
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.event.EventHandler
import javafx.geometry.Point3D
import javafx.scene.Camera
import javafx.scene.Node
import javafx.scene.SubScene
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.shape.Sphere
import javafx.scene.transform.Rotate
import javafx.scene.transform.Translate
import tornadofx.*
import kotlin.math.*
import space.kscience.visionforge.solid.specifications.Camera as CameraSpec


public class OrbitControls internal constructor(camera: Camera, canvas: SubScene, spec: CameraSpec) {

    public val distanceProperty: SimpleDoubleProperty = SimpleDoubleProperty(spec.distance)
    public var distance: Double by distanceProperty

    public val azimuthProperty: SimpleDoubleProperty = SimpleDoubleProperty(spec.azimuth)
    public var azimuth: Double by azimuthProperty

    public val zenithProperty: SimpleDoubleProperty = SimpleDoubleProperty(PI / 2 - spec.latitude)
    public var zenith: Double by zenithProperty
//
//    public val latitudeProperty: DoubleBinding = zenithProperty.unaryMinus().plus(PI / 2)
//    public val latitude by latitudeProperty

    public val baseXProperty: SimpleDoubleProperty = SimpleDoubleProperty(0.0)
    public var x: Double by baseXProperty
    public val baseYProperty: SimpleDoubleProperty = SimpleDoubleProperty(0.0)
    public var y: Double by baseYProperty
    public val baseZProperty: SimpleDoubleProperty = SimpleDoubleProperty(0.0)
    public var z: Double by baseZProperty

    private val baseTranslate = Translate()

//    val basePositionProperty: ObjectBinding<Point3D> =
//        nonNullObjectBinding(baseXProperty, baseYProperty, baseZProperty) {
//            Point3D(x, y, z)
//        }
//
//    val basePosition by basePositionProperty

    private val inProgressProperty = SimpleBooleanProperty(false)

    public val centerMarker: Node by lazy {
        Sphere(10.0).also {
            it.transforms.setAll(baseTranslate)
            it.visibleProperty().bind(inProgressProperty)
        }
    }


    private val rx = Rotate(0.0, Rotate.X_AXIS)

    private val ry = Rotate(0.0, Rotate.Y_AXIS)

    private val rz = Rotate(0.0, Rotate.Z_AXIS)

    private val translate = Translate()


    init {
        camera.transforms.setAll(rx, ry, rz, baseTranslate, translate)
        update()
        val listener = InvalidationListener {
            update()
        }
        distanceProperty.addListener(listener)
        azimuthProperty.addListener(listener)
        zenithProperty.addListener(listener)
        baseXProperty.addListener(listener)
        baseYProperty.addListener(listener)
        baseZProperty.addListener(listener)

        canvas.apply {
//            center.xProperty().bind(widthProperty().divide(2))
//            center.zProperty().bind(heightProperty().divide(2))
            handleMouse()
        }
//        coordinateContainer?.vbox {
//            label(distanceProperty.asString())
//            label(azimuthProperty.asString())
//            label(zenithProperty.asString())
//        }
    }

    private fun update() {
        val spherePosition = Point3D(
            sin(zenith) * sin(azimuth),
            cos(zenith),
            sin(zenith) * cos(azimuth)
        ).times(distance)
        val basePosition = Point3D(x, y, z)
        baseTranslate.x = x
        baseTranslate.y = y
        baseTranslate.z = z
        //Create direction vector
        val cameraPosition = basePosition + spherePosition
        val camDirection: Point3D = (-spherePosition).normalize()

        val xRotation = Math.toDegrees(asin(-camDirection.y))
        val yRotation = Math.toDegrees(atan2(camDirection.x, camDirection.z))

        rx.pivotX = cameraPosition.x
        rx.pivotY = cameraPosition.y
        rx.pivotZ = cameraPosition.z
        rx.angle = xRotation

        ry.pivotX = cameraPosition.x
        ry.pivotY = cameraPosition.y
        ry.pivotZ = cameraPosition.z
        ry.angle = yRotation

        translate.x = cameraPosition.x
        translate.y = cameraPosition.y
        translate.z = cameraPosition.z
    }


    private fun Node.handleMouse() {

        var mousePosX = 0.0
        var mousePosY = 0.0
        var mouseOldX: Double
        var mouseOldY: Double
        var mouseDeltaX: Double
        var mouseDeltaY: Double

        onMousePressed = EventHandler<MouseEvent> { me ->
            mousePosX = me.sceneX
            mousePosY = me.sceneY
            mouseOldX = me.sceneX
            mouseOldY = me.sceneY
            inProgressProperty.set(true)
        }

        onMouseDragged = EventHandler<MouseEvent> { me ->
            mouseOldX = mousePosX
            mouseOldY = mousePosY
            mousePosX = me.sceneX
            mousePosY = me.sceneY
            mouseDeltaX = mouseOldX - mousePosX
            mouseDeltaY = mouseOldY - mousePosY

            val modifier = when {
                me.isControlDown -> CONTROL_MULTIPLIER
                me.isShiftDown -> SHIFT_MULTIPLIER
                else -> 1.0
            }

            if (me.isPrimaryButtonDown) {
                azimuth = (azimuth - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED).coerceIn(0.0, 2 * PI)
                zenith = (zenith - mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED).coerceIn(0.0, PI)
            } else if (me.isSecondaryButtonDown) {
                x += MOUSE_SPEED * modifier * TRACK_SPEED * (mouseDeltaX * cos(azimuth) + mouseDeltaY * sin(azimuth))
                z += MOUSE_SPEED * modifier * TRACK_SPEED * (-mouseDeltaX * sin(azimuth) + mouseDeltaY * cos(azimuth))
            }
        }

        onMouseReleased = EventHandler {
            inProgressProperty.set(false)
        }

        onScroll = EventHandler<ScrollEvent> { event ->
            distance = max(1.0, distance - MOUSE_SPEED * event.deltaY * RESIZE_SPEED)
        }
    }

    public companion object {
        private const val CONTROL_MULTIPLIER = 0.1
        private const val SHIFT_MULTIPLIER = 10.0
        private const val MOUSE_SPEED = 0.1
        private const val ROTATION_SPEED = 0.02
        private const val TRACK_SPEED = 20.0
        private const val RESIZE_SPEED = 10.0
    }
}

public fun Camera.orbitControls(canvas: SubScene, spec: CameraSpec): OrbitControls =
    OrbitControls(this, canvas, spec)