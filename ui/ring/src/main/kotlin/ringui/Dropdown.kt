package ringui

import org.w3c.dom.events.Event
import react.RBuilder
import react.RClass
import react.RHandler
import react.ReactElement
import react.dom.WithClassName

public external interface AnchorProps : WithClassName

public external interface DropdownProps : WithClassName {
    /**
     * Can be string, React element, or a function accepting an object with {active, pinned} properties and returning a React element
     * React element should render some interactive HTML element like `button` or `a`
     */
    public var anchor: dynamic //: PropTypes.oneOfType([PropTypes.node, PropTypes.func]).isRequired,
    public var initShown: Boolean
    public var activeClassName: String
    public var clickMode: Boolean
    public var hoverMode: Boolean
    public var hoverShowTimeOut: Number
    public var hoverHideTimeOut: Number
    public var onShow: () -> Unit
    public var onHide: () -> Unit
    public var onMouseEnter: (Event) -> Unit
    public var onMouseLeave: (Event) -> Unit
    //'data-test': PropTypes.string
}

@JsModule("@jetbrains/ring-ui/components/dropdown/dropdown")
public external object DropdownModule {
    public val Anchor: RClass<AnchorProps>

    @JsName("default")
    public val Dropdown: RClass<DropdownProps>
}


public fun RBuilder.ringDropdown(anchor: dynamic, handler: RHandler<DropdownProps>): ReactElement =
    DropdownModule.Dropdown {
        attrs {
            this.anchor = anchor
        }
        handler()
    }