package hep.dataforge.vision.spatial

import hep.dataforge.names.asName
import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class FileSerializationTest {
    @Test
    @Ignore
    fun testFileRead(){
        val text = this::class.java.getResourceAsStream("/cubes.json").readBytes().decodeToString()
        val visual = VisionGroup3D.parseJson(text)
        visual["composite_001".asName()]
    }
}