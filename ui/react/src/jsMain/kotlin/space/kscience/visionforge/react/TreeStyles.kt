package space.kscience.visionforge.react

import kotlinx.css.*
import kotlinx.css.properties.deg
import kotlinx.css.properties.rotate
import styled.StyleSheet

public object TreeStyles : StyleSheet("treeStyles", true) {
    /**
     * Remove default bullets
     */
    public val tree: RuleSet by css {
        paddingLeft = 5.px
        marginLeft = 0.px
        listStyleType = ListStyleType.none
    }

    /**
     * Style the caret/arrow
     */
    public val treeCaret: RuleSet by css {
        cursor = Cursor.pointer
        userSelect = UserSelect.none
        /* Create the caret/arrow with a unicode, and style it */
        before {
            content = "\u25B6".quoted
            color = Color.black
            display = Display.inlineBlock
            marginRight = 6.px
        }
    }

    /**
     *  Rotate the caret/arrow icon when clicked on (using JavaScript)
     */
    public val treeCaredDown:RuleSet by css {
        before {
            content = "\u25B6".quoted
            color = Color.black
            display = Display.inlineBlock
            marginRight = 6.px
            transform.rotate(90.deg)
        }
    }

    public val treeItem:RuleSet by css {
        alignItems = Align.center
        paddingLeft = 10.px
        borderLeftStyle = BorderStyle.dashed
        borderLeftWidth = 1.px
        borderLeftColor = Color.lightGray
    }

    public val treeLabel:RuleSet by css {
        border = Border.none
        padding = Padding(left = 4.pt, right = 4.pt, top = 0.pt, bottom = 0.pt)
        textAlign = TextAlign.left
        flex = Flex(1.0)
    }

    public val treeLabelInactive: RuleSet by css {
        color = Color.lightGray
    }

    public val treeLabelSelected:RuleSet by css {
        backgroundColor = Color.lightBlue
    }

}