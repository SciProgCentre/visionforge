package space.kscience.visionforge.solid

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.*
import space.kscience.visionforge.html.VisionOutput
import kotlin.reflect.KClass


public class Solids(meta: Meta) : VisionPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag
    override val visionSerializersModule: SerializersModule get() = serializersModuleForSolids

    public companion object : PluginFactory<Solids> {
        override val tag: PluginTag = PluginTag(name = "vision.solid", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out Solids> = Solids::class
        override fun invoke(meta: Meta, context: Context): Solids = Solids(meta)

        private fun PolymorphicModuleBuilder<Solid>.solids() {
            subclass(SolidGroup.serializer())
            subclass(SolidReferenceGroup.serializer())
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
        }

        public val serializersModuleForSolids: SerializersModule = SerializersModule {
            polymorphic(Vision::class) {
                subclass(VisionBase.serializer())
                subclass(VisionGroupBase.serializer())
                solids()
            }

            polymorphic(Solid::class) {
                default { SolidBase.serializer() }
                solids()
            }
        }

        internal val jsonForSolids: Json = Json(VisionManager.defaultJson) {
            serializersModule = serializersModuleForSolids
        }

        public fun encodeToString(solid: Solid): String =
            jsonForSolids.encodeToString(PolymorphicSerializer(Vision::class), solid)

        public fun decodeFromString(str: String): Solid =
            jsonForSolids.decodeFromString(PolymorphicSerializer(Solid::class), str)
    }
}

@VisionBuilder
@DFExperimental
public inline fun VisionOutput.solid(block: SolidGroup.() -> Unit): SolidGroup = SolidGroup().apply(block)
