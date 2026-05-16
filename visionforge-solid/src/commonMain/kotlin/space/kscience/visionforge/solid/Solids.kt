package space.kscience.visionforge.solid

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.serializer
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.NameToken
import space.kscience.visionforge.*
import space.kscience.visionforge.html.VisionOutput
import space.kscience.visionforge.solid.specifications.Canvas3DOptions

/**
 * The `Solids` class represents a plugin for managing and serializing 3D solid objects
 * in a vision-based system. It extends `VisionPlugin` and implements `MutableVisionContainer<Solid>`.
 *
 * This class facilitates the serialization, deserialization, and manipulation of various
 * 3D solid objects while integrating with the vision management system.
 *
 * Primary features include:
 * - A comprehensive serializers module specifically designed for various types of solid objects.
 * - Utilities for encoding and decoding solid objects to and from JSON representations.
 * - The ability to set a specific solid as the root within the vision management system.
 *
 * @constructor Initializes the plugin with the provided metadata.
 * @param meta Metadata necessary to configure this plugin.
 *
 * Properties:
 * @property tag A unique identifier for this plugin.
 * @property visionSerializersModule Module providing serializers for all supported solid types.
 *
 * Functions:
 * @function setVision Sets or replaces a solid in the vision container, assigning it as the root
 * in the associated `VisionManager`.
 *
 * Companion Object:
 * - Handles the creation of `Solids` instances.
 * - Provides a serializers module supporting polymorphic serialization of `Solid` objects.
 * - Contains utility methods for encoding and decoding solids to/from JSON strings.
 */
public class Solids(meta: Meta) : VisionPlugin(meta), MutableVisionContainer<Solid> {
    override val tag: PluginTag get() = Companion.tag

    override val visionSerializersModule: SerializersModule get() = serializersModuleForSolids

    override fun setVision(token: NameToken, vision: Solid?) {
        vision?.setAsRoot(visionManager)
    }

    public companion object : PluginFactory<Solids> {
        override val tag: PluginTag = PluginTag(name = "vision.solid", group = PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): Solids = Solids(meta)

        private fun PolymorphicModuleBuilder<Solid>.solids() {
            subclass(SolidGroup.serializer())
            subclass(SolidReference.serializer())
            subclass(Composite.serializer())
            subclass(Box.serializer())
            subclass(GenericHexagon.serializer())
            subclass(ConeSegment.serializer())
            subclass(ConeSurface.serializer())
            subclass(Convex.serializer())
            subclass(Extruded.serializer())
            subclass(Surface.serializer())
            subclass(PolyLine.serializer())
            subclass(SolidLabel.serializer())
            subclass(Sphere.serializer())
            subclass(SphereLayer.serializer())
            subclass(CutTube.serializer())

            subclass(AmbientLightSource.serializer())
            subclass(PointLightSource.serializer())

            subclass(AxesSolid.serializer())
        }

        public val serializersModuleForSolids: SerializersModule = SerializersModule {

            polymorphic(Vision::class) {
                subclass(SimpleVisionGroup.serializer())
                solids()
            }

            polymorphic(Solid::class) {
                defaultDeserializer { SolidBase.serializer(serializer<Solid>()) }
                solids()
            }
        }

        internal val jsonForSolids: Json = Json(VisionManager.defaultJson) {
            encodeDefaults = false
            serializersModule = serializersModuleForSolids
        }

        public fun encodeToString(solid: Solid): String =
            jsonForSolids.encodeToString(PolymorphicSerializer(Vision::class), solid)

        public fun decodeFromString(str: String): Solid =
            jsonForSolids.decodeFromString(PolymorphicSerializer(Solid::class), str)

//        override fun setChild(name: Name?, child: Solid?) {
//            default.setChild(name, child)
//        }
    }
}

public inline fun VisionOutput.solid(options: Canvas3DOptions? = null, block: SolidGroup.() -> Unit): SolidGroup {
    requirePlugin(Solids)
    options?.let {
        meta = options.meta
    }
    return SolidGroup().apply(block).apply {
        if (visions.values.none { it is LightSource }) {
            ambientLight()
        }
    }
}

public inline fun VisionOutput.solid(options: Canvas3DOptions.() -> Unit, block: SolidGroup.() -> Unit): SolidGroup =
    solid(Canvas3DOptions(options), block)
