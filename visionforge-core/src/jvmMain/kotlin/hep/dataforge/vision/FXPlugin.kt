package hep.dataforge.vision

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import hep.dataforge.meta.boolean
import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage
import tornadofx.*
import kotlin.concurrent.thread
import kotlin.reflect.KClass

/**
 * Plugin holding JavaFX application instance and its root stage
 */
class FXPlugin(meta: Meta = Meta.EMPTY) : AbstractPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag

    private val stages: ObservableSet<Stage> = FXCollections.observableSet()

    val consoleMode: Boolean by meta.boolean(true)

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
                    context.logger.debug("Starting FX application surrogate")
                    launch<ApplicationSurrogate>()
                }

                while (!FX.initialized.get()) {
                    if (Thread.interrupted()) {
                        throw RuntimeException("Interrupted application start")
                    }
                }
                Platform.setImplicitExit(false)
            } else {
                throw RuntimeException("FX Application not defined")
            }
        }
    }

    /**
     * Define an application to use in this context
     */
    fun setApp(app: Application, stage: Stage) {
        FX.registerApplication(FX.defaultScope, app, stage)
    }

    /**
     * Show something in a pre-constructed stage. Blocks thread until stage is created
     *
     * @param cons
     */
    fun display(action: Stage.() -> Unit) {
        runLater {
            val stage = Stage()
            stage.initOwner(FX.primaryStage)
            stage.action()
            stage.show()
            stages.add(stage)
            stage.setOnCloseRequest { stages.remove(stage) }
        }
    }

    fun display(component: UIComponent, width: Double = 800.0, height: Double = 600.0) {
        display {
            scene = Scene(component.root, width, height)
            title = component.title
        }
    }

    companion object : PluginFactory<FXPlugin> {
        override val type: KClass<out FXPlugin> = FXPlugin::class
        override val tag: PluginTag = PluginTag("vis.fx", group = PluginTag.DATAFORGE_GROUP)
        override fun invoke(meta: Meta, context: Context): FXPlugin =
            FXPlugin(meta)
    }

}

val dfIcon: Image = Image(Global::class.java.getResourceAsStream("/img/df.png"))
val dfIconView = ImageView(dfIcon)

/**
 * An application surrogate without any visible primary stage
 */
class ApplicationSurrogate : App() {
    override fun start(stage: Stage) {
        FX.registerApplication(this, stage)
        FX.initialized.value = true
    }
}

fun Context.display(width: Double = 800.0, height: Double = 600.0, component: () -> UIComponent) {
    plugins.fetch(FXPlugin).display(component(), width, height)
}