import com.mussonindustrial.ignition.embr.e2e.env.TestEnvironmentExtension
import com.mussonindustrial.ignition.embr.e2e.env.TestEnvironmentService

extensions.create<TestEnvironmentExtension>("testEnvironment")

val testEnvironmentExtension = extensions.getByType<TestEnvironmentExtension>()

val testEnvironment =
    gradle.sharedServices.registerIfAbsent("testEnvironment", TestEnvironmentService::class.java) {
        maxParallelUsages = 1
        parameters.ignitionDockerImage = testEnvironmentExtension.ignition.dockerImage
        parameters.ignitionGatewayBackup = testEnvironmentExtension.ignition.gatewayBackup
        parameters.ignitionModulesDir = testEnvironmentExtension.ignition.modulesDir
        parameters.playwrightDockerImage = testEnvironmentExtension.playwright.dockerImage
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
