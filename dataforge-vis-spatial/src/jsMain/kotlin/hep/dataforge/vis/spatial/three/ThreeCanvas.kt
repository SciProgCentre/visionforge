package hep.dataforge.vis.spatial.three

import hep.dataforge.context.Context
import hep.dataforge.meta.Meta
import hep.dataforge.meta.getProperty
import hep.dataforge.meta.string
import hep.dataforge.names.Name
import hep.dataforge.names.plus
import hep.dataforge.names.toName
import hep.dataforge.output.Renderer
import hep.dataforge.vis.Colors
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.specifications.Camera
import hep.dataforge.vis.spatial.specifications.Canvas
import hep.dataforge.vis.spatial.specifications.Controls
import hep.dataforge.vis.spatial.three.ThreeMaterials.HIGHLIGHT_MATERIAL
import hep.dataforge.vis.spatial.three.ThreeMaterials.SELECTED_MATERIAL
import info.laht.threekt.WebGLRenderer
import info.laht.threekt.cameras.PerspectiveCamera
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.core.Raycaster
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.external.controls.TrackballControls
import info.laht.threekt.geometries.EdgesGeometry
import info.laht.threekt.helpers.AxesHelper
import info.laht.threekt.materials.LineBasicMaterial
import info.laht.threekt.math.Vector2
import info.laht.threekt.objects.LineSegments
import info.laht.threekt.objects.Mesh
import info.laht.threekt.scenes.Scene
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.events.MouseEvent
import kotlin.browser.window
import kotlin.dom.clear
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 *
 */
class ThreeCanvas(element: HTMLElement, val three: ThreePlugin, val canvas: Canvas) : Renderer<VisualObject3D> {

    override val context: Context get() = three.context

    var content: VisualObject3D? = null
        private set

    private var root: Object3D? = null

    private val raycaster = Raycaster()
    private val mousePosition: Vector2 = Vector2()

    var onClick: ((Name?) -> Unit)? = null

    val axes = AxesHelper(canvas.axes.size.toInt()).apply {
        visible = canvas.axes.visible
    }

    val scene: Scene = Scene().apply {
        add(axes)
    }

    val camera = buildCamera(canvas.camera)

    private var picked: Object3D? = null

    init {
        element.clear()

        //Attach listener to track mouse changes
        element.addEventListener("mousemove", { event ->
            (event as? MouseEvent)?.run {
                val rect = element.getBoundingClientRect()
                mousePosition.x = ((event.clientX - rect.left) / element.clientWidth) * 2 - 1
                mousePosition.y = -((event.clientY - rect.top) / element.clientHeight) * 2 + 1
            }
        }, false)

        element.addEventListener("mousedown", {
            val picked = pick()
            onClick?.invoke(picked?.fullName())
        }, false)

        camera.aspect = 1.0

        val renderer = WebGLRenderer { antialias = true }.apply {
            setClearColor(Colors.skyblue, 1)
        }

        addControls(renderer.domElement, canvas.controls)

        fun animate() {
            val picked = pick()

            if (picked != null && this.picked != picked) {
                this.picked?.toggleHighlight(false,HIGHLIGHT_NAME, HIGHLIGHT_MATERIAL)
                picked.toggleHighlight(true, HIGHLIGHT_NAME, HIGHLIGHT_MATERIAL)
                this.picked = picked
            }

            window.requestAnimationFrame {
                animate()
            }
            renderer.render(scene, camera)
        }

        element.appendChild(renderer.domElement)

        renderer.setSize(max(canvas.minSize, element.offsetWidth), max(canvas.minSize, element.offsetWidth))

        element.onresize = {
            renderer.setSize(element.offsetWidth, element.offsetWidth)
            camera.updateProjectionMatrix()
        }

        animate()
    }

    /**
     * Resolve full name of the object relative to the global root
     */
    private fun Object3D.fullName(): Name {
        if (root == null) error("Can't resolve element name without the root")
        return if (parent == root) {
            name.toName()
        } else {
            (parent?.fullName() ?: Name.EMPTY) + name.toName()
        }
    }

    private fun Object3D.isStatic(): Boolean {
        return false
    }

    private fun Object3D?.upTrace(): Object3D? = if (this?.name?.startsWith("@") == true) parent else this

    private fun pick(): Object3D? {
        // update the picking ray with the camera and mouse position
        raycaster.setFromCamera(mousePosition, camera)

        // calculate objects intersecting the picking ray
        return root?.let { root ->
            val intersects = raycaster.intersectObject(root, true)
            val obj = intersects.map { it.`object` }.firstOrNull { !it.isStatic() }
            obj.upTrace()
        }
    }


    private fun buildCamera(spec: Camera) = PerspectiveCamera(
        spec.fov,
        1.0,
        spec.nearClip,
        spec.farClip
    ).apply {
        translateX(spec.distance * sin(spec.zenith) * sin(spec.azimuth))
        translateY(spec.distance * cos(spec.zenith))
        translateZ(spec.distance * sin(spec.zenith) * cos(spec.azimuth))
    }

    private fun addControls(element: Node, controls: Controls) {
        when (controls.getProperty("type").string) {
            "trackball" -> TrackballControls(camera, element)
            else -> OrbitControls(camera, element)
        }
    }

    fun clear(){
        scene.children.find { it.name == "@root" }?.let {
            scene.remove(it)
        }
    }

    override fun render(obj: VisualObject3D, meta: Meta) {
        //clear old root
        clear()


        val object3D = three.buildObject3D(obj)
        object3D.name = "@root"
        scene.add(object3D)
        content = obj
        root = object3D
    }

    private var highlighted: Object3D? = null

    /**
     * Toggle highlight for the given [Mesh] object
     */
    private fun Object3D.toggleHighlight(
        highlight: Boolean,
        edgesName: String,
        material: LineBasicMaterial = SELECTED_MATERIAL
    ) {
        if (userData[DO_NOT_HIGHLIGHT_TAG] == true) {
            return
        }
        if (this is Mesh) {
            if (highlight) {
                val edges = LineSegments(
                    EdgesGeometry(geometry as BufferGeometry),
                    material
                ).apply {
                    name = edgesName
                }
                add(edges)
            } else {
                val highlightEdges = children.find { it.name == edgesName }
                highlightEdges?.let { remove(it) }
            }
        } else {
            children.filter { it.name != edgesName }.forEach {
                it.toggleHighlight(highlight, edgesName, material)
            }
        }
    }

    /**
     * Toggle highlight for element with given name
     */
    fun select(name: Name?) {
        if (name == null) {
            highlighted?.toggleHighlight(false, SELECT_NAME, SELECTED_MATERIAL)
            highlighted = null
            return
        }
        val obj = root?.findChild(name)
        if (obj != null && highlighted != obj) {
            highlighted?.toggleHighlight(false, SELECT_NAME, SELECTED_MATERIAL)
            obj.toggleHighlight(true, SELECT_NAME, SELECTED_MATERIAL)
            highlighted = obj
        }
    }

    companion object {
        const val DO_NOT_HIGHLIGHT_TAG = "doNotHighlight"
        private const val HIGHLIGHT_NAME = "@highlight"
        private const val SELECT_NAME = "@select"
    }
}

fun ThreePlugin.output(element: HTMLElement, spec: Canvas = Canvas.empty()): ThreeCanvas =
    ThreeCanvas(element, this, spec)

fun ThreePlugin.render(element: HTMLElement, obj: VisualObject3D, spec: Canvas = Canvas.empty()): Unit =
    output(element, spec).render(obj)