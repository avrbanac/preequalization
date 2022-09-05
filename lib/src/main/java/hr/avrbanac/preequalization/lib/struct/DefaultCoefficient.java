package hr.avrbanac.preequalization.lib.struct;

import hr.avrbanac.preequalization.lib.PreEqException;

/**
 * <p>
 * Default implementation for the {@link Coefficient} interface. Currently, (as of v0.1.0) this is the only implementation, since as it
 * seems, docsis 3.0 and docsis 3.1 still work with same coefficient structure. Complex coefficients are stored in 2 bytes increments (total
 * of 4 bytes). Each byte increment (2 bytes in total) represent one number value (real/imaginary part alternating). Each number value is
 * thus comprised of 16 bits which we can think of in terms of 4 nibbles of 4 bits.
 * </p>
 * <hr/>
 *
 * <h3>
 * Problem with different representations
 * </h3>
 * <p>
 * Amongst different CM vendors, the representation of coefficients differ. There are variations in maximum amplitude (511, 1023, 2047), as
 * well as variations in the way the coefficients are interpreted (3 and 4 nibbles 2's complement). Since there are currently no CM devices
 * that stores maximum amplitude greater than 2047, there is a way to decode coefficients in universal way. First nibble can be ignored and
 * first bit of the remaining bits (fifth if 4 nibbles are considered) will define whether it is a positive (0) or a negative (1) number.
 * For positive number encoding, coefficient just needs to be converted from hex (binary) to dec. If the number is negative (leading bit of
 * the 3 remaining nibbles equals 1), 2's complement needs to be calculated (invert all bits values and add 1).
 * </p>
 */
public class DefaultCoefficient implements Coefficient {

    private static final int SIGN_MASK      = 0b0000_1000_0000_0000;
    private static final int NIBBLE_MASK    = 0b0000_1111_1111_1111;
    private static final int LOWER_INT_MASK = 0x00_00_ff_ff;
    private static final int BYTE_MASK      = 0x00_00_00_ff;
    private static final int REAL_SIZE      = 2;
    private static final int IMAG_SIZE      = 2;
    private static final int COMPLEX_SIZE   = REAL_SIZE + IMAG_SIZE;

    private final byte[] bytes;
    private final int real;
    private final int imag;

    public DefaultCoefficient(final byte[] bytes) {
        if (bytes.length != COMPLEX_SIZE) {
            throw PreEqException.COEFFICIENT_MISMATCH_BYTE_SIZE;
        }

        this.bytes = bytes;
        this.real = calculateValue(bytes[0], bytes[1]);
        this.imag = calculateValue(bytes[2], bytes[3]);
    }

    @Override
    public int getRealSize() {
        return REAL_SIZE;
    }

    @Override
    public int getImagSize() {
        return IMAG_SIZE;
    }

    @Override
    public int getCoefficientSize() {
        return COMPLEX_SIZE;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public int getReal() {
        return real;
    }

    @Override
    public int getImag() {
        return imag;
    }

    public int calculateValue(byte left, byte right) {
        // since int promotion is happening with upper portion it is important to delete it
        int input = (left << 8 | (right & BYTE_MASK)) & LOWER_INT_MASK;

        return ((input & SIGN_MASK) != 0)
                ? ((~input & NIBBLE_MASK) + 1) * -1
                : input & NIBBLE_MASK;
    }
}
