package space.kscience.visionforge.html

import org.jetbrains.compose.web.ExperimentalComposeWebApi
import org.jetbrains.compose.web.css.*


@OptIn(ExperimentalComposeWebApi::class)
public object TreeStyles : StyleSheet(VisionForgeStyles) {
    /**
     * Remove default bullets
     */
    public val tree: String by style {
        paddingLeft(10.px)
        marginLeft(0.px)
        listStyleType("none")
    }

    /**
     * Style the caret/arrow
     */
    public val treeCaret: String by style {
        cursor("pointer")
        userSelect(UserSelect.none)
        /* Create the caret/arrow with a unicode, and style it */
        (self + before) {
            content("\u25B6")
            color(Color.black)
            display(DisplayStyle.InlineBlock)
            marginRight(6.px)
        }
    }

    /**
     *  Rotate the caret/arrow icon when clicked on (using JavaScript)
     */
    public val treeCaretDown: String by style {
        (self + before) {
            content("\u25B6")
            color(Color.black)
            display(DisplayStyle.InlineBlock)
            marginRight(6.px)
            transform { rotate(90.deg) }
        }
    }

    public val treeItem: String by style {
        alignItems(AlignItems.Center)
        paddingLeft(10.px)
        property("border-left", CSSBorder().apply{
            width(1.px)
            color(Color.lightgray)
            style = LineStyle.Dashed
        })
    }

    public val treeLabel: String by style {
        border(style = LineStyle.None)
        paddingAll(left = 4.pt, right = 4.pt)
        textAlign("left")
        flex(1)
    }

    public val treeLabelInactive: String by style {
        color(Color.lightgray)
    }

    public val treeLabelSelected: String by style {
        backgroundColor(Color.lightblue)
    }

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