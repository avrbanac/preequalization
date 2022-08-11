package hr.avrbanac.preequalization.lib.struct;

import hr.avrbanac.preequalization.lib.PreEqException;

/**
 * Default implementation for the {@link Coefficient} interface. Currently, (as of v0.1.0) this is the only implementation, since as it
 * seems, docsis 3.0 and docsis 3.1 still work with same coefficient structure.
 */
public class DefaultCoefficient implements Coefficient {

    private static final int REAL_SIZE = 2;
    private static final int IMAG_SIZE = 2;
    private static final int COMPLEX_SIZE = REAL_SIZE + IMAG_SIZE;

    private final byte[] bytes;
    private final int real;
    private final int imag;

    public DefaultCoefficient(final byte[] bytes) {
        if (bytes.length != COMPLEX_SIZE) {
            throw PreEqException.COEFFICIENT_MISMATCH_BYTE_SIZE;
        }

        this.bytes = bytes;

        //int promotion will happen with upper portion (in case short will contain leading 1 - negative short number)
        real = (bytes[0] << 8 | (bytes[1] & 0xff));
        imag = (bytes[2] << 8 | (bytes[3] & 0xff));
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
}
