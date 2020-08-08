package hep.dataforge.vision.solid.gdml.demo

import hep.dataforge.vision.solid.gdml.LUnit
import hep.dataforge.vision.solid.gdml.readFile
import hep.dataforge.vision.solid.gdml.toVision
import hep.dataforge.vision.solid.stringify
import scientifik.gdml.GDML
import java.io.File
import java.nio.file.Paths

fun main(args: Array<String>) {
    require(args.isNotEmpty()){"At least one argument is required"}
    val inputFileName = args[0]
    require(inputFileName.endsWith(".gdml")){"GDML required"}
    val outputFileName = args.getOrNull(1)?:inputFileName.replace(".gdml",".json")

    val gdml = GDML.readFile(Paths.get(inputFileName))
        //GDML.readFile(Paths.get("D:\\Work\\Projects\\visionforge\\visionforge-spatial-gdml\\src\\jvmTest\\resources\\gdml\\simple1.gdml"))

    val visual = gdml.toVision {
        lUnit = LUnit.CM
    }

    val json = visual.stringify()
    println(json)
    File(outputFileName).writeText(json)
    //File("D:\\Work\\Projects\\gdml.kt\\gdml-source\\cubes.json").writeText(json)
}