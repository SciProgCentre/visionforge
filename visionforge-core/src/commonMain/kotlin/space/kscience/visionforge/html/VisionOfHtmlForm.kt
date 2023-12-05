package space.kscience.visionforge.html

import kotlinx.html.FORM
import kotlinx.html.TagConsumer
import kotlinx.html.form
import kotlinx.html.id
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.node

/**
 * @param formId an id of the element in rendered DOM, this form is bound to
 */
@Serializable
@SerialName("html.form")
public class VisionOfHtmlForm(
    public val formId: String,
) : VisionOfHtml() {
    public var values: Meta? by properties.node()
}

public fun <R> TagConsumer<R>.bindForm(
    visionOfForm: VisionOfHtmlForm,
    builder: FORM.() -> Unit,
): R = form {
    this.id = visionOfForm.formId
    builder()
}