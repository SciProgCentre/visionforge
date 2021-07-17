package space.kscience.visionforge.solid

import kotlinx.serialization.json.encodeToJsonElement
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.get
import space.kscience.visionforge.style
import space.kscience.visionforge.useStyle
import kotlin.test.Test
import kotlin.test.assertEquals

@DFExperimental
class SolidReferenceTest {
    val groupWithReference = SolidGroup {
        val theStyle by style {
            SolidMaterial.MATERIAL_COLOR_KEY put "red"
        }
        ref("test", Box(100f,100f,100f).apply {
            color("blue")
            useStyle(theStyle)
        })
    }


    @Test
    fun testReferenceProperty(){
        assertEquals("blue", (groupWithReference["test"] as Solid).color.string)
    }

    @Test
    fun testReferenceSerialization(){
        val serialized = Solids.jsonForSolids.encodeToJsonElement(groupWithReference)
        val deserialized = Solids.jsonForSolids.decodeFromJsonElement(SolidGroup.serializer(), serialized)
        assertEquals("blue", (deserialized["test"] as Solid).color.string)
    }
}