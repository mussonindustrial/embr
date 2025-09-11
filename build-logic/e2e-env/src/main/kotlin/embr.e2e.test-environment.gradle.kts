import com.mussonindustrial.ignition.embr.e2e.env.TestEnvironmentExtension
import com.mussonindustrial.ignition.embr.e2e.env.TestEnvironmentService
import com.mussonindustrial.ignition.embr.e2e.env.TestEnvironmentTask

extensions.create<TestEnvironmentExtension>("testEnvironment")

val testEnvironmentExtension = extensions.getByType<TestEnvironmentExtension>()

val testEnvironment =
    gradle.sharedServices.registerIfAbsent("testEnvironment", TestEnvironmentService::class.java) {
        maxParallelUsages = 1
        parameters.ignitionImageTag = testEnvironmentExtension.ignitionImageTag
        parameters.ignitionGatewayBackup = testEnvironmentExtension.ignitionGatewayBackup
        parameters.ignitionModulesDir = testEnvironmentExtension.ignitionModulesDir
        parameters.playwrightImageTag = testEnvironmentExtension.playwrightImageTag
    }

tasks.withType<Test>().configureEach {
    usesService(testEnvironment)

    doFirst {
        environment("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")

        testEnvironment.get().start()

        val ignition = testEnvironment.get().ignitionContainer
        systemProperty("ignition.gatewayUrl", ignition.gatewayUrl)

        val playwright = testEnvironment.get().playwrightContainer
        systemProperty("playwright.websocketUrl", playwright.websocketUrl)
    }
}
