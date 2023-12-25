package space.kscience.visionforge

import kotlinx.browser.document
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.js.button
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import org.w3c.xhr.FormData
import space.kscience.dataforge.context.debug
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.html.VisionOfHtmlButton
import space.kscience.visionforge.html.VisionOfHtmlForm

/**
 * Convert form data to Meta
 */
public fun FormData.toMeta(): Meta {
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


public fun VisionClient.sendMetaEvent(targetName: Name, payload: MetaRepr): Unit {
    context.launch {
        sendEvent(targetName, VisionMetaEvent(payload.toMeta()))
    }
}

internal val formVisionRenderer: ElementVisionRenderer =
    ElementVisionRenderer<VisionOfHtmlForm> { name, client, vision, _ ->

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
            client.context.launch {
                client.sendEvent(name, VisionClickEvent(name = name, payload = formData))
            }
            console.info("Sent form data: ${formData.toMap()}")
            false
        }
    }

internal val buttonVisionRenderer: ElementVisionRenderer =
    ElementVisionRenderer<VisionOfHtmlButton> { name, client, vision, _ ->
        button(type = ButtonType.button).also { button ->
            button.subscribeToVision(vision)
            button.onclick = {
                client.context.launch {
                    client.sendEvent(name, VisionClickEvent(name = name))
                }
            }
            vision.useProperty(VisionOfHtmlButton::label) {
                button.innerHTML = it ?: ""
            }

        }
    }
