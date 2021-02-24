package hep.dataforge.vision.examples

import com.github.ricky12awesome.jss.encodeToSchema
import hep.dataforge.vision.solid.SolidGroup
import hep.dataforge.vision.solid.SolidManager
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@ExperimentalSerializationApi
fun main() {
    val schema = Json {
        serializersModule = SolidManager.serializersModuleForSolids
        prettyPrintIndent = "  "
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }.encodeToSchema(SolidGroup.serializer(), generateDefinitions = false)
    println(schema)
}