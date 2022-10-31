package hr.avrbanac.docsis.lib.analysis;

/**
 * This enumeration defines in which way input array for FFT analysis needs to be organized. Online documentation describes main tap
 * coefficient being mapped to the middle point of the FFT input. Empirical testing proved that FFT output with input organized in that
 * manner was all mixed up. That is why current version is mapping first tap coefficient with first input point, and so on... Surplus input
 * points can be zeroed. Output calculated with such linear input point preparation needs to be rotated making higher half portion of output
 * points lower and vice verses.
 */
public enum PreEqFFTInputFormat {
    /**
     * Format of the FFT input array will contain main tap value in the middle of the array with unused points zeroed.
     */
    MAIN_TAP_MIDDLE (0),

    /**
     * Format of the FFT input array will contain first pre-main tap in the zero position of the array with unused points zeroed.
     */
    FIRST_TAP_FIRST_POINT (2);

    private final int rotationFactor;

    PreEqFFTInputFormat(final int rotationFactor) {
        this.rotationFactor = rotationFactor;
    }

    /**
     * Returns the FFT output rotation factor for the current pre-eq FFT input format.
     * @return int rotation factor
     */
    public int getRotationFactor() {
        return rotationFactor;
    }
}
