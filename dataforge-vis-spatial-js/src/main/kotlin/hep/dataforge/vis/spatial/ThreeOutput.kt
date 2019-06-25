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
import info.laht.threekt.lights.AmbientLight
import info.laht.threekt.math.ColorConstants
import info.laht.threekt.scenes.Scene
import org.w3c.dom.Element
import kotlin.browser.window
import kotlin.reflect.KClass

class ThreeOutput(override val context: Context, val meta: Meta = EmptyMeta) : Output<DisplayObject> {

    private val renderer = WebGLRenderer { antialias = true }.apply {
        setClearColor(ColorConstants.skyblue, 1)
        setSize(window.innerWidth, window.innerHeight)
    }

    val scene: Scene = Scene().apply {
        add(AmbientLight())
    }

    val camera = PerspectiveCamera(
        meta["fov"].int ?: 75,
        window.innerWidth.toDouble() / window.innerHeight,
        meta["camera.nearClip"].double?: World.CAMERA_NEAR_CLIP,
        meta["camera.farClip"].double?: World.CAMERA_FAR_CLIP
    ).apply {
        position.setZ(World.CAMERA_INITIAL_DISTANCE)
        rotation.set(World.CAMERA_INITIAL_X_ANGLE, World.CAMERA_INITIAL_Y_ANGLE, World.CAMERA_INITIAL_Z_ANGLE)
    }

    val controls: OrbitControls = OrbitControls(camera, renderer.domElement)

    val root get() = renderer.domElement

    private fun animate() {
        window.requestAnimationFrame {
            animate()
        }
        renderer.render(scene, camera)
    }

    fun start(element: Element) {
        window.addEventListener("resize", {
            camera.aspect = window.innerWidth.toDouble() / window.innerHeight;
            camera.updateProjectionMatrix();

            renderer.setSize(window.innerWidth, window.innerHeight)
        }, false)
        element.appendChild(root)
        animate()
    }


    private fun buildNode(obj: DisplayObject): Object3D? {
        return when (obj) {
            is DisplayGroup -> Group(obj.mapNotNull { buildNode(it) }).apply {
                ThreeFactory.updatePosition(obj, this)
            }
            //is Box -> ThreeBoxFactory(obj)
            //is JSRootObject -> ThreeJSRootFactory(obj)
            //is Convex -> ThreeConvexFactory(obj)
            else -> findFactory(obj::class)?.invoke(obj) ?: error("Factory not found")
        }
    }

    private fun <T : DisplayObject> findFactory(type: KClass<out T>): ThreeFactory<T>? {
        return context.content<ThreeFactory<*>>(ThreeFactory.TYPE).values.find { it.type == type } as? ThreeFactory<T>?
    }

    override fun render(obj: DisplayObject, meta: Meta) {
        buildNode(obj)?.let {
            scene.add(it)
        } ?: error("Renderer for ${obj::class} not found")
    }

}

//    init {
//        val cube: Mesh
//
//        cube = Mesh(
//            BoxBufferGeometry(1, 1, 1),
//            MeshPhongMaterial().apply {
//                this.color.set(ColorConstants.darkgreen)
//            }
//        ).also(scene::add)
//
//        Mesh(cube.geometry as BufferGeometry,
//            MeshBasicMaterial().apply {
//                this.wireframe = true
//                this.color.set(ColorConstants.black)
//            }
//        ).also(cube::add)
//
//        val points = CatmullRomCurve3(
//            arrayOf(
//                Vector3(-10, 0, 10),
//                Vector3(-5, 5, 5),
//                Vector3(0, 0, 0),
//                Vector3(5, -5, 5),
//                Vector3(10, 0, 10)
//            )
//        ).getPoints(50)
//
//        val geometry = BufferGeometry().setFromPoints(points)
//
//        val material = LineBasicMaterial().apply {
//            color.set(0xff0000)
//        }
//
//        // Create the final object to add to the scene
//        Line(geometry, material).apply(scene::add)
//    }