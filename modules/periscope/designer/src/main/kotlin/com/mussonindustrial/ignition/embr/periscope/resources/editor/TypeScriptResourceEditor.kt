package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceBuilder
import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceEditor
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.TypeScriptCompiler
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageDiagnostics
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageFeatures
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.ScriptLanguage
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.applyEdits
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation.OutlinePanel
import com.mussonindustrial.ignition.embr.periscope.resources.language.FileEdits
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import javax.swing.Icon
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

    override val virtualFileType = ".tsx"
    override val headerIcon: Icon
        get() = PeriscopeIcons.tsx

    override val syntaxStyle: String
        get() = SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT

    private lateinit var language: LanguageFeatures
    private lateinit var diagnostics: LanguageDiagnostics
    private lateinit var outline: OutlinePanel

    override fun init(resource: TypeScriptResource) {
        uninstallLanguage()
        super.init(resource)
        textArea.apply {
            parserDelay = 100
            convertTabsToSpaces()
        }

        language =
            LanguageFeatures(
                    PeriscopeDesignerContext.instance.languageProject,
                    editor,
                    resourcePath.path.toString(),
                    ScriptLanguage.TYPESCRIPT,
                    navigate = ::open,
                    edit = ::edit,
                )
                .apply { install() }
        diagnostics = LanguageDiagnostics(language.session, ::showAnalysis).apply { install() }
        outline = OutlinePanel(language.session, editor).apply { install() }
        addSidePanel(outline)
    }

    override fun dispose() {
        uninstallLanguage()
        super.dispose()
    }

    private fun uninstallLanguage() {
        if (::outline.isInitialized) outline.uninstall()
        if (::diagnostics.isInitialized) diagnostics.uninstall()
        if (::language.isInitialized) language.uninstall()
    }

    private fun open(location: Location) {
        val path = location.path ?: return
        openResource(path) { it.reveal(location) }
    }

    private fun edit(changes: FileEdits) {
        openResource(changes.path) { it.textArea.applyEdits(changes.edits) }
    }

    override fun getObjectForSave(): TypeScriptResource {
        return TypeScriptResource(resource.fileLocations, compiledContents, compilerMetadata)
    }

    override fun deserialize(resource: Resource): TypeScriptResource {
        return TypeScriptResource.fromResource(resource)
    }

    @Throws(JSONException::class)
    override fun serializeResource(builder: ResourceBuilder, resource: TypeScriptResource) {
        resource.applyToBuilder(builder)
    }
}
