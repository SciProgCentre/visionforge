package hep.dataforge.js

@JsName("require")
external fun requireJS(name: String): dynamic

inline fun <T : Any> jsObject(builder: T.() -> Unit): T {
    val obj: T = js("({})") as T
    return obj.apply {
        builder()
    }
}

inline fun js(builder: dynamic.() -> Unit): dynamic = jsObject(builder)

//fun <T : Any> clone(obj: T) = objectAssign(jsObject<T> {}, obj)

//inline fun <T : Any> assign(obj: T, builder: T.() -> Unit) = clone(obj).apply(builder)

fun toPlainObjectStripNull(obj: Any) = js {
    for (key in Object.keys(obj)) {
        val value = obj.asDynamic()[key]
        if (value != null) this[key] = value
    }
}