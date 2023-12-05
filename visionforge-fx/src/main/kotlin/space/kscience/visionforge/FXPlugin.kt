package space.kscience.visionforge

import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.boolean
import tornadofx.*
import kotlin.concurrent.thread
import kotlin.reflect.KClass

/**
 * Plugin holding JavaFX application instance and its root stage
 */
public class FXPlugin(meta: Meta = Meta.EMPTY) : AbstractPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag

    private val stages: ObservableSet<Stage> = FXCollections.observableSet()

    public val consoleMode: Boolean by meta.boolean(true)

    init {
        if (consoleMode) {
            stages.addListener(SetChangeListener { change ->
                if (change.set.isEmpty()) {
                    Platform.setImplicitExit(true)
                } else {
                    Platform.setImplicitExit(false)
                }
            })
        }
    }

    /**
     * Wait for application and toolkit to start if needed
     */
    override fun attach(context: Context) {
        super.attach(context)
        if (FX.getApplication(FX.defaultScope) == null) {
            if (consoleMode) {
                thread(name = "${context.name} FX application thread") {
                    context.logger.debug{"Starting FX application surrogate"}
                    launch<ApplicationSurrogate>()
                }

                while (!FX.initialized.get()) {
                    if (Thread.interrupted()) {
                        error("Interrupted application start")
                    }
                }
                Platform.setImplicitExit(false)
            } else {
                error("FX Application not defined")
            }
        }
    }

    /**
     * Define an application to use in this context
     */
    public fun setApp(app: Application, stage: Stage) {
        FX.registerApplication(FX.defaultScope, app, stage)
    }

    /**
     * Show something in a pre-constructed stage. Blocks thread until stage is created
     *
     * @param cons
     */
    public fun display(action: Stage.() -> Unit) {
        runLater {
            val stage = Stage()
            stage.initOwner(FX.primaryStage)
            stage.action()
            stage.show()
            stages.add(stage)
            stage.setOnCloseRequest { stages.remove(stage) }
        }
    }

    public fun display(component: UIComponent, width: Double = 800.0, height: Double = 600.0) {
        display {
            scene = Scene(component.root, width, height)
            title = component.title
        }
    }

    public companion object : PluginFactory<FXPlugin> {
        override val type: KClass<out FXPlugin> = FXPlugin::class
        override val tag: PluginTag = PluginTag("vis.fx", group = PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): FXPlugin =  FXPlugin(meta)

    }

}

public val dfIcon: Image = Image(Global::class.java.getResourceAsStream("/img/df.png"))
public val dfIconView: ImageView = ImageView(dfIcon)

/**
 * An application surrogate without any visible primary stage
 */
public class ApplicationSurrogate : App() {
    override fun start(stage: Stage) {
        FX.registerApplication(this, stage)
        FX.initialized.value = true
    }
}

public fun Context.display(width: Double = 800.0, height: Double = 600.0, component: () -> UIComponent) {
    fetch(FXPlugin).display(component(), width, height)
}