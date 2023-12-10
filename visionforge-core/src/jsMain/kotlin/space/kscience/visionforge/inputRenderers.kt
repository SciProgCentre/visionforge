package space.kscience.visionforge

import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.InputType
import kotlinx.html.div
import kotlinx.html.js.input
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import space.kscience.dataforge.meta.Value
import space.kscience.dataforge.meta.asValue
import space.kscience.dataforge.meta.double
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.html.*

/**
 * Subscribes the HTML element to a given vision.
 *
 * @param vision The vision to subscribe to.
 */
internal fun HTMLElement.subscribeToVision(vision: VisionOfHtml) {
    vision.useProperty(VisionOfHtml::classes) {
        classList.value = classes.joinToString(separator = " ")
    }
}


private fun VisionClient.sendInputEvent(name: Name, value: Value?) {
    context.launch {
        sendEvent(name, VisionValueChangeEvent(value, name))
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
        div().also { div ->
            div.subscribeToVision(vision)
            vision.useProperty(VisionOfPlainHtml::content) {
                div.clear()
                if (it != null) div.innerHTML = it
            }
        }
    }

internal val inputVisionRenderer: ElementVisionRenderer = ElementVisionRenderer<VisionOfHtmlInput>(
    acceptRating = ElementVisionRenderer.DEFAULT_RATING - 1
) { name, client, vision, _ ->
    input {
        type = InputType.text
    }.also { htmlInputElement ->
        val onEvent: (Event) -> Unit = {
            client.sendInputEvent(name, htmlInputElement.value.asValue())
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
                client.sendInputEvent(name, htmlInputElement.checked.asValue())
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
                client.sendInputEvent(name, htmlInputElement.value.asValue())
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
            type = InputType.number
        }.also { htmlInputElement ->

            val onEvent: (Event) -> Unit = {
                htmlInputElement.value.toDoubleOrNull()?.let {
                    client.sendInputEvent(name, htmlInputElement.value.asValue())
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
            type = InputType.range
            min = vision.min.toString()
            max = vision.max.toString()
            step = vision.step.toString()
        }.also { htmlInputElement ->

            val onEvent: (Event) -> Unit = {
                htmlInputElement.value.toDoubleOrNull()?.let {
                    client.sendInputEvent(name, htmlInputElement.value.asValue())
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
