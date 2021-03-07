package space.kscience.visionforge.gdml

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.adaptivity.xmlutil.StAXReader
import space.kscience.gdml.Gdml
import space.kscience.visionforge.solid.prototype
import space.kscience.visionforge.visitor.countDistinct
import space.kscience.visionforge.visitor.flowStatistics
import java.io.File
import kotlin.reflect.KClass

suspend fun main() {
    withContext(Dispatchers.Default) {
        //val stream = SingleChildReducer::class.java.getResourceAsStream("/gdml/BM@N.gdml")
        val stream =
            File("D:\\Work\\Projects\\dataforge-vis\\visionforge-gdml\\src\\jvmTest\\resources\\gdml\\BM@N.gdml").inputStream()

        val xmlReader = StAXReader(stream, "UTF-8")
        val xml = Gdml.format.decodeFromReader(Gdml.serializer(), xmlReader)
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