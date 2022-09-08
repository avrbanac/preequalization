package hr.avrbanac.preequalization.lib.struct;

/**
 * This interface defines pre-eq metrics to allow for better organisation of the pre-eq structure.
 */
public interface PreEqMetrics {
    /**
     * Returns adaptive equalizer main tap energy.
     * @return long value of main tap energy
     */
    long getMTE();

    /**
     * Returns nominal amplitude. This could be calculated as square root of total tap energy ({@link #getTTE()}). However, this
     * approximation can be improved by taking 2 to the power of the ratio between ln of the square root of TTE and ln of 2. The ratio needs
     * to be rounded up and subtracted by 1. Pre-eq taps exhibit different nominal or maximum amplitudes depending on CM implementations.
     * Maximum amplitude implementations are: 2047, 1023, or 511.
     * @return long value of nominal amplitude.
     */
    long getMTNA();

    /**
     * Returns main tap nominal energy. The square of the nominal amplitude ({@link #getMTNA()}) yields the nominal tap energy.
     * TTE ({@link #getTTE()}) can be used as approximation for this value.
     * @return long value of main tap nominal energy
     */
    long getMTNE();

    /**
     * Returns the summation of the energy in all equalizer taps prior to the main tap.
     * @return long value of pre-main tap energy
     */
    long getPreMTE();

    /**
     * Returns the summation of the energy in all equalizer taps after the main tap.
     * @return long value of post-main tap energy
     */
    long getPostMTE();

    /**
     * Returns the total tap energy (summation of the energy in all equalizer taps).
     * @return long value of total tap energy
     */
    long getTTE();

    /**
     * Returns the indicator of the available margin for the continued reliance on the equalization compensation process. MTC greater than
     * 2 dB may suggest that equalization compensation can no longer be successfully achieved. This translates to a less RF power level
     * delivered to the CMTS (2dB results in CMTS receiving 2dB less input power). Any level of MTC should raise an alarm (not expected
     * under normal operating conditions).
     * @return double value of main tap compression in dB
     */
    double getMTC();

    /**
     * Returns the ratio of energy in the main tap to the energy in all other taps combined. This is a useful metric to determine the
     * upstream distortion level. This metric is similar to {@link #getNMTER()} except at extremely high distortion levels. In most cases
     * MTR can be used instead of NMTER.
     * @return double value of the main tap ratio in dB
     */
    double getMTR();

    /**
     * Returns the ratio of non-main tap to total energy. Since MTE is missing here, this is a good estimation of the MER (except if signal
     * is impaired with non-linear distortion). This metric is similar to {@link #getMTR()} except at extremely high distortion levels. This
     * is a good indicator of the upstream performance based on amount of linear distortion. If a 27 dB CNR (carrier-to-noise ratio) is
     * assumed for negligible errors with 64 QAM signal, a NMTER target value of -27 dB can be assumed for comparable performance. The CNR
     * to NMTER relationship is useful in determining thresholds from the NMTER values (e.g. NMTER of -27 dB for 64 QAM could be defined as
     * immediate action required, while -30 dB threshold could be interpreted as more frequently monitoring required)
     * @return double value of the non-main tap to total energy ratio in dB
     */
    double getNMTER();

    /**
     * Returns pre-main tap energy to total energy ratio. It is a useful parameter, along with the adaptive equalizer's pre-post tap
     * symmetry ({@link #getPPESR()}), to determine the group delay level in the upstream path.
     * @return double value of the pre-main tap to total energy ratio in dB
     */
    double getPreMTTER();

    /**
     * Returns post-main tap energy to total energy ratio. It is a useful parameter to assess micro-reflection impairment contribution.
     * @return double value of the post-main tap to total energy ratio in dB
     */
    double getPostMTTER();

    /**
     * Returns the pre-post energy symmetry ratio which alongside with pre-main tap to total energy ratio ({@link #getPreMTTER()}) is a
     * useful parameter to indicate the presence of group delay in the upstream path. There is also a simplified version
     * ({@link #getPPTSR()}) using only 2 taps.
     * @return double value of the pre-post energy symmetry ratio in dB
     */
    double getPPESR();

    /**
     * Returns the simplified version of pre-post energy symmetry ratio ({@link #getPPESR()}). This calculation uses only 2 taps adjacent to
     * the main tap.
     * @return double approximated value of the pre-post energy symmetry ratio in dB
     */
    double getPPTSR();

    /**
     * Returns the number of nanoseconds it took for pre-eq string to be parsed from input format to final pre-eq structure together with
     * calculated key metrics;
     * @return long number of nanoseconds
     */
    long getElapsedTime();
}

