package space.kscience.visionforge.demo.factory.styles

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.asValue
import space.kscience.dataforge.meta.remove
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.MutableVision
import space.kscience.visionforge.Vision
import space.kscience.visionforge.demo.factory.model.FactorySceneConfig
import space.kscience.visionforge.getStyle
import space.kscience.visionforge.setStyle
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.get

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
                    val eqVision = eqGroup[eq.id.replace('-', '_').parseAsName()] as? MutableVision ?: continue
                    when (mode) {
                        DisplayMode.MATERIAL -> {
                            eqVision.properties.remove(Vision.STYLESHEET_KEY + MogStyles.HIGHLIGHT_STYLE_NAME)
                        }

                        DisplayMode.GOST14202 -> {
                            val neutralStyle = scene.getStyle(MogStyles.materialStyleName("Neutral")) ?: Meta.EMPTY
                            eqVision.setStyle(MogStyles.HIGHLIGHT_STYLE_NAME, neutralStyle)
                        }

                        DisplayMode.STATE -> {
                            val state = eqVision.properties.getValue(Name.of("dt:state"))?.string ?: eq.state
                            val stateStyle = scene.getStyle(MogStyles.stateStyleName(state)) ?: Meta.EMPTY
                            eqVision.setStyle(MogStyles.HIGHLIGHT_STYLE_NAME, stateStyle)
                        }
                    }
                }
            }

            // Piping
            val pipingGroup = line["Piping".parseAsName()] as? SolidGroup
            if (pipingGroup != null) {
                for (stream in config.streams) {
                    val streamVision =
                        pipingGroup[stream.id.replace('-', '_').parseAsName()] as? MutableVision ?: continue
                    when (mode) {
                        DisplayMode.MATERIAL -> {
                            streamVision.properties.remove(Vision.STYLESHEET_KEY + MogStyles.HIGHLIGHT_STYLE_NAME)
                        }

                        DisplayMode.GOST14202 -> {
                            val gostStyle = scene.getStyle(MogStyles.gostStyleName(stream.gost)) ?: Meta.EMPTY
                            streamVision.setStyle(MogStyles.HIGHLIGHT_STYLE_NAME, gostStyle)
                        }

                        DisplayMode.STATE -> {
                            val state = streamVision.properties.getValue(Name.of("dt:state"))?.string ?: "run"
                            val stateStyle = scene.getStyle(MogStyles.stateStyleName(state)) ?: Meta.EMPTY
                            streamVision.setStyle(MogStyles.HIGHLIGHT_STYLE_NAME, stateStyle)
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
        val eqVision = scene[path] as? MutableVision ?: error("Equipment vision not found at path: $path")
        eqVision.properties.setValue(Name.of("dt:state"), state.asValue())
        if (currentMode == DisplayMode.STATE) {
            val stateStyle = scene.getStyle(MogStyles.stateStyleName(state)) ?: Meta.EMPTY
            eqVision.setStyle(MogStyles.HIGHLIGHT_STYLE_NAME, stateStyle)
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
        val streamVision = scene[path] as? MutableVision ?: error("Stream vision not found at path: $path")
        streamVision.properties.setValue(Name.of("dt:state"), state.asValue())
        if (currentMode == DisplayMode.STATE) {
            val stateStyle = scene.getStyle(MogStyles.stateStyleName(state)) ?: Meta.EMPTY
            streamVision.setStyle(MogStyles.HIGHLIGHT_STYLE_NAME, stateStyle)
        }
    }
}
