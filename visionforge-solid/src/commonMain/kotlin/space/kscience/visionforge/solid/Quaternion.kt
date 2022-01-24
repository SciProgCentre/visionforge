package space.kscience.visionforge.solid

import kotlin.jvm.JvmInline

@JvmInline
public value class Quaternion(public val values: DoubleArray)

public operator fun Quaternion.component1(): Double = values[0]
public operator fun Quaternion.component2(): Double = values[1]
public operator fun Quaternion.component3(): Double = values[2]
public operator fun Quaternion.component4(): Double = values[3]