package com.mussonindustrial.embr.snmp;

import com.inductiveautomation.ignition.common.project.ClientPermissionsConstants;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.rpc.RpcDelegate;

import java.util.function.Supplier;

/**
 * This is the actual implementation of the RPC functions that will be called by the client/designer.
 * The @RunsOnClient annotation is needed for <b>any</b> RPC function that will be allowed to be invoked by Vision
 * clients.
 * If you do not have a custom client permission ID registered with the rest of the system, use the special UNRESTRICTED
 * value, as below.
 */
@RpcDelegate.RunsOnClient(clientPermissionId = ClientPermissionsConstants.UNRESTRICTED)
public class RpcFunctionsImpl implements RpcFunctions {

    public RpcFunctionsImpl(GatewayContext gatewayContext) {
    }

    @Override
    public String getV1(SnmpRequest request) {
        return "Testing String V1";
    }

    @Override
    public String getV2c(SnmpRequest request){
        return "Testing String V2";
    }

    @Override
    public String getV3(SnmpRequest request){
        return "Testing String V3";
    }
}
