package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceBuilder
import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceEditor
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.CompiledResource
import com.mussonindustrial.ignition.embr.periscope.resources.CssModuleResource
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.CssModuleCompiler
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

    override val headerIcon = PeriscopeIcons.css
    override val virtualFileType = ".css"

    override fun init(resource: CssModuleResource) {
        super.init(resource)
        textArea.apply {
            syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_CSS
            parserDelay = 100
            isCodeFoldingEnabled = true
            tabSize = 2
            tabsEmulated = true
            convertTabsToSpaces()
        }
    }

    override fun getObjectForSave(): CssModuleResource {
        val source = textArea.text
        val compiled =
            if (compileResult?.success == true) {
                compileResult!!.output!!
            } else {
                "/* Compilation failed */"
            }

        val fileContents = CompiledResource.FileContents(source, compiled)
        val compilerMetadata = CompiledResource.CompilerMetadata(compiler.version)
        return CssModuleResource(resource.fileLocations, fileContents, compilerMetadata)
    }

    override fun deserialize(resource: Resource): CssModuleResource {
        return CssModuleResource.fromResource(resource)
    }

    @Throws(JSONException::class)
    override fun serializeResource(builder: ResourceBuilder, resource: CssModuleResource) {
        resource.applyToBuilder(builder)
    }
}
