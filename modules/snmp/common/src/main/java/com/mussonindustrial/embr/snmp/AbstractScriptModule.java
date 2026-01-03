package com.mussonindustrial.embr.snmp;

import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.PyArgParser;
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs;
import com.inductiveautomation.ignition.common.script.hints.JythonElement;
import org.python.core.PyObject;

public abstract class AbstractScriptModule {
    static {
        BundleUtil.get().addBundle(
            AbstractScriptModule.class.getSimpleName(),
            AbstractScriptModule.class.getClassLoader(),
            AbstractScriptModule.class.getName().replace('.', '/')
        );
    }

    protected abstract String getV1(SnmpRequest request);

    @KeywordArgs(
            names = {"device", "oid"},
            types = {String.class, String.class}
    )
    @JythonElement(docBundlePrefix = "AbstractScriptModule")
    public String doSomethingComplicated(PyObject[] args, String[] keywords) {
        PyArgParser argParser = PyArgParser.parseArgs(
                args,
                keywords,
                new String[]{"device", "oid"},
                new Class<?>[]{String.class, String.class},
                "getV1"
        );
        SnmpRequest request = new SnmpRequest(argParser.requireString("device"), argParser.requireString("oid"));
        return getV1(request);
    }
}
