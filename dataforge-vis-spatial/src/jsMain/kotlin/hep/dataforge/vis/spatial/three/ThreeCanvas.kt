package hep.dataforge.vis.spatial.three

import hep.dataforge.context.Context
import hep.dataforge.meta.Meta
import hep.dataforge.meta.get
import hep.dataforge.meta.string
import hep.dataforge.output.Renderer
import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.spatial.Point3D
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.specifications.AxesSpec
import hep.dataforge.vis.spatial.specifications.CameraSpec
import hep.dataforge.vis.spatial.specifications.CanvasSpec
import hep.dataforge.vis.spatial.specifications.ControlsSpec
import info.laht.threekt.WebGLRenderer
import info.laht.threekt.cameras.PerspectiveCamera
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.external.controls.TrackballControls
import info.laht.threekt.helpers.AxesHelper
import info.laht.threekt.scenes.Scene
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import kotlin.browser.window
import kotlin.dom.clear
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class ThreeCanvas(val three: ThreePlugin, val spec: CanvasSpec) : Renderer<VisualObject3D> {

    override val context: Context get() = three.context

    var content: VisualObject3D? = null
        private set

    val axes = AxesHelper(spec.axes.size.toInt()).apply {
        visible = spec.axes.visible
    }

    val scene: Scene = Scene().apply {
        add(axes)
    }

    val camera = buildCamera(spec.camera)

    private fun buildCamera(spec: CameraSpec) = PerspectiveCamera(
        spec.fov,
        1.0,
        spec.nearClip,
        spec.farClip
    ).apply {
        translateX(spec.distance* sin(spec.zenith) * sin(spec.azimuth))
        translateY(spec.distance* cos(spec.zenith))
        translateZ(spec.distance * sin(spec.zenith) * cos(spec.azimuth))
    }

    private fun addControls(element: Node, controlsSpec: ControlsSpec) {
        when (controlsSpec["type"].string) {
            "trackball" -> TrackballControls(camera, element)
            else -> OrbitControls(camera, element)
        }
    }

    fun attach(element: HTMLElement) {
        element.clear()

        camera.aspect = 1.0

        val renderer = WebGLRenderer { antialias = true }.apply {
            setClearColor(Colors.skyblue, 1)

        }

        addControls(renderer.domElement, spec.controls ?: ControlsSpec.empty())

        fun animate() {
            window.requestAnimationFrame {
                animate()
            }
            renderer.render(scene, camera)
        }

        element.appendChild(renderer.domElement)

        renderer.setSize(max(spec.minSize, element.offsetWidth), max(spec.minSize, element.offsetWidth))

        element.onresize = {
            renderer.setSize(element.offsetWidth, element.offsetWidth)
            camera.updateProjectionMatrix()
        }

        animate()
    }

    override fun render(obj: VisualObject3D, meta: Meta) {
        content = obj
        scene.add(three.buildObject3D(obj))
    }
}

fun ThreePlugin.output(element: HTMLElement? = null, spec: CanvasSpec = CanvasSpec.empty()): ThreeCanvas =
    ThreeCanvas(this, spec).apply {
        if (element != null) {
            attach(element)
        }
    }