package com.mussonindustrial.embr.snmp;

import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.mussonindustrial.embr.snmp.ClientScriptModule;

public class DesignerHook extends AbstractDesignerModuleHook {

    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);

        manager.addScriptModule(
            "system.embr.snmp",
            new ClientScriptModule(),
            new PropertiesFileDocProvider()
        );
    }

}
