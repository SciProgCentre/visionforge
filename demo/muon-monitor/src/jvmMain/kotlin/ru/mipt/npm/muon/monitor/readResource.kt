package ru.mipt.npm.muon.monitor

actual fun readResource(path: String): String {
    return ClassLoader.getSystemClassLoader().getResourceAsStream(path)?.readBytes()?.decodeToString()
        ?: error("Resource '$path' not found")
}

internal actual fun readMonitorConfig(): String {
    return readResource("map-RMM110.sc16")
}