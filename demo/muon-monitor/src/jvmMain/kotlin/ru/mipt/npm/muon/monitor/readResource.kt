package ru.mipt.npm.muon.sim

actual fun readResource(path: String): String {
    return ClassLoader.getSystemClassLoader().getResourceAsStream("map-RMM110.sc16").readAllBytes().contentToString()
}