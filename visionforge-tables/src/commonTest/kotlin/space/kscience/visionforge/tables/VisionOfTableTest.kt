package space.kscience.visionforge.tables

import space.kscience.dataforge.values.Value
import space.kscience.dataforge.values.asValue
import space.kscience.dataforge.values.double
import space.kscience.dataforge.values.int
import space.kscience.tables.ColumnHeader
import space.kscience.tables.ColumnTable
import space.kscience.tables.get
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals

internal class VisionOfTableTest {
    @Test
    fun tableSerialization() {
        val x by ColumnHeader.typed<Value>()
        val y by ColumnHeader.typed<Value>()

        val table = ColumnTable(100) {
            x.fill { it.asValue() }
            y.values = x.values.map { it?.double?.pow(2)?.asValue() }
        }

        val vision = table.toVision()
        //println(Json.encodeToString(VisionOfTable.serializer(), table.toVision()))

        val rows = vision.rowSequence().toList()

        assertEquals(50, rows[50][x].int)
    }
}