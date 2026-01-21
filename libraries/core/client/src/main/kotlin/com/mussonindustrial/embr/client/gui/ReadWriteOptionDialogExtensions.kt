package com.mussonindustrial.embr.client.gui

import com.inductiveautomation.factorypmi.application.runtime.ClientGatewayConnection
import com.inductiveautomation.ignition.client.gateway_interface.GatewayConnectionManager
import com.inductiveautomation.ignition.client.sqltags.impl.db.ReadWriteOptionDialog

private fun <T> ReadWriteOptionDialog.runProtectedAction(
    allowedMode: Int,
    temporaryMode: Int,
    block: () -> T,
): T {
    val connectionManager = GatewayConnectionManager.getInstance()

    if (connectionManager.connectionMode == allowedMode) {
        return block()
    }

    val originalMode = connectionManager.connectionMode
    return when (showOptionDialog()) {
        ReadWriteOptionDialog.CHANGE_RW -> {
            connectionManager.connectionMode = temporaryMode
            block()
        }
        ReadWriteOptionDialog.ONE_TIME -> {
            connectionManager.connectionMode = temporaryMode
            val result = block()
            connectionManager.connectionMode = originalMode
            result
        }
        else ->
            throw IllegalStateException("Action not allowed in current Gateway communication mode.")
    }
}

fun <T> ReadWriteOptionDialog.runReadProtectedAction(block: () -> T): T =
    runProtectedAction(
        allowedMode = ClientGatewayConnection.MODE_DISCONNECTED,
        temporaryMode = ClientGatewayConnection.MODE_FULL,
        block = block,
    )

fun <T> ReadWriteOptionDialog.runWriteProtectedAction(block: () -> T): T =
    runProtectedAction(
        allowedMode = ClientGatewayConnection.MODE_FULL,
        temporaryMode = ClientGatewayConnection.MODE_READ_ONLY,
        block = block,
    )
