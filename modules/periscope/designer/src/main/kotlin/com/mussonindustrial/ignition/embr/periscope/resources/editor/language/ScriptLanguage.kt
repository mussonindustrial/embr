package com.mussonindustrial.ignition.embr.periscope.resources.editor.language

import com.mussonindustrial.ignition.embr.periscope.resources.language.ResourceFile
import org.fife.ui.rsyntaxtextarea.SyntaxConstants

enum class ScriptLanguage(val kind: String, val syntaxStyle: String) {
    TYPESCRIPT(ResourceFile.TYPESCRIPT, SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT),
    JAVASCRIPT(ResourceFile.JAVASCRIPT, SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT),
}
