package space.kscience.visionforge.html

import org.jetbrains.compose.web.css.*

public object VisionForgeStyles: StyleSheet() {

    public val propertyEditorButton: String by style {
        width(24.px)
        alignSelf(AlignSelf.Stretch)
        marginAll(1.px, 5.px)
        backgroundColor(Color.white)
        border {
            style(LineStyle.Solid)
        }
        borderRadius(2.px)
        textAlign("center")
        textDecoration("none")
        cursor("pointer")
        (self + disabled)  {
            cursor("auto")
            border {
                style(LineStyle.Dashed)
            }
            color(Color.lightgray)
        }
    }

}