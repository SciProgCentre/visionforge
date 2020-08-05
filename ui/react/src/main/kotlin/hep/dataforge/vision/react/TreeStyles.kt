package hep.dataforge.vision.react

import kotlinx.css.*
import kotlinx.css.properties.deg
import kotlinx.css.properties.rotate
import styled.StyleSheet

object TreeStyles : StyleSheet("treeStyles", true) {
    /**
     * Remove default bullets
     */
    val tree by css {
        paddingLeft = 8.px
        marginLeft = 0.px
        listStyleType = ListStyleType.none
    }

    /**
     * Style the caret/arrow
     */
    val treeCaret by css {
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

    val treeItem by css {
        alignItems = Align.center
        paddingLeft = 10.px
        borderLeftStyle = BorderStyle.dashed
        borderLeftWidth = 1.px
        borderLeftColor = Color.lightGray
    }

    val treeLeaf by css {
        display = Display.flex
        flexDirection = FlexDirection.row
        userSelect = UserSelect.none
        alignItems = Align.center
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

    val treeLabel by css {
        overflow = Overflow.hidden
    }

    val treeLabelInactive by css {
        color = Color.lightGray
    }

    val treeLabelSelected by css {
        backgroundColor = Color.lightBlue
    }

}