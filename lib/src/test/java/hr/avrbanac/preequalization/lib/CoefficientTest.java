package hr.avrbanac.preequalization.lib;

import hr.avrbanac.preequalization.lib.struct.Coefficient;
import hr.avrbanac.preequalization.lib.struct.DefaultCoefficient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoefficientTest {

    private static final byte[] INPUT_BYTES = new byte[]{
            (byte)0xff,
            (byte)0xf0,
            (byte)0x00,
            (byte)0x11
    };
    private static final int OUTPUT_REAL_PART = -16;
    private static final int OUTPUT_IMAG_PART = 17;

    @Test
    void testDefaultCoefficient() {
        Coefficient coefficient = new DefaultCoefficient(INPUT_BYTES);
        Assertions.assertEquals(OUTPUT_REAL_PART, coefficient.getReal());
        Assertions.assertEquals(OUTPUT_IMAG_PART, coefficient.getImag());
    }
}
