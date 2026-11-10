package space.kscience.visionforge.demo.factory.styles

import space.kscience.dataforge.meta.asValue
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName
import space.kscience.visionforge.demo.factory.model.FactorySceneConfig
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.get
import space.kscience.visionforge.styles

public enum class DisplayMode {
    MATERIAL,
    GOST14202,
    STATE
}

public object MogStateManager {

    public fun applyDisplayMode(
        scene: SolidGroup,
        config: FactorySceneConfig,
        mode: DisplayMode,
    ) {
        for (lineNum in config.layout.lines) {
            val line = scene["NMZ.CPNSK.MOG.Line$lineNum".parseAsName()] as? SolidGroup ?: continue

            // Equipment
            val eqGroup = line["Equipment".parseAsName()] as? SolidGroup
            if (eqGroup != null) {
                for (eq in config.equipment) {
                    val eqVision = eqGroup[eq.id.replace('-', '_').parseAsName()] ?: continue
                    when (mode) {
                        DisplayMode.MATERIAL -> {
                            val matStyle = MogStyles.materialStyleName(eq.material)
                            eqVision.styles = listOf(matStyle)
                        }

                        DisplayMode.GOST14202 -> {
                            val neutralStyle = MogStyles.materialStyleName("Neutral")
                            eqVision.styles = listOf(neutralStyle)
                        }

                        DisplayMode.STATE -> {
                            val state = eqVision.properties.getValue(Name.of("dt:state"))?.string ?: eq.state
                            val stateStyle = MogStyles.stateStyleName(state)
                            eqVision.styles = listOf(stateStyle)
                        }
                    }
                }
            }

            // Piping
            val pipingGroup = line["Piping".parseAsName()] as? SolidGroup
            if (pipingGroup != null) {
                for (stream in config.streams) {
                    val streamVision =
                        pipingGroup[stream.id.replace('-', '_').parseAsName()] as? SolidGroup ?: continue
                    when (mode) {
                        DisplayMode.MATERIAL -> {
                            val matStyle = MogStyles.materialStyleName(stream.material)
                            streamVision.styles = listOf(matStyle)
                        }

                        DisplayMode.GOST14202 -> {
                            val gostStyle = MogStyles.gostStyleName(stream.gost)
                            streamVision.styles = listOf(gostStyle)
                        }

                        DisplayMode.STATE -> {
                            val state = streamVision.properties.getValue(Name.of("dt:state"))?.string ?: "run"
                            val stateStyle = MogStyles.stateStyleName(state)
                            streamVision.styles = listOf(stateStyle)
                        }
                    }
                }
            }
        }
    }

    public fun setEquipmentState(
        scene: SolidGroup,
        lineNumber: Int,
        equipmentId: String,
        state: String,
        currentMode: DisplayMode = DisplayMode.STATE,
    ) {
        val path = "NMZ.CPNSK.MOG.Line$lineNumber.Equipment.${equipmentId.replace('-', '_')}".parseAsName()
        val eqVision = scene[path] ?: error("Equipment vision not found at path: $path")
        eqVision.properties.setValue(Name.of("dt:state"), state.asValue())
        if (currentMode == DisplayMode.STATE) {
            eqVision.styles = listOf(MogStyles.stateStyleName(state))
        }
    }

    public fun setStreamState(
        scene: SolidGroup,
        lineNumber: Int,
        streamId: String,
        state: String,
        currentMode: DisplayMode = DisplayMode.STATE,
    ) {
        val path = "NMZ.CPNSK.MOG.Line$lineNumber.Piping.${streamId.replace('-', '_')}".parseAsName()
        val streamVision = scene[path] ?: error("Stream vision not found at path: $path")
        streamVision.properties.setValue(Name.of("dt:state"), state.asValue())
        if (currentMode == DisplayMode.STATE) {
            streamVision.styles = listOf(MogStyles.stateStyleName(state))
        }
    }
}
