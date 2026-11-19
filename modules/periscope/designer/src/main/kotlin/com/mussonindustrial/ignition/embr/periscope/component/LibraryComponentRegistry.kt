package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.resourcecollection.ChangeOperation
import com.inductiveautomation.ignition.common.resourcecollection.ResourceFilter
import com.inductiveautomation.ignition.common.resourcecollection.ResourceListener
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentEventDescriptor
import com.inductiveautomation.perspective.common.api.ExtensionFunctionDescriptor
import com.inductiveautomation.perspective.designer.DesignerComponentRegistry
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.common.reflect.getSuperPrivateProperty
import com.mussonindustrial.ignition.embr.periscope.component.LibraryComponents.VIEWS
import com.mussonindustrial.ignition.embr.periscope.component.LibraryComponents.VIEW_FILE
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentFailure
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentInfo
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentLibraryInfo
import com.mussonindustrial.ignition.embr.periscope.resources.language.TypeScriptWorkspace
import java.util.Optional
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.Icon
import javax.swing.SwingUtilities.invokeLater
import kotlin.jvm.optionals.getOrNull

class LibraryComponentRegistry(
    private val context: DesignerContext,
    private val perspective: PerspectiveDesignerInterface,
    private val workspace: TypeScriptWorkspace,
) {

    private val logger = this.getLoggerEx()
    private val listeners = CopyOnWriteArrayList<() -> Unit>()
    private val executor = Executors.newSingleThreadScheduledExecutor {
        Thread(it, "embr-periscope-components").apply { isDaemon = true }
    }

    private val viewListener =
        object : ResourceListener {
            override fun resourcesCreated(
                projectName: String,
                changes: List<ChangeOperation.CreateResourceOperation>,
            ) = viewsChanged()

            override fun resourcesModified(
                projectName: String,
                changes: List<ChangeOperation.ModifyResourceOperation>,
            ) = viewsChanged()

            override fun resourcesDeleted(
                projectName: String,
                changes: List<ChangeOperation.DeleteResourceOperation>,
            ) = viewsChanged()

            override fun getResourceFilter() = ResourceFilter(ApplicationScope.ALL, listOf(VIEWS))
        }

    private val workspaceListener: (String) -> Unit = { scheduleLoad(SETTLE_MILLIS) }

    @Volatile private var loadedRevision: Int? = null
    private var scheduledLoad: ScheduledFuture<*>? = null
    private var reportedFailures: List<ComponentFailure>? = null
    private var registeredComponents = emptyMap<String, ComponentInfo?>()
    private var usedComponentIds = emptySet<String>()

    var components: List<ComponentInfo> = emptyList()
        private set

    var libraries: List<ComponentLibraryInfo> = emptyList()
        private set

    fun startup() {
        ComponentIcons.onImageLoaded = { invokeLater { context.frame.repaint() } }
        context.project?.addResourceListener(viewListener)
        viewsChanged()

        workspace.addChangeListener(workspaceListener)
        scheduleLoad(SETTLE_MILLIS)
    }

    fun shutdown() {
        context.project?.removeResourceListener(viewListener)
        workspace.removeChangeListener(workspaceListener)
        executor.shutdownNow()
        ComponentIcons.onImageLoaded = {}

        invokeLater {
            usedComponentIds = emptySet()
            updateComponents(emptyList(), emptyList())
        }
    }

    fun addListener(listener: () -> Unit) {
        listeners += listener
    }

    @Synchronized
    private fun scheduleLoad(delayMillis: Long) {
        scheduledLoad?.cancel(false)
        scheduledLoad =
            try {
                executor.schedule(::loadComponents, delayMillis, TimeUnit.MILLISECONDS)
            } catch (e: RejectedExecutionException) {
                null
            }
    }

    private fun viewsChanged() {
        invokeLater {
            usedComponentIds = findUsedComponentIds()
            registerComponents()
        }
    }

    private fun findUsedComponentIds(): Set<String> {
        val views = context.project?.getResourcesOfType(VIEWS).orEmpty()
        val usedIds = mutableSetOf<String>()
        for (view in views) {
            val viewJson = view.getData(VIEW_FILE).getOrNull()?.bytesAsString.orEmpty()
            usedIds += LibraryComponents.usedIn(viewJson)
        }
        return usedIds
    }

    private fun loadComponents() {
        val result = runCatching {
            workspace.service
                .describeComponents(loadedRevision)
                .get(EVALUATE_SECONDS, TimeUnit.SECONDS)
        }
            .getOrNull()

        if (result == null) {
            scheduleLoad(RETRY_MILLIS)
            return
        }

        val loadedComponents = result.components ?: return
        val loadedLibraries = result.libraries.orEmpty()
        reportFailures(result.failures.orEmpty())
        loadedRevision = result.revision
        invokeLater {
            if (executor.isShutdown) return@invokeLater
            ComponentIcons.setLibraryIcons(loadedLibraries.associate { it.path to it.icon })
            updateComponents(loadedComponents, loadedLibraries)
        }
    }

    private fun reportFailures(failures: List<ComponentFailure>) {
        if (failures == reportedFailures) return
        reportedFailures = failures

        failures.forEach {
            logger.warn("Could not evaluate client resource '${it.path}': ${it.message}")
        }
        workspace.notifyEvaluated()
    }

    private fun updateComponents(
        loadedComponents: List<ComponentInfo>,
        loadedLibraries: List<ComponentLibraryInfo>,
    ) {
        val changed = loadedComponents != components || loadedLibraries != libraries
        components = loadedComponents
        libraries = loadedLibraries
        registerComponents()

        if (changed) listeners.forEach { it() }
    }

    private fun registerComponents() {
        val registry = perspective.designerComponentRegistry
        val libraryComponents = components.associateBy { it.id }
        val missingComponents = (usedComponentIds - libraryComponents.keys).associateWith { null }
        val wantedComponents: Map<String, ComponentInfo?> = missingComponents + libraryComponents

        val changes = mutableListOf<() -> Unit>()
        for (id in registeredComponents.changedFrom(wantedComponents).keys) {
            changes += { registry.removeComponent(LibraryComponents.typeOf(id)) }
        }
        for ((id, info) in wantedComponents.changedFrom(registeredComponents)) {
            changes += { registerComponent(registry, id, info) }
        }

        registeredComponents = wantedComponents
        if (changes.isEmpty()) return

        withoutRegistryListeners(registry) { changes.dropLast(1).forEach { it() } }
        changes.last().invoke()
        context.frame.repaint()
    }

    private fun registerComponent(
        registry: DesignerComponentRegistry,
        id: String,
        info: ComponentInfo?,
    ) {
        ComponentIcons.get(id).iconName = info?.icon
        try {
            registry.registerComponent(createDescriptor(id, info))
        } catch (e: Throwable) {
            logger.warn("Could not register component '$id'.", e)
        }
    }

    private fun Map<String, ComponentInfo?>.changedFrom(
        other: Map<String, ComponentInfo?>,
    ): Map<String, ComponentInfo?> {
        return filter { (id, info) -> !other.containsKey(id) || other[id] != info }
    }

    private fun withoutRegistryListeners(registry: Any, block: () -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val registryListeners = runCatching {
            registry.getSuperPrivateProperty("listeners") as MutableCollection<Any>
        }
            .getOrNull()

        if (registryListeners == null) {
            block()
            return
        }

        val heldListeners = registryListeners.toList()
        registryListeners.clear()
        try {
            block()
        } finally {
            registryListeners.addAll(heldListeners)
        }
    }

    private fun createDescriptor(id: String, info: ComponentInfo?): ComponentDescriptor {
        val events =
            info?.events.orEmpty().map {
                ComponentEventDescriptor(it.name, it.description.orEmpty(), parseSchema(it.schema))
            }

        val descriptor =
            LibraryComponents.descriptor(
                id,
                info?.name ?: id,
                info?.schema?.let(::parseSchema) ?: LibraryComponents.LOOSE_SCHEMA,
                info?.defaults ?: JsonObject(),
                events,
                info?.position?.let(::parseSchema),
            )
        return IconComponentDescriptor(descriptor, ComponentIcons.get(id))
    }

    private fun parseSchema(json: JsonObject): JsonSchema {
        return LibraryComponents.schema(json.toString())
    }

    private class IconComponentDescriptor(
        private val descriptor: ComponentDescriptor,
        private val icon: Icon,
    ) : ComponentDescriptor by descriptor {

        override fun childPositionSchema(): JsonSchema? = descriptor.childPositionSchema()

        override fun events(): Collection<ComponentEventDescriptor?> = descriptor.events()

        override fun extensionFunctions(): Collection<ExtensionFunctionDescriptor?> =
            descriptor.extensionFunctions()

        override fun getInitialProps(variantId: String?): JsonObject? =
            descriptor.getInitialProps(variantId)

        override fun getIcon(): Optional<Icon> = Optional.of(icon)
    }

    companion object {
        private const val SETTLE_MILLIS = 250L
        private const val RETRY_MILLIS = 2000L
        private const val EVALUATE_SECONDS = 20L
    }
}
