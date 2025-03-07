package com.mussonindustrial.embr.thermo

import com.hummeling.if97.IF97
import com.mussonindustrial.embr.common.scripting.PyArgOverload
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import kotlin.reflect.typeOf

object IF97PyArgOverloads {

    private val if97 = IF97(IF97.UnitSystem.SI)

    fun getFunction(property: String): PyArgOverload {
        val name = property.lowercase()
        val function = functions.find { it.name.lowercase() == name }
        return function
            ?: throw IllegalArgumentException("if97 property '$property' does not exist")
    }

    val compressibility =
        PyArgOverloadBuilder()
            .setName("compressibility")
            .addOverload(
                { if97.compressibilityHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.compressibilityPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.compressibilityPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.compressibilityPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val compressionFactor =
        PyArgOverloadBuilder()
            .setName("compressionFactor")
            .addOverload(
                { if97.compressionFactorPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val density =
        PyArgOverloadBuilder()
            .setName("density")
            .addOverload(
                { if97.densityHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.densityPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.densityPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.densityPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .addOverload(
                { if97.densityPX(it["p"] as Double, it["x"] as Double) },
                "p" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .addOverload(
                { if97.densityTX(it["t"] as Double, it["x"] as Double) },
                "t" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .build()

    val dielectricConstant =
        PyArgOverloadBuilder()
            .setName("dielectricConstant")
            .addOverload(
                { if97.dielectricConstantHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.dielectricConstantPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.dielectricConstantPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.dielectricConstantPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .addOverload(
                { if97.dielectricConstantRhoT(it["rho"] as Double, it["t"] as Double) },
                "rho" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val heatCapacityRatio =
        PyArgOverloadBuilder()
            .setName("heatCapacityRatio")
            .addOverload(
                { if97.heatCapacityRatioHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.heatCapacityRatioPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.heatCapacityRatioPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.heatCapacityRatioPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val isentropicExponent =
        PyArgOverloadBuilder()
            .setName("isentropicExponent")
            .addOverload(
                { if97.isentropicExponentHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isentropicExponentPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isentropicExponentPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isentropicExponentPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val isobaricCubicExpansionCoefficient =
        PyArgOverloadBuilder()
            .setName("isobaricCubicExpansionCoefficient")
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientPX(it["p"] as Double, it["x"] as Double) },
                "p" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientTX(it["t"] as Double, it["x"] as Double) },
                "t" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .build()

    val isobaricHeatCapacity =
        PyArgOverloadBuilder()
            .setName("isobaricHeatCapacity")
            .addOverload(
                { if97.isobaricHeatCapacityHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isobaricHeatCapacityPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isobaricHeatCapacityPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isobaricHeatCapacityPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val isochoricHeatCapacity =
        PyArgOverloadBuilder()
            .setName("isochoricHeatCapacity")
            .addOverload(
                { if97.isochoricHeatCapacityHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isochoricHeatCapacityPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isochoricHeatCapacityPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.isochoricHeatCapacityPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val kinematicViscosity =
        PyArgOverloadBuilder()
            .setName("kinematicViscosity")
            .addOverload(
                { if97.kinematicViscosityHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.kinematicViscosityPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.kinematicViscosityPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.kinematicViscosityPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .addOverload(
                { if97.kinematicViscosityRhoT(it["rho"] as Double, it["t"] as Double) },
                "rho" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val prandtl =
        PyArgOverloadBuilder()
            .setName("prandtl")
            .addOverload(
                { if97.PrandtlHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.PrandtlPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.PrandtlPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.PrandtlPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val pressure =
        PyArgOverloadBuilder()
            .setName("pressure")
            .addOverload(
                { if97.pressureHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .build()

    val refractiveIndex =
        PyArgOverloadBuilder()
            .setName("refractiveIndex")
            .addOverload(
                {
                    if97.refractiveIndexHSLambda(
                        it["h"] as Double,
                        it["s"] as Double,
                        it["l"] as Double,
                    )
                },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
                "l" to typeOf<Double>(),
            )
            .addOverload(
                {
                    if97.refractiveIndexPHLambda(
                        it["p"] as Double,
                        it["h"] as Double,
                        it["l"] as Double,
                    )
                },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
                "l" to typeOf<Double>(),
            )
            .addOverload(
                {
                    if97.refractiveIndexPSLambda(
                        it["p"] as Double,
                        it["s"] as Double,
                        it["l"] as Double,
                    )
                },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
                "l" to typeOf<Double>(),
            )
            .addOverload(
                {
                    if97.refractiveIndexPTLambda(
                        it["p"] as Double,
                        it["t"] as Double,
                        it["l"] as Double,
                    )
                },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
                "l" to typeOf<Double>(),
            )
            .addOverload(
                {
                    if97.refractiveIndexRhoTLambda(
                        it["rho"] as Double,
                        it["t"] as Double,
                        it["l"] as Double,
                    )
                },
                "rho" to typeOf<Double>(),
                "t" to typeOf<Double>(),
                "l" to typeOf<Double>(),
            )
            .build()

    val saturationPressure =
        PyArgOverloadBuilder()
            .setName("saturationPressure")
            .addOverload({ if97.saturationPressureT(it["t"] as Double) }, "t" to typeOf<Double>())
            .addOverload(
                { if97.saturationPressureHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .build()

    val saturationTemperature =
        PyArgOverloadBuilder()
            .setName("saturationTemperature")
            .addOverload(
                { if97.saturationTemperatureP(it["p"] as Double) },
                "p" to typeOf<Double>(),
            )
            .addOverload(
                { if97.saturationTemperatureHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .build()

    val specificEnthalpy =
        PyArgOverloadBuilder()
            .setName("specificEnthalpy")
            .addOverload(
                { if97.specificEnthalpyPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificEnthalpyPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificEnthalpyPX(it["p"] as Double, it["x"] as Double) },
                "p" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificEnthalpyTX(it["t"] as Double, it["x"] as Double) },
                "t" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .build()

    val specificEnthalpySaturatedLiquid =
        PyArgOverloadBuilder()
            .setName("specificEnthalpySaturatedLiquid")
            .addOverload(
                { if97.specificEnthalpySaturatedLiquidP(it["p"] as Double) },
                "p" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificEnthalpySaturatedLiquidT(it["t"] as Double) },
                "t" to typeOf<Double>(),
            )
            .build()

    val specificEnthalpySaturatedVapor =
        PyArgOverloadBuilder()
            .setName("specificEnthalpySaturatedVapor")
            .addOverload(
                { if97.specificEnthalpySaturatedVapourP(it["p"] as Double) },
                "p" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificEnthalpySaturatedVapourT(it["t"] as Double) },
                "t" to typeOf<Double>(),
            )
            .build()

    val specificEntropy =
        PyArgOverloadBuilder()
            .setName("specificEntropy")
            .addOverload(
                { if97.specificEntropyPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificEntropyPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificEntropyPX(it["p"] as Double, it["x"] as Double) },
                "p" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificEntropyTX(it["t"] as Double, it["x"] as Double) },
                "t" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .build()

    val specificEntropySaturatedLiquid =
        PyArgOverloadBuilder()
            .setName("specificEntropySaturatedLiquid")
            .addOverload(
                { if97.specificEntropySaturatedLiquidP(it["p"] as Double) },
                "p" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificEntropySaturatedLiquidT(it["t"] as Double) },
                "t" to typeOf<Double>(),
            )
            .build()

    val specificEntropySaturatedVapor =
        PyArgOverloadBuilder()
            .setName("specificEntropySaturatedVapor")
            .addOverload(
                { if97.specificEntropySaturatedVapourP(it["p"] as Double) },
                "p" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificEntropySaturatedVapourT(it["t"] as Double) },
                "t" to typeOf<Double>(),
            )
            .build()

    val specificGibbsFreeEnergy =
        PyArgOverloadBuilder()
            .setName("specificGibbsFreeEnergy")
            .addOverload(
                { if97.specificGibbsFreeEnergyPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val specificInternalEnergy =
        PyArgOverloadBuilder()
            .setName("specificInternalEnergy")
            .addOverload(
                { if97.specificInternalEnergyPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificInternalEnergyPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificInternalEnergyPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificInternalEnergyPX(it["p"] as Double, it["x"] as Double) },
                "p" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificInternalEnergyTX(it["t"] as Double, it["x"] as Double) },
                "t" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .build()

    val specificInternalEnergySaturatedLiquid =
        PyArgOverloadBuilder()
            .setName("specificInternalEnergySaturatedLiquid")
            .addOverload(
                { if97.specificInternalEnergySaturatedLiquidP(it["p"] as Double) },
                "p" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificInternalEnergySaturatedLiquidT(it["t"] as Double) },
                "t" to typeOf<Double>(),
            )
            .build()

    val specificInternalEnergySaturatedVapor =
        PyArgOverloadBuilder()
            .setName("specificInternalEnergySaturatedVapor")
            .addOverload(
                { if97.specificInternalEnergySaturatedVapourP(it["p"] as Double) },
                "p" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificInternalEnergySaturatedVapourT(it["t"] as Double) },
                "t" to typeOf<Double>(),
            )
            .build()

    val specificVolume =
        PyArgOverloadBuilder()
            .setName("specificVolume")
            .addOverload(
                { if97.specificVolumeHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificVolumePH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificVolumePS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificVolumePT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificVolumePX(it["p"] as Double, it["x"] as Double) },
                "p" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificVolumeTX(it["t"] as Double, it["x"] as Double) },
                "t" to typeOf<Double>(),
                "x" to typeOf<Double>(),
            )
            .build()

    val specificVolumeSaturatedLiquid =
        PyArgOverloadBuilder()
            .setName("specificVolumeSaturatedLiquid")
            .addOverload(
                { if97.specificVolumeSaturatedLiquidP(it["p"] as Double) },
                "p" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificVolumeSaturatedLiquidT(it["t"] as Double) },
                "t" to typeOf<Double>(),
            )
            .build()

    val specificVolumeSaturatedVapor =
        PyArgOverloadBuilder()
            .setName("specificVolumeSaturatedVapor")
            .addOverload(
                { if97.specificVolumeSaturatedVapourP(it["p"] as Double) },
                "p" to typeOf<Double>(),
            )
            .addOverload(
                { if97.specificVolumeSaturatedVapourT(it["t"] as Double) },
                "t" to typeOf<Double>(),
            )
            .build()

    val speedOfSound =
        PyArgOverloadBuilder()
            .setName("speedOfSound")
            .addOverload(
                { if97.speedOfSoundHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.speedOfSoundPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.speedOfSoundPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.speedOfSoundPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val surfaceTension =
        PyArgOverloadBuilder()
            .setName("surfaceTension")
            .addOverload({ if97.surfaceTensionP(it["p"] as Double) }, "p" to typeOf<Double>())
            .addOverload({ if97.surfaceTensionT(it["t"] as Double) }, "t" to typeOf<Double>())
            .build()

    val temperature =
        PyArgOverloadBuilder()
            .setName("temperature")
            .addOverload(
                { if97.temperatureHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.temperaturePH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.temperaturePS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .build()

    val thermalConductivity =
        PyArgOverloadBuilder()
            .setName("thermalConductivity")
            .addOverload(
                { if97.thermalConductivityHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.thermalConductivityPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.thermalConductivityPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.thermalConductivityPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .addOverload(
                { if97.thermalConductivityRhoT(it["rho"] as Double, it["t"] as Double) },
                "rho" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val thermalDiffusivity =
        PyArgOverloadBuilder()
            .setName("thermalDiffusivity")
            .addOverload(
                { if97.thermalDiffusivityHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.thermalDiffusivityPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.thermalDiffusivityPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.thermalDiffusivityPT(it["p"] as Double, it["t"] as Double) },
                "p" to typeOf<Double>(),
                "t" to typeOf<Double>(),
            )
            .build()

    val vaporFraction =
        PyArgOverloadBuilder()
            .setName("vaporFraction")
            .addOverload(
                { if97.vapourFractionHS(it["h"] as Double, it["s"] as Double) },
                "h" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.vapourFractionPH(it["p"] as Double, it["h"] as Double) },
                "p" to typeOf<Double>(),
                "h" to typeOf<Double>(),
            )
            .addOverload(
                { if97.vapourFractionPS(it["p"] as Double, it["s"] as Double) },
                "p" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .addOverload(
                { if97.vapourFractionTS(it["t"] as Double, it["s"] as Double) },
                "t" to typeOf<Double>(),
                "s" to typeOf<Double>(),
            )
            .build()

    private val functions =
        listOf(
            compressibility,
            compressionFactor,
            density,
            dielectricConstant,
            heatCapacityRatio,
            isentropicExponent,
            isobaricCubicExpansionCoefficient,
            isobaricHeatCapacity,
            isochoricHeatCapacity,
            kinematicViscosity,
            prandtl,
            pressure,
            refractiveIndex,
            saturationPressure,
            saturationTemperature,
            specificEnthalpy,
            specificEnthalpySaturatedLiquid,
            specificEnthalpySaturatedVapor,
            specificEntropy,
            specificEntropySaturatedLiquid,
            specificEntropySaturatedVapor,
            specificGibbsFreeEnergy,
            specificInternalEnergy,
            specificInternalEnergySaturatedLiquid,
            specificInternalEnergySaturatedVapor,
            specificVolume,
            specificVolumeSaturatedLiquid,
            specificVolumeSaturatedVapor,
            speedOfSound,
            surfaceTension,
            temperature,
            thermalConductivity,
            thermalDiffusivity,
            vaporFraction,
        )
}
