package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.client.util.gui.AntialiasLabel
import com.inductiveautomation.ignition.client.util.gui.HeaderLabel
import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.mussonindustrial.ignition.embr.periscope.i18n
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentAttributes
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ComponentAttributesPanel
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ComponentDelegatePanel
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.EditorSection
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.EditorTabs
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.ScriptLanguage
import com.mussonindustrial.ignition.embr.periscope.resources.findComponentResources
import com.mussonindustrial.ignition.embr.periscope.resources.pathString
import java.util.concurrent.ConcurrentHashMap
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel
import net.miginfocom.swing.MigLayout

class ComponentResourceEditor(workspace: TabbedResourceWorkspace, path: ResourcePath) :
    ScriptResourceEditor<ComponentResource>(workspace, path) {

    companion object {
        private const val COMPONENT_TAB = "component"
        private const val DELEGATE_TAB = "delegate"
        private const val ATTRIBUTES_SECTION = "attributes"
        private const val DESCRIPTION_SECTION = "description"
        private val selectedTabsByPath = ConcurrentHashMap<ResourcePath, String>()
        val factory = ResourceEditorFactory(::ComponentResourceEditor)
    }

    private lateinit var attributesPanel: ComponentAttributesPanel
    private lateinit var delegatePanel: ComponentDelegatePanel
    private lateinit var tabs: EditorTabs

    private val otherComponents: List<ProjectResource>
        get() =
            PeriscopeDesignerContext.instance.context.findComponentResources().filter {
                it.resourcePath != resourcePath
            }

    override val descriptor
        get() = ComponentResource

    override val scriptLanguage
        get() = ScriptLanguage.COMPONENT

    override val headerIcon: Icon
        get() = PeriscopeIcons.component

    override fun createHeaderPanel(resource: ComponentResource): JComponent {
        attributesPanel =
            ComponentAttributesPanel(
                resource.attributes,
                resourcePath.pathString.substringAfterLast('/'),
                palettePaths(),
                findDuplicate = ::pathOfOtherComponent,
                onChanged = ::attributesChanged,
            )
        showTitleDetail(attributesPanel.attributes.id)

        return EditorSection(
            i18n("component.section.attributes"),
            attributesPanel,
            ATTRIBUTES_SECTION,
            keepExpanded = attributesPanel.hasInvalidId,
        )
    }

    override fun createBodyPanel(resource: ComponentResource, editor: JComponent): JComponent {
        delegatePanel =
            ComponentDelegatePanel(resource.delegate, resource.delegateEnabled, ::delegateChanged)

        tabs =
            EditorTabs().apply {
                addTab(COMPONENT_TAB, i18n("component.tab.component"), createComponentPanel(editor))
                addTab(DELEGATE_TAB, i18n("component.tab.delegate"), delegatePanel)
                select(selectedTabsByPath[resourcePath] ?: COMPONENT_TAB)
                onSelected { selectedTabsByPath[resourcePath] = it }
            }

        showDelegateMarker()
        return tabs
    }

    override fun onStatusChanged(hasErrors: Boolean) {
        tabs.mark(COMPONENT_TAB, EditorTabs.Marker.ERROR.takeIf { hasErrors })
    }

    override fun getObjectForSave(): ComponentResource {
        return super.getObjectForSave()
            .withAttributes(attributesPanel.attributes)
            .withDelegate(delegatePanel.delegate, delegatePanel.isDelegateEnabled)
    }

    private fun createComponentPanel(editor: JComponent): JComponent {
        val description =
            AntialiasLabel("<html>${i18n("component.desc")}").apply {
                border = BorderFactory.createEmptyBorder(6, 6, 6, 6)
            }

        return JPanel(MigLayout("ins 0, fill, wrap 1", "[grow, fill]")).apply {
            add(
                EditorSection(
                    BundleUtil.i18n("words.description"),
                    description,
                    DESCRIPTION_SECTION,
                ),
                "wmin 0",
            )
            add(HeaderLabel.forKey("words.script"), "gaptop 8")
            add(editor, "push, grow")
        }
    }

    private fun attributesChanged() {
        showTitleDetail(attributesPanel.attributes.id)
        notifyChanged()
    }

    private fun delegateChanged() {
        showDelegateMarker()
        notifyChanged()
    }

    private fun showDelegateMarker() {
        tabs.mark(
            DELEGATE_TAB,
            EditorTabs.Marker.CONFIGURED.takeIf {
                delegatePanel.isDelegateEnabled && delegatePanel.delegate.isNotEmpty()
            },
        )
    }

    private fun pathOfOtherComponent(id: String): String? {
        return otherComponents
            .find { ComponentAttributes.fromResource(it).id == id }
            ?.resourcePath
            ?.pathString
    }

    private fun palettePaths(): List<List<String>> {
        return otherComponents
            .map { ComponentAttributes.fromResource(it).paletteFolders }
            .filter { it.isNotEmpty() }
            .distinct()
    }
}
