package com.mussonindustrial.ignition.embr.periscope.resources.language

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.resourcecollection.ChangeOperation
import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceFilter
import com.inductiveautomation.ignition.common.resourcecollection.ResourceListener
import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceManager
import com.mussonindustrial.ignition.embr.periscope.resources.CssModuleResource
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class TypeScriptWorkspace(
    private val context: DesignerContext,
    private val clientResourceManager: ClientResourceManager,
    val service: TypeScriptLanguageService,
) {

    private val openFiles = ConcurrentHashMap<String, ResourceFile>()
    private val changeListeners = CopyOnWriteArrayList<(String) -> Unit>()

    private val listener =
        object : ResourceListener {
            override fun resourcesCreated(
                projectName: String,
                changes: List<ChangeOperation.CreateResourceOperation>,
            ) = update(changes)

            override fun resourcesModified(
                projectName: String,
                changes: List<ChangeOperation.ModifyResourceOperation>,
            ) = update(changes)

            override fun resourcesDeleted(
                projectName: String,
                changes: List<ChangeOperation.DeleteResourceOperation>,
            ) = remove(changes)

            override fun getResourceFilter() =
                ResourceFilter(ApplicationScope.ALL, listOf(ClientResource.type))
        }

    fun startup() {
        context.project?.addResourceListener(listener)
        CompletableFuture.runAsync {
            service.setTypings()
            service.setFiles(savedFiles())
            openFiles.values.forEach(service::updateFile)
            service.warmUp()
        }
    }

    fun shutdown() {
        context.project?.removeResourceListener(listener)
    }

    fun addChangeListener(listener: (path: String) -> Unit) {
        changeListeners += listener
    }

    fun removeChangeListener(listener: (path: String) -> Unit) {
        changeListeners -= listener
    }

    fun update(file: ResourceFile) {
        openFiles[file.path] = file
        notifyChanged(listOf(file.path), service.updateFile(file))
    }

    fun close(path: String) {
        openFiles.remove(path)
        val saved = context.project?.getResource(ResourcePath(ClientResource.type, path))
        val file = saved?.orElse(null)?.toResourceFile()
        notifyChanged(
            listOf(path),
            if (file == null) service.removeFile(path) else service.updateFile(file),
        )
    }

    private fun update(changes: List<ChangeOperation>) {
        val files =
            changes
                .mapNotNull { ChangeOperation.getResourceFromChange(it)?.toResourceFile() }
                .filter { !openFiles.containsKey(it.path) }
        notifyChanged(files.map { it.path }, *files.map(service::updateFile).toTypedArray())
    }

    private fun remove(changes: List<ChangeOperation>) {
        val paths =
            changes
                .mapNotNull {
                    ChangeOperation.getResourceIdFromChange(it)?.resourcePath?.path?.toString()
                }
                .filter { !openFiles.containsKey(it) }
        notifyChanged(paths, *paths.map(service::removeFile).toTypedArray())
    }

    private fun notifyChanged(paths: List<String>, vararg changes: CompletableFuture<*>) {
        CompletableFuture.allOf(*changes).thenRun {
            paths.forEach { path -> changeListeners.forEach { it(path) } }
        }
    }

    private fun savedFiles() =
        context.project?.getResourcesOfType(ClientResource.type).orEmpty().mapNotNull {
            it.toResourceFile()
        }

    private fun Resource.toResourceFile(): ResourceFile? {
        val kind =
            when (ClientResource.getType(this)) {
                TypeScriptResource.type -> ResourceFile.TYPESCRIPT
                CssModuleResource.type -> ResourceFile.CSS
                else -> return null
            }
        val clientResource = clientResourceManager.fromResource(this) ?: return null
        return ResourceFile(resourcePath.path.toString(), kind, clientResource.fileContents.source)
    }
}
