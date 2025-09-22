package com.mussonindustrial.embr.gateway.secrets

import com.inductiveautomation.ignition.gateway.model.GatewayContext
import com.inductiveautomation.ignition.gateway.secrets.Secret
import com.inductiveautomation.ignition.gateway.secrets.SecretConfig

fun GatewayContext.getAsString(secret: SecretConfig) : String {
    return Secret.create(this, secret).plaintext.asString
}