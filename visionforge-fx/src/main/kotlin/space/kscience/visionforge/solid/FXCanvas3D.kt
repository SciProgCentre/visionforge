package space.kscience.visionforge.solid

import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.*
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import org.fxyz3d.scene.Axes
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import tornadofx.*

public class FXCanvas3D(
    public val fx3d: FX3DPlugin,
    public val options: Canvas3DOptions = Canvas3DOptions.empty(),
) : Fragment(), ContextAware {

    override val context: Context get() = fx3d.context

    public val world: Group = Group().apply {
        //transforms.add(Rotate(180.0, Rotate.Z_AXIS))
    }

    public val axes: Axes = Axes().also {
        it.setHeight(options.axes.size)
        it.setRadius(options.axes.width)
        it.isVisible = options.axes.visible
        world.add(it)
    }

    public val light: AmbientLight = AmbientLight()

    private val camera = PerspectiveCamera().apply {
        nearClip = options.camera.nearClip
        farClip = options.camera.farClip
        fieldOfView = options.camera.fov.toDouble()

        add(light)
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

    override val root: BorderPane = borderpane {
        center = canvas
    }

    public val controls: OrbitControls = camera.orbitControls(canvas, options.camera).also {
        world.add(it.centerMarker)
    }

    public val rootObjectProperty: ObjectProperty<Solid> = SimpleObjectProperty()
    public var rootObject: Solid? by rootObjectProperty

    private val rootNodeProperty = rootObjectProperty.objectBinding {
        it?.let { fx3d.buildNode(it) }
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

    public fun render(vision: Solid) {
        rootObject = vision
    }
}