package com.mussonindustrial.ignition.embr.charts

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.kotest.core.extensions.Extension
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.listeners.BeforeTestListener
import io.kotest.core.spec.Spec

class PlaywrightExtension :
    Extension, BeforeSpecListener, AfterSpecListener, BeforeTestListener, AfterTestListener {

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
        browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
        context = browser.newContext()
        page = context.newPage()
    }

    override suspend fun afterSpec(spec: Spec) {
        context.close()
        browser.close()
        playwright.close()
    }

    //    override suspend fun beforeTest(testCase: TestCase) {
    //        page = context.newPage()
    //    }
    //
    //    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
    //        page.close()
    //    }
}
