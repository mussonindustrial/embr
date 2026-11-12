package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.common.project.resource.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.TypeScriptCompiler
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageFeatures
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.ScriptLanguage
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.applyEdits
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation.OutlinePanel
import com.mussonindustrial.ignition.embr.periscope.resources.language.FileEdits
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import javax.swing.Icon
import org.fife.ui.rsyntaxtextarea.SyntaxConstants

class TypeScriptResourceEditor(workspace: TabbedResourceWorkspace, path: ResourcePath) :
    CompiledResourceEditor<TypeScriptResource>(workspace, path) {

    override val compiler = TypeScriptCompiler(PeriscopeDesignerContext.instance.bridge)

    companion object {
        val factory = ResourceEditorFactory(::TypeScriptResourceEditor)
    }

    override val descriptor
        get() = TypeScriptResource

    override val virtualFileType = ".tsx"
    override val headerIcon: Icon
        get() = PeriscopeIcons.tsx

    override val syntaxStyle: String
        get() = SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT

    private lateinit var language: LanguageFeatures

    override fun init(resource: TypeScriptResource) {
        onClosed()
        super.init(resource)

        language =
            LanguageFeatures(
                    PeriscopeDesignerContext.instance.typeScriptWorkspace,
                    editor,
                    resourcePath.path.toString(),
                    ScriptLanguage.TYPESCRIPT,
                    navigate = ::open,
                    edit = ::edit,
                    showDiagnostics = ::showAnalysis,
                )
                .apply { install() }
        addSidePanel(OutlinePanel(language.session, editor))
    }

    override fun onClosed() {
        if (::language.isInitialized) language.uninstall()
    }

    private fun open(location: Location) {
        val path = location.path ?: return
        openResource(path) { it.reveal(location) }
    }

    private fun edit(changes: FileEdits) {
        openResource(changes.path) { it.textArea.applyEdits(changes.edits) }
    }
}
