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
import java.util.concurrent.ConcurrentHashMap

class ClientResourceLanguageProject(
    private val context: DesignerContext,
    private val clientResourceManager: ClientResourceManager,
    val service: TypeScriptLanguageService,
) {

    private val openFiles = ConcurrentHashMap<String, ResourceFile>()

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
        service.background {
            service.setTypings()
            sync()
            service.warmUp()
        }
    }

    fun shutdown() {
        context.project?.removeResourceListener(listener)
    }

    fun update(file: ResourceFile) {
        openFiles[file.path] = file
        service.updateFile(file)
    }

    fun close(path: String) {
        openFiles.remove(path)
        val saved = context.project?.getResource(ResourcePath(ClientResource.type, path))
        val file = saved?.orElse(null)?.toResourceFile()
        service.background {
            if (file == null) service.removeFile(path) else service.updateFile(file)
        }
    }

    private fun update(changes: List<ChangeOperation>) {
        val files =
            changes
                .mapNotNull { ChangeOperation.getResourceFromChange(it)?.toResourceFile() }
                .filter { !openFiles.containsKey(it.path) }
        service.background { files.forEach { service.updateFile(it) } }
    }

    private fun remove(changes: List<ChangeOperation>) {
        val paths =
            changes
                .mapNotNull {
                    ChangeOperation.getResourceIdFromChange(it)?.resourcePath?.path?.toString()
                }
                .filter { !openFiles.containsKey(it) }
        service.background { paths.forEach { service.removeFile(it) } }
    }

    private fun sync() {
        val resources = context.project?.getResourcesOfType(ClientResource.type) ?: return
        val files = resources.mapNotNull { it.toResourceFile() }
        service.setFiles(files.map { openFiles[it.path] ?: it })
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
