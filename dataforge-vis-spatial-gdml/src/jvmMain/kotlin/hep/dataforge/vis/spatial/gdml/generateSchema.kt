package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.JSON_PRETTY
import hep.dataforge.meta.Meta
import hep.dataforge.vis.spatial.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerialModuleCollector
import kotlin.reflect.KClass

internal val SerialDescriptor.jsonType
    get() = when (this.kind) {
        StructureKind.LIST -> "array"
        PrimitiveKind.BYTE, PrimitiveKind.SHORT, PrimitiveKind.INT, PrimitiveKind.LONG,
        PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE -> "number"
        PrimitiveKind.STRING, PrimitiveKind.CHAR, UnionKind.ENUM_KIND -> "string"
        PrimitiveKind.BOOLEAN -> "boolean"
        else -> "object"
    }


private fun SerialModule.enumerate(type: KClass<*>): Sequence<SerialDescriptor> {
    val list = ArrayList<SerialDescriptor>()
    fun send(descriptor: SerialDescriptor) = list.add(descriptor)

    val enumerator = object : SerialModuleCollector {
        override fun <T : Any> contextual(kClass: KClass<T>, serializer: KSerializer<T>) {
            if (kClass == type) {
                send(serializer.descriptor)
            }
        }

        override fun <Base : Any, Sub : Base> polymorphic(
            baseClass: KClass<Base>,
            actualClass: KClass<Sub>,
            actualSerializer: KSerializer<Sub>
        ) {
            if (baseClass == type) {
                send(actualSerializer.descriptor)
            }
        }

    }
    dumpTo(enumerator)
    return list.asSequence()
}

/**
 * Creates an [JsonObject] which contains Json Schema of given [descriptor].
 *
 * Schema can contain following fields:
 * `description`, `type` for all descriptors;
 * `properties` and `required` for objects;
 * `enum` for enums;
 * `items` for arrays.
 *
 * User can modify this schema to add additional validation keywords
 * (as per [https://json-schema.org/latest/json-schema-validation.html])
 * if they want.
 */
private fun jsonSchema(descriptor: SerialDescriptor, context: SerialModule): JsonObject {

    if (descriptor.serialName in arrayOf(
            "hep.dataforge.vis.spatial.Point3D",
            "hep.dataforge.vis.spatial.Point2D",
            Meta::class.qualifiedName
        )
    ) return json {
        "\$ref" to "#/definitions/${descriptor.serialName}"
    }


    val properties: MutableMap<String, JsonObject> = mutableMapOf()
    val requiredProperties: MutableSet<String> = mutableSetOf()
    val isEnum = descriptor.kind == UnionKind.ENUM_KIND
    val isPolymorphic = descriptor.kind is PolymorphicKind


    if (!isEnum && !isPolymorphic) descriptor.elementDescriptors().forEachIndexed { index, child ->
        val elementName = descriptor.getElementName(index)

        properties[elementName] = when (elementName) {
            "templates" ->  json {
                "\$ref" to "#/definitions/hep.dataforge.vis.spatial.VisualGroup3D"
            }
            "properties" ->  json {
                "\$ref" to "#/definitions/${Meta::class.qualifiedName}"
            }
            "first", "second" -> json{
                "\$ref" to "#/definitions/children"
            }
            "styleSheet" -> json {
                "type" to "object"
                "additionalProperties" to json {
                    "\$ref" to "#/definitions/${Meta::class.qualifiedName}"
                }
            }
            in arrayOf("children") ->  json {
                "type" to "object"
                "additionalProperties" to json {
                    "\$ref" to "#/definitions/children"
                }
            }
            else -> jsonSchema(child, context)
        }

        if (!descriptor.isElementOptional(index)) requiredProperties.add(elementName)
    }

    val jsonType = descriptor.jsonType
    val objectData: MutableMap<String, JsonElement> = mutableMapOf(
        "description" to JsonLiteral(descriptor.serialName),
        "type" to JsonLiteral(jsonType)
    )
    if (isEnum) {
        val allElementNames = (0 until descriptor.elementsCount).map(descriptor::getElementName)
        objectData += "enum" to JsonArray(allElementNames.map(::JsonLiteral))
    }
    when (jsonType) {
        "object" -> {
            objectData["properties"] = JsonObject(properties)
            val required = requiredProperties.map { JsonLiteral(it) }
            if (required.isNotEmpty()) {
                objectData["required"] = JsonArray(required)
            }
        }
        "array" -> objectData["items"] = properties.values.let {
            check(it.size == 1) { "Array descriptor has returned inconsistent number of elements: expected 1, found ${it.size}" }
            it.first()
        }
        else -> { /* no-op */
        }
    }
    return JsonObject(objectData)
}

fun main() {
    val context = Visual3D.serialModule
    val definitions = json {
        "children" to json {
            "anyOf" to jsonArray {
                context.enumerate(VisualObject3D::class).forEach {
                    if (it.serialName == "hep.dataforge.vis.spatial.VisualGroup3D") {
                        +json {
                            "\$ref" to "#/definitions/${it.serialName}"
                        }
                    } else {
                        +jsonSchema(it, context)
                    }
                }
            }
        }
        "hep.dataforge.vis.spatial.Point3D" to jsonSchema(Point3DSerializer.descriptor, context)
        "hep.dataforge.vis.spatial.Point2D" to jsonSchema(Point2DSerializer.descriptor, context)
        "hep.dataforge.vis.spatial.VisualGroup3D" to jsonSchema(VisualGroup3D.serializer().descriptor, context)

    }

    println(
        JSON_PRETTY.stringify(
            JsonObjectSerializer,
            json {
                "definitions" to definitions
                "\$ref" to "#/definitions/hep.dataforge.vis.spatial.VisualGroup3D"
            }
        )
    )
}

