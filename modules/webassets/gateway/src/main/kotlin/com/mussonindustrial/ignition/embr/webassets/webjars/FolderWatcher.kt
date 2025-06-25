package com.mussonindustrial.ignition.embr.webassets.webjars

import java.io.File
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.CopyOnWriteArrayList

class FolderWatcher(private val directory: File) : Runnable {

    interface Listener {
        fun onFileCreated(file: File) {}

        fun onFileModified(file: File) {}

        fun onFileDeleted(file: File) {}
    }

    private val listeners = CopyOnWriteArrayList<Listener>()
    private val watchService = FileSystems.getDefault().newWatchService()
    @Volatile private var running = true

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun stop() {
        running = false
        watchService.close()
    }

    override fun run() {
        require(directory.isDirectory) { "Path must be a directory" }

        val path = directory.toPath()
        path.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE,
        )

        while (running) {
            val key =
                try {
                    watchService.take()
                } catch (e: ClosedWatchServiceException) {
                    break
                }

            key.pollEvents().forEach { event ->
                val kind = event.kind()
                val context = event.context() as? Path ?: return@forEach
                val changedFile = directory.toPath().resolve(context).toFile()

                when (kind) {
                    StandardWatchEventKinds.ENTRY_CREATE -> {
                        listeners.forEach { it.onFileCreated(changedFile) }
                    }
                    StandardWatchEventKinds.ENTRY_MODIFY -> {
                        listeners.forEach { it.onFileModified(changedFile) }
                    }
                    StandardWatchEventKinds.ENTRY_DELETE -> {
                        listeners.forEach { it.onFileDeleted(changedFile) }
                    }
                }
            }

            val valid = key.reset()
            if (!valid) break
        }
    }
}
