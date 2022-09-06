package hr.avrbanac.preequalization.lib;

import hr.avrbanac.preequalization.lib.struct.DefaultPreEqData;
import hr.avrbanac.preequalization.lib.struct.PreEqData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreEqDataTest {
    private static final Logger LOG = LoggerFactory.getLogger(PreEqDataTest.class);
    private static final String TAP_FORMAT = "%3d  |%4x%3x%3x%3x  |%6d%5d  |%8d  |%13.4f";
    private static final String RATIO_FORMAT =
            "RATIO: [MTC: %.3f dB, MTR: %.3f dB, NMTER: %.3f dB, preMTTER: %.3f dB, postMTTER: %.3f dB, PPESR: %.3f dB, PPTSR: %.3f dB]";
    private static final String INPUT_STRING = "08 01 18 00 " + // header
            "00 04 ff fd ff fb ff fa ff fd ff fd 00 07 00 04 ff f8 00 00 00 17 ff ff ff d6 ff e8 " + // group > pre-main taps [F1..F7]
            "07 f7 ff f9 " + // main tap [F8]
            "ff 8a ff 94 ff f7 00 28 00 11 ff ec ff f7 00 19 00 06 ff f5 ff fc ff ff 00 0d ff fb 00 01 00 01 " + // [F9..F16]
            "00 04 00 04 ff f6 00 07 00 07 ff fb 00 00 00 08 ff ff ff fe 00 00 00 04 ff fc ff ff 00 08 00 00"; // [F17..F24]
    private static final int MAIN_TAP_INDEX = 8;
    private static final int TAP_COUNT = 24;
    private static final int COEFFICIENT_PER_SYMBOL = 1;
    private static final long MTE = 4157570L;
    private static final long MTNA = 2047L;
    private static final long MTNE = 4190209L;
    private static final long PRE_MTE = 3103L;
    private static final long POST_MTE = 29455L;
    private static final long TTE = 4190128L;

    @Test
    void testDefaultPreEqData() {
        PreEqData ped = new DefaultPreEqData(INPUT_STRING);
        LOG.info("INPUT: {}", ped.getPreEqString());
        Assertions.assertEquals(INPUT_STRING.toLowerCase().replace(" ", ""), ped.getPreEqString());

        LOG.info("HEADER: [main_tap_index: {}, tap_count: {}, coefficient_per_symbol: {}]",
                ped.getMainTapIndex(), ped.getTapCount(), ped.getCoefficientPerSymbol());
        Assertions.assertEquals(MAIN_TAP_INDEX, ped.getMainTapIndex());
        Assertions.assertEquals(TAP_COUNT, ped.getTapCount());
        Assertions.assertEquals(COEFFICIENT_PER_SYMBOL, ped.getCoefficientPerSymbol());

        LOG.info("INDEX  |  xREAL xIMAG  |  real imag  |  ENERGY  |  ENERGY RATIO");
        LOG.info("-------+---------------+-------------+----------+--------------");
        ped.getCoefficients().forEach(tap-> {
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

    }
}
