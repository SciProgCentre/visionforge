package space.kscience.visionforge

import kotlinx.browser.document
import kotlinx.html.InputType
import kotlinx.html.js.input
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import org.w3c.xhr.FormData
import space.kscience.dataforge.context.debug
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.meta.*
import space.kscience.visionforge.html.*


private fun HTMLElement.subscribeToVision(vision: VisionOfHtml) {
    vision.useProperty(VisionOfHtml::classes) {
        classList.value = classes.joinToString(separator = " ")
    }
}


private fun HTMLInputElement.subscribeToInput(inputVision: VisionOfHtmlInput) {
    subscribeToVision(inputVision)
    inputVision.useProperty(VisionOfHtmlInput::disabled) {
        disabled = it
    }
}


internal fun JsVisionClient.textVisionRenderer(): ElementVisionRenderer =
    ElementVisionRenderer<VisionOfTextField> { visionName, vision, _ ->
        input {
            type = InputType.text
            onChangeFunction = {
                notifyPropertyChanged(visionName, VisionOfTextField::text.name, value)
            }
        }.apply {
            subscribeToInput(vision)
            vision.useProperty(VisionOfTextField::text) {
                value = (it ?: "").asValue()
            }
        }
    }

internal fun JsVisionClient.numberVisionRenderer(): ElementVisionRenderer =
    ElementVisionRenderer<VisionOfNumberField> { visionName, vision, _ ->
        input {
            type = InputType.text
            onChangeFunction = {
                notifyPropertyChanged(visionName, VisionOfNumberField::value.name, value)
            }
        }.apply {
            subscribeToInput(vision)
            vision.useProperty(VisionOfNumberField::value) {
                value = (it?.double ?: 0.0).asValue()
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

internal fun JsVisionClient.formVisionRenderer(): ElementVisionRenderer =
    ElementVisionRenderer<VisionOfHtmlForm> { visionName, vision, _ ->

        val form = document.getElementById(vision.formId) as? HTMLFormElement
            ?: error("An element with id = '${vision.formId} is not a form")

        form.subscribeToVision(vision)

        logger.debug { "Adding hooks to form with id = '$vision.formId'" }

        vision.useProperty(VisionOfHtmlForm::values) { values ->
            logger.debug { "Updating form '${vision.formId}' with values $values" }
            val inputs = form.getElementsByTagName("input")
            values?.valueSequence()?.forEach { (token, value) ->
                (inputs[token.toString()] as? HTMLInputElement)?.value = value.toString()
            }
        }

        form.onsubmit = { event ->
            event.preventDefault()
            val formData = FormData(form).toMeta()
            notifyPropertyChanged(visionName, VisionOfHtmlForm::values.name, formData)
            console.info("Sent: ${formData.toMap()}")
            false
        }
    }