package hep.dataforge.vision.examples

import com.github.ricky12awesome.jss.encodeToSchema
import hep.dataforge.vision.solid.SolidGroup
import hep.dataforge.vision.solid.Solids
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@ExperimentalSerializationApi
fun main() {
    val schema = Json {
        serializersModule = Solids.serializersModuleForSolids
        prettyPrintIndent = "  "
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }.encodeToSchema(SolidGroup.serializer(), generateDefinitions = false)
    println(schema)
}