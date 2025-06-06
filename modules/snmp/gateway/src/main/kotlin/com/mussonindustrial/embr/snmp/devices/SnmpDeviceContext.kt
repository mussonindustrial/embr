package com.mussonindustrial.embr.snmp.devices

import com.inductiveautomation.ignition.gateway.opcua.server.api.DeviceContext

class SnmpDeviceContext(context: DeviceContext) : DeviceContext by context

fun DeviceContext.asSnmpDeviceContext(): SnmpDeviceContext = SnmpDeviceContext(this)
