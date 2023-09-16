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
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.*
import space.kscience.visionforge.html.VisionOutput
import space.kscience.visionforge.solid.specifications.Canvas3DOptions


public class Solids(meta: Meta) : VisionPlugin(meta), MutableVisionContainer<Solid> {
    override val tag: PluginTag get() = Companion.tag

    override val visionSerializersModule: SerializersModule get() = serializersModuleForSolids

    override fun setChild(name: Name?, child: Solid?) {
        child?.setAsRoot(visionManager)
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
            subclass(PolyLine.serializer())
            subclass(SolidLabel.serializer())
            subclass(Sphere.serializer())
            subclass(SphereLayer.serializer())

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

@VisionBuilder
public inline fun VisionOutput.solid(options: Canvas3DOptions? = null, block: SolidGroup.() -> Unit): SolidGroup {
    requirePlugin(Solids)
    options?.let {
        meta = options.meta
    }
    return SolidGroup().apply(block).apply {
        if (children.values.none { it is LightSource }) {
            ambientLight()
        }
    }
}

@VisionBuilder
public inline fun VisionOutput.solid(options: Canvas3DOptions.() -> Unit, block: SolidGroup.() -> Unit): SolidGroup =
    solid(Canvas3DOptions(options), block)
