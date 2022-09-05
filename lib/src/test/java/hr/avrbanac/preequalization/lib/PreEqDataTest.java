package hr.avrbanac.preequalization.lib;

import hr.avrbanac.preequalization.lib.struct.DefaultPreEqData;
import hr.avrbanac.preequalization.lib.struct.PreEqData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PreEqDataTest {

    private static final String INPUT_STRING = "08 01 18 00 " + // header
            "00 04 ff fd ff fb ff fa ff fd ff fd 00 07 00 04 ff f8 00 00 00 17 ff ff ff d6 ff e8 " + // group > pre-main taps
            "07 f7 ff f9 " + // main tap
            "ff 8a ff 94 ff f7 00 28 00 11 ff ec ff f7 00 19 00 06 ff f5 ff fc ff ff 00 0d ff fb 00 01 00 01 " + // [F9..F16]
            "00 04 00 04 ff f6 00 07 00 07 ff fb 00 00 00 08 ff ff ff fe 00 00 00 04 ff fc ff ff 00 08 00 00"; // [F17..F24]
    private static final int MAIN_TAP_INDEX = 8;
    private static final int TAP_COUNT = 24;
    private static final int COEFFICIENT_PER_SYMBOL = 1;

    @Test
    void testDefaultPreEqData() {
        PreEqData preEqData = new DefaultPreEqData(INPUT_STRING);
        Assertions.assertEquals(INPUT_STRING.toLowerCase().replace(" ", ""), preEqData.getPreEqString());
        Assertions.assertEquals(MAIN_TAP_INDEX, preEqData.getMainTapIndex());
        Assertions.assertEquals(TAP_COUNT, preEqData.getTapCount());
        Assertions.assertEquals(COEFFICIENT_PER_SYMBOL, preEqData.getCoefficientPerSymbol());

    }
}
