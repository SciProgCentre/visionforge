package hep.dataforge.vis.spatial.fx

import hep.dataforge.vis.spatial.World.CAMERA_FAR_CLIP
import hep.dataforge.vis.spatial.World.CAMERA_INITIAL_DISTANCE
import hep.dataforge.vis.spatial.World.CAMERA_INITIAL_X_ANGLE
import hep.dataforge.vis.spatial.World.CAMERA_INITIAL_Y_ANGLE
import hep.dataforge.vis.spatial.World.CAMERA_INITIAL_Z_ANGLE
import hep.dataforge.vis.spatial.World.CAMERA_NEAR_CLIP
import javafx.event.EventHandler
import javafx.scene.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.paint.Color
import org.fxyz3d.utils.CameraTransformer
import tornadofx.*

class Canvas3D : Fragment() {
    val world: Group = Group()

    private val camera = PerspectiveCamera().apply {
        nearClip = CAMERA_NEAR_CLIP
        farClip = CAMERA_FAR_CLIP
        translateZ = CAMERA_INITIAL_DISTANCE
    }

    private val cameraShift = CameraTransformer().apply {
        val cameraFlip = CameraTransformer()
        cameraFlip.children.add(camera)
        cameraFlip.setRotateZ(180.0)
        children.add(cameraFlip)
    }

    val translationXProperty get() = cameraShift.t.xProperty()
    var translateX by translationXProperty
    val translationYProperty get() = cameraShift.t.yProperty()
    var translateY by translationYProperty
    val translationZProperty get() = cameraShift.t.zProperty()
    var translateZ by translationZProperty

    private val cameraRotation = CameraTransformer().apply {
        children.add(cameraShift)
        ry.angle = CAMERA_INITIAL_Y_ANGLE
        rx.angle = CAMERA_INITIAL_X_ANGLE
        rz.angle = CAMERA_INITIAL_Z_ANGLE
    }

    val rotationXProperty get() = cameraRotation.rx.angleProperty()
    var angleX by rotationXProperty
    val rotationYProperty get() = cameraRotation.ry.angleProperty()
    var angleY by rotationYProperty
    val rotationZProperty get() = cameraRotation.rz.angleProperty()
    var angleZ by rotationZProperty


    override val root = borderpane {
        center = SubScene(
            Group(world, cameraRotation).apply { DepthTest.ENABLE },
            1024.0,
            768.0,
            true,
            SceneAntialiasing.BALANCED
        ).apply {
            fill = Color.GREY
            this.camera = this@Canvas3D.camera
            id = "canvas"
            handleKeyboard(this)
            handleMouse(this)
        }
    }


    private fun handleKeyboard(scene: SubScene) {
        scene.onKeyPressed = EventHandler<KeyEvent> { event ->
            if (event.isControlDown) {
                when (event.code) {
                    KeyCode.Z -> {
                        cameraShift.t.x = 0.0
                        cameraShift.t.y = 0.0
                        camera.translateZ = CAMERA_INITIAL_DISTANCE
                        cameraRotation.ry.angle = CAMERA_INITIAL_Y_ANGLE
                        cameraRotation.rx.angle = CAMERA_INITIAL_X_ANGLE
                    }
//                    KeyCode.X -> axisGroup.isVisible = !axisGroup.isVisible
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
                cameraRotation.rz.angle =
                    cameraRotation.rz.angle + mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED
                cameraRotation.rx.angle =
                    cameraRotation.rx.angle + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED
            } else if (me.isSecondaryButtonDown) {
                cameraShift.t.x = cameraShift.t.x + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED
                cameraShift.t.y = cameraShift.t.y + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED
            }
        }
        scene.onScroll = EventHandler<ScrollEvent> { event ->
            val z = camera.translateZ
            val newZ = z + MOUSE_SPEED * event.deltaY * RESIZE_SPEED
            camera.translateZ = newZ
        }
    }

    companion object {
        private const val AXIS_LENGTH = 2000.0
        private const val CONTROL_MULTIPLIER = 0.1
        private const val SHIFT_MULTIPLIER = 10.0
        private const val MOUSE_SPEED = 0.1
        private const val ROTATION_SPEED = 2.0
        private const val TRACK_SPEED = 6.0
        private const val RESIZE_SPEED = 50.0
        private const val LINE_WIDTH = 3.0
    }
}