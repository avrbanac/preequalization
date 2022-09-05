package hr.avrbanac.preequalization.lib.struct;

/**
 * Coefficient (interface) is a main building block for the pre-equalization data. It contains original data together with the parsed data
 * separated into real and imaginary part of the complex number it represents. Same coefficient structure is used for different QAM
 * constellations. Each energy tap in pre-equalization string is represented by separate coefficient.
 */
public interface Coefficient {

    /**
     * Returns byte array size for the real portion of the complex number representing coefficient.
     * @return int size of the real portion of the complex number (coefficient)
     */
    int getRealSize();

    /**
     * Returns byte array size for the imaginary portion of the complex number representing coefficient.
     * @return int size of the imaginary portion of the complex number (coefficient)
     */
    int getImagSize();

    /**
     * Returns byte array size for the whole complex number representing coefficient.
     * @return int size of the complex number (coefficient)
     */
    int getCoefficientSize();

    /**
     * Returns original byte array for a single coefficient. Provided byte array is the one used for creation of the current coefficient
     * structure.
     * @return original coefficient byte array
     */
    byte[] getBytes();

    /**
     * Returns real part of the complex number representing coefficient in QAM constellation.
     * @return int imaginary part of real complex number (coefficient)
     */
    int getReal();

    /**
     * Returns imaginary part of the complex number representing coefficient in QAM constellation.
     * @return int imaginary part of the complex number (coefficient)
     */
    int getImag();
}
