package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.execution.ExecutionManager
import com.inductiveautomation.ignition.common.execution.impl.BasicExecutionEngine
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.jsonschema.JsonSchema
import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.project.ChangeOperation
import com.inductiveautomation.ignition.common.project.ProjectResourceListener
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.gateway.project.ResourceFilter
import com.inductiveautomation.perspective.common.api.ComponentDescriptor
import com.inductiveautomation.perspective.common.api.ComponentEventDescriptor
import com.inductiveautomation.perspective.common.api.ExtensionFunctionDescriptor
import com.inductiveautomation.perspective.designer.DesignerComponentRegistry
import com.inductiveautomation.perspective.designer.api.PerspectiveDesignerInterface
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.common.reflect.getSuperPrivateProperty
import com.mussonindustrial.ignition.embr.periscope.component.ProjectComponents.VIEWS
import com.mussonindustrial.ignition.embr.periscope.component.ProjectComponents.VIEW_FILE
import com.mussonindustrial.ignition.embr.periscope.i18n
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentAttributes
import com.mussonindustrial.ignition.embr.periscope.resources.findComponentResources
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentDefinition
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentFailure
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentsResult
import com.mussonindustrial.ignition.embr.periscope.resources.language.TypeScriptWorkspace
import com.mussonindustrial.ignition.embr.periscope.resources.pathString
import java.util.Optional
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.swing.Icon
import javax.swing.SwingUtilities.invokeLater

class ProjectComponentRegistry(
    private val context: DesignerContext,
    private val perspectiveDesignerInterface: PerspectiveDesignerInterface,
    private val workspace: TypeScriptWorkspace,
) {

    companion object {
        private const val THREAD_NAME = "embr-periscope-components"
        private const val SETTLE_WAIT_MS = 250L
        private const val RETRY_WAIT_MS = 2000L
        private const val EVALUATE_WAIT_SECONDS = 20L
    }

    private val logger = this.getLoggerEx()
    private val listeners = CopyOnWriteArrayList<() -> Unit>()
    private val executionManager: ExecutionManager = BasicExecutionEngine(1, THREAD_NAME)

    private val resourceListener =
        object : ProjectResourceListener {
            override fun resourcesCreated(
                projectName: String,
                changes: List<ChangeOperation.CreateResourceOperation>,
            ) {
                resourcesChanged()
            }

            override fun resourcesModified(
                projectName: String,
                changes: List<ChangeOperation.ModifyResourceOperation>,
            ) {
                resourcesChanged()
            }

            override fun resourcesDeleted(
                projectName: String,
                changes: List<ChangeOperation.DeleteResourceOperation>,
            ) {
                resourcesChanged()
            }

            override fun getResourceFilter(): ResourceFilter {
                return ResourceFilter(ApplicationScope.ALL, listOf(VIEWS, ClientResource.type))
            }
        }

    private val workspaceListener: (String) -> Unit = { scheduleLoad(SETTLE_WAIT_MS) }

    private val stopped = AtomicBoolean(false)
    private val scheduledLoad = AtomicReference<ScheduledFuture<*>?>()

    private var loadedRevision: Int? = null
    private var reportedFailures: List<ComponentFailure>? = null
    private var definitions = emptyList<ComponentDefinition>()

    private var registeredComponentsById = emptyMap<String, ComponentInfo?>()
    private var usedComponentIds = emptySet<String>()

    var components: List<ComponentInfo> = emptyList()
        private set

    fun startup() {
        invokeLater { ComponentIcons.onImageLoaded = { context.frame.repaint() } }
        context.project?.addProjectResourceListener(resourceListener)
        scheduleUsedComponentIds()

        workspace.addChangeListener(workspaceListener)
        scheduleLoad(SETTLE_WAIT_MS)
    }

    fun shutdown() {
        context.project?.removeProjectResourceListener(resourceListener)
        workspace.removeChangeListener(workspaceListener)
        stopped.set(true)
        executionManager.shutdown()

        invokeLater {
            ComponentIcons.onImageLoaded = {}
            usedComponentIds = emptySet()
            updateComponents(emptyList())
        }
    }

    fun addListener(listener: () -> Unit) {
        listeners += listener
    }

    private fun schedule(task: () -> Unit, delayMs: Long): ScheduledFuture<*>? {
        return try {
            executionManager.executeOnce(task, delayMs, TimeUnit.MILLISECONDS)
        } catch (e: RejectedExecutionException) {
            null
        }
    }

    private fun scheduleLoad(delayMs: Long) {
        scheduledLoad.getAndSet(schedule(::loadComponents, delayMs))?.cancel(false)
    }

    private fun scheduleUsedComponentIds() {
        schedule(::loadUsedComponentIds, 0)
    }

    private fun resourcesChanged() {
        scheduleUsedComponentIds()
        scheduleLoad(SETTLE_WAIT_MS)
    }

    private fun loadUsedComponentIds() {
        val usedIds = findUsedComponentIds()

        invokeLater {
            if (stopped.get()) {
                return@invokeLater
            }
            usedComponentIds = usedIds
            registerComponents()
        }
    }

    private fun findUsedComponentIds(): Set<String> {
        val views = context.project?.getResourcesOfType(VIEWS).orEmpty()
        return views
            .flatMap { view ->
                val viewJson = view.getData(VIEW_FILE)?.decodeToString().orEmpty()
                ProjectComponents.findUsedComponents(viewJson)
            }
            .toSet()
    }

    private fun loadComponents() {
        val result = describeComponents()
        if (result == null) {
            scheduleLoad(RETRY_WAIT_MS)
            return
        }

        result.components?.let {
            definitions = it
            reportFailures(result.failures.orEmpty())
            loadedRevision = result.revision
        }
        val loadedComponents = componentsOf(definitions)

        invokeLater {
            if (stopped.get()) {
                return@invokeLater
            }
            updateComponents(loadedComponents)
        }
    }

    private fun componentsOf(definitions: List<ComponentDefinition>): List<ComponentInfo> {
        val attributesByPath = findComponentAttributes()
        val identified =
            definitions
                .sortedBy { it.path }
                .mapNotNull { definition ->
                    attributesByPath[definition.path]
                        ?.takeIf { it.hasValidId }
                        ?.let { definition to it }
                }

        val firstPathsById = mutableMapOf<String, String>()
        return identified.map { (definition, attributes) ->
            val firstPath = firstPathsById.getOrPut(attributes.id) { definition.path }
            ComponentInfo(attributes, definition, diagnosticsOf(definition, attributes, firstPath))
        }
    }

    private fun diagnosticsOf(
        definition: ComponentDefinition,
        attributes: ComponentAttributes,
        firstPath: String,
    ): List<String> {
        val diagnostics = definition.diagnostics.orEmpty()
        if (firstPath == definition.path) {
            return diagnostics
        }

        return diagnostics + i18n("component.error.duplicateId", attributes.id, firstPath)
    }

    private fun findComponentAttributes(): Map<String, ComponentAttributes> {
        return context.findComponentResources().associate {
            it.resourcePath.pathString to ComponentAttributes.fromResource(it)
        }
    }

    private fun describeComponents(): ComponentsResult? {
        return try {
            workspace.service
                .describeComponents(loadedRevision)
                .get(EVALUATE_WAIT_SECONDS, TimeUnit.SECONDS)
        } catch (e: Exception) {
            logger.debug("Failed to describe components.", e)
            null
        }
    }

    private fun reportFailures(failures: List<ComponentFailure>) {
        if (failures == reportedFailures) {
            return
        }
        reportedFailures = failures

        failures.forEach {
            logger.warn("Failed to evaluate client resource '${it.path}': ${it.message}")
        }
        workspace.notifyEvaluated()
    }

    private fun updateComponents(loadedComponents: List<ComponentInfo>) {
        val changed = loadedComponents != components
        components = loadedComponents
        registerComponents()

        if (changed) {
            listeners.forEach { it() }
        }
    }

    private fun registerComponents() {
        val registry = perspectiveDesignerInterface.designerComponentRegistry
        val projectComponents = components.reversed().associateBy { it.id }
        val missingComponents = (usedComponentIds - projectComponents.keys).associateWith { null }
        val wantedComponents: Map<String, ComponentInfo?> = missingComponents + projectComponents

        val changes = mutableListOf<() -> Unit>()
        registeredComponentsById.changedFrom(wantedComponents).keys.forEach { id ->
            changes += { registry.removeComponent(ProjectComponents.getType(id)) }
        }
        wantedComponents.changedFrom(registeredComponentsById).forEach { (id, info) ->
            changes += { registerComponent(registry, id, info) }
        }

        registeredComponentsById = wantedComponents
        if (changes.isEmpty()) {
            return
        }

        applyChanges(registry, changes)
        context.frame.repaint()
    }

    private fun registerComponent(
        registry: DesignerComponentRegistry,
        id: String,
        info: ComponentInfo?,
    ) {
        ComponentIcons.get(id).show(info?.definition?.icon)
        try {
            registry.registerComponent(createDescriptor(id, info))
        } catch (e: Exception) {
            logger.warn("Failed to register React Component '$id'.", e)
        }
    }

    private fun Map<String, ComponentInfo?>.changedFrom(
        other: Map<String, ComponentInfo?>,
    ): Map<String, ComponentInfo?> {
        return filter { (id, info) -> !other.containsKey(id) || other[id] != info }
    }

    private fun applyChanges(registry: DesignerComponentRegistry, changes: List<() -> Unit>) {
        // Each change makes the Designer rebuild its palette, so only the last one notifies it.
        @Suppress("UNCHECKED_CAST")
        val registryListeners =
            try {
                registry.getSuperPrivateProperty("listeners") as MutableCollection<Any>
            } catch (e: Exception) {
                changes.forEach { it() }
                return
            }

        val heldListeners = registryListeners.toList()
        registryListeners.clear()
        try {
            changes.dropLast(1).forEach { it() }
        } finally {
            registryListeners.addAll(heldListeners)
        }
        changes.last()()
    }

    private fun createDescriptor(id: String, info: ComponentInfo?): ComponentDescriptor {
        val descriptor =
            if (info == null) ProjectComponents.createDescriptor(id) else descriptorOf(info)
        return IconComponentDescriptor(descriptor, ComponentIcons.get(id))
    }

    private fun descriptorOf(info: ComponentInfo): ComponentDescriptor {
        val events =
            info.definition.events.orEmpty().map {
                ComponentEventDescriptor(it.name, it.description.orEmpty(), parseSchema(it.schema))
            }

        return ProjectComponents.createDescriptor(
            info.id,
            info.name,
            parseSchema(info.definition.schema),
            info.definition.defaults,
            events,
            info.definition.position?.let(::parseSchema),
        )
    }

    private fun parseSchema(json: JsonObject): JsonSchema {
        return ProjectComponents.createSchema(json.toString())
    }

    private class IconComponentDescriptor(
        private val descriptor: ComponentDescriptor,
        private val icon: Icon,
    ) : ComponentDescriptor by descriptor {

        override fun childPositionSchema(): JsonSchema? {
            return descriptor.childPositionSchema()
        }

        override fun events(): Collection<ComponentEventDescriptor?> {
            return descriptor.events()
        }

        override fun extensionFunctions(): Collection<ExtensionFunctionDescriptor?> {
            return descriptor.extensionFunctions()
        }

        override fun getInitialProps(variantId: String?): JsonObject? {
            return descriptor.getInitialProps(variantId)
        }

        override fun getIcon(): Optional<Icon> {
            return Optional.of(icon)
        }
    }
}
