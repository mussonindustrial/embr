package com.mussonindustrial.embr.perspective.gateway.model

import com.inductiveautomation.perspective.gateway.model.PageModel

fun requireThreadPage(): PageModel {
    val page =
        PageModel.PAGE.get()
            ?: throw IllegalAccessException("Must be called in a Perspective Page context")
    return page
}
