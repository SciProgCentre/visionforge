package materialui.components.slider

import materialui.components.StandardProps
import materialui.components.input.enums.InputStyle
import react.RBuilder
import react.RClass
import react.ReactElement

@JsModule("@material-ui/core/Slider")
private external val sliderModule: dynamic

external interface SliderProps : StandardProps {
    var defaultValue: Number?
    var disabled: Boolean?// = false
    var getAriaLabel: String?
    var getAriaValueText: String?
    var marks: Array<String>?
    var max: Number?// = 100
    var min: Number?// = 0,
    var name: String?
    var onChange: ((dynamic, Number) -> Unit)?
    var onChangeCommitted: ((dynamic, Number) -> Unit)?
    var orientation: SliderOrientation?
    var scale: ((Number) -> Number)?// {it}
    var step: Number? // = 1,
    //ThumbComponent = 'span',
    var track: SliderTrack
    var value: Number?
    var ValueLabelComponent: ReactElement?
    var valueLabelDisplay: SliderValueLabelDisplay
    //valueLabelFormat = Identity,
}

@Suppress("UnsafeCastFromDynamic")
private val sliderComponent: RClass<SliderProps> = sliderModule.default

fun RBuilder.slider(vararg classMap: Pair<InputStyle, String>, block: SliderElementBuilder<SliderProps>.() -> Unit) =
    child(SliderElementBuilder(sliderComponent, classMap.toList()).apply(block).create())
