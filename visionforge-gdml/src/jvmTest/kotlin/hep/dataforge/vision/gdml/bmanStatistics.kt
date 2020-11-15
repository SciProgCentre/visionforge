package hep.dataforge.vision.gdml

import hep.dataforge.vision.solid.prototype
import hep.dataforge.vision.visitor.countDistinct
import hep.dataforge.vision.visitor.flowStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kscience.gdml.GDML
import nl.adaptivity.xmlutil.StAXReader
import java.io.File
import kotlin.reflect.KClass

suspend fun main() {
    withContext(Dispatchers.Default) {
        //val stream = SingleChildReducer::class.java.getResourceAsStream("/gdml/BM@N.gdml")
        val stream =
            File("D:\\Work\\Projects\\dataforge-vis\\visionforge-gdml\\src\\jvmTest\\resources\\gdml\\BM@N.gdml").inputStream()

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = GDML.format.parse(GDML.serializer(), xmlReader)
        val vision = xml.toVision()


        vision.flowStatistics<KClass<*>>{ _, child ->
            child.prototype::class
        }.countDistinct().forEach { (depth, size) ->
            println("$depth\t$size")
        }

//        println("***REDUCED***")
//
//        vision.optimizeGdml()
//
//        vision.flowStatistics().countDistinctBy { it.type }.forEach { (depth, size) ->
//            println("$depth\t$size")
//        }
    }
}