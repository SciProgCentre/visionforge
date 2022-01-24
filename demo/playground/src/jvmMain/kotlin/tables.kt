package space.kscience.visionforge.examples

import space.kscience.dataforge.values.ValueType
import space.kscience.tables.ColumnHeader
import space.kscience.tables.valueRow
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.tables.table
import kotlin.math.pow

fun main() {
    val x by ColumnHeader.value(ValueType.NUMBER)
    val y by ColumnHeader.value(ValueType.NUMBER)

    makeVisionFile(resourceLocation = ResourceLocation.SYSTEM) {
        vision {
            table(x, y) {
                repeat(100) {
                    valueRow(x to it, y to it.toDouble().pow(2))
                }
            }
        }
    }
}