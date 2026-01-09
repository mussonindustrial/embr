package com.mussonindustrial.embr.snmp;

public class GatewayScriptModule extends AbstractScriptModule {

    public GatewayScriptModule() {
    }

    @Override
    protected String getV1(SnmpRequest request) {
        return "TestGateway1 String";
    }

    @Override
    protected String getV2c(SnmpRequest request) {
        return "TestGateway2 String";
    }

    @Override
    protected String getV3(SnmpRequest request) {
        return "TestGateway3 String";
    }
}
