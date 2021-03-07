package space.kscience.visionforge.solid

import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.*
import javafx.scene.paint.Color
import org.fxyz3d.scene.Axes
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import tornadofx.*

class FXCanvas3D(
    val plugin: FX3DPlugin,
    val spec: Canvas3DOptions = Canvas3DOptions.empty(),
) : Fragment(), ContextAware {

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

    val rootObjectProperty: ObjectProperty<Solid> = SimpleObjectProperty()
    var rootObject: Solid? by rootObjectProperty

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

    fun render(vision: Solid) {
        rootObject = vision
    }
}