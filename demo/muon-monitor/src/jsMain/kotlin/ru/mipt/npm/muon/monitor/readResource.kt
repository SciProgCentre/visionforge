package ru.mipt.npm.muon.sim

import hep.dataforge.js.requireJS

actual fun readResource(path: String): String {
    return requireJS(path) as String
}