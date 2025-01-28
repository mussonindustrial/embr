package com.mussonindustrial.ignition.embr.periscope.model

import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.model.ViewModel
import com.inductiveautomation.perspective.gateway.session.InternalSession
import java.util.*
import kotlin.jvm.optionals.getOrNull

class PerspectiveExecutionContext(
    private val context: PerspectiveContext,
    private val pageId: String? = null,
    private val sessionId: String? = null
) {

    fun getView(): ViewModel? {
        return ViewModel.VIEW.get()
    }

    fun getPage(): PageModel? {
        val session = getSession() ?: return null
        if (pageId?.isNotEmpty() == true) {
            val page = session.findPage(pageId)
            return page.getOrNull()
        }

        val page = PageModel.PAGE.get()

        require(
            !(sessionId?.isNotEmpty() == true &&
                UUID.fromString(sessionId) != page.session.sessionId)
        ) {
            "\"pageId\" is required when targeting another session."
        }

        return page
    }

    fun getSession(): InternalSession? {
        if (sessionId?.isNotEmpty() == true) {
            val sessionUuid = UUID.fromString(sessionId)
            val session = context.sessionMonitor.findSession(sessionUuid)
            return session.getOrNull()
        }

        return InternalSession.SESSION.get()
    }
}
