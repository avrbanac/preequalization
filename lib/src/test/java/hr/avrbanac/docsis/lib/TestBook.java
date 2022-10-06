package hr.avrbanac.docsis.lib;

import hr.avrbanac.docsis.lib.struct.DefaultCoefficient;
import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.List;

public class TestBook {

    private static final List<TestPreEqStructure> preEqTests = new ArrayList<>();
    private static final List<TestCoefficientStructure> coefficientStructures = new ArrayList<>();
    private static final List<TestParabolicInterpolation> parabolicInterpolations = new ArrayList<>();

    static {
        //****************************
        // ALL PRE-EQUALIZATION TESTS:
        //****************************
        preEqTests.add(new TestPreEqStructure.Builder()
                .setPreEqString("080118000004fffdfffbfffafffdfffd00070004fff800000017ffffffd6ffe807f7fff9ff8aff94fff700280011ffecfff700190006fff5fffcffff000dfffb0001000100040004fff600070007fffb00000008fffffffe00000004fffcffff00080000")
                .setMainTapIndex(8)
                .setTapCount(24)
                .setCoefficientPerSymbol(1)
                .setlMTE(4157570L)
                .setlMTNA(2047L)
                .setlMTNE(4190209L)
                .setlPreMTE(3103L)
                .setlPostMTE(29455L)
                .setlTTE(4190128L)
                .setdMTC(0.034d)
                .setdMTR(21.062d)
                .setdNMTER(-21.096d)
                .setdPreMTTER(-31.304d)
                .setdPostMTTER(-21.531d)
                .setdPPESR(-9.774d)
                .setdPPTSR(-10.388d)
                .setdTDR(21.96d)
                .setDaICFR(new double[]{
                        0.5492d, 0.9645d, 1.1571d, 1.0260d, 0.8909d, 0.5997d, 0.5409d, 0.4252d,
                        0.2880d, -0.0535d, -0.1058d, -0.2230d, -0.3263d, -0.5362d, -0.5669d, -0.6914d,
                        -0.5617d, -0.5845d, -0.7247d, -0.7140d, -0.6001d, -0.6557d, -0.6870d, -0.6488d,
                        -0.2410d, -0.2443d, -0.1699d, -0.0415d, 0.0545d, -0.0124d, 0.4373d, 0.2381d})
                .build());

        //***********************
        // ALL COEFFICIENT TESTS:
        //***********************
        coefficientStructures.add(new TestCoefficientStructure(
                new DefaultCoefficient(new byte[]{(byte) 0xff, (byte) 0xf0, (byte) 0x00, (byte) 0x11}, 0, true),
                -16, 17
        ));
        coefficientStructures.add(new TestCoefficientStructure(
                new DefaultCoefficient(new byte[]{(byte) 0x00, (byte) 0x04, (byte) 0xff, (byte) 0xfd}, 0, false),
                4, -3
        ));

        //***********************************
        // ALL PARABOLIC INTERPOLATION TESTS:
        //***********************************
        parabolicInterpolations.add(new TestParabolicInterpolation(
                new Complex(9, 35),
                new Complex(10, 40),
                new Complex(11, 29),
                new Complex(9.8125, 40.28125)
        ));
    }

    public static List<TestPreEqStructure> getPreEqTests() {
        return preEqTests;
    }

    public static List<TestCoefficientStructure> getCoefficientStructures() {
        return coefficientStructures;
    }

    public static List<TestParabolicInterpolation> getParabolicInterpolations() {
        return parabolicInterpolations;
    }
}
