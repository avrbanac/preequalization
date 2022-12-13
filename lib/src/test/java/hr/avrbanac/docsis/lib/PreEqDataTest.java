package hr.avrbanac.docsis.lib;

import hr.avrbanac.docsis.lib.analysis.ChannelWidth;
import hr.avrbanac.docsis.lib.analysis.PreEqAnalysis;
import hr.avrbanac.docsis.lib.analysis.Signature;
import hr.avrbanac.docsis.lib.struct.PreEqData;
import hr.avrbanac.docsis.lib.struct.DefaultPreEqData;
import hr.avrbanac.docsis.lib.util.MathUtility;
import hr.avrbanac.docsis.lib.util.ParsingUtility;
import org.apache.commons.math3.util.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            Assertions.assertEquals(testStructure.getDelay(), signature.getDelay());
        });
    }

    /**
     * Method for testing pre-eq parabolic interpolation algorithm. This test uses {@link MathUtility.ParabolicInterpolation#V1} only.
     */
    @Test
    void testMathParabolicInterpolation() {
        TestBook.getParabolicInterpolations().forEach(testParabolicInterpolation -> {
            double calculated = MathUtility.ParabolicInterpolation.V1.calculate(
                    testParabolicInterpolation.getLeftPoint(),
                    testParabolicInterpolation.getMiddlePoint(),
                    testParabolicInterpolation.getRightPoint());
            Assertions.assertEquals(testParabolicInterpolation.getExpectedInterpolation(), calculated);
        });
    }

    /**
     * Method for testing TDR calculation. Test uses {@link MathUtility.ParabolicInterpolation#V2} only.
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
