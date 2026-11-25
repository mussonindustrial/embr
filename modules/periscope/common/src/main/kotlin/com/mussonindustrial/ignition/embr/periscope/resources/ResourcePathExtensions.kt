package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.project.resource.ResourcePath

val ResourcePath.pathString: String
    get() = path.toString()
