
@file:JsModule("react-dom/client")
@file:JsNonModule

package space.kscience.visionforge.react

import org.w3c.dom.Element
import react.dom.client.Root
import react.dom.client.RootOptions

/**
 * Compatibility method to work with old browser API
 */
public external fun createRoot(
    container: Element,
    options: RootOptions = definedExternally,
): Root
