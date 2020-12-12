package hep.dataforge.vision

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


/**
 * Check if the asset exists in given local location and put it there if it does not
 * @param
 */
internal fun checkOrStoreFile(basePath: Path, filePath: Path, resource: String): Path {
    val fullPath = basePath.resolveSibling(filePath).toAbsolutePath()

    if (Files.exists(fullPath)) {
        //TODO checksum
    } else {
        //TODO add logging

        val bytes = VisionManager::class.java.getResourceAsStream(resource).readAllBytes()
        Files.createDirectories(fullPath.parent)
        Files.write(fullPath, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
    }

    return if (basePath.isAbsolute && fullPath.startsWith(basePath)) {
        basePath.relativize(fullPath)
    } else {
        filePath
    }
}

/**
 * A header that automatically copies relevant scripts to given path
 */
internal fun fileScriptHeader(
    basePath: Path,
    scriptPath: Path,
    resource: String
): HtmlFragment = {
    val relativePath = checkOrStoreFile(basePath, scriptPath, resource)
    script {
        type = "text/javascript"
        src = relativePath.toString()
    }
}

internal fun embedScriptHeader(resource: String): HtmlFragment = {
    script {
        type = "text/javascript"
        unsafe {
            val bytes = VisionManager::class.java.getResourceAsStream(resource).readAllBytes()
            +bytes.toString(Charsets.UTF_8)
        }
    }
}

internal fun fileCssHeader(
    basePath: Path,
    cssPath: Path,
    resource: String
): HtmlFragment = {
    val relativePath = checkOrStoreFile(basePath, cssPath, resource)
    link {
        rel = "stylesheet"
        href = relativePath.toString()
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
