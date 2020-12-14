package hep.dataforge.vision.react

import kotlinx.css.*
import kotlinx.css.properties.*
import styled.StyleSheet

public object TreeStyles : StyleSheet("treeStyles", true) {
    /**
     * Remove default bullets
     */
    public val tree: RuleSet by css {
        paddingLeft = 8.px
        marginLeft = 0.px
        listStyleType = ListStyleType.none
    }

    /**
     * Style the caret/arrow
     */
    public val treeCaret by css {
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
    val treeCaredDown by css {
        before {
            content = "\u25B6".quoted
            color = Color.black
            display = Display.inlineBlock
            marginRight = 6.px
            transform.rotate(90.deg)
        }
    }

    val treeItem by css {
        alignItems = Align.center
        paddingLeft = 10.px
        borderLeftStyle = BorderStyle.dashed
        borderLeftWidth = 1.px
        borderLeftColor = Color.lightGray
        borderBottomStyle = BorderStyle.dashed
        borderBottomWidth = 1.px
        borderBottomColor = Color.lightGray
    }

    val treeLeaf by css {
        display = Display.flex
        flexDirection = FlexDirection.row
        flexWrap = FlexWrap.nowrap
        //alignItems = Align.center
    }

    val treeLabel by css {
        overflow = Overflow.hidden
        flex(flexGrow = 1.0, flexShrink = 1.0)
    }

    val treeLabelInactive by css {
        color = Color.lightGray
    }

    val treeLabelSelected by css {
        backgroundColor = Color.lightBlue
    }

    val linkButton by css {
        backgroundColor = Color.white
        border = "none"
        padding(left = 4.pt, right = 4.pt, top = 0.pt, bottom = 0.pt)
        textAlign = TextAlign.left
        fontFamily = "arial,sans-serif"
        color = Color("#069")
        cursor = Cursor.pointer
        hover {
            textDecoration(TextDecorationLine.underline)
        }
    }

    val removeButton by css {
        backgroundColor = Color.white
        borderStyle = BorderStyle.solid
        borderRadius = 2.px
        padding(1.px, 5.px)
        marginLeft = 4.px
        textAlign = TextAlign.center
        textDecoration = TextDecoration.none
        display = Display.inlineBlock
        flexShrink = 1.0
        cursor = Cursor.pointer
        disabled {
            cursor = Cursor.auto
            borderStyle = BorderStyle.dashed
            color = Color.lightGray
        }
    }

    val resizeableInput by css {
        overflow = Overflow.hidden
        maxWidth = 120.pt
        flex(flexGrow = 2.0, flexShrink = 2.0, flexBasis = 60.pt)
        input {
            textAlign = TextAlign.right
            width = 100.pct
        }
        select{
            textAlign = TextAlign.right
            width = 100.pct
        }
    }
}