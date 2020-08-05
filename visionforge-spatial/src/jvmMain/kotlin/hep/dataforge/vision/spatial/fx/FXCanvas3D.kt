package hep.dataforge.vision.spatial.fx

import hep.dataforge.context.Context
import hep.dataforge.context.ContextAware
import hep.dataforge.meta.Meta
import hep.dataforge.output.Renderer
import hep.dataforge.vision.spatial.Vision3D
import hep.dataforge.vision.spatial.specifications.Canvas3DOptions
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.*
import javafx.scene.paint.Color
import org.fxyz3d.scene.Axes
import tornadofx.*

class FXCanvas3D(val plugin: FX3DPlugin, val spec: Canvas3DOptions = Canvas3DOptions.empty()) :
    Fragment(), Renderer<Vision3D>, ContextAware {

    override val context: Context get() = plugin.context

    val world = Group().apply {
        //transforms.add(Rotate(180.0, Rotate.Z_AXIS))
    }

    val axes = Axes().also {
        it.setHeight(spec.axes.size)
        it.setRadius(spec.axes.width)
        it.isVisible = spec.axes.visible
        world.add(it)
    }

    val light = AmbientLight()

    private val camera = PerspectiveCamera().apply {
        nearClip = spec.camera.nearClip
        farClip = spec.camera.farClip
        fieldOfView = spec.camera.fov.toDouble()
        this.add(light)
    }

    private val canvas = SubScene(
        Group(world, camera).apply { DepthTest.ENABLE },
        400.0,
        400.0,
        true,
        SceneAntialiasing.BALANCED
    ).also { scene ->
        scene.fill = Color.GREY
        scene.camera = camera
    }

    override val root = borderpane {
        center = canvas
    }

    val controls = camera.orbitControls(canvas, spec.camera).also {
        world.add(it.centerMarker)
    }

    val rootObjectProperty: ObjectProperty<Vision3D> = SimpleObjectProperty()
    var rootObject: Vision3D? by rootObjectProperty

    private val rootNodeProperty = rootObjectProperty.objectBinding {
        it?.let { plugin.buildNode(it) }
    }

    init {
        canvas.widthProperty().bind(root.widthProperty())
        canvas.heightProperty().bind(root.heightProperty())
        rootNodeProperty.addListener { _, oldValue: Node?, newValue: Node? ->
            Platform.runLater {
                if (oldValue != null) {
                    world.children.remove(oldValue)
                }
                if (newValue != null) {
                    world.children.add(newValue)
                }
            }
        }
    }

    override fun render(obj: Vision3D, meta: Meta) {
        rootObject = obj
    }
}