package com.mussonindustrial.ignition.embr.muijoy

import com.inductiveautomation.ignition.common.BundleUtil

object Meta {
    const val MODULE_ID = "com.mussonindustrial.embr.muijoy"
    const val SHORT_MODULE_ID = "embr-muijoy"
    const val BUNDLE_PREFIX = "muijoy"

    fun addI18NBundle() {
        BundleUtil.get().addBundle(BUNDLE_PREFIX, Meta::class.java, "localization")
    }

    fun removeI18NBundle() {
        BundleUtil.get().removeBundle(BUNDLE_PREFIX)
    }
}
