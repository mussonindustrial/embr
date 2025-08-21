package com.mussonindustrial.ignition.embr.charts

import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec

class GatewayTest :
    FunSpec({
        install(Containers.extension)

        test("gateway responds") {
            println("Gateway at ${Containers.gateway.gatewayUrl}")
            // assertions go here
        }
    })

class GatewayTest2 :
    FunSpec({
        install(Containers.extension)

        test("gateway responds") {
            println("Gateway at ${Containers.gateway.gatewayUrl}")
            // assertions go here
        }
    })
