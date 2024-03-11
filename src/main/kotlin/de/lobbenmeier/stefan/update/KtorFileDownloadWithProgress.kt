package de.lobbenmeier.stefan.update

import de.lobbenmeier.stefan.ytdlp.CustomUpdateDownloadProgress
import de.lobbenmeier.stefan.ytdlp.DownloadCompleted
import de.lobbenmeier.stefan.ytdlp.UpdateDownloadProgress
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.util.zip.ZipInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

private val logger = KotlinLogging.logger {}

suspend fun HttpClient.downloadFile(
    url: String,
    targetFile: File,
    unzipFile: Boolean = false,
    onProgress: suspend (UpdateDownloadProgress) -> Unit,
): File {
    if (targetFile.exists()) {
        logger.info { "Target file $targetFile has already been downloaded" }
        onProgress(DownloadCompleted)
        return targetFile
    }

    return CoroutineScope(Dispatchers.IO).run {
        logger.info { "Starting download to $targetFile from $url" }

        val downloadFile =
            get(url) {
                onDownload { bytesSentTotal, contentLength ->
                    val progress = bytesSentTotal.toFloat() / contentLength
                    onProgress(CustomUpdateDownloadProgress(progress))
                }
            }

        targetFile.parentFile.mkdirs()

        val executable = PosixFilePermissions.fromString("rwxr-xr-x")
        val permissions = PosixFilePermissions.asFileAttribute(executable)
        Files.deleteIfExists(targetFile.toPath())
        Files.createFile(targetFile.toPath(), permissions)

        if (unzipFile) {
            copyFirstEntryToFile(downloadFile.bodyAsChannel().toInputStream(), targetFile)
        } else {
            downloadFile.bodyAsChannel().copyTo(targetFile.writeChannel())
        }

        onProgress(DownloadCompleted)
        logger.info { "Completed download to $targetFile from $url" }

        return targetFile
    }
}

private suspend fun copyFirstEntryToFile(zipInputStream: InputStream, targetFile: File) {
    ZipInputStream(zipInputStream).use {
        if (it.nextEntry != null) {
            it.copyTo(targetFile.writeChannel())
        }
    }
}
