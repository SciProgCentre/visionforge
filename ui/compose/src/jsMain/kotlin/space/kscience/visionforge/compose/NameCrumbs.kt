package space.kscience.visionforge.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Nav
import org.jetbrains.compose.web.dom.Ol
import org.jetbrains.compose.web.dom.Text
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.length

@Composable
public fun NameCrumbs(name: Name?, link: (Name) -> Unit): Unit = Nav({
    attr("aria-label","breadcrumb")
}) {
    Ol({classes("breadcrumb")}) {
        Li({
            classes("breadcrumb-item")
            onClick {
                link(Name.EMPTY)
            }
        }) {
            Text("\u2302")
        }

        if (name != null) {
            val tokens = ArrayList<NameToken>(name.length)
            name.tokens.forEach { token ->
                tokens.add(token)
                val fullName = Name(tokens.toList())
                Text(".")
                Li({
                    classes("breadcrumb-item")
                    if(tokens.size == name.length) classes("active")
                    onClick {
                        link(fullName)
                    }
                }) {
                    Text(token.toString())
                }
            }
        }
    }
}