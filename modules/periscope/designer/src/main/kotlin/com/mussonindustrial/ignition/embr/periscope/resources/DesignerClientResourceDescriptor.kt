package com.mussonindustrial.ignition.embr.periscope.resources

import com.mussonindustrial.ignition.embr.periscope.navtree.NavTreeNodeFactory
import com.mussonindustrial.ignition.embr.periscope.resources.editor.ResourceEditorFactory
import javax.swing.Icon

interface DesignerClientResourceDescriptor<T : ClientResource> : ClientResourceDescriptor<T> {
    override val icon: Icon
    val navTreeNodeFactory: NavTreeNodeFactory
    val resourceEditorFactory: ResourceEditorFactory<T>
}

class DesignerClientResourceDescriptorWrapper<T : ClientResource>(
    private val definition: ClientResourceDescriptor<T>,
    override val icon: Icon,
    override val navTreeNodeFactory: NavTreeNodeFactory,
    override val resourceEditorFactory: ResourceEditorFactory<T>,
) : ClientResourceDescriptor<T> by definition, DesignerClientResourceDescriptor<T>

fun <T : ClientResource> ClientResourceDescriptor<T>.asDesignerDescriptor(
    icon: Icon,
    navTreeNodeFactory: NavTreeNodeFactory,
    resourceEditorFactory: ResourceEditorFactory<T>,
): DesignerClientResourceDescriptor<T> {
    return DesignerClientResourceDescriptorWrapper(
        this,
        icon,
        navTreeNodeFactory,
        resourceEditorFactory,
    )
}
