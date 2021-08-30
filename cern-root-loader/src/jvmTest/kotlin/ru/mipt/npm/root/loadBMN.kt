package ru.mipt.npm.root

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.mipt.npm.root.serialization.TGeoManager
import space.kscience.visionforge.solid.Solids
import java.time.Duration
import kotlin.system.measureTimeMillis

private fun JsonElement.countTypes(): Sequence<String> = sequence {
    when (val json = this@countTypes) {
        is JsonObject -> {
            json["_typename"]?.let { yield(it.jsonPrimitive.content) }
            json.values.forEach { yieldAll(it.countTypes()) }
        }
        is JsonArray -> {
            json.forEach {
                yieldAll(it.countTypes())
            }
        }
        else -> {
        }
    }
}

fun main() {
    val string = TGeoManager::class.java.getResourceAsStream("/BM@N.root.json")!!
        .readAllBytes().decodeToString()
    val time = measureTimeMillis {
        val geo = TGeoManagerScheme.parse(string)
        val solid = geo.toSolid()

        println(Solids.encodeToString(solid))
    }

//    val json = Json.parseToJsonElement(string)
//    val sizes = json.countTypes().groupBy { it }.mapValues { it.value.size }
//    sizes.forEach {
//        println(it)
//    }
//
//    val time = measureTimeMillis {
//        val geo = TObject.decodeFromString(TGeoManager.serializer(), string)
//        val solid = geo.toSolid()
//
//        println(Solids.encodeToString(solid))
//    }
//
    println(Duration.ofMillis(time))
}