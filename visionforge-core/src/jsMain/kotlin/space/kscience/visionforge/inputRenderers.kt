package space.kscience.visionforge

import kotlinx.browser.document
import kotlinx.html.InputType
import kotlinx.html.div
import kotlinx.html.js.input
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import org.w3c.xhr.FormData
import space.kscience.dataforge.context.debug
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.meta.*
import space.kscience.visionforge.html.*

/**
 * Subscribes the HTML element to a given vision.
 *
 * @param vision The vision to subscribe to.
 */
private fun HTMLElement.subscribeToVision(vision: VisionOfHtml) {
    vision.useProperty(VisionOfHtml::classes) {
        classList.value = classes.joinToString(separator = " ")
    }
}


/**
 * Subscribes the HTML input element to a given vision.
 *
 * @param inputVision The input vision to subscribe to.
 */
private fun HTMLInputElement.subscribeToInput(inputVision: VisionOfHtmlInput) {
    subscribeToVision(inputVision)
    inputVision.useProperty(VisionOfHtmlInput::disabled) {
        disabled = it
    }
}

internal val htmlVisionRenderer: ElementVisionRenderer =
    ElementVisionRenderer<VisionOfPlainHtml> { _, _, vision, _ ->
        div {}.also { div ->
            div.subscribeToVision(vision)
            vision.useProperty(VisionOfPlainHtml::content) {
                div.textContent = it
            }
        }
    }

internal val inputVisionRenderer: ElementVisionRenderer =
    ElementVisionRenderer<VisionOfHtmlInput>(
        acceptRating = ElementVisionRenderer.DEFAULT_RATING - 1
    ) { name, client, vision, _ ->
        input {
            type = InputType.text
        }.also { htmlInputElement ->
            val onEvent: (Event) -> Unit = {
                client.sendEvent(name, VisionValueChangeEvent(htmlInputElement.value.asValue()))
            }


            when (vision.feedbackMode) {
                InputFeedbackMode.ONCHANGE -> htmlInputElement.onchange = onEvent

                InputFeedbackMode.ONINPUT -> htmlInputElement.oninput = onEvent
                InputFeedbackMode.NONE -> {}
            }

            htmlInputElement.subscribeToInput(vision)
            vision.useProperty(VisionOfHtmlInput::value) {
                htmlInputElement.value = it?.string ?: ""
            }
        }
    }

internal val checkboxVisionRenderer: ElementVisionRenderer =
    ElementVisionRenderer<VisionOfCheckbox> { name, client, vision, _ ->
        input {
            type = InputType.checkBox
        }.also { htmlInputElement ->
            val onEvent: (Event) -> Unit = {
                client.sendEvent(name, VisionValueChangeEvent(htmlInputElement.checked.asValue()))
            }


            when (vision.feedbackMode) {
                InputFeedbackMode.ONCHANGE -> htmlInputElement.onchange = onEvent

                InputFeedbackMode.ONINPUT -> htmlInputElement.oninput = onEvent
                InputFeedbackMode.NONE -> {}
            }

            htmlInputElement.subscribeToInput(vision)
            vision.useProperty(VisionOfCheckbox::checked) {
                htmlInputElement.checked = it ?: false
            }
        }
    }

internal val textVisionRenderer: ElementVisionRenderer =
    ElementVisionRenderer<VisionOfTextField> { name, client, vision, _ ->
        input {
            type = InputType.text
        }.also { htmlInputElement ->
            val onEvent: (Event) -> Unit = {
                client.sendEvent(name, VisionValueChangeEvent(htmlInputElement.value.asValue()))
            }


            when (vision.feedbackMode) {
                InputFeedbackMode.ONCHANGE -> htmlInputElement.onchange = onEvent

                InputFeedbackMode.ONINPUT -> htmlInputElement.oninput = onEvent
                InputFeedbackMode.NONE -> {}
            }

            htmlInputElement.subscribeToInput(vision)
            vision.useProperty(VisionOfTextField::text) {
                htmlInputElement.value = it ?: ""
            }
        }
    }

internal val numberVisionRenderer: ElementVisionRenderer =
    ElementVisionRenderer<VisionOfNumberField> { name, client, vision, _ ->
        input {
            type = InputType.text
        }.also { htmlInputElement ->

            val onEvent: (Event) -> Unit = {
                htmlInputElement.value.toDoubleOrNull()?.let {
                    client.sendEvent(name, VisionValueChangeEvent(it.asValue()))
                }
            }

            when (vision.feedbackMode) {
                InputFeedbackMode.ONCHANGE -> htmlInputElement.onchange = onEvent

                InputFeedbackMode.ONINPUT -> htmlInputElement.oninput = onEvent
                InputFeedbackMode.NONE -> {}
            }
            htmlInputElement.subscribeToInput(vision)
            vision.useProperty(VisionOfNumberField::value) {
                htmlInputElement.valueAsNumber = it?.double ?: 0.0
            }
        }
    }

internal val rangeVisionRenderer: ElementVisionRenderer =
    ElementVisionRenderer<VisionOfRangeField> { name, client, vision, _ ->
        input {
            type = InputType.text
            min = vision.min.toString()
            max = vision.max.toString()
            step = vision.step.toString()
        }.also { htmlInputElement ->

            val onEvent: (Event) -> Unit = {
                htmlInputElement.value.toDoubleOrNull()?.let {
                    client.sendEvent(name, VisionValueChangeEvent(it.asValue()))
                }
            }

            when (vision.feedbackMode) {
                InputFeedbackMode.ONCHANGE -> htmlInputElement.onchange = onEvent

                InputFeedbackMode.ONINPUT -> htmlInputElement.oninput = onEvent
                InputFeedbackMode.NONE -> {}
            }
            htmlInputElement.subscribeToInput(vision)
            vision.useProperty(VisionOfRangeField::value) {
                htmlInputElement.valueAsNumber = it?.double ?: 0.0
            }
        }
    }

internal fun FormData.toMeta(): Meta {
    @Suppress("UNUSED_VARIABLE") val formData = this
    //val res = js("Object.fromEntries(formData);")
    val `object` = js("{}")
    //language=JavaScript
    js(
        """
    formData.forEach(function(value, key){
        // Reflect.has in favor of: object.hasOwnProperty(key)
        if(!Reflect.has(object, key)){
            object[key] = value;
            return;
        }
        if(!Array.isArray(object[key])){
            object[key] = [object[key]];    
        }
        object[key].push(value);
    }); 
    """
    )
    return DynamicMeta(`object`)
}

internal val formVisionRenderer: ElementVisionRenderer =
    ElementVisionRenderer<VisionOfHtmlForm> { _, vision, _ ->

        val form = document.getElementById(vision.formId) as? HTMLFormElement
            ?: error("An element with id = '${vision.formId} is not a form")

        form.subscribeToVision(vision)

        vision.manager?.logger?.debug { "Adding hooks to form with id = '$vision.formId'" }

        vision.useProperty(VisionOfHtmlForm::values) { values ->
            vision.manager?.logger?.debug { "Updating form '${vision.formId}' with values $values" }
            val inputs = form.getElementsByTagName("input")
            values?.valueSequence()?.forEach { (token, value) ->
                (inputs[token.toString()] as? HTMLInputElement)?.value = value.toString()
            }
        }

        form.onsubmit = { event ->
            event.preventDefault()
            val formData = FormData(form).toMeta()
            vision.values = formData
            console.info("Sent: ${formData.toMap()}")
            false
        }
    }