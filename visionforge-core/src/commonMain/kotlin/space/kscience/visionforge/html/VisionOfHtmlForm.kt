package space.kscience.visionforge.html

import kotlinx.html.FORM
import kotlinx.html.TagConsumer
import kotlinx.html.form
import kotlinx.html.id
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.node

@Serializable
@SerialName("html.form")
public class VisionOfHtmlForm(
    public val formId: String,
) : VisionOfHtmlInput() {
    public var values: Meta? by meta.node()
}

public inline fun <R> TagConsumer<R>.visionOfForm(id: String, crossinline builder: FORM.() -> Unit): VisionOfHtmlForm {
    form {
        this.id = id
        builder()
    }
    return VisionOfHtmlForm(id)
}