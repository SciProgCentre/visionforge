package hep.dataforge.vision.solid

import hep.dataforge.names.asName
import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class FileSerializationTest {
    @Test
    @Ignore
    fun testFileRead(){
        val text = this::class.java.getResourceAsStream("/cubes.json").readBytes().decodeToString()
        val visual = SolidGroup.decodeFromString(text)
        visual["composite_001".asName()]
    }
}