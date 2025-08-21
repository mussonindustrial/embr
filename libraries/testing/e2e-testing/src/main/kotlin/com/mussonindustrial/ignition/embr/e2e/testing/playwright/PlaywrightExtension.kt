package com.mussonindustrial.ignition.embr.e2e.testing.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.kotest.core.extensions.Extension
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.listeners.BeforeTestListener
import io.kotest.core.spec.Spec
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class PlaywrightContainer(image: String = "mcr.microsoft.com/playwright:v1.55.0-noble") :
    GenericContainer<PlaywrightContainer>(DockerImageName.parse(image)) {

    init {
        withExposedPorts(3000)
        withCommand(
            "/bin/sh",
            "-c",
            "npx -y playwright@1.55.0 run-server --port 3000 --host 0.0.0.0",
        )
        withWorkingDirectory("/home/pwuser")
        withCreateContainerCmdModifier { cmd -> cmd.withUser("pwuser") }
    }
}

class PlaywrightExtension(val websocketUrl: String) :
    Extension, BeforeSpecListener, AfterSpecListener, BeforeTestListener, AfterTestListener {

    //    lateinit var container: PlaywrightContainer
    //        private set

    lateinit var playwright: Playwright
        private set

    lateinit var browser: Browser
        private set

    lateinit var context: BrowserContext
        private set

    lateinit var page: Page
        private set

    override suspend fun beforeSpec(spec: Spec) {
        playwright = Playwright.create()
        browser = playwright.chromium().connect(websocketUrl)
        context = browser.newContext()
        page = context.newPage()
    }

    override suspend fun afterSpec(spec: Spec) {
        context.close()
        browser.close()
        playwright.close()
        //        container.stop()
    }
}
