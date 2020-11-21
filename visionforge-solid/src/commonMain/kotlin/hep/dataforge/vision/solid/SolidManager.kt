package hep.dataforge.vision.solid

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionGroup
import hep.dataforge.vision.VisionGroupBase
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.VisionManager.Companion.VISION_SERIALIZER_MODULE_TARGET
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass


public class SolidManager(meta: Meta) : AbstractPlugin(meta) {

    public val visionManager: VisionManager by require(VisionManager)

    override val tag: PluginTag get() = Companion.tag

    override fun content(target: String): Map<Name, Any> = when (target) {
        VISION_SERIALIZER_MODULE_TARGET -> mapOf(tag.name.toName() to serializersModuleForSolids)
        else -> super.content(target)
    }

    public companion object : PluginFactory<SolidManager> {
        override val tag: PluginTag = PluginTag(name = "visual.spatial", group = PluginTag.DATAFORGE_GROUP)
        override val type: KClass<out SolidManager> = SolidManager::class
        override fun invoke(meta: Meta, context: Context): SolidManager = SolidManager(meta)

        private fun PolymorphicModuleBuilder<Solid>.solids() {
            subclass(SolidGroup.serializer())
            subclass(SolidReference.serializer())
            subclass(Composite.serializer())
            subclass(Tube.serializer())
            subclass(Box.serializer())
            subclass(Convex.serializer())
            subclass(Extruded.serializer())
            subclass(PolyLine.serializer())
            subclass(SolidLabel.serializer())
            subclass(Sphere.serializer())
        }

        public val serializersModuleForSolids: SerializersModule = SerializersModule {
            polymorphic(Vision::class) {
                subclass(VisionGroupBase.serializer())
                solids()
            }

            polymorphic(Solid::class) {
                solids()
            }
        }

        internal val jsonForSolids: Json = Json(VisionManager.defaultJson){
            serializersModule = serializersModuleForSolids
        }

        public fun encodeToString(solid: Solid): String = jsonForSolids.encodeToString(Vision.serializer(), solid)

        public fun decodeFromString(str: String): Vision = jsonForSolids.decodeFromString(Vision.serializer(), str).also {
            if(it is VisionGroup){
                it.attachChildren()
            }
        }
    }
}
