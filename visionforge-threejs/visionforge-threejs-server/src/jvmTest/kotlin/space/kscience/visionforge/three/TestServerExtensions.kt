package space.kscience.visionforge.three

import kotlinx.html.stream.createHTML
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.VisionPage
import space.kscience.visionforge.html.appendTo
import space.kscience.visionforge.html.importScriptHeader
import kotlin.test.Test

class TestServerExtensions {

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testServerHeader(){
        val string = createHTML().apply {
            VisionPage.importScriptHeader(
                "js/visionforge-three.js",
                ResourceLocation.SYSTEM
            ).appendTo(this)
        }.finalize()


        //println(string)
    }
}