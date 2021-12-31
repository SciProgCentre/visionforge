package space.kscience.visionforge.examples

import space.kscience.dataforge.context.Context
import space.kscience.gdml.*
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.color
import space.kscience.visionforge.solid.invoke
import space.kscience.visionforge.visible
import java.nio.file.Path

fun main() {
    val context = Context {
        plugin(Solids)
    }

    context.makeVisionFile(Path.of("curves.html"), resourceLocation = ResourceLocation.EMBED) {
        vision("canvas") {
            Gdml {
                // geometry variables
                val worldSize = 500
                // chamber
                val chamberHeight = 30 // length of the chamber
                val chamberDiameter = 102 // inner diameter of the copper chamber
                val chamberOuterSquareSide = 134 // chamber has a square footprint
                val chamberBackplateThickness = 15 // thickness of the backplate of the chamber
                // teflon disk
                val cathodeTeflonDiskHoleRadius = 15
                val cathodeTeflonDiskThickness = 5
                val cathodeCopperSupportOuterRadius = 45
                val cathodeCopperSupportInnerRadius = 8.5
                val cathodeCopperSupportThickness = 1
                // mylar cathode
                val mylarCathodeThickness = 0.004
                // patern
                val cathodePatternLineWidth = 0.3
                val cathodePatternDiskRadius = 4.25
                // readout
                val chamberTeflonWallThickness = 1
                val readoutKaptonThickness = 0.5
                val readoutCopperThickness = 0.2
                val readoutPlaneSide = 60

                structure {
                    val worldMaterial = materials.composite("G4_AIR")
                    val worldBox = solids.box(worldSize, worldSize, worldSize, name = "world")

                    val shieldingMaterial = materials.composite("G4_Pb")
                    val scintillatorMaterial = materials.composite("BC408")
                    val captureMaterial = materials.composite("G4_Cd")

                    // chamber
                    val copperMaterial = materials.composite("G4_Cu")
                    val chamberSolidBase = solids.box(chamberOuterSquareSide, chamberOuterSquareSide, chamberHeight)
                    val chamberSolidHole = solids.tube(chamberDiameter / 2, chamberHeight)
                    val chamberSolid = solids.subtraction(chamberSolidBase, chamberSolidHole)
                    val chamberBodyVolume = volume(copperMaterial, chamberSolid)
                    val chamberBackplateSolid =
                        solids.box(chamberOuterSquareSide, chamberOuterSquareSide, chamberBackplateThickness)
                    val chamberBackplateVolume = volume(copperMaterial, chamberBackplateSolid)
                    // chamber teflon walls
                    val teflonMaterial = materials.composite("G4_TEFLON")
                    val chamberTeflonWallSolid = solids.tube(chamberDiameter / 2, chamberHeight) {
                        rmin = chamberDiameter / 2.0 - chamberTeflonWallThickness
                    }
                    val chamberTeflonWallVolume = volume(teflonMaterial, chamberTeflonWallSolid)
                    // cathode
                    val cathodeCopperDiskMaterial = materials.composite("G4_Cu")
                    val cathodeWindowMaterial = materials.composite("G4_MYLAR")

                    val cathodeTeflonDiskSolidBase =
                        solids.tube(chamberOuterSquareSide / 2, cathodeTeflonDiskThickness) {
                            rmin = cathodeTeflonDiskHoleRadius
                        }
                    val cathodeCopperDiskSolid =
                        solids.tube(cathodeCopperSupportOuterRadius, cathodeCopperSupportThickness) {
                            rmin = cathodeCopperSupportInnerRadius
                        }

                    val cathodeTeflonDiskSolid = solids.subtraction(cathodeTeflonDiskSolidBase, cathodeCopperDiskSolid)
                    val cathodeTeflonDiskVolume = volume(teflonMaterial, cathodeTeflonDiskSolid)

                    val cathodeWindowSolid = solids.tube(cathodeTeflonDiskHoleRadius, mylarCathodeThickness)
                    val cathodeWindowVolume = volume(cathodeWindowMaterial, cathodeWindowSolid)

                    val cathodeFillingMaterial = materials.composite("G4_Galactic")
                    val cathodeFillingSolidBase = solids.tube(cathodeTeflonDiskHoleRadius, cathodeTeflonDiskThickness)

                    val cathodeFillingSolid = solids.subtraction(cathodeFillingSolidBase, cathodeCopperDiskSolid) {
                        position(z = chamberHeight / 2 - mylarCathodeThickness / 2)
                    }
                    val cathodeFillingVolume = volume(cathodeFillingMaterial, cathodeFillingSolid)

                    // pattern
                    val cathodePatternLineAux = solids.box(
                        cathodePatternLineWidth,
                        cathodeCopperSupportInnerRadius * 2,
                        cathodeCopperSupportThickness
                    )
                    val cathodePatternCentralHole = solids.tube(
                        cathodePatternDiskRadius - 0 * cathodePatternLineWidth,
                        cathodeCopperSupportThickness * 1.1
                    )
                    val cathodePatternLine = solids.subtraction(cathodePatternLineAux, cathodePatternCentralHole)

                    val cathodePatternDisk = solids.tube(
                        cathodePatternDiskRadius,
                        cathodeCopperSupportThickness
                    ) { rmin = cathodePatternDiskRadius - cathodePatternLineWidth }


                    val cathodeCopperDiskSolidAux0 =
                        solids.union(cathodeCopperDiskSolid, cathodePatternLine) {
                            rotation(x = 0, y = 0, z = 0)
                        }
                    val cathodeCopperDiskSolidAux1 =
                        solids.union(cathodeCopperDiskSolidAux0, cathodePatternLine) {
                            rotation = GdmlRotation(
                                "cathodePatternRotation1", x = 0, y = 0, z = 45
                            )
                        }
                    val cathodeCopperDiskSolidAux2 =
                        solids.union(cathodeCopperDiskSolidAux1, cathodePatternLine) {
                            rotation = GdmlRotation(
                                "cathodePatternRotation2", x = 0, y = 0, z = 90
                            )
                        }
                    val cathodeCopperDiskSolidAux3 =
                        solids.union(cathodeCopperDiskSolidAux2, cathodePatternLine) {
                            rotation = GdmlRotation(
                                "cathodePatternRotation3", x = 0, y = 0, z = 135
                            )
                        }

                    val cathodeCopperDiskFinal =
                        solids.union(cathodeCopperDiskSolidAux3, cathodePatternDisk)


                    val cathodeCopperDiskVolume =
                        volume(cathodeCopperDiskMaterial, cathodeCopperDiskFinal)

                    val gasSolidOriginal = solids.tube(
                        chamberDiameter / 2 - chamberTeflonWallThickness,
                        chamberHeight
                    )

                    val kaptonReadoutMaterial = materials.composite("G4_KAPTON")
                    val kaptonReadoutSolid = solids.box(
                        chamberOuterSquareSide,
                        chamberOuterSquareSide,
                        readoutKaptonThickness)
                    val kaptonReadoutVolume = volume( kaptonReadoutMaterial, kaptonReadoutSolid)

                    val copperReadoutSolid =
                        solids.box(readoutPlaneSide, readoutPlaneSide, readoutCopperThickness)
                    val copperReadoutVolume = volume(copperMaterial, copperReadoutSolid)

                    val gasSolidAux =
                        solids.subtraction(gasSolidOriginal, copperReadoutSolid) {
                            position(z = -chamberHeight / 2 + readoutCopperThickness / 2)
                        }

                    val gasMaterial = materials.composite("G4_Ar")
                    val gasSolid =
                        solids.subtraction( gasSolidAux, cathodeWindowSolid) {
                            position(z = chamberHeight / 2 - mylarCathodeThickness / 2)
                            rotation(z = 45)
                        }
                    val gasVolume = volume(gasMaterial, gasSolid)

                    // world setup
                    world = volume(worldMaterial, worldBox) {
                        physVolume(gasVolume) {
                            name = "gas"
                        }
                        physVolume(kaptonReadoutVolume) {
                            name = "kaptonReadout"
                            position {
                                z = -chamberHeight / 2 - readoutKaptonThickness / 2
                            }
                        }
                        physVolume(copperReadoutVolume) {
                            name = "copperReadout"
                            position {
                                z = -chamberHeight / 2 + readoutCopperThickness / 2
                            }
                            rotation { z = 45 }
                        }
                        physVolume(chamberBodyVolume) {
                            name = "chamberBody"
                        }
                        physVolume(chamberBackplateVolume) {
                            name = "chamberBackplate"
                            position {
                                z = -chamberHeight / 2 - readoutKaptonThickness - chamberBackplateThickness / 2
                            }
                        }
                        physVolume(chamberTeflonWallVolume) {
                            name = "chamberTeflonWall"
                        }
                        physVolume(cathodeTeflonDiskVolume) {
                            name = "cathodeTeflonDisk"
                            position {
                                z = chamberHeight / 2 + cathodeTeflonDiskThickness / 2
                            }
                        }
                        physVolume(cathodeCopperDiskVolume) {
                            name = "cathodeCopperDisk"
                            position {
                                z = chamberHeight / 2 + cathodeCopperSupportThickness / 2
                            }
                        }
                        physVolume(cathodeWindowVolume) {
                            name = "cathodeWindow"
                            position {
                                z = chamberHeight / 2 - mylarCathodeThickness / 2
                            }
                        }
                        physVolume(cathodeFillingVolume) {
                            name = "cathodeFilling"
                            position {
                                z = chamberHeight / 2 + cathodeTeflonDiskThickness / 2
                            }
                        }
                    }
                }
            }.toVision {
                configure { _, solid, _ ->
                    //disable visibility for the world box
                    if(solid.name == "world"){
                        visible = false
                    }
                    if(solid.name.startsWith("gas")){
                        color("green")
                    } else {
                        //make all solids semi-transparent
                        transparent()
                    }
                }
            }
        }
    }
}