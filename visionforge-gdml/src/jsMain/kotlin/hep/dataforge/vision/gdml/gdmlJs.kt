package hep.dataforge.vision.gdml

actual class Counter {
    private var count: Int = 0
    actual fun get(): Int = count

    actual fun incrementAndGet(): Int = count++
}