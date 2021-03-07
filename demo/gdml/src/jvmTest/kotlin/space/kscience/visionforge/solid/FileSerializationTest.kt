package space.kscience.visionforge.solid

import org.junit.jupiter.api.Test
import space.kscience.dataforge.names.asName
import kotlin.test.Ignore

class FileSerializationTest {
    @Test
    @Ignore
    fun testFileRead(){
        val text = this::class.java.getResourceAsStream("/cubes.json").readBytes().decodeToString()
        val visual = Solids.decodeFromString(text) as SolidGroup
        visual["composite_001".asName()]
    }
}