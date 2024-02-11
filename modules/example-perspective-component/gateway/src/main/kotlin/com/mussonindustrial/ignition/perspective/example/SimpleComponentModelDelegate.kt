package com.mussonindustrial.ignition.perspective.example;

import com.inductiveautomation.perspective.gateway.api.Component
import com.inductiveautomation.perspective.gateway.api.ComponentModelDelegate;


class SimpleComponentModelDelegate(component: Component?) : ComponentModelDelegate(component) {

    override fun onStartup() {
        // Called when the Gateway's ComponentModel starts.  The start itself happens when the client project is
        // loading and includes an instance of the the component type in the page/view being started.
        log.debugf("Starting up delegate for '%s'!", component.componentAddressPath);
    }

    override fun onShutdown() {
        // Called when the component is removed from the page/view and the model is shutting down.
        log.debugf("Shutting down delegate for '%s'!", component.componentAddressPath);
    }
}