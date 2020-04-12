/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.vis.editor

import hep.dataforge.values.Value


/**
 * @param success
 * @param value Value after change
 * @param message Message on unsuccessful change
 */
class ValueCallbackResponse(val success: Boolean, val value: Value, val message: String)

/**
 * A callback for some visual object trying to change some value
 * @author [Alexander Nozik](mailto:altavir@gmail.com)
 */
typealias ValueCallback = (Value) -> ValueCallbackResponse

