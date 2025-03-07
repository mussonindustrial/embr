package com.mussonindustrial.embr.thermo.scripting

import org.python.core.PyObject

interface IF97ScriptModule {

    fun compressibility(args: Array<PyObject>, keywords: Array<String>): Double

    fun compressionFactor(args: Array<PyObject>, keywords: Array<String>): Double

    fun density(args: Array<PyObject>, keywords: Array<String>): Double

    fun dielectricConstant(args: Array<PyObject>, keywords: Array<String>): Double

    fun heatCapacityRatio(args: Array<PyObject>, keywords: Array<String>): Double

    fun isentropicExponent(args: Array<PyObject>, keywords: Array<String>): Double

    fun isobaricCubicExpansionCoefficient(args: Array<PyObject>, keywords: Array<String>): Double

    fun isobaricHeatCapacity(args: Array<PyObject>, keywords: Array<String>): Double

    fun isochoricHeatCapacity(args: Array<PyObject>, keywords: Array<String>): Double

    fun kinematicViscosity(args: Array<PyObject>, keywords: Array<String>): Double

    fun prandtl(args: Array<PyObject>, keywords: Array<String>): Double

    fun pressure(args: Array<PyObject>, keywords: Array<String>): Double

    fun refractiveIndex(args: Array<PyObject>, keywords: Array<String>): Double

    fun saturationPressure(args: Array<PyObject>, keywords: Array<String>): Double

    fun saturationTemperature(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificEnthalpy(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificEnthalpySaturatedLiquid(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificEnthalpySaturatedVapor(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificEntropy(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificEntropySaturatedLiquid(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificEntropySaturatedVapor(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificGibbsFreeEnergy(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificInternalEnergy(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificInternalEnergySaturatedLiquid(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): Double

    fun specificInternalEnergySaturatedVapor(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificVolume(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificVolumeSaturatedLiquid(args: Array<PyObject>, keywords: Array<String>): Double

    fun specificVolumeSaturatedVapor(args: Array<PyObject>, keywords: Array<String>): Double

    fun speedOfSound(args: Array<PyObject>, keywords: Array<String>): Double

    fun surfaceTension(args: Array<PyObject>, keywords: Array<String>): Double

    fun temperature(args: Array<PyObject>, keywords: Array<String>): Double

    fun thermalConductivity(args: Array<PyObject>, keywords: Array<String>): Double

    fun thermalDiffusivity(args: Array<PyObject>, keywords: Array<String>): Double

    fun vaporFraction(args: Array<PyObject>, keywords: Array<String>): Double
}
