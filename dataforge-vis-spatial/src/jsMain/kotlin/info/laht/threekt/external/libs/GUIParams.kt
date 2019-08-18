package info.laht.threekt.external.libs

/**
 * @param name The name of this GUI
 * @param load JSON object representing the saved state of this GUI
 * @param auto default true
 * @param parent The GUI I'm nested in
 * @param closed If true, starts closed
 * @param closeOnTop If true, close/open button shows on top of the GUI
 * @param width
 */
data class GUIParams(
    var name: String? = undefined,
    var auto: Boolean? = undefined,
    var load: dynamic = undefined,
    var parent: dat.GUI? = undefined,
    var closed: Boolean? = undefined,
    var closeOnTop: Boolean? = undefined,
    var autoPlace: Boolean? = undefined,
    var width: Int? = undefined
)