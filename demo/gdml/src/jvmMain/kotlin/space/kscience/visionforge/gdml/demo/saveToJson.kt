package space.kscience.visionforge.gdml.demo

import space.kscience.gdml.Gdml
import space.kscience.gdml.LUnit
import space.kscience.gdml.decodeFromFile
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.solid.Solids
import java.io.File
import java.nio.file.Paths

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "At least one argument is required" }
    val inputFileName = args[0]
    require(inputFileName.endsWith(".gdml")) { "GDML required" }
    val outputFileName = args.getOrNull(1) ?: inputFileName.replace(".gdml", ".json")

    val gdml = Gdml.decodeFromFile(Paths.get(inputFileName), true)
    //GDML.readFile(Paths.get("D:\\Work\\Projects\\visionforge\\visionforge-spatial-gdml\\src\\jvmTest\\resources\\gdml\\simple1.gdml"))

    val vision = gdml.toVision {
        lUnit = LUnit.CM
    }

    val json = Solids.encodeToString(vision)
    println(json)
    File(outputFileName).writeText(json)
    //File("D:\\Work\\Projects\\gdml.kt\\gdml-source\\cubes.json").writeText(json)
}