
plugins {
    id("embr.ignition-module-conventions")
}

ignitionModule {
    name.set("Embr Web Assets")
    moduleDescription.set("Adds the ability to serve web assets from the gateway.")
    id.set("com.mussonindustrial.embr.webassets")
    fileName.set("Embr-WebAssets-${version}.modl")
    freeModule.set(true)
    requiredIgnitionVersion.set(libs.versions.ignition)
    license.set("license.html")

    projectScopes.putAll(
        mapOf(
            ":modules:webassets:common" to "GD",
            ":modules:webassets:designer" to "D",
            ":modules:webassets:gateway" to "G",
        ),
    )

    hooks.putAll(
        mapOf(
            "com.mussonindustrial.ignition.embr.webassets.WebAssetsGatewayHook" to "G",
            "com.mussonindustrial.ignition.embr.webassets.WebAssetsDesignerHook" to "D",
        ),
    )
}