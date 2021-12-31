package space.kscience.visionforge

import org.w3c.xhr.FormData
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.stringList
import kotlin.test.Test
import kotlin.test.assertEquals

class FormTest {
    @Test
    fun testFormConversion() {
        val fd = FormData()
        fd.append("a", "22")
        fd.append("b", "1")
        fd.append("b", "2")
        val meta = fd.toMeta()
        assertEquals(22, meta["a"].int)
        assertEquals(listOf("1","2"), meta["b"]?.stringList)
    }

}