package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceEditor
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace

@FunctionalInterface
interface ResourceEditorFactory<T> {
    fun createResourceEditor(
        workspace: TabbedResourceWorkspace,
        path: ResourcePath,
    ): ResourceEditor<T>
}
