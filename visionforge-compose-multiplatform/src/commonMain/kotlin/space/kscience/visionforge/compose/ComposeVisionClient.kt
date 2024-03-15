package space.kscience.visionforge.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.*
import space.kscience.visionforge.html.VisionOutput
import space.kscience.visionforge.html.VisionTagConsumer

/**
 * A Kotlin-browser plugin that renders visions based on provided renderers and governs communication with the server.
 */
public class ComposeVisionClient : AbstractPlugin(), VisionClient {
    override val tag: PluginTag get() = Companion.tag
    override val visionManager: VisionManager by require(VisionManager)


    private val renderers by lazy { context.gather<ComposeVisionRenderer>(ComposeVisionRenderer.TYPE).values }

    private fun findRendererFor(vision: Vision): ComposeVisionRenderer? = renderers.mapNotNull {
        val rating = it.rateVision(vision)
        if (rating > 0) {
            rating to it
        } else {
            null
        }
    }.maxByOrNull { it.first }?.second


    private val mutex = Mutex()


    private val rootChangeCollector = VisionChangeBuilder()

    /**
     * Communicate vision property changed from rendering engine to model
     */
    override fun notifyPropertyChanged(visionName: Name, propertyName: Name, item: Meta?) {
        context.launch {
            mutex.withLock {
                rootChangeCollector.propertyChanged(visionName, propertyName, item)
            }
        }
    }

    private val eventCollector = MutableSharedFlow<Pair<Name, VisionEvent>>(meta["feedback.eventCache"].int ?: 100)

    /**
     * Send a custom feedback event
     */
    override suspend fun sendEvent(targetName: Name, event: VisionEvent) {
        eventCollector.emit(targetName to event)
    }

    @Composable
    public fun renderVision(name: Name, vision: Vision, outputMeta: Meta) {
        val renderer: ComposeVisionRenderer = remember(vision) {
            findRendererFor(vision) ?: error("Could not find renderer for ${vision::class}")
        }

        key(vision) {
            vision.setAsRoot(visionManager)
        }
        //subscribe to a backwards events propagation for control visions
        if (vision is ControlVision) {
            LaunchedEffect(vision) {
                vision.controlEventFlow.collect {
                    sendEvent(name, it)
                }
            }
        }

        renderer.render(name, vision, outputMeta)
    }


//    override fun content(target: String): Map<Name, Any> = if (target == ComposeVisionRenderer.TYPE) {
//        listOf(
//            htmlVisionRenderer,
//            inputVisionRenderer,
//            checkboxVisionRenderer,
//            numberVisionRenderer,
//            textVisionRenderer,
//            rangeVisionRenderer,
//            formVisionRenderer,
//            buttonVisionRenderer
//        ).associateBy { it.toString().asName() }
//    } else super<AbstractPlugin>.content(target)

    public companion object : PluginFactory<ComposeVisionClient> {
        override fun build(context: Context, meta: Meta): ComposeVisionClient = ComposeVisionClient()

        override val tag: PluginTag = PluginTag(name = "vision.client.compose", group = PluginTag.DATAFORGE_GROUP)
    }
}

/**
 * Render an Element vision via injected vision renderer inside compose-html
 */
@Composable
public fun Vision(
    context: Context,
    vision: Vision,
    name: Name? = null,
    meta: Meta = Meta.EMPTY,
) {
    val actualName = name ?: NameToken(VisionTagConsumer.DEFAULT_VISION_NAME, vision.hashCode().toUInt().toString()).asName()
    context.request(ComposeVisionClient).renderVision(actualName, vision, meta)
}

@Composable
public fun Vision(
    context: Context,
    name: Name? = null,
    meta: Meta = Meta.EMPTY,
    buildOutput: VisionOutput.() -> Vision,
) {
    val actualName = name ?: NameToken(VisionTagConsumer.DEFAULT_VISION_NAME, buildOutput.hashCode().toUInt().toString()).asName()
    val output = VisionOutput(context, actualName)
    val vision = output.buildOutput()
    context.request(ComposeVisionClient).renderVision(actualName, vision, meta)
}