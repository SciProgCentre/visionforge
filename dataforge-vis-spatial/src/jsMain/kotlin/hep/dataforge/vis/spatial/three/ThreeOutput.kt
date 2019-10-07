package hep.dataforge.vis.spatial.three

import hep.dataforge.context.Context
import hep.dataforge.meta.*
import hep.dataforge.output.Output
import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.spatial.VisualObject3D
import info.laht.threekt.WebGLRenderer
import info.laht.threekt.helpers.AxesHelper
import info.laht.threekt.lights.AmbientLight
import info.laht.threekt.scenes.Scene
import org.w3c.dom.HTMLElement
import kotlin.browser.window
import kotlin.dom.clear
import kotlin.math.max

class ThreeOutput(val three: ThreePlugin, val meta: Meta = EmptyMeta) : Output<VisualObject3D> {

    override val context: Context get() = three.context

    val axes = AxesHelper(meta["axes.size"].int ?: 50).apply { visible = false }

    val scene: Scene = Scene().apply {
        add(AmbientLight())
        if (meta["axes.visible"].boolean == true) {
            axes.visible = true
        }
        add(axes)
    }

    val camera = three.buildCamera(meta["camera"].node ?: EmptyMeta)

    fun attach(element: HTMLElement) {
        element.clear()

        camera.aspect = 1.0

        val renderer = WebGLRenderer { antialias = true }.apply {
            setClearColor(Colors.skyblue, 1)

        }

        three.addControls(camera, renderer.domElement, meta["controls"].node ?: EmptyMeta)

        fun animate() {
            window.requestAnimationFrame {
                animate()
            }
            renderer.render(scene, camera)
        }

        element.appendChild(renderer.domElement)

        val minSize by meta.number(0).int

        renderer.setSize(max(minSize, element.offsetWidth), max(minSize, element.offsetWidth))

        element.onresize = {
            renderer.setSize(element.offsetWidth, element.offsetWidth)
            camera.updateProjectionMatrix()
        }

        animate()
    }

    override fun render(obj: VisualObject3D, meta: Meta) {
        scene.add(three.buildObject3D(obj))
    }
}

fun ThreePlugin.output(element: HTMLElement? = null, meta: Meta = EmptyMeta, override: MetaBuilder.() -> Unit = {}) =
    ThreeOutput(this, buildMeta(meta, override)).apply {
        if (element != null) {
            attach(element)
        }
    }