package com.mussonindustrial.ignition.embr.periscope

import com.inductiveautomation.ignition.common.BundleUtil

object Meta {
    const val MODULE_ID = "com.mussonindustrial.embr.periscope"
    const val SHORT_MODULE_ID = "embr-periscope"
    const val BUNDLE_PREFIX = "embr_periscope"

    fun addI18NBundle() {
        BundleUtil.get().addBundle(BUNDLE_PREFIX, Meta::class.java, "localization")
    }

    fun removeI18NBundle() {
        BundleUtil.get().removeBundle(BUNDLE_PREFIX)
    }
}
