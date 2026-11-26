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
    private static final List<TestPreEqError> preEqErrors = new ArrayList<>();
    private static final List<TestEcho> echoes = new ArrayList<>();
    private static final List<TestEcho> longEchoes = new ArrayList<>();
    private static final List<TestEcho> subSymbolEchoes = new ArrayList<>();

    static {
        //****************************
        // ALL PRE-EQUALIZATION TESTS:
        //****************************
        preEqTests.add(new TestPreEqStructure.Builder()
                .setPreEqData("080118000004fffdfffbfffafffdfffd00070004fff800000017ffffffd6ffe807f7fff9ff8aff94fff700280011ffecfff700190006fff5fffcffff000dfffb0001000100040004fff600070007fffb00000008fffffffe00000004fffcffff00080000", 8, 24, 1)
                .setLongMetrics(4157570L, 2047L, 4190209L, 3103L, 29455L, 4190128L)
                .setDBMetrics(0.034d, 21.062d, -21.096d, -31.304d, -21.531d, -9.774d, -10.388d)
                // v0.5.0 band-limited method: echo at 1.014 T * 25.47 m/T (6.4 MHz) = 25.82 m. Consistent with the CableLabs
                // CM-GL-PNMP-V03 6.6.4 reading (tap 9 dominant, ~1 T = 25.5 m). The method itself is validated by the ground-truth
                // echo tests; this real-string value is a regression check. History: 21.96 (pre-v0.3.4 algorithm), 12.2 (v0.3.4 -
                // v0.4.1 V2 interpolation, ~2x too short for echoes next to the main tap).
                .setdTDR(25.82d)
                .setDaICFR(new double[]{
                        0.5492d, 0.9645d, 1.1571d, 1.0260d, 0.8909d, 0.5997d, 0.5409d, 0.4252d,
                        0.2880d, -0.0535d, -0.1058d, -0.2230d, -0.3263d, -0.5362d, -0.5669d, -0.6914d,
                        -0.5617d, -0.5845d, -0.7247d, -0.7140d, -0.6001d, -0.6557d, -0.6870d, -0.6488d,
                        -0.2410d, -0.2443d, -0.1699d, -0.0415d, 0.0545d, -0.0124d, 0.4373d, 0.2381d})
                // delay 198.0 ns = 1.014 T (v0.5.0 echo estimate); before v0.5.0 the coarse ICFR peak method reported 0
                .setSignature(-19.339d, "highMonitoringFrequency", 198.0d)
                .build());

        // Default string of the test app (preeq-view.fxml): MTNA 511 CM, strong echo between taps 10 and 11 (~2.6 T after the main
        // tap). REGRESSION VALUES (v0.5.0 output). Hand-checked: MTE = 494^2, MTC = 10log(262111/244036) = 0.31 dB, PPTSR from
        // taps 7 / 9 energies 740 / 689 = 0.31 dB; TDR 2.617 T * 25.47 m/T (CableLabs 6.6.4 method gives 2.72 T / 69.2 m).
        preEqTests.add(new TestPreEqStructure.Builder()
                .setPreEqData("08011800FFFF000000000001FFFEFFFD00030004FFFAFFFB00080009FFF0FFEA01EE0000FFEFFFEC0038FFD80055FFD7FFE20003001FFFE5FFFCFFFA0001FFFE0001FFF7FFFE0002FFFFFFFDFFFF000000000000FFFF0000000000000000000000000000", 8, 24, 1)
                .setLongMetrics(244036L, 511L, 261121L, 986L, 17089L, 262111L)
                .setDBMetrics(0.31d, 11.304d, -11.614d, -24.246d, -11.858d, -12.388d, 0.31d)
                .setdTDR(66.67d)
                .setDaICFR(new double[]{
                        -0.867d, -0.0421d, 1.2504d, 1.7045d, 1.151d, 0.0781d, -1.0563d, -1.8742d,
                        -2.3057d, -2.3728d, -2.044d, -1.4109d, -0.5198d, 0.627d, 1.8216d, 2.2687d,
                        1.6623d, 0.4953d, -0.7458d, -1.8261d, -2.5356d, -2.8753d, -2.7852d, -2.2839d,
                        -1.328d, -0.1983d, 1.045d, 2.0591d, 2.2617d, 1.6123d, 0.4746d, -0.544d})
                .setSignature(-10.82d, "immediateActionRequired", 511.2d)
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

        //************************************************************************************************
        // ALL INVALID PRE-EQ STRING TESTS (valid synthetic string with exactly one header byte / char broken):
        //************************************************************************************************
        // header: main tap index | taps per symbol | forward taps (n) | reverse taps (m) -> RFC 4546 DocsEqualizerData
        String body = fourNibble.substring(8);
        preEqErrors.add(new TestPreEqError("reverse taps m=1", "08011801" + body,
                PreEqException.ErrorClass.REVERSE_TAP_COUNT_NOT_SUPPORTED));
        preEqErrors.add(new TestPreEqError("main tap 0", "00011800" + body,
                PreEqException.ErrorClass.MAIN_TAP_INDEX_OUT_OF_RANGE));
        preEqErrors.add(new TestPreEqError("main tap 1 (no pre-main tap)", "01011800" + body,
                PreEqException.ErrorClass.MAIN_TAP_INDEX_OUT_OF_RANGE));
        preEqErrors.add(new TestPreEqError("main tap 24 (no post-main tap)", "18011800" + body,
                PreEqException.ErrorClass.MAIN_TAP_INDEX_OUT_OF_RANGE));
        preEqErrors.add(new TestPreEqError("main tap 25", "19011800" + body,
                PreEqException.ErrorClass.MAIN_TAP_INDEX_OUT_OF_RANGE));
        preEqErrors.add(new TestPreEqError("main tap 0x80 (negative as signed byte)", "80011800" + body,
                PreEqException.ErrorClass.MAIN_TAP_INDEX_OUT_OF_RANGE));
        preEqErrors.add(new TestPreEqError("2 taps per symbol", "08021800" + body,
                PreEqException.ErrorClass.COEFFICIENT_PER_SYMBOL_MISMATCH));
        preEqErrors.add(new TestPreEqError("16 forward taps", "08011000" + body,
                PreEqException.ErrorClass.WRONG_TAP_COUNT));
        preEqErrors.add(new TestPreEqError("one byte short", fourNibble.substring(2),
                PreEqException.ErrorClass.STRING_MISMATCH_BYTE_SIZE));
        preEqErrors.add(new TestPreEqError("non-hex character", "080118zz" + body,
                PreEqException.ErrorClass.STRING_NOT_BYTE_REPRESENTATION));

        //******************************************************************************************************************
        // ALL GROUND-TRUTH ECHO TESTS (EchoChannelGenerator: single echo, main tap 8, amplitude 2047). Levels avoid the
        // CableLabs severity thresholds (-18 / -25 dBc, CM-GL-PNMP-V03 6.7.2). Long delays (>= 7.5 T) are only used for the delay
        // test: their MR level reads ~1.3 dB low (open question, see docs/references).
        //******************************************************************************************************************
        double[] delays = {0.9d, 1d, 1.1d, 1.25d, 1.5d, 1.75d, 2d, 2.4d, 3d, 3.3d, 4.6d, 5d, 5.7d};
        double[] phases = {0d, 2.1d, -1.2d};
        for (double delay : delays) {
            for (double phase : phases) {
                echoes.add(new TestEcho(delay, -15d, phase, "immediateActionRequired"));
                echoes.add(new TestEcho(delay, -21.5d, phase, "highMonitoringFrequency"));
                echoes.add(new TestEcho(delay, -30d, phase, "noActionRequired"));
            }
        }
        double[] longDelays = {7.5d, 9.2d, 12.3d, 15d};
        for (double delay : longDelays) {
            for (double phase : phases) {
                longEchoes.add(new TestEcho(delay, -15d, phase, null));
                longEchoes.add(new TestEcho(delay, -30d, phase, null));
            }
        }
        // echoes too close to the main tap to be located from T-spaced taps (must be reported as not resolved)
        double[] subSymbolDelays = {0.3d, 0.5d};
        for (double delay : subSymbolDelays) {
            for (double phase : phases) {
                subSymbolEchoes.add(new TestEcho(delay, -15d, phase, null));
                subSymbolEchoes.add(new TestEcho(delay, -30d, phase, null));
            }
        }
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

    /**
     * Returns statically defined data for invalid pre-eq string test.
     * @return {@link List} of {@link TestPreEqError} data
     */
    public static List<TestPreEqError> getPreEqErrors() {
        return preEqErrors;
    }

    /**
     * Returns statically defined ground-truth echo test data.
     * @return {@link List} of {@link TestEcho} data
     */
    public static List<TestEcho> getEchoes() {
        return echoes;
    }

    /**
     * Returns statically defined ground-truth echo test data with long delays (used for the delay test only).
     * @return {@link List} of {@link TestEcho} data
     */
    public static List<TestEcho> getLongEchoes() {
        return longEchoes;
    }

    /**
     * Returns statically defined ground-truth echoes that are too close to the main tap to be resolved.
     * @return {@link List} of {@link TestEcho} data
     */
    public static List<TestEcho> getSubSymbolEchoes() {
        return subSymbolEchoes;
    }
}
