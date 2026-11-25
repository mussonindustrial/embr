package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.ScriptLanguage
import javax.swing.Icon

class TypeScriptResourceEditor(workspace: TabbedResourceWorkspace, path: ResourcePath) :
    ScriptResourceEditor<TypeScriptResource>(workspace, path) {

    companion object {
        val factory = ResourceEditorFactory(::TypeScriptResourceEditor)
    }

    override val descriptor
        get() = TypeScriptResource

    override val scriptLanguage
        get() = ScriptLanguage.TYPESCRIPT

    override val headerIcon: Icon
        get() = PeriscopeIcons.tsx
}
