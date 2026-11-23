package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.BundleUtil

fun i18n(key: String, vararg args: Any): String {
    return BundleUtil.i18n("${Meta.BUNDLE_PREFIX}.$key", *args)
}
