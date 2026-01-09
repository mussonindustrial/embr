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

    protected abstract String getV2c(SnmpRequest request);

    protected abstract String getV3(SnmpRequest request);

    @KeywordArgs(
            names = {"device", "oid"},
            types = {String.class, String.class}
    )
    @JythonElement(docBundlePrefix = "AbstractScriptModule")
    public String getV1(PyObject[] args, String[] keywords) {
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

    @KeywordArgs(
            names = {"device", "oid"},
            types = {String.class, String.class}
    )
    @JythonElement(docBundlePrefix = "AbstractScriptModule")
    public String getV2c(PyObject[] args, String[] keywords) {
        PyArgParser argParser = PyArgParser.parseArgs(
                args,
                keywords,
                new String[]{"device", "oid"},
                new Class<?>[]{String.class, String.class},
                "getV1"
        );
        SnmpRequest request = new SnmpRequest(argParser.requireString("device"), argParser.requireString("oid"));
        return getV2c(request);
    }

    @KeywordArgs(
            names = {"device", "oid"},
            types = {String.class, String.class}
    )
    @JythonElement(docBundlePrefix = "AbstractScriptModule")
    public String getV3(PyObject[] args, String[] keywords) {
        PyArgParser argParser = PyArgParser.parseArgs(
                args,
                keywords,
                new String[]{"device", "oid"},
                new Class<?>[]{String.class, String.class},
                "getV1"
        );
        SnmpRequest request = new SnmpRequest(argParser.requireString("device"), argParser.requireString("oid"));
        return getV3(request);
    }
}
