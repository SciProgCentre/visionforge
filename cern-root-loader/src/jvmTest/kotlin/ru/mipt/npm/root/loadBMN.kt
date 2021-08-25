package ru.mipt.npm.root

import kotlinx.serialization.json.*
import java.time.Duration
import kotlin.system.measureTimeMillis

private fun JsonElement.countTypes(): Sequence<String> = sequence {
    val json = this@countTypes
    when (json){
        is JsonObject -> {
            json["_typename"]?.let { yield(it.jsonPrimitive.content) }
            json.values.forEach { yieldAll(it.countTypes()) }
        }
        is JsonArray -> {
            json.forEach {
                yieldAll(it.countTypes())
            }
        }
        else -> {}
    }
}

fun main() {
    val string = TGeoManager::class.java.getResourceAsStream("/BM@N.root.json")!!
        .readAllBytes().decodeToString()
    val json = Json.parseToJsonElement(string)
    val sizes = json.countTypes().groupBy { it }.mapValues { it.value.size }
    sizes.forEach {
        println(it)
    }

    val time = measureTimeMillis {
        val geo = TObject.decodeFromString(TGeoManager.serializer(), string)
        val solid = geo.toSolid()

        println(solid)
    }

    println(Duration.ofMillis(time))
}