package hr.avrbanac.docsis.lib;

import hr.avrbanac.docsis.lib.analysis.ChannelWidth;
import hr.avrbanac.docsis.lib.analysis.EchoEstimate;
import hr.avrbanac.docsis.lib.analysis.PreEqAnalysis;
import hr.avrbanac.docsis.lib.analysis.Signature;
import hr.avrbanac.docsis.lib.analysis.TdrMethod;
import hr.avrbanac.docsis.lib.struct.Coefficient;
import hr.avrbanac.docsis.lib.struct.PreEqData;
import hr.avrbanac.docsis.lib.struct.DefaultPreEqData;
import hr.avrbanac.docsis.lib.util.MathUtility;
import hr.avrbanac.docsis.lib.util.ParsingUtility;
import org.apache.commons.math3.util.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Main test class for pre-eq lib. This classes method will use {@link TestBook} with wrapped data to iterate it and run tests with all
 * available data.
 */
class PreEqDataTest {
    private static final Logger LOG = LoggerFactory.getLogger(PreEqDataTest.class);
    private static final String TAP_FORMAT = "%3d  |   %s  %s  |%6d%5d  |%11d  |%13.4f";
    private static final String RATIO_FORMAT =
            "RATIO: [MTC: %.3f dB, MTR: %.3f dB, NMTER: %.3f dB, preMTTER: %.3f dB, postMTTER: %.3f dB, PPESR: %.3f dB, PPTSR: %.3f dB]";

    @Test
    void testDefaultCoefficient() {
        TestBook.getCoefficientStructures().forEach(testCoefficientStructure -> {
            Assertions.assertEquals(testCoefficientStructure.getExpectedReal(), testCoefficientStructure.getCoefficient().getReal());
            Assertions.assertEquals(testCoefficientStructure.getExpectedImag(), testCoefficientStructure.getCoefficient().getImag());
        });
    }

    /**
     * Method for testing the per-string choice of 3-nibble or 4-nibble coefficient decoding.
     */
    @Test
    void testPreEqDecoding() {
        TestBook.getPreEqDecodings().forEach(testDecoding -> {
            PreEqData ped = new DefaultPreEqData(testDecoding.getPreEqString());
            Coefficient tap = ped.getCoefficients().get(testDecoding.getTapIndex() - 1);
            Assertions.assertEquals(testDecoding.getExpectedReal(), tap.getReal());
            Assertions.assertEquals(testDecoding.getExpectedImag(), tap.getImag());
        });
    }

    /**
     * Ground-truth test: micro-reflection level and severity calculated from a synthetic single-echo pre-eq string must match the echo
     * level the string was generated from (within 0.5 dB).
     */
    @Test
    void testEchoMicroReflection() {
        TestBook.getEchoes().forEach(testEcho -> {
            PreEqData ped = new DefaultPreEqData(testEcho.toPreEqString());
            Signature signature = new PreEqAnalysis(ped).getSignature(ChannelWidth.CW_US_6_4);
            Assertions.assertEquals(testEcho.getLevelDb(), signature.getMicroReflection(), 0.5d, testEcho.toString());
            Assertions.assertEquals(testEcho.getExpectedSeverity(), signature.getMicroReflectionSeverity().getName(), testEcho.toString());
        });
    }

    /**
     * Method for testing that invalid pre-eq strings are rejected with the expected error class.
     */
    @Test
    void testPreEqErrors() {
        TestBook.getPreEqErrors().forEach(testError -> {
            PreEqException e = Assertions.assertThrows(
                    PreEqException.class,
                    () -> new DefaultPreEqData(testError.getPreEqString()),
                    testError.getDescription());
            Assertions.assertEquals(testError.getExpectedErrorClass(), e.getErrorClass(), testError.getDescription());
        });
    }

    /**
     * Method for testing pre-eq data structure. Some logging was added to enable this test to also be useful for data presentation.
     */
    @Test
    void testDefaultPreEqData() {

        TestBook.getPreEqTests().forEach(testStructure -> {
            PreEqData ped = new DefaultPreEqData(testStructure.getPreEqString());
            LOG.info("It took {} nanoseconds for data to be parsed and calculated", ped.getElapsedTime());
            LOG.info("INPUT: {}", ped.getPreEqString());
            Assertions.assertEquals(
                    testStructure.getPreEqString().toLowerCase()
                            .replace(":", "")
                            .replace(" ", ""),
                    ped.getPreEqString());

            LOG.info("HEADER: [main_tap_index: {}, tap_count: {}, coefficient_per_symbol: {}]",
                    ped.getMainTapIndex(), ped.getTapCount(), ped.getCoefficientPerSymbol());
            Assertions.assertEquals(testStructure.getMainTapIndex(), ped.getMainTapIndex());
            Assertions.assertEquals(testStructure.getTapCount(), ped.getTapCount());
            Assertions.assertEquals(testStructure.getCoefficientPerSymbol(), ped.getCoefficientPerSymbol());

            LOG.info("INDEX  |  xREAL xIMAG  |  real imag  |    ENERGY   |  ENERGY RATIO");
            LOG.info("-------+---------------+-------------+-------------+--------------");
            ped.getCoefficients().forEach(tap -> {
                byte[] b = tap.getBytes();
                LOG.info("{}{}",
                        tap.getIndex() == ped.getMainTapIndex() ? "m>" : "  ",
                        String.format(TAP_FORMAT,
                                tap.getIndex(),
                                ParsingUtility.byteArrayToHexString(b, 0, 2),
                                ParsingUtility.byteArrayToHexString(b, 2, 4),
                                tap.getReal(), tap.getImag(),
                                tap.getEnergy(), tap.getNominalEnergyRatio(ped.getMTNE())));
            });

            LOG.info("ENERGY: [MTE: {}, MTNA: {}, MTNE: {}, preMTE: {}, postMTE: {} TTE: {}]",
                    ped.getMTE(), ped.getMTNA(), ped.getMTNE(), ped.getPreMTE(), ped.getPostMTE(), ped.getTTE());

            Assertions.assertEquals(testStructure.getlMTE(), ped.getMTE());
            Assertions.assertEquals(testStructure.getlMTNA(), ped.getMTNA());
            Assertions.assertEquals(testStructure.getlMTNE(), ped.getMTNE());
            Assertions.assertEquals(testStructure.getlPreMTE(), ped.getPreMTE());
            Assertions.assertEquals(testStructure.getlPostMTE(), ped.getPostMTE());
            Assertions.assertEquals(testStructure.getlTTE(), ped.getTTE());

            LOG.info(String.format(RATIO_FORMAT,
                    ped.getMTC(), ped.getMTR(), ped.getNMTER(), ped.getPreMTTER(), ped.getPostMTTER(), ped.getPPESR(), ped.getPPTSR()));

            Assertions.assertEquals(testStructure.getdMTC(), Precision.round(ped.getMTC(), 3));
            Assertions.assertEquals(testStructure.getdMTR(), Precision.round(ped.getMTR(), 3));
            Assertions.assertEquals(testStructure.getdNMTER(), Precision.round(ped.getNMTER(), 3));
            Assertions.assertEquals(testStructure.getdPreMTTER(), Precision.round(ped.getPreMTTER(), 3));
            Assertions.assertEquals(testStructure.getdPostMTTER(), Precision.round(ped.getPostMTTER(), 3));
            Assertions.assertEquals(testStructure.getdPPESR(), Precision.round(ped.getPPESR(), 3));
            Assertions.assertEquals(testStructure.getdPPTSR(), Precision.round(ped.getPPTSR(), 3));
        });
    }

    /**
     * Method for testing pre-eq analysis. Minimal information set is provided for each test datum.
     */
    @Test
    void testPreEqAnalysis() {
        TestBook.getPreEqTests().forEach(testStructure -> {
            double[] expectedICFR = testStructure.getDaICFR();
            PreEqData ped = new DefaultPreEqData(testStructure.getPreEqString());
            PreEqAnalysis pea = new PreEqAnalysis(ped);
            double[] icfr = pea.getInChannelFrequencyResponseMagnitude();
            LOG.info("It took {} ns for FFT preparation and calculation.", pea.getElapsedTime());
            LOG.info("ICFR: {}", icfr);
            for (int i = 0; i < icfr.length; i++) {
                Assertions.assertEquals(expectedICFR[i], Precision.round(icfr[i], 4));
            }
            Signature signature = pea.getSignature(ChannelWidth.CW_US_6_4);
            LOG.info("Signature: {}", signature);
            Assertions.assertEquals(testStructure.getMicroReflection(), Precision.round(signature.getMicroReflection(), 3));
            Assertions.assertEquals(testStructure.getSeverity(), signature.getMicroReflectionSeverity().getName());
            Assertions.assertEquals(testStructure.getDelay(), Precision.round(signature.getDelay(), 1));
        });
    }

    /**
     * Method for testing the CableLabs three-point parabolic interpolation ({@link MathUtility#parabolicPeak}). Points in the test book
     * are evenly spaced; only the middle x value is used.
     */
    @Test
    void testMathParabolicInterpolation() {
        TestBook.getParabolicInterpolations().forEach(testParabolicInterpolation -> {
            double calculated = MathUtility.parabolicPeak(
                    testParabolicInterpolation.getMiddlePoint().getReal(),
                    testParabolicInterpolation.getLeftPoint().getImaginary(),
                    testParabolicInterpolation.getMiddlePoint().getImaginary(),
                    testParabolicInterpolation.getRightPoint().getImaginary());
            Assertions.assertEquals(testParabolicInterpolation.getExpectedInterpolation(), calculated);
        });
    }

    /**
     * Ground-truth test: echo delay estimated from a synthetic single-echo pre-eq string must match the delay the string was generated
     * from. Band-limited (default) method: within 0.15 T below 1.5 T and within 0.08 T from 1.5 T on; TDR distance and signature delay
     * must be consistent with it. CableLabs method: within 0.25 T when resolved, never resolved for echoes up to 1.25 T.
     */
    @Test
    void testEchoDelay() {
        List<TestEcho> allEchoes = new ArrayList<>(TestBook.getEchoes());
        allEchoes.addAll(TestBook.getLongEchoes());
        allEchoes.forEach(testEcho -> {
            PreEqAnalysis pea = new PreEqAnalysis(new DefaultPreEqData(testEcho.toPreEqString()));
            double expected = testEcho.getDelay();
            double tolerance = expected < 1.5d ? 0.15d : 0.08d;

            EchoEstimate echo = pea.getEchoEstimate(0, false);
            Assertions.assertTrue(echo.isResolved(), testEcho.toString());
            Assertions.assertEquals(expected, echo.getDelay(), tolerance, testEcho.toString());

            double metersPerT = MathUtility.getTDRSpeedFactor(ChannelWidth.CW_US_6_4.getSymRate());
            Assertions.assertEquals(expected * metersPerT, pea.getTDR(ChannelWidth.CW_US_6_4, 0, false), tolerance * metersPerT,
                    testEcho.toString());

            double nsPerT = 1000d / ChannelWidth.CW_US_6_4.getSymRate();
            Signature signature = pea.getSignature(ChannelWidth.CW_US_6_4);
            Assertions.assertTrue(signature.isDelayResolved(), testEcho.toString());
            Assertions.assertEquals(expected * nsPerT, signature.getDelay(), tolerance * nsPerT, testEcho.toString());

            EchoEstimate cableLabs = pea.getEchoEstimate(0, false, TdrMethod.CABLELABS_PARABOLIC);
            if (expected <= 1.25d) {
                Assertions.assertFalse(cableLabs.isResolved(), testEcho.toString());
            } else if (cableLabs.isResolved()) {
                Assertions.assertEquals(expected, cableLabs.getDelay(), 0.25d, testEcho.toString());
            }
        });
    }

    /**
     * Ground-truth test: echoes too close to the main tap cannot be located from T-spaced taps and must be reported as not resolved,
     * with the nominal one-symbol delay.
     */
    @Test
    void testSubSymbolEcho() {
        TestBook.getSubSymbolEchoes().forEach(testEcho -> {
            PreEqAnalysis pea = new PreEqAnalysis(new DefaultPreEqData(testEcho.toPreEqString()));
            EchoEstimate echo = pea.getEchoEstimate(0, false);
            Assertions.assertFalse(echo.isResolved(), testEcho.toString());
            Assertions.assertEquals(EchoEstimate.UNRESOLVED_DELAY, echo.getDelay(), testEcho.toString());
            Assertions.assertFalse(pea.getSignature(ChannelWidth.CW_US_6_4).isDelayResolved(), testEcho.toString());
        });
    }

    /**
     * Method for testing TDR calculation (default band-limited method) on real pre-eq strings.
     */
    @Test
    void testTDR() {
        TestBook.getPreEqTests().forEach(testStructure -> {
            PreEqData ped = new DefaultPreEqData(testStructure.getPreEqString());
            LOG.info("{}", ped.getPreEqString());

            PreEqAnalysis pea = new PreEqAnalysis(ped);
            double tdr = pea.getTDR(ChannelWidth.CW_US_6_4, 1, false);

            LOG.info("TDR: {}", tdr);
            Assertions.assertEquals(testStructure.getdTDR(), Precision.round(tdr, 2));
        });
    }
}
