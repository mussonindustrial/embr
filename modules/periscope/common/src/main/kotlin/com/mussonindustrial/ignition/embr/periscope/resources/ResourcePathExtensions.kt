package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath

val ResourcePath.pathString: String
    get() = path.toString()
