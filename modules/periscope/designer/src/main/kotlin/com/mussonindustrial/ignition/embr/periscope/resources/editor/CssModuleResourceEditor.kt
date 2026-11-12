package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.CssModuleResource
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.CssModuleCompiler
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import javax.swing.Icon
import org.fife.ui.rsyntaxtextarea.SyntaxConstants

class CssModuleResourceEditor(workspace: TabbedResourceWorkspace, path: ResourcePath) :
    CompiledResourceEditor<CssModuleResource>(workspace, path) {

    override val compiler = CssModuleCompiler(PeriscopeDesignerContext.instance.bridge)

    companion object {
        val factory = ResourceEditorFactory(::CssModuleResourceEditor)
    }

    private val classNamePattern = Regex("\"([^\"]+)\"")

    override val descriptor
        get() = CssModuleResource

    override val virtualFileType = ".css"
    override val headerIcon: Icon
        get() = PeriscopeIcons.css

    override val syntaxStyle: String
        get() = SyntaxConstants.SYNTAX_STYLE_CSS

    override fun reveal(location: Location) {
        val className = classNamePattern.find(location.lineText)?.groupValues?.get(1)
        val selector =
            className
                ?.let { listOf(it, it.camelToKebab()) }
                ?.firstOrNull { ".$it" in textArea.text }
        if (selector == null) {
            editor.select(0, 0)
            return
        }
        editor.select(textArea.text.indexOf(".$selector") + 1, selector.length)
    }

    private fun String.camelToKebab() = replace(Regex("([a-z0-9])([A-Z])"), "$1-$2").lowercase()
}
