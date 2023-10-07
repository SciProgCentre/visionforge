package space.kscience.visionforge

import kotlinx.browser.document
import kotlinx.html.InputType
import kotlinx.html.js.input
import kotlinx.html.js.label
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import org.w3c.xhr.FormData
import space.kscience.dataforge.context.debug
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.meta.DynamicMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.toMap
import space.kscience.dataforge.meta.valueSequence
import space.kscience.visionforge.html.VisionOfHtmlForm
import space.kscience.visionforge.html.VisionOfNumberField
import space.kscience.visionforge.html.VisionOfTextField

internal fun textVisionRenderer(
    client: VisionClient,
): ElementVisionRenderer = ElementVisionRenderer<VisionOfTextField> { name, vision, _ ->
    val fieldName = vision.name ?: "input[${vision.hashCode().toUInt()}]"
    vision.label?.let {
        label {
            htmlFor = fieldName
            +it
        }
    }
    input {
        type = InputType.text
        this.name = fieldName
        vision.useProperty(VisionOfTextField::text) {
            value = it ?: ""
        }
        onChangeFunction = {
            client.notifyPropertyChanged(name, VisionOfTextField::text.name, value)
        }
    }
}

internal fun numberVisionRenderer(
    client: VisionClient,
): ElementVisionRenderer = ElementVisionRenderer<VisionOfNumberField> { name, vision, _ ->
    val fieldName = vision.name ?: "input[${vision.hashCode().toUInt()}]"
    vision.label?.let {
        label {
            htmlFor = fieldName
            +it
        }
    }
    input {
        type = InputType.text
        this.name = fieldName
        vision.useProperty(VisionOfNumberField::value) {
            value = it?.toDouble() ?: 0.0
        }
        onChangeFunction = {
            client.notifyPropertyChanged(name, VisionOfNumberField::value.name, value)
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

internal fun formVisionRenderer(
    client: VisionClient,
): ElementVisionRenderer = ElementVisionRenderer<VisionOfHtmlForm> { name, vision, _ ->

    val form = document.getElementById(vision.formId) as? HTMLFormElement
        ?: error("An element with id = '${vision.formId} is not a form")

    client.logger.debug{"Adding hooks to form with id = '$vision.formId'"}

    vision.useProperty(VisionOfHtmlForm::values) { values ->
        client.logger.debug{"Updating form '${vision.formId}' with values $values"}
        val inputs = form.getElementsByTagName("input")
        values?.valueSequence()?.forEach { (token, value) ->
            (inputs[token.toString()] as? HTMLInputElement)?.value = value.toString()
        }
    }

    form.onsubmit = { event ->
        event.preventDefault()
        val formData = FormData(form).toMeta()
        client.notifyPropertyChanged(name, VisionOfHtmlForm::values.name, formData)
        console.info("Sent: ${formData.toMap()}")
        false
    }
}