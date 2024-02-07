package space.kscience.visionforge.examples

import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.markup.markdown

fun main() = makeVisionFile(resourceLocation = ResourceLocation.SYSTEM) {
    vision {
        markdown{
            content = """
                # h1 Heading 8-)
                ## h2 Heading
                ### h3 Heading
                #### h4 Heading
                ##### h5 Heading
                ###### h6 Heading
            """.trimIndent()
        }
    }
}