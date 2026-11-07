package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceBuilder
import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceEditor
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.CssModuleResource
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.CssModuleCompiler
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import javax.swing.Icon
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.json.JSONException

class CssModuleResourceEditor(workspace: TabbedResourceWorkspace, path: ResourcePath) :
    CompiledResourceEditor<CssModuleResource>(workspace, path) {

    override val compiler = CssModuleCompiler(PeriscopeDesignerContext.instance.bridge)

    companion object {
        val factory =
            object : ResourceEditorFactory<CssModuleResource> {
                override fun createResourceEditor(
                    workspace: TabbedResourceWorkspace,
                    path: ResourcePath,
                ): ResourceEditor<CssModuleResource> = CssModuleResourceEditor(workspace, path)
            }
    }

    private val classNamePattern = Regex("\"([^\"]+)\"")

    override val virtualFileType = ".css"
    override val headerIcon: Icon
        get() = PeriscopeIcons.css

    override val syntaxStyle: String
        get() = SyntaxConstants.SYNTAX_STYLE_CSS

    override fun init(resource: CssModuleResource) {
        super.init(resource)
        textArea.apply {
            parserDelay = 100
            convertTabsToSpaces()
        }
    }

    override fun reveal(location: Location) {
        val className = classNamePattern.find(location.lineText)?.groupValues?.get(1)
        val selector =
            className
                ?.let { listOf(it, it.camelToKebab()) }
                ?.firstOrNull { ".$it" in textArea.text }
        if (selector == null) {
            select(0, 0)
            return
        }
        select(textArea.text.indexOf(".$selector") + 1, selector.length)
    }

    private fun String.camelToKebab() = replace(Regex("([a-z0-9])([A-Z])"), "$1-$2").lowercase()

    override fun getObjectForSave(): CssModuleResource {
        return CssModuleResource(resource.fileLocations, compiledContents, compilerMetadata)
    }

    override fun deserialize(resource: Resource): CssModuleResource {
        return CssModuleResource.fromResource(resource)
    }

    @Throws(JSONException::class)
    override fun serializeResource(builder: ResourceBuilder, resource: CssModuleResource) {
        resource.applyToBuilder(builder)
    }
}
