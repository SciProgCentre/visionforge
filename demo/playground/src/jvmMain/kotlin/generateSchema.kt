package space.kscience.visionforge.examples

import com.github.ricky12awesome.jss.encodeToSchema
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.Solids

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    serializersModule = Solids.serializersModuleForSolids
    prettyPrintIndent = "  "
    prettyPrint = true
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    encodeDefaults = true
}

@ExperimentalSerializationApi
fun main() {
    val schema = json.encodeToSchema(SolidGroup.serializer(), generateDefinitions = false)
    println(schema)
}