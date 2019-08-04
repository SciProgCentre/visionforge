package hep.dataforge.vis.spatial.three

import hep.dataforge.context.Context
import hep.dataforge.meta.*
import hep.dataforge.output.Output
import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.hmr.require
import hep.dataforge.vis.spatial.VisualObject3D
import info.laht.threekt.WebGLRenderer
import info.laht.threekt.helpers.AxesHelper
import info.laht.threekt.lights.AmbientLight
import info.laht.threekt.scenes.Scene
import org.w3c.dom.Element
import kotlin.browser.window

private val elementResizeEvent = require("element-resize-event")

class ThreeOutput(val three: ThreePlugin, val meta: Meta = EmptyMeta) : Output<VisualObject3D> {

    override val context: Context get() = three.context

    val scene: Scene = Scene().apply {
        add(AmbientLight())
        if (meta["axis"] != null) {
            val axesHelper = AxesHelper(meta["axis.size"].int ?: 1)
            add(axesHelper)
        }
    }

    private val camera = three.buildCamera(meta["camera"].node ?: EmptyMeta)

    fun attach(element: Element, computeWidth: Element.() -> Int = { element.clientWidth }) {
        val width by meta.number(computeWidth(element)).int

        val height: Int = (width / camera.aspect).toInt()

        val renderer = WebGLRenderer { antialias = true }.apply {
            setClearColor(Colors.skyblue, 1)
            setSize(width, height)
        }

        three.addControls(camera, renderer.domElement, meta["controls"].node ?: EmptyMeta)

        fun animate() {
            window.requestAnimationFrame {
                animate()
            }
            renderer.render(scene, camera)
        }

        elementResizeEvent(element) {
            camera.updateProjectionMatrix()
            val newWidth = computeWidth(element)
            renderer.setSize(newWidth, (newWidth / camera.aspect).toInt())
        }

        element.replaceWith(renderer.domElement)
        animate()
    }

    override fun render(obj: VisualObject3D, meta: Meta) {
        scene.add(three.buildObject3D(obj))
    }
}

fun ThreePlugin.output(element: Element? = null, meta: Meta = EmptyMeta, override: MetaBuilder.() -> Unit = {}) =
    ThreeOutput(this, buildMeta(meta, override)).apply {
        if(element!=null){
            attach(element)
        }
    }