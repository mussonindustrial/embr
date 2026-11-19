package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentLibraryResource
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.ScriptLanguage
import javax.swing.Icon

class ComponentLibraryResourceEditor(workspace: TabbedResourceWorkspace, path: ResourcePath) :
    ScriptResourceEditor<ComponentLibraryResource>(workspace, path) {

    companion object {
        val factory = ResourceEditorFactory(::ComponentLibraryResourceEditor)
    }

    override val descriptor
        get() = ComponentLibraryResource

    override val scriptLanguage
        get() = ScriptLanguage.COMPONENT_LIBRARY

    override val headerIcon: Icon
        get() = PeriscopeIcons.componentLibrary
}
