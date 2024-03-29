package space.kscience.visionforge.html

import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.CSSAutoKeyword

public enum class UserSelect {
    inherit, initial, revert, revertLayer, unset,

    none, auto, text, contain, all;
}

public fun StyleScope.userSelect(value: UserSelect) {
    property("user-select", value.name)
}

public fun StyleScope.content(value: String) {
    property("content", "'$value'")
}

public fun StyleScope.paddingAll(
    top: CSSNumeric = 0.pt,
    right: CSSNumeric = top,
    bottom: CSSNumeric = top,
    left: CSSNumeric = right,
) {
    padding(top, right, bottom, left)
}

public fun StyleScope.marginAll(
    top: CSSNumeric = 0.pt,
    right: CSSNumeric = top,
    bottom: CSSNumeric = top,
    left: CSSNumeric = right,
) {
    margin(top, right, bottom, left)
}

public fun StyleScope.zIndex(value: Int) {
    property("z-index", "$value")
}

public fun StyleScope.zIndex(value: CSSAutoKeyword) {
    property("z-index", value)
}