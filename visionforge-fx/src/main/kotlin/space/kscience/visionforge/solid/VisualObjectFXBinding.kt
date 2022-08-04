package space.kscience.visionforge.solid

import javafx.application.Platform
import javafx.beans.binding.*
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.startsWith
import space.kscience.dataforge.values.Value
import space.kscience.visionforge.Vision
import space.kscience.visionforge.getProperty
import space.kscience.visionforge.onPropertyChange
import tornadofx.*

/**
 * A caching binding collection for [Vision] properties
 */
public class VisualObjectFXBinding(public val fx: FX3DPlugin, public val obj: Vision) {
    private val bindings = HashMap<Name, ObjectBinding<Meta?>>()

    init {
        obj.onPropertyChange { name ->
            bindings.filter { it.key.startsWith(name) }.forEach { entry ->
                Platform.runLater {
                    entry.value.invalidate()
                }
            }
//            var currentName = name
//            while (!currentName.isEmpty()) {
//                //recursively update all upper level bindings
//                bindings[currentName]?.invalidate()
//                currentName = currentName.cutLast()
//            }
        }
    }

    public operator fun get(key: Name): ObjectBinding<Meta?> {
        return bindings.getOrPut(key) {
            object : ObjectBinding<Meta?>() {
                override fun computeValue(): Meta = obj.getProperty(key)
            }
        }
    }

    public operator fun get(key: String): ObjectBinding<Meta?> = get(Name.parse(key))
}

public fun ObjectBinding<Meta?>.value(): Binding<Value?> = objectBinding { it?.value }
public fun ObjectBinding<Meta?>.string(): StringBinding = stringBinding { it.string }
public fun ObjectBinding<Meta?>.number(): Binding<Number?> = objectBinding { it.number }
public fun ObjectBinding<Meta?>.double(): Binding<Double?> = objectBinding { it.double }
public fun ObjectBinding<Meta?>.float(): Binding<Float?> = objectBinding { it.float }
public fun ObjectBinding<Meta?>.int(): Binding<Int?> = objectBinding { it.int }
public fun ObjectBinding<Meta?>.long(): Binding<Long?> = objectBinding { it.long }

public fun ObjectBinding<Meta?>.string(default: String): StringBinding = stringBinding { it.string ?: default }
public fun ObjectBinding<Meta?>.double(default: Double): DoubleBinding = doubleBinding { it.double ?: default }
public fun ObjectBinding<Meta?>.float(default: Float): FloatBinding = floatBinding { it.float ?: default }
public fun ObjectBinding<Meta?>.int(default: Int): IntegerBinding = integerBinding { it.int ?: default }
public fun ObjectBinding<Meta?>.long(default: Long): LongBinding = longBinding { it.long ?: default }

public fun <T> ObjectBinding<Meta?>.transform(transform: (Meta) -> T): Binding<T?> = objectBinding { it?.let(transform) }
