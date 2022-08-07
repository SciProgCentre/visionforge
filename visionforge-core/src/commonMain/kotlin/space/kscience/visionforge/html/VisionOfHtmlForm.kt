package space.kscience.visionforge.html

import kotlinx.html.FORM
import kotlinx.html.TagConsumer
import kotlinx.html.form
import kotlinx.html.id
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.node

@Serializable
@SerialName("html.form")
public class VisionOfHtmlForm(
    public val formId: String,
) : VisionOfHtmlInput() {
    public var values: Meta? by mutableProperties.node()
}

public class HtmlFormFragment internal constructor(
    public val vision: VisionOfHtmlForm,
    public val formBody: HtmlFragment,
){
    public val values: Meta? get() = vision.values
    public operator fun get(valueName: String): Meta? = values?.get(valueName)
}

public fun HtmlFormFragment(id: String? = null, builder: FORM.() -> Unit): HtmlFormFragment {
    val realId = id ?: "form[${builder.hashCode().toUInt()}]"
    return HtmlFormFragment(VisionOfHtmlForm(realId)) {
        form {
            this.id = realId
            builder()
        }
    }
}

public fun <R> TagConsumer<R>.formFragment(
    id: String? = null,
    builder: FORM.() -> Unit,
): VisionOfHtmlForm {
    val formFragment = HtmlFormFragment(id, builder)
    fragment(formFragment.formBody)
    return formFragment.vision
}