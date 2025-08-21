package com.mussonindustrial.ignition.embr.e2e.testing.playwright

import io.kotest.core.spec.Spec

fun Spec.usePlaywrightContainer(websocketUrl: String): PlaywrightExtension {
    return extension(PlaywrightExtension(websocketUrl))
}
