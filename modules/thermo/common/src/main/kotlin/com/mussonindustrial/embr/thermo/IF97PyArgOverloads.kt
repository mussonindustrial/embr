package com.mussonindustrial.embr.thermo

import com.hummeling.if97.IF97
import com.mussonindustrial.embr.common.scripting.PyArgOverload
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder

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
                { if97.compressibilityHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.compressibilityPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.compressibilityPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.compressibilityPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .build()

    val compressionFactor =
        PyArgOverloadBuilder()
            .setName("compressionFactor")
            .addOverload(
                { if97.compressionFactorPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .build()

    val density =
        PyArgOverloadBuilder()
            .setName("density")
            .addOverload(
                { if97.densityHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.densityPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.densityPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.densityPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .addOverload(
                { if97.densityPX(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "x" to Double::class
            )
            .addOverload(
                { if97.densityTX(it[0] as Double, it[1] as Double) },
                "t" to Double::class,
                "x" to Double::class
            )
            .build()

    val dielectricConstant =
        PyArgOverloadBuilder()
            .setName("dielectricConstant")
            .addOverload(
                { if97.dielectricConstantHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.dielectricConstantPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.dielectricConstantPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.dielectricConstantPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .addOverload(
                { if97.dielectricConstantRhoT(it[0] as Double, it[1] as Double) },
                "rho" to Double::class,
                "t" to Double::class
            )
            .build()

    val heatCapacityRatio =
        PyArgOverloadBuilder()
            .setName("heatCapacityRatio")
            .addOverload(
                { if97.heatCapacityRatioHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.heatCapacityRatioPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.heatCapacityRatioPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.heatCapacityRatioPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .build()

    val isentropicExponent =
        PyArgOverloadBuilder()
            .setName("isentropicExponent")
            .addOverload(
                { if97.isentropicExponentHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.isentropicExponentPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.isentropicExponentPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.isentropicExponentPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .build()

    val isobaricCubicExpansionCoefficient =
        PyArgOverloadBuilder()
            .setName("isobaricCubicExpansionCoefficient")
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientPX(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "x" to Double::class
            )
            .addOverload(
                { if97.isobaricCubicExpansionCoefficientTX(it[0] as Double, it[1] as Double) },
                "t" to Double::class,
                "x" to Double::class
            )
            .build()

    val isobaricHeatCapacity =
        PyArgOverloadBuilder()
            .setName("isobaricHeatCapacity")
            .addOverload(
                { if97.isobaricHeatCapacityHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.isobaricHeatCapacityPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.isobaricHeatCapacityPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.isobaricHeatCapacityPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .build()

    val isochoricHeatCapacity =
        PyArgOverloadBuilder()
            .setName("isochoricHeatCapacity")
            .addOverload(
                { if97.isochoricHeatCapacityHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.isochoricHeatCapacityPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.isochoricHeatCapacityPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.isochoricHeatCapacityPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .build()

    val kinematicViscosity =
        PyArgOverloadBuilder()
            .setName("kinematicViscosity")
            .addOverload(
                { if97.kinematicViscosityHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.kinematicViscosityPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.kinematicViscosityPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.kinematicViscosityPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .addOverload(
                { if97.kinematicViscosityRhoT(it[0] as Double, it[1] as Double) },
                "rho" to Double::class,
                "t" to Double::class
            )
            .build()

    val prandtl =
        PyArgOverloadBuilder()
            .setName("prandtl")
            .addOverload(
                { if97.PrandtlHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.PrandtlPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.PrandtlPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.PrandtlPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .build()

    val pressure =
        PyArgOverloadBuilder()
            .setName("pressure")
            .addOverload(
                { if97.pressureHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .build()

    val refractiveIndex =
        PyArgOverloadBuilder()
            .setName("refractiveIndex")
            .addOverload(
                { if97.refractiveIndexHSLambda(it[0] as Double, it[1] as Double, it[2] as Double) },
                "h" to Double::class,
                "s" to Double::class,
                "l" to Double::class
            )
            .addOverload(
                { if97.refractiveIndexPHLambda(it[0] as Double, it[1] as Double, it[2] as Double) },
                "p" to Double::class,
                "h" to Double::class,
                "l" to Double::class
            )
            .addOverload(
                { if97.refractiveIndexPSLambda(it[0] as Double, it[1] as Double, it[2] as Double) },
                "p" to Double::class,
                "s" to Double::class,
                "l" to Double::class
            )
            .addOverload(
                { if97.refractiveIndexPTLambda(it[0] as Double, it[1] as Double, it[2] as Double) },
                "p" to Double::class,
                "t" to Double::class,
                "l" to Double::class
            )
            .addOverload(
                {
                    if97.refractiveIndexRhoTLambda(
                        it[0] as Double,
                        it[1] as Double,
                        it[2] as Double
                    )
                },
                "rho" to Double::class,
                "t" to Double::class,
                "l" to Double::class
            )
            .build()

    val saturationPressure =
        PyArgOverloadBuilder()
            .setName("saturationPressure")
            .addOverload({ if97.saturationPressureT(it[0] as Double) }, "t" to Double::class)
            .addOverload(
                { if97.saturationPressureHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .build()

    val saturationTemperature =
        PyArgOverloadBuilder()
            .setName("saturationTemperature")
            .addOverload({ if97.saturationTemperatureP(it[0] as Double) }, "p" to Double::class)
            .addOverload(
                { if97.saturationTemperatureHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .build()

    val specificEnthalpy =
        PyArgOverloadBuilder()
            .setName("specificEnthalpy")
            .addOverload(
                { if97.specificEnthalpyPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.specificEnthalpyPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .addOverload(
                { if97.specificEnthalpyPX(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "x" to Double::class
            )
            .addOverload(
                { if97.specificEnthalpyTX(it[0] as Double, it[1] as Double) },
                "t" to Double::class,
                "x" to Double::class
            )
            .build()

    val specificEnthalpySaturatedLiquid =
        PyArgOverloadBuilder()
            .setName("specificEnthalpySaturatedLiquid")
            .addOverload(
                { if97.specificEnthalpySaturatedLiquidP(it[0] as Double) },
                "p" to Double::class
            )
            .addOverload(
                { if97.specificEnthalpySaturatedLiquidT(it[0] as Double) },
                "t" to Double::class
            )
            .build()

    val specificEnthalpySaturatedVapor =
        PyArgOverloadBuilder()
            .setName("specificEnthalpySaturatedVapor")
            .addOverload(
                { if97.specificEnthalpySaturatedVapourP(it[0] as Double) },
                "p" to Double::class
            )
            .addOverload(
                { if97.specificEnthalpySaturatedVapourT(it[0] as Double) },
                "t" to Double::class
            )
            .build()

    val specificEntropy =
        PyArgOverloadBuilder()
            .setName("specificEntropy")
            .addOverload(
                { if97.specificEntropyPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.specificEntropyPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .addOverload(
                { if97.specificEntropyPX(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "x" to Double::class
            )
            .addOverload(
                { if97.specificEntropyTX(it[0] as Double, it[1] as Double) },
                "t" to Double::class,
                "x" to Double::class
            )
            .build()

    val specificEntropySaturatedLiquid =
        PyArgOverloadBuilder()
            .setName("specificEntropySaturatedLiquid")
            .addOverload(
                { if97.specificEntropySaturatedLiquidP(it[0] as Double) },
                "p" to Double::class
            )
            .addOverload(
                { if97.specificEntropySaturatedLiquidT(it[0] as Double) },
                "t" to Double::class
            )
            .build()

    val specificEntropySaturatedVapor =
        PyArgOverloadBuilder()
            .setName("specificEntropySaturatedVapor")
            .addOverload(
                { if97.specificEntropySaturatedVapourP(it[0] as Double) },
                "p" to Double::class
            )
            .addOverload(
                { if97.specificEntropySaturatedVapourT(it[0] as Double) },
                "t" to Double::class
            )
            .build()

    val specificGibbsFreeEnergy =
        PyArgOverloadBuilder()
            .setName("specificGibbsFreeEnergy")
            .addOverload(
                { if97.specificGibbsFreeEnergyPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .build()

    val specificInternalEnergy =
        PyArgOverloadBuilder()
            .setName("specificInternalEnergy")
            .addOverload(
                { if97.specificInternalEnergyPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.specificInternalEnergyPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.specificInternalEnergyPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .addOverload(
                { if97.specificInternalEnergyPX(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "x" to Double::class
            )
            .addOverload(
                { if97.specificInternalEnergyTX(it[0] as Double, it[1] as Double) },
                "t" to Double::class,
                "x" to Double::class
            )
            .build()

    val specificInternalEnergySaturatedLiquid =
        PyArgOverloadBuilder()
            .setName("specificInternalEnergySaturatedLiquid")
            .addOverload(
                { if97.specificInternalEnergySaturatedLiquidP(it[0] as Double) },
                "p" to Double::class
            )
            .addOverload(
                { if97.specificInternalEnergySaturatedLiquidT(it[0] as Double) },
                "t" to Double::class
            )
            .build()

    val specificInternalEnergySaturatedVapor =
        PyArgOverloadBuilder()
            .setName("specificInternalEnergySaturatedVapor")
            .addOverload(
                { if97.specificInternalEnergySaturatedVapourP(it[0] as Double) },
                "p" to Double::class
            )
            .addOverload(
                { if97.specificInternalEnergySaturatedVapourT(it[0] as Double) },
                "t" to Double::class
            )
            .build()

    val specificVolume =
        PyArgOverloadBuilder()
            .setName("specificVolume")
            .addOverload(
                { if97.specificVolumeHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.specificVolumePH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.specificVolumePS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.specificVolumePT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .addOverload(
                { if97.specificVolumePX(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "x" to Double::class
            )
            .addOverload(
                { if97.specificVolumeTX(it[0] as Double, it[1] as Double) },
                "t" to Double::class,
                "x" to Double::class
            )
            .build()

    val specificVolumeSaturatedLiquid =
        PyArgOverloadBuilder()
            .setName("specificVolumeSaturatedLiquid")
            .addOverload(
                { if97.specificVolumeSaturatedLiquidP(it[0] as Double) },
                "p" to Double::class
            )
            .addOverload(
                { if97.specificVolumeSaturatedLiquidT(it[0] as Double) },
                "t" to Double::class
            )
            .build()

    val specificVolumeSaturatedVapor =
        PyArgOverloadBuilder()
            .setName("specificVolumeSaturatedVapor")
            .addOverload(
                { if97.specificVolumeSaturatedVapourP(it[0] as Double) },
                "p" to Double::class
            )
            .addOverload(
                { if97.specificVolumeSaturatedVapourT(it[0] as Double) },
                "t" to Double::class
            )
            .build()

    val speedOfSound =
        PyArgOverloadBuilder()
            .setName("speedOfSound")
            .addOverload(
                { if97.speedOfSoundHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.speedOfSoundPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.speedOfSoundPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.speedOfSoundPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .build()

    val surfaceTension =
        PyArgOverloadBuilder()
            .setName("surfaceTension")
            .addOverload({ if97.surfaceTensionP(it[0] as Double) }, "p" to Double::class)
            .addOverload({ if97.surfaceTensionT(it[0] as Double) }, "t" to Double::class)
            .build()

    val temperature =
        PyArgOverloadBuilder()
            .setName("temperature")
            .addOverload(
                { if97.temperatureHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.temperaturePH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.temperaturePS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .build()

    val thermalConductivity =
        PyArgOverloadBuilder()
            .setName("thermalConductivity")
            .addOverload(
                { if97.thermalConductivityHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.thermalConductivityPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.thermalConductivityPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.thermalConductivityPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .addOverload(
                { if97.thermalConductivityRhoT(it[0] as Double, it[1] as Double) },
                "rho" to Double::class,
                "t" to Double::class
            )
            .build()

    val thermalDiffusivity =
        PyArgOverloadBuilder()
            .setName("thermalDiffusivity")
            .addOverload(
                { if97.thermalDiffusivityHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.thermalDiffusivityPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.thermalDiffusivityPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.thermalDiffusivityPT(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "t" to Double::class
            )
            .build()

    val vaporFraction =
        PyArgOverloadBuilder()
            .setName("vaporFraction")
            .addOverload(
                { if97.vapourFractionHS(it[0] as Double, it[1] as Double) },
                "h" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.vapourFractionPH(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "h" to Double::class
            )
            .addOverload(
                { if97.vapourFractionPS(it[0] as Double, it[1] as Double) },
                "p" to Double::class,
                "s" to Double::class
            )
            .addOverload(
                { if97.vapourFractionTS(it[0] as Double, it[1] as Double) },
                "t" to Double::class,
                "s" to Double::class
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
            vaporFraction
        )
}
