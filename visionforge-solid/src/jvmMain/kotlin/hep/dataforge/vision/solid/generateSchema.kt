//package hep.dataforge.vision.solid
//
//import hep.dataforge.meta.JSON_PRETTY
//import kotlinx.serialization.*
//import kotlinx.serialization.descriptors.PrimitiveKind
//import kotlinx.serialization.descriptors.SerialDescriptor
//import kotlinx.serialization.descriptors.StructureKind
//import kotlinx.serialization.descriptors.UnionKind
//import kotlinx.serialization.json.*
//import kotlinx.serialization.modules.SerialModule
//import kotlin.reflect.KClass
//
//private fun SerialDescriptor.getJsonType() = when (this.kind) {
//    StructureKind.LIST -> "array"
//    PrimitiveKind.BYTE, PrimitiveKind.SHORT, PrimitiveKind.INT, PrimitiveKind.LONG,
//    PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE -> "number"
//    PrimitiveKind.STRING, PrimitiveKind.CHAR, UnionKind.ENUM_KIND -> "string"
//    PrimitiveKind.BOOLEAN -> "boolean"
//    else -> "object"
//}
//
//private fun SerialDescriptor.isVisualObject() = serialName.startsWith("solid")||serialName.startsWith("group.solid")
//
//private const val definitionNode = "\$defs"
//
//private fun SerialModule.enumerate(type: KClass<*>): Sequence<SerialDescriptor> {
//    val list = ArrayList<SerialDescriptor>()
//    fun send(descriptor: SerialDescriptor) = list.add(descriptor)
//
//    val enumerator = object : SerialModuleCollector {
//        override fun <T : Any> contextual(kClass: KClass<T>, serializer: KSerializer<T>) {
//            if (kClass == type) {
//                send(serializer.descriptor)
//            }
//        }
//
//        override fun <Base : Any, Sub : Base> polymorphic(
//            baseClass: KClass<Base>,
//            actualClass: KClass<Sub>,
//            actualSerializer: KSerializer<Sub>
//        ) {
//            if (baseClass == type) {
//                send(actualSerializer.descriptor)
//            }
//        }
//
//    }
//    dumpTo(enumerator)
//    return list.asSequence()
//}
//
///**
// * Creates an [JsonObject] which contains Json Schema of given [descriptor].
// *
// * Schema can contain following fields:
// * `description`, `type` for all descriptors;
// * `properties` and `required` for objects;
// * `enum` for enums;
// * `items` for arrays.
// *
// * User can modify this schema to add additional validation keywords
// * (as per [https://json-schema.org/latest/json-schema-validation.html])
// * if they want.
// */
//private fun jsonSchema(descriptor: SerialDescriptor, context: SerialModule): JsonObject {
//
//    if (descriptor.serialName in arrayOf(
//            "hep.dataforge.vision.solid.Point3D",
//            "hep.dataforge.vision.solid.Point3D?",
//            "hep.dataforge.vision.solid.Point2D",
//            "hep.dataforge.vision.solid.Point2D?",
//            "hep.dataforge.meta.Meta",
//            "hep.dataforge.meta.Meta?"
//        )
//    ) return json {
//        "\$ref" to "#/$definitionNode/${descriptor.serialName.replace("?", "")}"
//    }
//
//
//    val properties: MutableMap<String, JsonObject> = mutableMapOf()
//    val requiredProperties: MutableSet<String> = mutableSetOf()
//    val isEnum = descriptor.kind == UnionKind.ENUM_KIND
//    val isPolymorphic = descriptor.kind is PolymorphicKind
//
//
//    if (!isEnum && !isPolymorphic) descriptor.elementDescriptors().forEachIndexed { index, child ->
//        val elementName = descriptor.getElementName(index)
//
//        val elementSchema = when (elementName) {
//            "properties" -> buildJsonObject {
//                put("\$ref", "#/$definitionNode/hep.dataforge.meta.Meta")
//            }
//            "first", "second" -> buildJsonObject {
//                put("\$ref", "#/$definitionNode/children")
//            }
//            "styleSheet" -> buildJsonObject {
//                put("type", "object")
//                put("additionalProperties", buildJsonObject {
//                    put("\$ref", "#/$definitionNode/hep.dataforge.meta.Meta")
//                })
//            }
//            in arrayOf("children", "prototypes") -> buildJsonObject {
//                put("type", "object")
//                put("additionalProperties", buildJsonObject {
//                    put("\$ref", "#/$definitionNode/children")
//                })
//            }
//            else -> jsonSchema(child, context)
//        }
//        properties[elementName] = elementSchema
//
//        if (!descriptor.isElementOptional(index)) requiredProperties.add(elementName)
//    }
//
//    val jsonType = descriptor.getJsonType()
//    val objectData: MutableMap<String, JsonElement> = mutableMapOf(
//        "description" to JsonLiteral(descriptor.serialName),
//        "type" to JsonLiteral(jsonType)
//    )
//    if (isEnum) {
//        val allElementNames = (0 until descriptor.elementsCount).map(descriptor::getElementName)
//        objectData += "enum" to JsonArray(allElementNames.map(::JsonLiteral))
//    }
//    when (jsonType) {
//        "object" -> {
//            if(descriptor.isVisualObject()) {
//                properties["type"] = json {
//                    "const" to descriptor.serialName
//                }
//            }
//            objectData["properties"] = JsonObject(properties)
//            val required = requiredProperties.map { JsonLiteral(it) }
//            if (required.isNotEmpty()) {
//                objectData["required"] = JsonArray(required)
//            }
//        }
//        "array" -> objectData["items"] = properties.values.let {
//            check(it.size == 1) { "Array descriptor has returned inconsistent number of elements: expected 1, found ${it.size}" }
//            it.first()
//        }
//        else -> { /* no-op */
//        }
//    }
//    return JsonObject(objectData)
//}
//
//fun main() {
//    val context = SolidManager.serialModule
//    val definitions = json {
//        "children" to json {
//            "anyOf" to jsonArray {
//                context.enumerate(Solid::class).forEach {
//                    if (it.serialName == "hep.dataforge.vis.spatial.SolidGroup") {
//                        +json {
//                            "\$ref" to "#/$definitionNode/${it.serialName}"
//                        }
//                    } else {
//                        +jsonSchema(it, context)
//                    }
//                }
//            }
//        }
//        "hep.dataforge.meta.Meta" to json {
//            "type" to "object"
//        }
//        "hep.dataforge.vision.solid.Point3D" to json {
//            "type" to "object"
//            "properties" to json {
//                "x" to json {
//                    "type" to "number"
//                }
//                "y" to json {
//                    "type" to "number"
//                }
//                "z" to json {
//                    "type" to "number"
//                }
//            }
//        }
//        "hep.dataforge.vision.solid.Point2D" to json {
//            "type" to "object"
//            "properties" to json {
//                "x" to json {
//                    "type" to "number"
//                }
//                "y" to json {
//                    "type" to "number"
//                }
//            }
//        }
//        "hep.dataforge.vision.solid.SolidGroup" to jsonSchema(
//            SolidGroup.serializer().descriptor,
//            context
//        )
//
//    }
//
//    println(
//        JSON_PRETTY.stringify(
//            JsonObjectSerializer,
//            json {
//                "\$defs" to definitions
//                "\$ref" to "#/$definitionNode/hep.dataforge.vision.solid.SolidGroup"
//            }
//        )
//    )
//}
//
