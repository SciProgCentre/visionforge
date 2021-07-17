package space.kscience.visionforge.solid

import javafx.application.Platform
import javafx.beans.binding.*
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.startsWith
import space.kscience.dataforge.names.toName
import space.kscience.dataforge.values.Value
import space.kscience.visionforge.Vision
import space.kscience.visionforge.onPropertyChange
import tornadofx.*

/**
 * A caching binding collection for [Vision] properties
 */
public class VisualObjectFXBinding(public val fx: FX3DPlugin, public val obj: Vision) {
    private val bindings = HashMap<Name, ObjectBinding<MetaItem?>>()

    init {
        obj.onPropertyChange(fx.context) { name ->
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

    public operator fun get(key: Name): ObjectBinding<MetaItem?> {
        return bindings.getOrPut(key) {
            object : ObjectBinding<MetaItem?>() {
                override fun computeValue(): MetaItem? = obj.getProperty(key)
            }
        }
    }

    public operator fun get(key: String) = get(key.toName())
}

public fun ObjectBinding<MetaItem?>.value(): Binding<Value?> = objectBinding { it.value }
public fun ObjectBinding<MetaItem?>.string(): StringBinding = stringBinding { it.string }
public fun ObjectBinding<MetaItem?>.number(): Binding<Number?> = objectBinding { it.number }
public fun ObjectBinding<MetaItem?>.double(): Binding<Double?> = objectBinding { it.double }
public fun ObjectBinding<MetaItem?>.float(): Binding<Float?> = objectBinding { it.float }
public fun ObjectBinding<MetaItem?>.int(): Binding<Int?> = objectBinding { it.int }
public fun ObjectBinding<MetaItem?>.long(): Binding<Long?> = objectBinding { it.long }
public fun ObjectBinding<MetaItem?>.node(): Binding<Meta?> = objectBinding { it.node }

public fun ObjectBinding<MetaItem?>.string(default: String): StringBinding = stringBinding { it.string ?: default }
public fun ObjectBinding<MetaItem?>.double(default: Double): DoubleBinding = doubleBinding { it.double ?: default }
public fun ObjectBinding<MetaItem?>.float(default: Float): FloatBinding = floatBinding { it.float ?: default }
public fun ObjectBinding<MetaItem?>.int(default: Int): IntegerBinding = integerBinding { it.int ?: default }
public fun ObjectBinding<MetaItem?>.long(default: Long): LongBinding = longBinding { it.long ?: default }

public fun <T> ObjectBinding<MetaItem?>.transform(transform: (MetaItem) -> T): Binding<T?> = objectBinding { it?.let(transform) }
