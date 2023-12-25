package space.kscience.visionforge.html

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.html.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.node
import space.kscience.dataforge.meta.string
import space.kscience.visionforge.ClickControl
import space.kscience.visionforge.onClick

/**
 * @param formId an id of the element in rendered DOM, this form is bound to
 */
@Serializable
@SerialName("html.form")
public class VisionOfHtmlForm(
    public val formId: String,
) : VisionOfHtmlControl(), ClickControl {
    public var values: Meta? by properties.node()
}


/**
 * Create a [VisionOfHtmlForm] and bind this form to the id
 */
@HtmlTagMarker
public inline fun <T, C : TagConsumer<T>> C.visionOfForm(
    vision: VisionOfHtmlForm,
    action: String? = null,
    encType: FormEncType? = null,
    method: FormMethod? = null,
    classes: String? = null,
    crossinline block: FORM.() -> Unit = {},
) : T  = form(action, encType, method, classes){
    this.id = vision.formId
    block()
}


public fun VisionOfHtmlForm.onSubmit(scope: CoroutineScope, block: (Meta?) -> Unit): Job = onClick(scope) { block(payload) }


@Serializable
@SerialName("html.button")
public class VisionOfHtmlButton : VisionOfHtmlControl(), ClickControl {
    public var label: String? by properties.string()
}


@Suppress("UnusedReceiverParameter")
public inline fun VisionOutput.button(
    text: String,
    block: VisionOfHtmlButton.() -> Unit = {},
): VisionOfHtmlButton = VisionOfHtmlButton().apply {
    label = text
    block()
}