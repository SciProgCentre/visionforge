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
import space.kscience.dataforge.meta.DynamicMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.valueSequence
import space.kscience.visionforge.html.VisionOfHtmlForm
import space.kscience.visionforge.html.VisionOfNumberField
import space.kscience.visionforge.html.VisionOfTextField

public val textVisionRenderer: ElementVisionRenderer = ElementVisionRenderer<VisionOfTextField> { vision, _ ->
    val name = vision.name ?: "input[${vision.hashCode().toUInt()}]"
    vision.label?.let {
        label {
            htmlFor = name
            +it
        }
    }
    input {
        type = InputType.text
        this.name = name
        vision.useProperty(VisionOfTextField::text) {
            value = it ?: ""
        }
        onChangeFunction = {
            vision.text = value
        }
    }
}

public val numberVisionRenderer: ElementVisionRenderer = ElementVisionRenderer<VisionOfNumberField> { vision, _ ->
    val name = vision.name ?: "input[${vision.hashCode().toUInt()}]"
    vision.label?.let {
        label {
            htmlFor = name
            +it
        }
    }
    input {
        type = InputType.text
        this.name = name
        vision.useProperty(VisionOfNumberField::value) {
            value = it?.toDouble() ?: 0.0
        }
        onChangeFunction = {
            vision.value = value.toDoubleOrNull()
        }
    }
}

internal fun FormData.toMeta(): Meta {
    @Suppress("UNUSED_VARIABLE") val formData = this
    //val res = js("Object.fromEntries(formData);")
    val `object` = js("{}")
    //language=JavaScript
    js("""
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
    """)
    return DynamicMeta(`object`)
}

public val formVisionRenderer: ElementVisionRenderer = ElementVisionRenderer<VisionOfHtmlForm> { vision, _ ->

    val form = document.getElementById(vision.formId) as? HTMLFormElement
        ?: error("An element with id = '${vision.formId} is not a form")

    console.info("Adding hooks to form '$form'")

    vision.useProperty(VisionOfHtmlForm::values) { values ->
        val inputs = form.getElementsByTagName("input")
        values?.valueSequence()?.forEach { (token, value) ->
            (inputs[token.toString()] as? HTMLInputElement)?.value = value.toString()
        }
    }

    form.onsubmit = { event ->
        event.preventDefault()
        val formData = FormData(form).toMeta()
        console.log(formData.toString())
        vision.values = formData
        false
    }
}