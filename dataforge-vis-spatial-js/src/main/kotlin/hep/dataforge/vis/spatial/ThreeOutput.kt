package hep.dataforge.vis.spatial

import hep.dataforge.context.Context
import hep.dataforge.context.content
import hep.dataforge.meta.*
import hep.dataforge.output.Output
import hep.dataforge.vis.common.DisplayGroup
import hep.dataforge.vis.common.DisplayObject
import hep.dataforge.vis.spatial.three.Group
import info.laht.threekt.WebGLRenderer
import info.laht.threekt.cameras.PerspectiveCamera
import info.laht.threekt.core.Object3D
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.helpers.AxesHelper
import info.laht.threekt.lights.AmbientLight
import info.laht.threekt.math.ColorConstants
import info.laht.threekt.scenes.Scene
import org.w3c.dom.Element
import kotlin.browser.window

class ThreeOutput(override val context: Context, val meta: Meta = EmptyMeta) : Output<DisplayObject> {

    private val aspectRatio by meta.number(1.0).double

    val scene: Scene = Scene().apply {
        add(AmbientLight())
        if (meta["axis"] != null) {
            val axesHelper = AxesHelper(meta["axis.size"].int ?: 1)
            add(axesHelper)
        }
    }

    val camera = PerspectiveCamera(
        meta["camera.fov"].int ?: 75,
        aspectRatio,
        meta["camera.nearClip"].double ?: World.CAMERA_NEAR_CLIP,
        meta["camera.farClip"].double ?: World.CAMERA_FAR_CLIP
    ).apply {
        position.setZ(World.CAMERA_INITIAL_DISTANCE)
        rotation.set(World.CAMERA_INITIAL_X_ANGLE, World.CAMERA_INITIAL_Y_ANGLE, World.CAMERA_INITIAL_Z_ANGLE)
    }

    fun attach(element: Element, computeWidth: Element.() -> Int = { element.clientWidth }) {

        val width by meta.number(computeWidth(element)).int

        val height: Int = (width / aspectRatio).toInt()

        val renderer = WebGLRenderer { antialias = true }.apply {
            setClearColor(ColorConstants.skyblue, 1)
            setSize(width, height)
        }

        val controls: OrbitControls = OrbitControls(camera, renderer.domElement)

        fun animate() {
            window.requestAnimationFrame {
                animate()
            }
            renderer.render(scene, camera)
        }

        window.addEventListener("resize", {
            camera.updateProjectionMatrix()

            val width by meta.number(computeWidth(element)).int

            renderer.setSize(width, (width / aspectRatio).toInt())
        }, false)

        element.replaceWith(renderer.domElement)
        animate()
    }

    private fun buildNode(obj: DisplayObject): Object3D? {
        return if (obj is DisplayGroup) Group(obj.mapNotNull { buildNode(it) }).apply {
            ThreeFactory.updatePosition(obj, this)
        } else {
            val factory = context.content<ThreeFactory<*>>(ThreeFactory.TYPE).values.find { it.type == obj::class }
            when {
                factory != null -> factory(obj)
                obj is Shape -> ThreeShapeFactory(obj)
                else -> error("Renderer for ${obj::class} not found")
            }
        }
    }

    override fun render(obj: DisplayObject, meta: Meta) {
        buildNode(obj)?.let {
            scene.add(it)
        } ?: error("Renderer for ${obj::class} not found")
    }


    companion object {
        fun build(context: Context, meta: Meta = EmptyMeta, override: MetaBuilder.() -> Unit) =
            ThreeOutput(context, buildMeta(meta,override))
    }
}