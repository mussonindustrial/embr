package com.mussonindustrial.embr.perspective.gateway.model

import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.model.ViewModel
import com.inductiveautomation.perspective.gateway.session.InternalSession
import com.mussonindustrial.embr.perspective.gateway.model.ThreadContext.Companion.get
import com.mussonindustrial.embr.perspective.gateway.model.ThreadContext.Companion.set
import java.lang.ref.WeakReference

class ThreadContext(view: ViewModel?, page: PageModel?, session: InternalSession?) {
    val view = WeakReference(view)
    val page = WeakReference(page)
    val session = WeakReference(session)

    companion object {
        fun get(): ThreadContext {
            return ThreadContext(
                ViewModel.VIEW.get(),
                PageModel.PAGE.get(),
                InternalSession.SESSION.get(),
            )
        }

        fun set(threadContext: ThreadContext) {
            ViewModel.VIEW.set(threadContext.view.get())
            PageModel.PAGE.set(threadContext.page.get())
            InternalSession.SESSION.set(threadContext.session.get())
        }
    }
}

fun withThreadContext(threadContext: ThreadContext, block: () -> Unit) {
    val previous = get()
    try {
        set(threadContext)
        block()
    } finally {
        set(previous)
    }
}
