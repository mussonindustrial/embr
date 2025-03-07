package com.mussonindustrial.embr.thermo.scripting

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.mussonindustrial.embr.thermo.IF97PyArgOverloads
import com.mussonindustrial.embr.thermo.scriptModulePath
import org.python.core.PyObject

class IF97ScriptModuleImpl : IF97ScriptModule {

    companion object {
        const val BUNDLE_PREFIX = "IF97ScriptModule"
        const val PATH = "$scriptModulePath.if97"

        init {
            BundleUtil.get()
                .addBundle(
                    IF97ScriptModule::class.java.getSimpleName(),
                    IF97ScriptModule::class.java.getClassLoader(),
                    IF97ScriptModule::class.java.getName().replace('.', '/'),
                )
        }
    }

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class],
    )
    override fun compressibility(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.compressibility.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["p", "t"], types = [Double::class, Double::class])
    override fun compressionFactor(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.compressionFactor.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t", "x"],
        types = [Double::class, Double::class, Double::class, Double::class, Double::class],
    )
    override fun density(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.density.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "rho", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class, Double::class],
    )
    override fun dielectricConstant(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.dielectricConstant.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class],
    )
    override fun heatCapacityRatio(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.heatCapacityRatio.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class],
    )
    override fun isentropicExponent(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.isentropicExponent.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t", "x"],
        types = [Double::class, Double::class, Double::class, Double::class, Double::class],
    )
    override fun isobaricCubicExpansionCoefficient(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): Double = IF97PyArgOverloads.isobaricCubicExpansionCoefficient.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class],
    )
    override fun isobaricHeatCapacity(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.isobaricHeatCapacity.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class],
    )
    override fun isochoricHeatCapacity(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.isochoricHeatCapacity.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "rho", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class, Double::class],
    )
    override fun kinematicViscosity(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.kinematicViscosity.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class],
    )
    override fun prandtl(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.prandtl.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["h", "s"], types = [Double::class, Double::class])
    override fun pressure(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.pressure.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "lambda", "p", "rho", "s", "t"],
        types =
            [
                Double::class,
                Double::class,
                Double::class,
                Double::class,
                Double::class,
                Double::class,
            ],
    )
    override fun refractiveIndex(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.refractiveIndex.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["h", "s", "t"], types = [Double::class, Double::class, Double::class])
    override fun saturationPressure(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.saturationPressure.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["h", "p", "s"], types = [Double::class, Double::class, Double::class])
    override fun saturationTemperature(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.saturationTemperature.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["p", "s", "t", "x"],
        types = [Double::class, Double::class, Double::class, Double::class],
    )
    override fun specificEnthalpy(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.specificEnthalpy.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["p", "t"], types = [Double::class, Double::class])
    override fun specificEnthalpySaturatedLiquid(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): Double = IF97PyArgOverloads.specificEnthalpySaturatedLiquid.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["p", "t"], types = [Double::class, Double::class])
    override fun specificEnthalpySaturatedVapor(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): Double = IF97PyArgOverloads.specificEnthalpySaturatedVapor.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "t", "x"],
        types = [Double::class, Double::class, Double::class, Double::class],
    )
    override fun specificEntropy(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.specificEntropy.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["p", "t"], types = [Double::class, Double::class])
    override fun specificEntropySaturatedLiquid(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): Double = IF97PyArgOverloads.specificEntropySaturatedLiquid.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["p", "t"], types = [Double::class, Double::class])
    override fun specificEntropySaturatedVapor(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): Double = IF97PyArgOverloads.specificEntropySaturatedVapor.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["p", "t"], types = [Double::class, Double::class])
    override fun specificGibbsFreeEnergy(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.specificGibbsFreeEnergy.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t", "x"],
        types = [Double::class, Double::class, Double::class, Double::class, Double::class],
    )
    override fun specificInternalEnergy(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.specificInternalEnergy.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["p", "t"], types = [Double::class, Double::class])
    override fun specificInternalEnergySaturatedLiquid(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): Double =
        IF97PyArgOverloads.specificInternalEnergySaturatedLiquid.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["p", "t"], types = [Double::class, Double::class])
    override fun specificInternalEnergySaturatedVapor(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): Double =
        IF97PyArgOverloads.specificInternalEnergySaturatedVapor.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t", "x"],
        types = [Double::class, Double::class, Double::class, Double::class, Double::class],
    )
    override fun specificVolume(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.specificVolume.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["p", "t"], types = [Double::class, Double::class])
    override fun specificVolumeSaturatedLiquid(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): Double = IF97PyArgOverloads.specificVolumeSaturatedLiquid.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["p", "t"], types = [Double::class, Double::class])
    override fun specificVolumeSaturatedVapor(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): Double = IF97PyArgOverloads.specificVolumeSaturatedVapor.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class],
    )
    override fun speedOfSound(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.speedOfSound.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["p", "t"], types = [Double::class, Double::class])
    override fun surfaceTension(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.surfaceTension.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(names = ["h", "p", "s"], types = [Double::class, Double::class, Double::class])
    override fun temperature(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.temperature.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "rho", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class, Double::class],
    )
    override fun thermalConductivity(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.thermalConductivity.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class],
    )
    override fun thermalDiffusivity(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.thermalDiffusivity.call(args, keywords) as Double

    @ScriptFunction(docBundlePrefix = BUNDLE_PREFIX)
    @KeywordArgs(
        names = ["h", "p", "s", "t"],
        types = [Double::class, Double::class, Double::class, Double::class],
    )
    override fun vaporFraction(args: Array<PyObject>, keywords: Array<String>): Double =
        IF97PyArgOverloads.vaporFraction.call(args, keywords) as Double
}
