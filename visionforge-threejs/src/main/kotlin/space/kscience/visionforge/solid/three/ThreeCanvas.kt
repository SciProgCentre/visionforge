package space.kscience.visionforge.solid.three

import info.laht.threekt.WebGLRenderer
import info.laht.threekt.cameras.PerspectiveCamera
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.core.Raycaster
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.external.controls.TrackballControls
import info.laht.threekt.geometries.EdgesGeometry
import info.laht.threekt.helpers.AxesHelper
import info.laht.threekt.lights.AmbientLight
import info.laht.threekt.materials.LineBasicMaterial
import info.laht.threekt.math.Box3
import info.laht.threekt.math.Vector2
import info.laht.threekt.objects.LineSegments
import info.laht.threekt.objects.Mesh
import info.laht.threekt.scenes.Scene
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Node
import org.w3c.dom.events.MouseEvent
import space.kscience.dataforge.context.info
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.meta.useProperty
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.names.toName
import space.kscience.visionforge.Colors
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.specifications.*
import space.kscience.visionforge.solid.three.ThreeMaterials.HIGHLIGHT_MATERIAL
import space.kscience.visionforge.solid.three.ThreeMaterials.SELECTED_MATERIAL
import kotlin.math.cos
import kotlin.math.sin

/**
 *
 */
public class ThreeCanvas(
    public val three: ThreePlugin,
    public val options: Canvas3DOptions,
) {

    private var boundingBox: Box3? = null
    private var root: Object3D? = null
        set(value) {
            field = value
            if (value != null) boundingBox = Box3().setFromObject(value)
        }

    private val raycaster = Raycaster()
    private val mousePosition: Vector2 = Vector2()

    public var content: Solid? = null
        private set

    private val scene: Scene = Scene().apply {
        options.useProperty(Canvas3DOptions::axes) { axesConfig ->
            getObjectByName(AXES_NAME)?.let { remove(it) }
            val axesObject = AxesHelper(axes.size.toInt()).apply { visible = axes.visible }
            axesObject.name = AXES_NAME
            add(axesObject)
        }

        //Set up light
        options.useProperty(Canvas3DOptions::light) { lightConfig ->
            //remove old light if present
            getObjectByName(LIGHT_NAME)?.let { remove(it) }
            //add new light
            val lightObject = buildLight(lightConfig)
            lightObject.name = LIGHT_NAME
            add(lightObject)
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
        options.useProperty(Canvas3DOptions::layers) { selectedLayers ->
            (0..31).forEach {
                if (it in selectedLayers) {
                    this@apply.layers.enable(it)
                } else {
                    this@apply.layers.disable(it)
                }
            }
        }
    }


    public val camera: PerspectiveCamera = buildCamera(options.camera)

    private var picked: Object3D? = null

    private val renderer = WebGLRenderer {
        antialias = true
    }.apply {
        setClearColor(Colors.skyblue, 1)
    }

    private val canvas = (renderer.domElement as HTMLCanvasElement).apply {
        className += "three-canvas"
        width = 600
        height = 600
        style.apply {
            width = "100%"
            height = "100%"
            display = "block"
        }
    }


    /**
     * Force camera aspect ration and renderer size recalculation
     */
    public fun updateSize() {
        val width = canvas.clientWidth
        val height = canvas.clientHeight
        canvas.style.apply {
            minWidth = "${options.minWith.toInt()}px"
            maxWidth = "${options.maxWith.toInt()}px"
            minHeight = "${options.minHeight.toInt()}px"
            maxHeight = "${options.maxHeight.toInt()}px"
        }
        renderer.setSize(width, height, false)
        camera.aspect = width.toDouble() / height.toDouble()
        camera.updateProjectionMatrix()
    }

    /**
     * Attach canvas to given [HTMLElement]
     */
    init {
        canvas.addEventListener("pointerdown", {
            val picked = pick()
            options.onSelect?.invoke(picked?.fullName())
        }, false)

        //Attach listener to track mouse changes
        canvas.addEventListener("mousemove", { event ->
            (event as? MouseEvent)?.run {
                val rect = canvas.getBoundingClientRect()
                mousePosition.x = ((event.clientX - rect.left) / canvas.clientWidth) * 2 - 1
                mousePosition.y = -((event.clientY - rect.top) / canvas.clientHeight) * 2 + 1
            }
        }, false)

        canvas.onresize = {
            updateSize()
        }

        addControls(canvas, options.controls)

        renderer.setAnimationLoop {
            val picked = pick()

            if (picked != null && this.picked != picked) {
                this.picked?.toggleHighlight(false, HIGHLIGHT_NAME, HIGHLIGHT_MATERIAL)
                picked.toggleHighlight(true, HIGHLIGHT_NAME, HIGHLIGHT_MATERIAL)
                this.picked = picked
            }

            renderer.render(scene, camera)
        }
    }

    internal fun attach(element: Element) {
        check(element.getElementsByClassName("three-canvas").length == 0) { "Three canvas already created in this element" }
        element.appendChild(canvas)
        updateSize()
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

    //find first non-static parent in this object ancestry
    private fun Object3D?.upTrace(): Object3D? = if (this?.name?.startsWith("@") == true) parent else this

    private fun pick(): Object3D? {
        // update the picking ray with the camera and mouse position
        raycaster.setFromCamera(mousePosition, camera)

        // calculate objects intersecting the picking ray
        return root?.let { root ->
            val intersects = raycaster.intersectObject(root, true)
            //skip invisible objects
            val obj = intersects.map { it.`object` }.firstOrNull { it.visible }
            obj.upTrace()
        }
    }

    private fun buildLight(spec: Light?): info.laht.threekt.lights.Light = AmbientLight(0x404040)

    private fun addControls(element: Node, controls: Controls) {
        when (controls["type"].string) {
            "trackball" -> TrackballControls(camera, element)
            else -> OrbitControls(camera, element)
        }
    }

    public fun clear() {
        scene.children.find { it.name == "@root" }?.let {
            scene.remove(it)
        }
    }

    public fun render(vision: Solid) {
        three.logger.info { "Replacing root node in three canvas" }
        scene.findChild("@root".asName())?.let { scene.remove(it) }

        val object3D = three.buildObject3D(vision)
        object3D.name = "@root"
        scene.add(object3D)
        content = vision
        root = object3D
    }

    private var selected: Object3D? = null

    /**
     * Toggle highlight for the given [Mesh] object
     */
    private fun Object3D.toggleHighlight(
        highlight: Boolean,
        edgesName: String,
        material: LineBasicMaterial = SELECTED_MATERIAL,
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
    public fun select(name: Name?) {
        if (name == null) {
            selected?.toggleHighlight(false, SELECT_NAME, SELECTED_MATERIAL)
            selected = null
            return
        }
        val obj = root?.findChild(name)
        if (obj != null && selected != obj) {
            selected?.toggleHighlight(false, SELECT_NAME, SELECTED_MATERIAL)
            obj.toggleHighlight(true, SELECT_NAME, SELECTED_MATERIAL)
            selected = obj
        }
    }

    public companion object {
        public const val DO_NOT_HIGHLIGHT_TAG: String = "doNotHighlight"
        private const val HIGHLIGHT_NAME = "@highlight"
        private const val SELECT_NAME = "@select"
        private const val LIGHT_NAME = "@light"
        private const val AXES_NAME = "@axes"
    }
}