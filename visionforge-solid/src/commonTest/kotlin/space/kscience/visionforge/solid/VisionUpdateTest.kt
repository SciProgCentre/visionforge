package space.kscience.visionforge.solid

import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.fetch
import space.kscience.dataforge.meta.MetaItem
import space.kscience.dataforge.names.toName
import space.kscience.visionforge.VisionChange
import space.kscience.visionforge.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VisionUpdateTest {
    val solidManager = Global.fetch(Solids)
    val visionManager = solidManager.visionManager

    @Test
    fun testVisionUpdate(){
        val targetVision = SolidGroup {
            box(200,200,200, name = "origin")
        }
        val dif = VisionChange(visionManager){
            group("top") {
                color(123)
                box(100,100,100)
            }
            propertyChanged("top".toName(), SolidMaterial.MATERIAL_COLOR_KEY, MetaItem.of("red"))
            propertyChanged("origin".toName(), SolidMaterial.MATERIAL_COLOR_KEY, MetaItem.of("red"))
        }
        targetVision.update(dif)
        assertTrue { targetVision["top"] is SolidGroup }
        assertEquals("red", (targetVision["origin"] as Solid).color.string) // Should work
        assertEquals("#00007b", (targetVision["top"] as Solid).color.string) // new item always takes precedence
    }

    @Test
    fun testVisionChangeSerialization(){
        val change = VisionChange(visionManager){
            group("top") {
                color(123)
                box(100,100,100)
            }
            propertyChanged("top".toName(), SolidMaterial.MATERIAL_COLOR_KEY, MetaItem.of("red"))
            propertyChanged("origin".toName(), SolidMaterial.MATERIAL_COLOR_KEY, MetaItem.of("red"))
        }
        val serialized = visionManager.jsonFormat.encodeToString(VisionChange.serializer(), change)
        println(serialized)
        val reconstructed = visionManager.jsonFormat.decodeFromString(VisionChange.serializer(), serialized)
        assertEquals(change.properties,reconstructed.properties)
    }

    @Test
    fun testDeserialization(){
        val str = """
            {
                "propertyChange": {
                    "layer[4]": {
                        "material": {
                            "color": 123
                        }
                    },
                    "layer[2]": {
                        "material": {
                        }
                    }
                },
                "childrenChange": {
                }
            }
        """.trimIndent()

        val reconstructed = visionManager.jsonFormat.decodeFromString(VisionChange.serializer(), str)
    }

}