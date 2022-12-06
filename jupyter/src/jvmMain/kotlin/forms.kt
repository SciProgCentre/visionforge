package space.kscience.visionforge.jupyter

import kotlinx.html.FORM
import kotlinx.html.form
import kotlinx.html.id
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.visionforge.html.HtmlFragment
import space.kscience.visionforge.html.VisionOfHtmlForm

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