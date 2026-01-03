package com.mussonindustrial.embr.snmp;

public class GatewayScriptModule extends AbstractScriptModule {

    public GatewayScriptModule() {
    }

    @Override
    protected String getV1(SnmpRequest request) {
        return "TestGateway String";
    }
}
