package hr.avrbanac.preequalization.lib;

import hr.avrbanac.preequalization.lib.analysis.PreEqAnalysis;
import hr.avrbanac.preequalization.lib.struct.Coefficient;
import hr.avrbanac.preequalization.lib.struct.DefaultCoefficient;
import hr.avrbanac.preequalization.lib.struct.DefaultPreEqData;
import hr.avrbanac.preequalization.lib.struct.PreEqData;
import org.apache.commons.math3.util.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PreEqDataTest {
    private static final Logger LOG = LoggerFactory.getLogger(PreEqDataTest.class);
    private static final String TAP_FORMAT = "%3d  |%4x%3x%3x%3x  |%6d%5d  |%11d  |%13.4f";
    private static final String RATIO_FORMAT =
            "RATIO: [MTC: %.3f dB, MTR: %.3f dB, NMTER: %.3f dB, preMTTER: %.3f dB, postMTTER: %.3f dB, PPESR: %.3f dB, PPTSR: %.3f dB]";
    private static final byte[] INPUT_BYTES = new byte[]{
            (byte) 0xff,
            (byte) 0xf0,
            (byte) 0x00,
            (byte) 0x11
    };
    private static final int OUTPUT_REAL_PART = -16;
    private static final int OUTPUT_IMAG_PART = 17;
    private static final String INPUT_STRING = "08 01 18 00 " + // header
            "00 04 ff fd ff fb ff fa ff fd ff fd 00 07 00 04 ff f8 00 00 00 17 ff ff ff d6 ff e8 " + // group > pre-main taps [F1..F7]
            "07 f7 ff f9 " + // main tap [F8]
            "ff 8a ff 94 ff f7 00 28 00 11 ff ec ff f7 00 19 00 06 ff f5 ff fc ff ff 00 0d ff fb 00 01 00 01 " + // [F9..F16]
            "00 04 00 04 ff f6 00 07 00 07 ff fb 00 00 00 08 ff ff ff fe 00 00 00 04 ff fc ff ff 00 08 00 00"; // [F17..F24]

    // for provided pre-eq string, these are the metrics values calculated by the third party system:
    private static final int MAIN_TAP_INDEX = 8;
    private static final int TAP_COUNT = 24;
    private static final int COEFFICIENT_PER_SYMBOL = 1;
    private static final long MTE = 4157570L;
    private static final long MTNA = 2047L;
    private static final long MTNE = 4190209L;
    private static final long PRE_MTE = 3103L;
    private static final long POST_MTE = 29455L;
    private static final long TTE = 4190128L;
    private static final double MTC = 0.034d;
    private static final double MTR = 21.062d;
    private static final double NMTER = -21.096d;
    private static final double PRE_MTTER = -31.304d;
    private static final double POST_MTTER = -21.531d;
    private static final double PPESR = -9.774d;
    private static final double PPTSR = -10.388d;
    private static final double[] ICFR = {
            0.5492d,0.9645d,1.1571d,1.0260d,0.8909d,0.5997d,0.5409d,0.4252d,
            0.2880d,-0.0535d,-0.1058d, -0.2230d,-0.3263d,-0.5362d,-0.5669d,-0.6914d,
            -0.5617d,-0.5845d,-0.7247d,-0.7140d,-0.6001d,-0.6557d,-0.6870d,-0.6488d,
            -0.2410d,-0.2443d,-0.1699d,-0.0415d,0.0545d,-0.0124d,0.4373d,0.2381d};

    @Test
    void testDefaultCoefficient() {
        Coefficient coefficient = new DefaultCoefficient(INPUT_BYTES, 0, true);
        Assertions.assertEquals(OUTPUT_REAL_PART, coefficient.getReal());
        Assertions.assertEquals(OUTPUT_IMAG_PART, coefficient.getImag());
    }

    @Test
    void testDefaultPreEqData() {
        PreEqData ped = new DefaultPreEqData(INPUT_STRING);
        LOG.info("It took {} nanoseconds for data to be parsed and calculated", ped.getElapsedTime());
        LOG.info("INPUT: {}", ped.getPreEqString());
        Assertions.assertEquals(
                INPUT_STRING.toLowerCase()
                        .replace(":", "")
                        .replace(" ", ""),
                ped.getPreEqString());

        LOG.info("HEADER: [main_tap_index: {}, tap_count: {}, coefficient_per_symbol: {}]",
                ped.getMainTapIndex(), ped.getTapCount(), ped.getCoefficientPerSymbol());
        Assertions.assertEquals(MAIN_TAP_INDEX, ped.getMainTapIndex());
        Assertions.assertEquals(TAP_COUNT, ped.getTapCount());
        Assertions.assertEquals(COEFFICIENT_PER_SYMBOL, ped.getCoefficientPerSymbol());

        LOG.info("INDEX  |  xREAL xIMAG  |  real imag  |    ENERGY   |  ENERGY RATIO");
        LOG.info("-------+---------------+-------------+-------------+--------------");
        ped.getCoefficients().forEach(tap -> {
            byte[] b = tap.getBytes();
            LOG.info("{}{}",
                    tap.getIndex() == ped.getMainTapIndex() ? "m>" : "  ",
                    String.format(TAP_FORMAT,
                            tap.getIndex(),
                            b[0], b[1], b[2], b[3],
                            tap.getReal(), tap.getImag(),
                            tap.getEnergy(), tap.getEnergyRatio(ped.getMTNE())));
        });

        LOG.info("ENERGY: [MTE: {}, MTNA: {}, MTNE: {}, preMTE: {}, postMTE: {} TTE: {}]",
                ped.getMTE(), ped.getMTNA(), ped.getMTNE(), ped.getPreMTE(), ped.getPostMTE(), ped.getTTE());

        Assertions.assertEquals(MTE, ped.getMTE());
        Assertions.assertEquals(MTNA, ped.getMTNA());
        Assertions.assertEquals(MTNE, ped.getMTNE());
        Assertions.assertEquals(PRE_MTE, ped.getPreMTE());
        Assertions.assertEquals(POST_MTE, ped.getPostMTE());
        Assertions.assertEquals(TTE, ped.getTTE());

        LOG.info(String.format(RATIO_FORMAT,
                ped.getMTC(), ped.getMTR(), ped.getNMTER(), ped.getPreMTTER(), ped.getPostMTTER(), ped.getPPESR(), ped.getPPTSR()));

        Assertions.assertEquals(MTC, Precision.round(ped.getMTC(), 3));
        Assertions.assertEquals(MTR, Precision.round(ped.getMTR(), 3));
        Assertions.assertEquals(NMTER, Precision.round(ped.getNMTER(), 3));
        Assertions.assertEquals(PRE_MTTER, Precision.round(ped.getPreMTTER(), 3));
        Assertions.assertEquals(POST_MTTER, Precision.round(ped.getPostMTTER(), 3));
        Assertions.assertEquals(PPESR, Precision.round(ped.getPPESR(), 3));
        Assertions.assertEquals(PPTSR, Precision.round(ped.getPPTSR(), 3));
    }

    @Test
    void testPreEqAnalysis() {
        PreEqData ped = new DefaultPreEqData(INPUT_STRING);
        PreEqAnalysis pea = new PreEqAnalysis(ped);
        double[] icfr = pea.getInChannelFrequencyResponseMagnitude();
        LOG.info("It took {} ns for FFT preparation and calculation.", pea.getElapsedTime());
        LOG.info("ICFR: {}", icfr);
        for (int i = 0; i < icfr.length; i++) {
            Assertions.assertEquals(ICFR[i], Precision.round(icfr[i],4));
        }
    }
}
