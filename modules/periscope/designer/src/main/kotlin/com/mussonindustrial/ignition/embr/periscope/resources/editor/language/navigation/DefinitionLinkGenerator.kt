package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import javax.swing.event.HyperlinkEvent
import org.fife.ui.rsyntaxtextarea.LinkGenerator
import org.fife.ui.rsyntaxtextarea.LinkGeneratorResult
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea

class DefinitionLinkGenerator(
    private val session: LanguageSession,
    private val navigate: (Location) -> Unit,
) : LinkGenerator {

    override fun isLinkAtOffset(textArea: RSyntaxTextArea, offset: Int): LinkGeneratorResult? {
        val result =
            session.query(LanguageSession.HOVER_WAIT_MS) { definition(session.path, offset) }
                ?: return null
        val target = result.target ?: return null

        return object : LinkGeneratorResult {
            override fun execute(): HyperlinkEvent? {
                navigate(target)
                return null
            }

            override fun getSourceOffset(): Int = result.span.start
        }
    }
}
