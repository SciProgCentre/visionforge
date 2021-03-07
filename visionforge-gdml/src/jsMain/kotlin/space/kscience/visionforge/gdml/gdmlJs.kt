package space.kscience.visionforge.gdml

public actual class Counter {
    private var count: Int = 0
    public actual fun get(): Int = count

    public actual fun incrementAndGet(): Int = count++
}