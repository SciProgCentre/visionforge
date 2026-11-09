package space.kscience.visionforge.demo.factory.scene

import space.kscience.dataforge.meta.asValue
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.demo.factory.model.FactorySceneConfig
import space.kscience.visionforge.demo.factory.scene.MogEquipmentBuilder.buildEquipment
import space.kscience.visionforge.demo.factory.scene.MogPipingBuilder.buildPiping
import space.kscience.visionforge.demo.factory.styles.MogStyles
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.useStyle
import kotlin.math.PI

public object MogSceneBuilder {

    public fun buildScene(config: FactorySceneConfig): SolidGroup {
        val root = SolidGroup {
            rotationX = -PI/2

            // Apply all VisionForge styles (Materials, GOST, States)
            MogStyles.applyStyles(this, config)

            // Calculate canonical line nozzles
            val nozzlesByEq = config.equipment.associate { eq ->
                eq.id to MogEquipmentBuilder.calculateNozzles(eq)
            }
            val nozzlesMap = nozzlesByEq.values.flatten().associateBy { "${it.equipmentId}.${it.spec.id}" }

            solidGroup("NMZ") {
                properties.setValue(Name.of("dexpi:class"), "Plant".asValue())
                properties.setValue(Name.of("dt:nameRu"), "Надеждинский металлургический завод".asValue())

                solidGroup("CPNSK") {
                    properties.setValue(Name.of("dexpi:class"), "PlantSection".asValue())
                    properties.setValue(Name.of("dt:nameRu"), "Цех производства и нейтрализации серной кислоты".asValue())

                    solidGroup("MOG") {
                        properties.setValue(Name.of("dexpi:class"), "PlantUnit".asValue())
                        properties.setValue(Name.of("dt:nameRu"), "Отделение мокрой очистки газа".asValue())

                        // Foundation Slab
                        val slab = config.layout.slab
                        val slabXMin = slab.x.minOrNull() ?: -12.0
                        val slabXMax = slab.x.maxOrNull() ?: 82.0
                        val slabYMin = slab.y.minOrNull() ?: -31.0
                        val slabYMax = slab.y.maxOrNull() ?: 96.0

                        val slabXSize = slabXMax - slabXMin
                        val slabYSize = slabYMax - slabYMin
                        val slabThickness = slab.thickness

                        box(
                            xSize = slabXSize,
                            ySize = slabYSize,
                            zSize = slabThickness,
                            name = "Slab"
                        ) {
                            x = (slabXMin + slabXMax) / 2.0
                            y = -(slabYMin + slabYMax) / 2.0
                            z = -slabThickness / 2.0
                            opacity = 0.5
                            properties.setValue(Name.of("dt:nameRu"), "Плита пола (условно)".asValue())
                            useStyle(MogStyles.materialStyleName("Concrete"))
                        }

                        // Technological Lines
                        for (lineNumber in config.layout.lines) {
                            val lineOffset = (lineNumber - 1) * config.layout.lineSpacing_m

                            solidGroup("Line$lineNumber") {
                                y = -lineOffset
                                properties.setValue(Name.of("dexpi:class"), "PlantTrain".asValue())
                                properties.setValue(Name.of("dt:nameRu"), "Линия $lineNumber".asValue())
                                properties.setValue(Name.of("dt:lineNumber"), lineNumber.asValue())

                                val eqGroup = solidGroup("Equipment") {
                                    properties.setValue(Name.of("dexpi:class"), "EquipmentGroup".asValue())
                                    for (eq in config.equipment) {
                                        val eqNozzles = nozzlesByEq[eq.id] ?: emptyList()
                                        buildEquipment(eq, eqNozzles)
                                    }
                                }

                                buildPiping(config.streams, nozzlesMap)
                            }
                        }
                    }
                }
            }
        }

        return root
    }
}
