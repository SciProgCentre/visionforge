package space.kscience.visionforge.demo.factory.telemetry

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import space.kscience.visionforge.demo.factory.styles.DisplayMode
import space.kscience.visionforge.demo.factory.styles.MogStateManager
import space.kscience.visionforge.solid.SolidGroup
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

public class MogTelemetrySimulator(
    private val scene: SolidGroup,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {
    private val logger = LoggerFactory.getLogger(MogTelemetrySimulator::class.java)
    private var job: Job? = null
    public var currentDisplayMode: DisplayMode = DisplayMode.MATERIAL

    public val isRunning: Boolean get() = job?.isActive == true

    public fun start() {
        if (isRunning) return
        job = scope.launch {
            logger.info("Starting MOG digital twin telemetry simulation loop...")
            val simulatedEquipment = listOf("B-03A", "B-03B", "B-04A", "B-04B", "P-01A", "P-01B", "P-02A", "P-02B", "E-01A", "F-01")
            val possibleStates = listOf("run", "standby", "flush", "fault", "maintenance")

            while (isActive) {
                delay(1500.milliseconds)
                val line = Random.nextInt(1, 4)
                val eq = simulatedEquipment.random()
                val state = possibleStates.random()

                logger.info("Telemetry event: Line $line, Equipment $eq -> state '$state'")
                MogStateManager.setEquipmentState(scene, line, eq, state, currentDisplayMode)

                // Occasionally simulate a stream flush or fault
                if (Random.nextDouble() < 0.3) {
                    val streamId = listOf("W-01", "W-02", "G-01", "L-01").random()
                    val streamState = listOf("flush", "standby", "run").random()
                    logger.info("Telemetry event: Line $line, Stream $streamId -> state '$streamState'")
                    MogStateManager.setStreamState(scene, line, streamId, streamState, currentDisplayMode)
                }
            }
        }
    }

    public fun stop() {
        job?.cancel()
        job = null
        logger.info("Stopped MOG telemetry simulation loop.")
    }
}
