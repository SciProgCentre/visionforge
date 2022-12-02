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
import space.kscience.dataforge.context.fetch
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.*
import space.kscience.visionforge.html.VisionOutput
import kotlin.reflect.KClass


public class Solids(meta: Meta) : VisionPlugin(meta), MutableVisionContainer<Solid> {
    override val tag: PluginTag get() = Companion.tag

    override val visionSerializersModule: SerializersModule get() = serializersModuleForSolids

    override fun setChild(name: Name?, child: Solid?) {
        child?.setAsRoot(visionManager)
    }

    public companion object : PluginFactory<Solids>, MutableVisionContainer<Solid> {
        override val tag: PluginTag = PluginTag(name = "vision.solid", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out Solids> = Solids::class

        public val default: Solids by lazy {
            Context("@Solids") {
                plugin(Solids)
            }.fetch(Solids)
        }

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

            subclass(AmbientLightSource.serializer())
            subclass(PointLightSource.serializer())
        }

        public val serializersModuleForSolids: SerializersModule = SerializersModule {
            polymorphic(Vision::class) {
                subclass(SimpleVisionGroup.serializer())
                solids()
            }

            polymorphic(Solid::class) {
                default { SolidBase.serializer(serializer<Solid>()) }
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

        override fun setChild(name: Name?, child: Solid?) {
            default.setChild(name, child)
        }
    }
}

@VisionBuilder
@DFExperimental
public inline fun VisionOutput.solid(block: SolidGroup.() -> Unit): SolidGroup {
    requirePlugin(Solids)
    return SolidGroup().apply(block)
}
