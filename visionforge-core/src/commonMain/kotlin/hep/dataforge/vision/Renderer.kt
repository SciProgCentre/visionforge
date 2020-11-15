package hep.dataforge.vision

public fun interface Renderer<in V: Vision> {
    public fun render(vision: V)
}