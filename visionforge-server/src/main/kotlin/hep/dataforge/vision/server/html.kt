package hep.dataforge.vision.server

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

public class HtmlFragment(public val visit: TagConsumer<*>.() -> Unit) {
    override fun toString(): String {
        return createHTML().also(visit).finalize()
    }
}

public operator fun HtmlFragment.plus(other: HtmlFragment): HtmlFragment = HtmlFragment {
    this@plus.run { visit() }
    other.run { visit() }
}

/**
 * Check if the asset exists in given local location and put it there if it does not
 */
internal fun checkOrStoreFile(basePath: Path, filePath: Path, resource: String): Path {
    val fullPath = basePath.resolveSibling(filePath).toAbsolutePath()

    if (Files.exists(fullPath)) {
        //TODO checksum
    } else {
        //TODO add logging

        val bytes = HtmlFragment::class.java.getResourceAsStream(resource).readAllBytes()
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
public fun localScriptHeader(
    basePath: Path,
    scriptPath: Path,
    resource: String
): HtmlFragment = HtmlFragment {
    val relativePath = checkOrStoreFile(basePath, scriptPath, resource)
    script {
        type = "text/javascript"
        src = relativePath.toString()
        attributes["onload"] = "console.log('Script successfully loaded from $relativePath')"
        attributes["onerror"] = "console.log('Failed to load script from $relativePath')"
    }
}

public fun localCssHeader(
    basePath: Path,
    cssPath: Path,
    resource: String
): HtmlFragment = HtmlFragment {
    val relativePath = checkOrStoreFile(basePath, cssPath, resource)
    link {
        rel = "stylesheet"
        href = relativePath.toString()
    }
}