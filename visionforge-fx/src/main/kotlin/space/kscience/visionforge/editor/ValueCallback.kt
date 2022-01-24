/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package space.kscience.visionforge.editor

import space.kscience.dataforge.values.Value


/**
 * @param success
 * @param value Value after change
 * @param message Message on unsuccessful change
 */
public class ValueCallbackResponse(public val success: Boolean, public val value: Value, public val message: String)

/**
 * A callback for some visual object trying to change some value
 * @author [Alexander Nozik](mailto:altavir@gmail.com)
 */
public typealias ValueCallback = (Value) -> ValueCallbackResponse

