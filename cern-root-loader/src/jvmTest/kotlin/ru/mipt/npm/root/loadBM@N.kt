package ru.mipt.npm.root

fun main() {
    val string = TGeoManager::class.java.getResourceAsStream("/BM@N.root.json")!!
        .readAllBytes().decodeToString()
    val geo = TGeoManager.decodeFromString(string)
}