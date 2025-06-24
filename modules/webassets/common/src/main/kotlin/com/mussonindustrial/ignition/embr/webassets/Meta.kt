package com.mussonindustrial.ignition.embr.webassets

import com.inductiveautomation.ignition.common.BundleUtil

object Meta {
    const val MODULE_ID = "com.mussonindustrial.embr.webassets"
    const val SHORT_MODULE_ID = "embr-webassets"
    const val BUNDLE_PREFIX = "webassets"

    fun addI18NBundle() {
        BundleUtil.get().addBundle(BUNDLE_PREFIX, Meta::class.java, "localization")
    }

    fun removeI18NBundle() {
        BundleUtil.get().removeBundle(BUNDLE_PREFIX)
    }
}
