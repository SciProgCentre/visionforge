package hep.dataforge.vis.spatial.gdml

import scientifik.gdml.GDMLPosition
import scientifik.gdml.GDMLRotation
import scientifik.gdml.GDMLSolid
import kotlin.math.PI

enum class LUnit(val value: Double) {
    MM(1.0),
    CM(10.0),
    M(1000.0)
}

enum class AUnit(val value: Double) {
    DEG(PI / 180),
    RAD(1.0),
    RADIAN(1.0)
}

fun GDMLPosition.unit(): LUnit = LUnit.valueOf(unit.toUpperCase())

fun GDMLPosition.x(unit: LUnit): Double = if (unit.name == this.unit) {
    x.toDouble()
} else {
    x.toDouble() / unit.value * unit().value
}

fun GDMLPosition.y(unit: LUnit): Double = if (unit.name == this.unit) {
    y.toDouble()
} else {
    y.toDouble() / unit.value * unit().value
}

fun GDMLPosition.z(unit: LUnit): Double = if (unit.name == this.unit) {
    z.toDouble()
} else {
    z.toDouble() / unit.value * unit().value
}

fun GDMLRotation.unit(): AUnit = AUnit.valueOf(unit.toUpperCase())

fun GDMLRotation.x(unit: AUnit = AUnit.RAD): Double = if (unit.name == this.unit) {
    x.toDouble()
} else {
    x.toDouble() / unit.value * unit().value
}

fun GDMLRotation.y(unit: AUnit = AUnit.RAD): Double = if (unit.name == this.unit) {
    y.toDouble()
} else {
    y.toDouble() / unit.value * unit().value
}

fun GDMLRotation.z(unit: AUnit = AUnit.RAD): Double = if (unit.name == this.unit) {
    z.toDouble()
} else {
    z.toDouble() / unit.value * unit().value
}

fun GDMLSolid.lscale(unit: LUnit): Double {
    val solidUnit = lunit?.let { LUnit.valueOf(it.toUpperCase()) } ?: return 1.0
    return if (solidUnit == unit) {
        1.0
    } else {
        solidUnit.value / unit.value
    }
}

fun GDMLSolid.ascale(unit: AUnit = AUnit.RAD): Double {
    val solidUnit = aunit?.let { AUnit.valueOf(it.toUpperCase()) } ?: return 1.0
    return if (solidUnit == unit) {
        1.0
    } else {
        solidUnit.value / unit.value
    }
}