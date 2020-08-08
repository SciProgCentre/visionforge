package materialui.components.slider

import kotlinx.html.DIV
import kotlinx.html.Tag
import materialui.components.MaterialElementBuilder
import materialui.components.getValue
import materialui.components.inputbase.enums.InputBaseStyle
import materialui.components.setValue
import react.RClass
import react.ReactElement

class SliderElementBuilder<Props: SliderProps> internal constructor(
    type: RClass<Props>,
    classMap: List<Pair<Enum<*>, String>>
) : MaterialElementBuilder<DIV, Props>(type, classMap, { DIV(mapOf(), it) }) {
    fun Tag.classes(vararg classMap: Pair<InputBaseStyle, String>) {
        classes(classMap.toList())
    }
    var Tag.defaultValue: Number? by materialProps
    var Tag.disabled: Boolean? by materialProps// = false
    var Tag.getAriaLabel: String? by materialProps
    var Tag.getAriaValueText: String? by materialProps
    var Tag.marks: Array<String>? by materialProps
    var Tag.max: Number? by materialProps// = 100
    var Tag.min: Number? by materialProps// = 0,
    var Tag.name: String? by materialProps
    var Tag.onChange: ((dynamic, Number) -> Unit)? by materialProps
    var Tag.onChangeCommitted: ((dynamic, Number) -> Unit)? by materialProps
    var Tag.orientation: SliderOrientation? by materialProps
    var Tag.scale: ((Number) -> Number)? by materialProps// {it}
    var Tag.step: Number? by materialProps// = 1,
    //ThumbComponent = 'span',
    var Tag.track: SliderTrack by materialProps
    var Tag.value: Number? by materialProps
    var Tag.ValueLabelComponent: ReactElement? by materialProps
    var Tag.valueLabelDisplay: SliderValueLabelDisplay by materialProps
}