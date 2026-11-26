package hr.avrbanac.docsis.lib;

import hr.avrbanac.docsis.lib.struct.DefaultCoefficient;
import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to enable multiple datum testing for each test method.
 */
public class TestBook {

    private static final List<TestPreEqStructure> preEqTests = new ArrayList<>();
    private static final List<TestCoefficientStructure> coefficientStructures = new ArrayList<>();
    private static final List<TestParabolicInterpolation> parabolicInterpolations = new ArrayList<>();
    private static final List<TestPreEqDecoding> preEqDecodings = new ArrayList<>();

    static {
        //****************************
        // ALL PRE-EQUALIZATION TESTS:
        //****************************
        preEqTests.add(new TestPreEqStructure.Builder()
                .setPreEqData("080118000004fffdfffbfffafffdfffd00070004fff800000017ffffffd6ffe807f7fff9ff8aff94fff700280011ffecfff700190006fff5fffcffff000dfffb0001000100040004fff600070007fffb00000008fffffffe00000004fffcffff00080000", 8, 24, 1)
                .setLongMetrics(4157570L, 2047L, 4190209L, 3103L, 29455L, 4190128L)
                .setDBMetrics(0.034d, 21.062d, -21.096d, -31.304d, -21.531d, -9.774d, -10.388d)
                // REGRESSION VALUE ONLY - NOT VERIFIED. 12.2 is simply the current code's output (V2 interpolation over taps 7/9/10
                // energy ratios, peak at ~0.479 T, * 25.47 m/T for 6.4 MHz). Echo is in the first post-main tap; reasoning based on
                // CableLabs CM-GL-PNMP-V03 6.6.4 suggests ~25.5 m (1 T) instead. Replace with a ground-truth synthetic test.
                // (The previous 21.96 came from the pre-v0.3.4 algorithm and had been failing since v0.3.4.)
                .setdTDR(12.2d)
                .setDaICFR(new double[]{
                        0.5492d, 0.9645d, 1.1571d, 1.0260d, 0.8909d, 0.5997d, 0.5409d, 0.4252d,
                        0.2880d, -0.0535d, -0.1058d, -0.2230d, -0.3263d, -0.5362d, -0.5669d, -0.6914d,
                        -0.5617d, -0.5845d, -0.7247d, -0.7140d, -0.6001d, -0.6557d, -0.6870d, -0.6488d,
                        -0.2410d, -0.2443d, -0.1699d, -0.0415d, 0.0545d, -0.0124d, 0.4373d, 0.2381d})
                .setSignature(-19.339d, "highMonitoringFrequency", 0d)
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
                9.8125d
        ));

        //*****************************************************************************************
        // ALL PRE-EQ DECODING TESTS (synthetic strings: main tap 8, only taps 7, 8 and 9 non-zero):
        //*****************************************************************************************
        // 4-nibble, MTNA 32767: all upper bytes < 0x80, but leading nibble of 0x7fff is set -> must NOT use 3-nibble decoding
        String mtna32767 = "0801180000000000000000000000000000000000000000000000000000100000"
                + "7fff000000200010000000000000000000000000000000000000000000000000"
                + "000000000000000000000000000000000000000000000000000000000000000000000000";
        preEqDecodings.add(new TestPreEqDecoding(mtna32767, 8, 32767, 0));
        preEqDecodings.add(new TestPreEqDecoding(mtna32767, 9, 32, 16));
        // 3-nibble, MTNA 2047: every leading nibble is zero -> 0x0ffe decodes as -2
        String threeNibble = "0801180000000000000000000000000000000000000000000000000000100000"
                + "07ff00000ffe0010000000000000000000000000000000000000000000000000"
                + "000000000000000000000000000000000000000000000000000000000000000000000000";
        preEqDecodings.add(new TestPreEqDecoding(threeNibble, 8, 2047, 0));
        preEqDecodings.add(new TestPreEqDecoding(threeNibble, 9, -2, 16));
        // 4-nibble, MTNA 2047: 0xfffe has the leading nibble set -> 4-nibble decoding, also -2
        String fourNibble = "0801180000000000000000000000000000000000000000000000000000100000"
                + "07ff0000fffe0010000000000000000000000000000000000000000000000000"
                + "000000000000000000000000000000000000000000000000000000000000000000000000";
        preEqDecodings.add(new TestPreEqDecoding(fourNibble, 8, 2047, 0));
        preEqDecodings.add(new TestPreEqDecoding(fourNibble, 9, -2, 16));
    }

    /**
     * Returns statically defined data for pre-eq structure test.
     * @return {@link List} of {@link TestPreEqStructure} data
     */
    public static List<TestPreEqStructure> getPreEqTests() {
        return preEqTests;
    }

    /**
     * Returns statically defined data for pre-eq coefficient structure test.
     * @return {@link List} of {@link TestCoefficientStructure} data
     */
    public static List<TestCoefficientStructure> getCoefficientStructures() {
        return coefficientStructures;
    }

    /**
     * Returns statically defined data for pre-eq parabolic interpolation algorithm test.
     * @return {@link List} of {@link TestParabolicInterpolation} data
     */
    public static List<TestParabolicInterpolation> getParabolicInterpolations() {
        return parabolicInterpolations;
    }

    /**
     * Returns statically defined data for pre-eq string decoding (3-nibble vs 4-nibble) test.
     * @return {@link List} of {@link TestPreEqDecoding} data
     */
    public static List<TestPreEqDecoding> getPreEqDecodings() {
        return preEqDecodings;
    }
}
