package hr.avrbanac.docsis.lib.struct;

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

    /**
     * Returns calculated energy level for current coefficient.
     * @return long value of tap energy
     */
    long getEnergy();

    /**
     * Returns calculated energy ratio of the current coefficient (tap) to the total tap energy.
     * @param TTE long value of calculated total tap energy
     * @return double value of the energy ratio (current to TTE) in dB
     */
    double getEnergyRatio(final long TTE);

    /**
     * Returns calculated nominal energy ratio of the current coefficient (tap) to the main tap nominal energy.
     * @param lMTNE long value of calculated main tap nominal energy
     * @return double value of the nominal energy ratio (current to MTNE) in dB
     */
    double getNominalEnergyRatio(final long lMTNE);

    /**
     * Returns coefficient index. This is an actual index and not an array index. For array access, use value of one less.
     * @return int coefficient index
     */
    int getIndex();

    /**
     * Returns relative power for real part of coefficient.
     * @param lMTNA long value of calculated main tap nominal amplitude
     * @return double value of relative power for real portion of the coefficient
     */
    double getRelativePowerReal(final long lMTNA);

    /**
     * Returns relative power for imaginary part of coefficient.
     * @param lMTNA long value of calculated main tap nominal amplitude
     * @return double value of relative power for imaginary portion of the coefficient
     */
    double getRelativePowerImag(final long lMTNA);

    /**
     * Returns the lowest boundary (minimal possible value) of the tap energy ratio that the {@link #getNominalEnergyRatio(long)} method can
     * return.
     */
    double getTapEnergyRatioBoundary();
}
