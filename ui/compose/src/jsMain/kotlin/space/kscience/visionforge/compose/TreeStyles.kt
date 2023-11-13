package space.kscience.visionforge.compose

import kotlinx.css.*
import org.jetbrains.compose.web.css.*

public object TreeStyles : StyleSheet() {
    /**
     * Remove default bullets
     */
    public val tree: String by style {
        paddingLeft(5.px)
        marginLeft(0.px)
        listStyleType("none")
    }

    /**
     * Style the caret/arrow
     */
    public val treeCaret by style {
        cursor("pointer")
        userSelect = UserSelect.none
        /* Create the caret/arrow with a unicode, and style it */
        before {
            content = "\u25B6".quoted
            color(Color.black)
            display(DisplayStyle.InlineBlock)
            marginRight(6.px)
        }
    }

    /**
     *  Rotate the caret/arrow icon when clicked on (using JavaScript)
     */
    public val treeCaredDown by style {
        before {
            content = "\u25B6".quoted
            color(Color.black)
            display(DisplayStyle.InlineBlock)
            marginRight(6.px)
            transform { rotate(90.deg) }
        }
    }

    public val treeItem: String by style {
        alignItems(AlignItems.Center)
        paddingLeft(10.px)
        border {
            left{
                width(1.px)
                color(Color.lightgray)
                style = LineStyle.Dashed
            }
        }
    }

    public val treeLabel by style {
        border(style = LineStyle.None)
        padding(left = 4.pt, right = 4.pt, top = 0.pt, bottom = 0.pt)
        textAlign("left")
        flex(1)
    }

    public val treeLabelInactive: RuleSet by css {
        color = Color.lightGray
    }

    public val treeLabelSelected: RuleSet by css {
        backgroundColor = Color.lightBlue
    }

}