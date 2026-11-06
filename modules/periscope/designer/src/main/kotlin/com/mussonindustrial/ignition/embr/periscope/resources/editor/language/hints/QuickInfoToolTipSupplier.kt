package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.hints

import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.Html
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import java.awt.event.MouseEvent
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextArea
import org.fife.ui.rtextarea.ToolTipSupplier

class QuickInfoToolTipSupplier(private val session: LanguageSession) : ToolTipSupplier {

    override fun getToolTipText(textArea: RTextArea, e: MouseEvent): String? {
        val offset = textArea.viewToModel2D(e.point)
        if (offset < 0) return null

        val problems =
            (textArea as RSyntaxTextArea)
                .parserNotices
                .filter { it.containsPosition(offset) }
                .map { it.message }
        session.sync()
        val info = session.service.quickInfo(session.path, offset)
        if (info == null && problems.isEmpty()) return null
        return Html.tooltip(problems, info?.display, info?.documentationHtml.orEmpty())
    }
}
