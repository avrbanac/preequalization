package hr.avrbanac.preequalization.lib.struct;

import hr.avrbanac.preequalization.lib.PreEqException;

/**
 * <p>
 * Default implementation for the {@link Coefficient} interface. Currently, (as of v0.1.0) this is the only implementation, since as it
 * seems, DOCSIS 2.0 and DOCSIS 3.0 work with same coefficient structure. Complex coefficients are stored in 2 bytes increments (total
 * of 4 bytes). Each byte increment (2 bytes in total) represent one number value (real/imaginary part alternating). Each number value is
 * thus comprised of 16 bits which we can think of in terms of 4 nibbles of 4 bits.
 * </p>
 * <hr/>
 *
 * <h3>
 * Problem with different representations
 * </h3>
 * <strong>DEPRECATED:</strong>
 * <p>
 * Amongst different CM vendors, the representation of coefficients differ. There are variations in maximum amplitude (511, 1023, 2047), as
 * well as variations in the way the coefficients are interpreted (3 and 4 nibbles 2's complement). Since there are currently no CM devices
 * that stores maximum amplitude greater than 2047, there is a way to decode coefficients in universal way. First nibble can be ignored and
 * first bit of the remaining bits (fifth if 4 nibbles are considered) will define whether it is a positive (0) or a negative (1) number.
 * For positive number encoding, coefficient just needs to be converted from hex (binary) to dec. If the number is negative (leading bit of
 * the 3 remaining nibbles equals 1), 2's complement needs to be calculated (invert all bits values and add 1).
 * </p>
 * <strong>CURRENT VERSION: </strong>
 * <p>
 * Amongst different CM vendors, the representation of coefficients differ. There are variations in maximum amplitude (511, 1023, 2047 or
 * higher), as vell as variations in the way the coefficients are interpreted (3 and 4 nibbles 2's complement). There are currently some CM
 * devices that store max amplitude greater than 2047, so there is no universal way to decode values. That is why different approaches need
 * to be defined for 3 and 4 nibble decoding. Only if no value is stored using 4 nibbles throughout entire pre-eq string, then 3 nibble
 * encoding is used, otherwise default is 4 nibble decoding.
 * </p>
 *
 */
public class DefaultCoefficient implements Coefficient {

    // Defined masks for coefficient parsing:
    private static final int SIGN_MASK      = 0b0000_1000_0000_0000;
    private static final int NIBBLE_MASK    = 0b0000_1111_1111_1111;
    private static final int LOWER_INT_MASK = 0x00_00_ff_ff;
    private static final int BYTE_MASK      = 0x00_00_00_ff;

    /**
     * Size in bytes for the real portion of the complex coefficient.
     */
    private static final int REAL_SIZE      = 2;
    /**
     * Size in bytes for the imaginary portion of the complex coefficient.
     */
    private static final int IMAG_SIZE      = 2;
    /**
     * Total size for one complex coefficient in bytes.
     */
    private static final int COMPLEX_SIZE   = REAL_SIZE + IMAG_SIZE;
    /**
     * Original bytes array provided to CTOR.
     */
    private final byte[] bytes;
    /**
     * Index of the coefficient (energy tap), i.e. this is not an array index but real tap index instead.
     */
    private final int index;
    /**
     * Dec. value of the coefficient real part.
     */
    private final int real;
    /**
     * Dec. value of the coefficient imaginary part.
     */
    private final int imag;
    /**
     * Energy contained in coefficient which corresponds to re^2 + im^2.
     */
    private final long energy;

    public DefaultCoefficient(
            final byte[] bytes,
            final int index,
            final boolean use3NibbleEncoding) {

        if (bytes.length != COMPLEX_SIZE) {
            throw PreEqException.COEFFICIENT_MISMATCH_BYTE_SIZE;
        }

        this.bytes = bytes;
        this.index = index;
        this.real = calculateValue(bytes[0], bytes[1], use3NibbleEncoding);
        this.imag = calculateValue(bytes[2], bytes[3], use3NibbleEncoding);
        this.energy = (long) this.real * this.real + (long) this.imag * this.imag;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public int getRealSize() {
        return REAL_SIZE;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public int getImagSize() {
        return IMAG_SIZE;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public int getCoefficientSize() {
        return COMPLEX_SIZE;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public byte[] getBytes() {
        return bytes;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public int getReal() {
        return real;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public int getImag() {
        return imag;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public long getEnergy() {
        return energy;
    }

    /**
     * {@inheritDoc}
     * @param lMTNE long value of calculated main tap nominal energy
     */
    @Override
    public double getEnergyRatio(final long lMTNE) {
        double energyRatio = 10 * Math.log10(1d * energy / lMTNE);
        return Math.max(energyRatio, -100d);
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public int getIndex() {
        return index;
    }

    /**
     * Coefficients real or imaginary part can be encoded either in 3 or 4 4-bit nibble. If 3 nibble encoding is used, first 4 bits need to
     * be zeroed and fifth bit will define whether the value is positive or not. If 4 nibble encoding is used, 2 bytes are used and only
     * int promotion needs to be accounted for.
     * @param left first input byte
     * @param right second input byte
     * @return decoded value using either 3 or 4 nibble decoding
     */
    private int calculateValue(
            final byte left,
            final byte right,
            final boolean is3NibbleEncoding) {

        if (!is3NibbleEncoding) {
            return left << 8 | right & BYTE_MASK;
        }

        // since int promotion is happening with upper portion it is important to delete it
        int input = (left << 8 | (right & BYTE_MASK)) & LOWER_INT_MASK;

        return ((input & (SIGN_MASK)) != 0)
                ? ((~input & NIBBLE_MASK) + 1) * -1
                : input & NIBBLE_MASK;
    }

}
