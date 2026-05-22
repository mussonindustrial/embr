package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceBuilder
import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceEditor
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.CompiledResource
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.*
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.json.JSONException

class TypeScriptResourceEditor(workspace: TabbedResourceWorkspace, path: ResourcePath) :
    CompiledResourceEditor<TypeScriptResource>(workspace, path) {

    override val compiler = TypeScriptCompiler(PeriscopeDesignerContext.instance.bridge)

    companion object {
        val factory =
            object : ResourceEditorFactory<TypeScriptResource> {
                override fun createResourceEditor(
                    workspace: TabbedResourceWorkspace,
                    path: ResourcePath,
                ): ResourceEditor<TypeScriptResource> = TypeScriptResourceEditor(workspace, path)
            }
    }

    override val headerIcon = PeriscopeIcons.tsx
    override val virtualFileType = ".tsx"

    override fun init(resource: TypeScriptResource) {
        super.init(resource)
        textArea.apply {
            syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT
            parserDelay = 100
            isCodeFoldingEnabled = true
            tabSize = 2
            tabsEmulated = true
            convertTabsToSpaces()
        }
    }

    override fun getObjectForSave(): TypeScriptResource {
        val source = textArea.text
        val compiled =
            if (compileResult?.success == true) {
                compileResult!!.output!!
            } else {
                "/* Compilation failed */"
            }

        val fileContents = CompiledResource.FileContents(source, compiled)
        val compilerMetadata = CompiledResource.CompilerMetadata(compiler.version)
        return TypeScriptResource(resource.fileLocations, fileContents, compilerMetadata)
    }

    override fun deserialize(resource: Resource): TypeScriptResource {
        return TypeScriptResource.fromResource(resource)
    }

    @Throws(JSONException::class)
    override fun serializeResource(builder: ResourceBuilder, resource: TypeScriptResource) {
        resource.applyToBuilder(builder)
    }
}
