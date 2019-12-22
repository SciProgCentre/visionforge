package hep.dataforge.vis.spatial.fx

import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.meta.*
import hep.dataforge.output.Renderer
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.World.CAMERA_FAR_CLIP
import hep.dataforge.vis.spatial.World.CAMERA_INITIAL_DISTANCE
import hep.dataforge.vis.spatial.World.CAMERA_INITIAL_X_ANGLE
import hep.dataforge.vis.spatial.World.CAMERA_INITIAL_Y_ANGLE
import hep.dataforge.vis.spatial.World.CAMERA_NEAR_CLIP
import javafx.event.EventHandler
import javafx.scene.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.paint.Color
import org.fxyz3d.scene.Axes
import org.fxyz3d.utils.CameraTransformer
import tornadofx.*

class FXCanvas3D(val plugin: FX3DPlugin, meta: Meta = EmptyMeta) :
    Fragment(), Renderer<VisualObject3D>, ContextAware {

    override val context: Context get() = plugin.context

    val world = Group()

    val axes = Axes().also {
        it.setHeight(meta["axis.size"].double ?: AXIS_LENGTH)
        it.setRadius(meta["axis.width"].double ?: LINE_WIDTH)
        it.isVisible = meta["axis.visible"].boolean ?: (meta["axis"] != null)
        world.add(it)
    }

    val light = AmbientLight()

    private val camera = PerspectiveCamera().apply {
        nearClip = CAMERA_NEAR_CLIP
        farClip = CAMERA_FAR_CLIP
        translateZ = CAMERA_INITIAL_DISTANCE
        this.add(light)
    }

    val cameraTransform = CameraTransformer().also {
        it.add(camera)
    }

    val translationXProperty get() = cameraTransform.t.xProperty()
    var translateX by translationXProperty
    val translationYProperty get() = cameraTransform.t.yProperty()
    var translateY by translationYProperty
    val translationZProperty get() = cameraTransform.t.zProperty()
    var translateZ by translationZProperty

    val rotationXProperty get() = cameraTransform.rx.angleProperty()
    var angleX by rotationXProperty
    val rotationYProperty get() = cameraTransform.ry.angleProperty()
    var angleY by rotationYProperty
    val rotationZProperty get() = cameraTransform.rz.angleProperty()
    var angleZ by rotationZProperty

    private val canvas = SubScene(
        Group(world, cameraTransform).apply { DepthTest.ENABLE },
        400.0,
        400.0,
        true,
        SceneAntialiasing.BALANCED
    ).also { scene ->
        scene.fill = Color.GREY
        scene.camera = camera
        //id = "canvas"
        handleKeyboard(scene)
        handleMouse(scene)
    }

    override val root = borderpane {
        center = canvas
    }

    init {
        canvas.widthProperty().bind(root.widthProperty())
        canvas.heightProperty().bind(root.heightProperty())
    }


    private fun handleKeyboard(scene: SubScene) {
        scene.onKeyPressed = EventHandler<KeyEvent> { event ->
            if (event.isControlDown) {
                when (event.code) {
                    KeyCode.Z -> {
                        translateX = 0.0
                        translateY = 0.0
                        camera.translateZ = CAMERA_INITIAL_DISTANCE
                        angleY = CAMERA_INITIAL_Y_ANGLE
                        angleX = CAMERA_INITIAL_X_ANGLE
                    }
                    KeyCode.X -> axes.isVisible = !axes.isVisible
//                    KeyCode.S -> snapshot()
//                    KeyCode.DIGIT1 -> pixelMap.filterKeys { it.getLayerNumber() == 1 }.values.forEach {
//                        toggleTransparency(
//                            it
//                        )
//                    }
//                    KeyCode.DIGIT2 -> pixelMap.filterKeys { it.getLayerNumber() == 2 }.values.forEach {
//                        toggleTransparency(
//                            it
//                        )
//                    }
//                    KeyCode.DIGIT3 -> pixelMap.filterKeys { it.getLayerNumber() == 3 }.values.forEach {
//                        toggleTransparency(
//                            it
//                        )
//                    }
                    else -> {
                    }//do nothing
                }
            }
        }
    }

    private fun handleMouse(scene: SubScene) {

        var mousePosX: Double = 0.0
        var mousePosY: Double = 0.0
        var mouseOldX: Double = 0.0
        var mouseOldY: Double = 0.0
        var mouseDeltaX: Double = 0.0
        var mouseDeltaY: Double = 0.0

        scene.onMousePressed = EventHandler<MouseEvent> { me ->
            mousePosX = me.sceneX
            mousePosY = me.sceneY
            mouseOldX = me.sceneX
            mouseOldY = me.sceneY
        }

        scene.onMouseDragged = EventHandler<MouseEvent> { me ->
            mouseOldX = mousePosX
            mouseOldY = mousePosY
            mousePosX = me.sceneX
            mousePosY = me.sceneY
            mouseDeltaX = mousePosX - mouseOldX
            mouseDeltaY = mousePosY - mouseOldY

            val modifier = when {
                me.isControlDown -> CONTROL_MULTIPLIER
                me.isShiftDown -> SHIFT_MULTIPLIER
                else -> 1.0
            }

            if (me.isPrimaryButtonDown) {
                angleY += mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED
                angleX += mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED
            } else if (me.isSecondaryButtonDown) {
                translateX -= mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED
                translateY -= mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED
            }
        }
        scene.onScroll = EventHandler<ScrollEvent> { event ->
            val z = camera.translateZ
            val newZ = z + MOUSE_SPEED * event.deltaY * RESIZE_SPEED
            camera.translateZ = newZ
        }
    }

    override fun render(obj: VisualObject3D, meta: Meta) {
        val node = plugin.buildNode(obj) ?: kotlin.error("Can't render FX node for object $obj")
        world.children.add(node)
    }

    companion object {
        private const val AXIS_LENGTH = 400.0
        private const val CONTROL_MULTIPLIER = 0.1
        private const val SHIFT_MULTIPLIER = 10.0
        private const val MOUSE_SPEED = 0.1
        private const val ROTATION_SPEED = 2.0
        private const val TRACK_SPEED = 6.0
        private const val RESIZE_SPEED = 50.0
        private const val LINE_WIDTH = 1.0
    }
}