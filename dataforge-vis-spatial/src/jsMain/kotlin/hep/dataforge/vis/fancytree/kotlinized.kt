package ru.mipt.npm.fancytreekt

import kotlin.js.json

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "FunctionName")
fun NodeData(block: NodeData.() -> Unit): NodeData = (json() as NodeData).apply(block)