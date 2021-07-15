package space.kscience.visionforge.solid

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.event.EventHandler
import javafx.scene.Camera
import javafx.scene.Node
import javafx.scene.SubScene
import javafx.scene.shape.Sphere
import javafx.scene.transform.Rotate
import javafx.scene.transform.Rotate.X_AXIS
import javafx.scene.transform.Rotate.Y_AXIS
import javafx.scene.transform.Translate
import space.kscience.dataforge.meta.useProperty
import tornadofx.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import space.kscience.visionforge.solid.specifications.Camera as CameraSpec

public class OrbitControls internal constructor(camera: Camera, canvas: SubScene, spec: CameraSpec) {

    /**
     * Azimuth angle in radians
     */
    public val azimuthProperty: SimpleDoubleProperty = SimpleDoubleProperty().apply {
        spec.useProperty(CameraSpec::azimuth){
            set(spec.azimuth)
        }
    }
    public var azimuth: Double by azimuthProperty

    /**
     * Zenith angle in radians
     */
    public val zenithProperty: SimpleDoubleProperty = SimpleDoubleProperty().apply {
        spec.useProperty(CameraSpec::latitude){
            set(PI / 2 - spec.latitude)
        }
    }

    public var zenith: Double by zenithProperty


    private val baseTranslate = Translate(0.0, 0.0, 0.0)

    public var x: Double by baseTranslate.xProperty()
    public var y: Double by baseTranslate.yProperty()
    public var z: Double by baseTranslate.zProperty()

    private val distanceProperty = SimpleDoubleProperty().apply {
        spec.useProperty(CameraSpec::distance) {
            set(it)
        }
    }

    private val distanceTranslation = Translate().apply {
        zProperty().bind(-distanceProperty)
    }

    public var distance: Double by distanceProperty

    private val centering = Translate().apply {
        xProperty().bind(-canvas.widthProperty() / 2)
        yProperty().bind(-canvas.heightProperty() / 2)
    }

    private val yUpRotation = Rotate(180.0, X_AXIS)

    private val azimuthRotation = Rotate().apply {
        axis = Y_AXIS
        angleProperty().bind(azimuthProperty * (180.0 / PI))
    }

    private val zenithRotation = Rotate().apply {
        axisProperty().bind(objectBinding(azimuthProperty) {
            azimuthRotation.inverseTransform(X_AXIS)
        })
        angleProperty().bind(-zenithProperty * (180.0 / PI))
    }

    private val inProgressProperty = SimpleBooleanProperty(false)


    public val centerMarker: Node by lazy {
        Sphere(10.0).also {
            it.transforms.setAll(baseTranslate)
            it.visibleProperty().bind(inProgressProperty)
        }
    }

    init {
        camera.transforms.setAll(
            baseTranslate,
            yUpRotation,
            azimuthRotation,
            zenithRotation,
            distanceTranslation,
            centering,
        )

        canvas.apply {
            handleMouse()
        }
    }

    private fun Node.handleMouse() {

        var mousePosX = 0.0
        var mousePosY = 0.0
        var mouseOldX: Double
        var mouseOldY: Double
        var mouseDeltaX: Double
        var mouseDeltaY: Double

        onMousePressed = EventHandler { me ->
            mousePosX = me.sceneX
            mousePosY = me.sceneY
            mouseOldX = me.sceneX
            mouseOldY = me.sceneY
            inProgressProperty.set(true)
        }

        onMouseDragged = EventHandler { me ->
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
                azimuth = (azimuth - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED)
                zenith = (zenith - mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED).coerceIn(-PI/2, PI/2)
            } else if (me.isSecondaryButtonDown) {
                x += MOUSE_SPEED * modifier * TRACK_SPEED * (mouseDeltaX * cos(azimuth) - mouseDeltaY * sin(azimuth))
                z += MOUSE_SPEED * modifier * TRACK_SPEED * ( mouseDeltaX * sin(azimuth) + mouseDeltaY * cos(azimuth))
            }
        }

        onMouseReleased = EventHandler {
            inProgressProperty.set(false)
        }

        onScroll = EventHandler { event ->
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
