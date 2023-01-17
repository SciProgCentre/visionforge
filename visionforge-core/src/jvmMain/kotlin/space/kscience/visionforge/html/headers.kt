package space.kscience.visionforge.html

import kotlinx.html.link
import kotlinx.html.script
import kotlinx.html.unsafe
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import kotlin.io.path.readText


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


private fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

/**
 * Check if the asset exists in given local location and put it there if it does not
 * @param
 */
internal fun checkOrStoreFile(htmlPath: Path, filePath: Path, resource: String, classLoader: ClassLoader): Path {
    val logger = LoggerFactory.getLogger("")

    logger.info("Resolving or storing resource file $resource")
    val fullPath = htmlPath.resolveSibling(filePath).toAbsolutePath().resolve(resource)
    logger.debug("Full path to resource file $resource: $fullPath")

    val bytes = classLoader.getResourceAsStream(resource)?.readAllBytes()
        ?: error("Resource $resource not found on classpath")
    val md = MessageDigest.getInstance("MD5")

    val checksum = md.digest(bytes).toHexString()
    val md5File = fullPath.resolveSibling(fullPath.fileName.toString() + ".md5")
    val skip: Boolean = Files.exists(fullPath) && Files.exists(md5File) && md5File.readText() == checksum

    if (!skip) {
        logger.debug("File $fullPath does not exist or wrong checksum. Writing file")
        Files.createDirectories(fullPath.parent)
        Files.write(
            fullPath,
            bytes,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
        Files.write(
            md5File,
            checksum.encodeToByteArray(),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
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

internal fun embedScriptHeader(resource: String, classLoader: ClassLoader): HtmlFragment = {
    script {
        type = "text/javascript"
        unsafe {
            val bytes = classLoader.getResourceAsStream(resource)!!.readAllBytes()
            +bytes.toString(Charsets.UTF_8)
        }
    }
}

internal fun fileCssHeader(
    basePath: Path,
    cssPath: Path,
    resource: String,
    classLoader: ClassLoader,
): HtmlFragment = {
    val relativePath = checkOrStoreFile(basePath, cssPath, resource, classLoader)
    link {
        rel = "stylesheet"
        href = relativePath.toString()
    }
}

/**
 * Make a script header from a resource file, automatically copying file to appropriate location
 */
public fun VisionPage.Companion.importScriptHeader(
    scriptResource: String,
    resourceLocation: ResourceLocation,
    htmlPath: Path? = null,
    classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
): HtmlFragment {
    val targetPath = when (resourceLocation) {
        ResourceLocation.LOCAL -> checkOrStoreFile(
            htmlPath ?: Path.of("."),
            Path.of(VISIONFORGE_ASSETS_PATH),
            scriptResource,
            classLoader
        )

        ResourceLocation.SYSTEM -> checkOrStoreFile(
            Path.of("."),
            Path.of(System.getProperty("user.home")).resolve(VISIONFORGE_ASSETS_PATH),
            scriptResource, classLoader
        )

        ResourceLocation.EMBED -> null
    }
    return if (targetPath == null) {
        embedScriptHeader(scriptResource, classLoader)
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
