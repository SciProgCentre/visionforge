package hep.dataforge.vision

import hep.dataforge.context.Context
import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.html.HtmlFragment
import kotlinx.html.link
import kotlinx.html.script
import kotlinx.html.unsafe
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * The location of resources for plot.
 */
public enum class ResourceLocation {
//    /**
//     * Use cdn or other remote source for assets
//     */
//    REMOTE,

    /**
     * Store assets in a sibling folder `.dataforge/assets` or in a system-wide folder if this is a default temporary file
     */
    LOCAL,

    /**
     * Store assets in a system-window `~/.dataforge/assets` folder
     */
    SYSTEM,

    /**
     * Embed the asset into the html. Could produce very large files.
     */
    EMBED
}

internal const val VISIONFORGE_ASSETS_PATH = ".dataforge/vision/assets"


/**
 * Check if the asset exists in given local location and put it there if it does not
 * @param
 */
internal fun checkOrStoreFile(htmlPath: Path, filePath: Path, resource: String): Path {
    val fullPath = htmlPath.resolveSibling(filePath).toAbsolutePath().resolve(resource)

    if (Files.exists(fullPath)) {
        //TODO checksum
    } else {
        //TODO add logging

        val bytes = VisionManager::class.java.getResourceAsStream("/$resource").readAllBytes()
        Files.createDirectories(fullPath.parent)
        Files.write(fullPath, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
    }

    return if (htmlPath.isAbsolute && fullPath.startsWith(htmlPath.parent)) {
        htmlPath.parent.relativize(fullPath)
    } else {
        fullPath
    }
}

/**
 * A header that automatically copies relevant scripts to given path
 */
internal fun fileScriptHeader(
    path: Path,
): HtmlFragment = {
    script {
        type = "text/javascript"
        src = path.toString()
    }
}

internal fun embedScriptHeader(resource: String): HtmlFragment = {
    script {
        type = "text/javascript"
        unsafe {
            val bytes = VisionManager::class.java.getResourceAsStream("/$resource").readAllBytes()
            +bytes.toString(Charsets.UTF_8)
        }
    }
}

internal fun fileCssHeader(
    basePath: Path,
    cssPath: Path,
    resource: String,
): HtmlFragment = {
    val relativePath = checkOrStoreFile(basePath, cssPath, resource)
    link {
        rel = "stylesheet"
        href = relativePath.toString()
    }
}

/**
 * Make a script header, automatically copying file to appropriate location
 */
@DFExperimental
public fun Context.scriptHeader(
    scriptResource: String,
    htmlPath: Path,
    resourceLocation: ResourceLocation,
): HtmlFragment {
    val targetPath = when (resourceLocation) {
        ResourceLocation.LOCAL -> checkOrStoreFile(
            htmlPath,
            Path.of(VISIONFORGE_ASSETS_PATH),
            scriptResource
        )
        ResourceLocation.SYSTEM -> checkOrStoreFile(
            Path.of("."),
            Path.of(System.getProperty("user.home")).resolve(VISIONFORGE_ASSETS_PATH),
            scriptResource
        )
        ResourceLocation.EMBED -> null
    }
    return if (targetPath == null) {
        embedScriptHeader(scriptResource)
    } else {
        fileScriptHeader(targetPath)
    }
}

//
///**
// * A system-wide plotly store location
// */
//val systemHeader = HtmlFragment {
//    val relativePath = checkOrStoreFile(
//        Path.of("."),
//        Path.of(System.getProperty("user.home")).resolve(".plotly/$assetsDirectory$PLOTLY_SCRIPT_PATH"),
//        PLOTLY_SCRIPT_PATH
//    )
//    script {
//        type = "text/javascript"
//        src = relativePath.toString()
//    }
//}
//
//
///**
// * embedded plotly script
// */
//val embededHeader = HtmlFragment {
//    script {
//        unsafe {
//            val bytes = HtmlFragment::class.java.getResourceAsStream(PLOTLY_SCRIPT_PATH).readAllBytes()
//            +bytes.toString(Charsets.UTF_8)
//        }
//    }
//}


//internal fun inferPlotlyHeader(
//    target: Path?,
//    resourceLocation: ResourceLocation
//): HtmlFragment = when (resourceLocation) {
//    ResourceLocation.REMOTE -> cdnPlotlyHeader
//    ResourceLocation.LOCAL -> if (target != null) {
//        localHeader(target)
//    } else {
//        systemPlotlyHeader
//    }
//    ResourceLocation.SYSTEM -> systemPlotlyHeader
//    ResourceLocation.EMBED -> embededPlotlyHeader
//}
