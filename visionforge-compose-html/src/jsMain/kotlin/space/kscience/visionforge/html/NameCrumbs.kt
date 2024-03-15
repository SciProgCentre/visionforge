package space.kscience.visionforge.html

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.length

@Composable
public fun NameCrumbs(name: Name?, link: (Name) -> Unit): Unit = Nav({
    attr("aria-label", "breadcrumb")
}) {
    Ol({
        classes("breadcrumb")
        style {
            property("--bs-breadcrumb-divider", "'.'")
            property("--bs-breadcrumb-item-padding-x", ".1rem")
        }
    }) {
        Li({
            classes("breadcrumb-item")
            onClick {
                link(Name.EMPTY)
            }
        }) {
            A("#") {
                Text("\u2302")
            }
        }

        if (name != null) {
            val tokens = ArrayList<NameToken>(name.length)
            name.tokens.forEach { token ->
                tokens.add(token)
                val fullName = Name(tokens.toList())
                Li({
                    classes("breadcrumb-item")
                    if (tokens.size == name.length) classes("active")
                    onClick {
                        link(fullName)
                    }
                }) {
                    A("#") {
                        Text(token.toString())
                    }
                }
            }
        }
    }
}