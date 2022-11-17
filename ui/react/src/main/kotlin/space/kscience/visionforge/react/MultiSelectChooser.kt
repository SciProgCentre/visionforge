package space.kscience.visionforge.react

import kotlinx.html.js.onChangeFunction
import kotlinx.html.org.w3c.dom.events.Event
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.asList
import react.FC
import react.dom.attrs
import react.dom.option
import react.dom.select
import react.fc
import space.kscience.dataforge.meta.asValue
import space.kscience.dataforge.meta.descriptors.allowedValues
import space.kscience.dataforge.meta.string

@JsExport
public val MultiSelectChooser: FC<ValueChooserProps> = fc("MultiSelectChooser") { props ->
    val onChange: (Event) -> Unit = { event: Event ->
        val newSelected = (event.target as HTMLSelectElement).selectedOptions.asList()
            .map { (it as HTMLOptionElement).value.asValue() }
        props.onValueChange(newSelected.asValue())
    }

    select {
        attrs {
            multiple = true
            values = (props.value?.list ?: emptyList()).mapTo(HashSet()) { it.string }
            onChangeFunction = onChange
        }
        props.descriptor?.allowedValues?.forEach { optionValue ->
            option {
                +optionValue.string
            }
        }

    }
}