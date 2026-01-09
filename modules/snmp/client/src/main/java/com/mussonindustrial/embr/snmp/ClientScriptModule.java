package com.mussonindustrial.embr.snmp;

import com.inductiveautomation.ignition.client.gateway_interface.GatewayConnection;

public class ClientScriptModule extends AbstractScriptModule {

    private static final RpcFunctions RPC = GatewayConnection.getRpcInterface(
            RpcFunctions.SERIALIZER,
            "com.mussonindustrial.embr.snmp",
            RpcFunctions.class
    );

    public ClientScriptModule() {}

    @Override
    protected String getV1(SnmpRequest request) {
        return RPC.getV1(request);
    }

    @Override
    protected String getV2c(SnmpRequest request) {
        return RPC.getV2c(request);
    }

    @Override
    protected String getV3(SnmpRequest request) {
        return RPC.getV3(request);
    }
}
