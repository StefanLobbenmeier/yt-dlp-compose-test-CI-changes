package de.lobbenmeier.stefan.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.lobbenmeier.stefan.model.DownloadQueue

@Composable
fun App() {
    val downloadQueue = remember { DownloadQueue() }

    Scaffold(topBar = {
        Header(onDownload = {
            downloadQueue.add(it)
        })
    }, bottomBar = { Footer() }) { DownloadList(downloadQueue) }
}

@Composable
fun Footer() {
    Column {
        Text("Ready to download")
        Row {
            Text("Folder")
            Text("Key")
            Text("Subtitles")
            Text("Spacer")
            Text("Media")
            Text("Quality")
            Text("Spacer")
            Text("When done")
            Text("Delete")
            Text("Download")
        }
    }
}
