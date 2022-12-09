package hr.avrbanac.docsis.lib.analysis;

/**
 * Pre-equalization strings carry the information of the line degradation. From pre-eq taps adjustments, using the FFT, a frequency domain
 * can be calculated out. This represents In Channel Frequency Response. By comparing multiple ICFRs from devices that share certain network
 * path, some similarities can be found. In other words, by comparing individual device response signatures, a correlation to bigger or
 * smaller degree can be found. If a correlation is strong enough for a group of devices on the same network path, it is considered a group
 * fault. The conclusion can be drawn that line degradation occurs in a shared network segment.
 *
 * @see PreEqAnalysis#getInChannelFrequencyResponseMagnitude()
 */
public class Signature {
    /**
     * Helper class for encapsulating min / max values from given array together with corresponding array indices. Min, max and peak-to-peak
     * values are calculated within this class. Also, this class keeps reference to the originally given array.
     */
    private static class SignatureArray {
        private double min;
        private int minPtr;
        private double max;
        private int maxPtr;
        private final double peakToPeak;
        private final double[] origIcfrMag;

        SignatureArray(final double[] icfrMag) {
            origIcfrMag = icfrMag;
            min = icfrMag[0];
            max = icfrMag[0];
            minPtr = 0;
            maxPtr = 0;

            for (int i = 1; i < icfrMag.length; i++) {
                if (icfrMag[i] > max) {
                    max = icfrMag[i];
                    maxPtr = i;
                }
                if (icfrMag[i] < min) {
                    min = icfrMag[i];
                    minPtr = i;
                }
            }

            peakToPeak = Math.abs(max - min);
        }

        /**
         * Returns minimal value from the original ICFR mag. array - i.e. valley.
         * @return ICFR mag. valley
         */
        public double getMin() {
            return min;
        }

        /**
         * Returns minimal value's array position from the original ICFR mag. array - i.e. valley's array position.
         * @return ICFR mag. valley's array position
         */
        public int getMinPtr() {
            return minPtr;
        }

        /**
         * Returns max value from the original ICFR mag. array - i.e. peak.
         * @return ICFR mag. peak
         */
        public double getMax() {
            return max;
        }

        /**
         * Returns max value's array position from the original ICFR mag. array - i.e. peak's array position.
         * @return ICFR mag. peak's array position
         */
        public int getMaxPtr() {
            return maxPtr;
        }

        /**
         * Returns calculated (vertical) distance from peak to valley.
         * @return peak-to-valley distance
         */
        public double getPeakToPeak() {
            return peakToPeak;
        }

        /**
         * Returns the newly created array transponded in such a way that the max value of the original array is in the first array position
         * of the transponded array; followed by all the remaining original array elements, up until the end of the original array; followed
         * by the skipped elements from the first position of the original array, up until the max value of the original array; with one
         * additional element: another copy of the max value.
         * <p>
         *     <strong>Example:</strong>
         *     <ul>
         *          <li>Original array: [4,5,6,7,1,2,3]</li>
         *          <li>Transponded array: [7,1,2,3,4,5,6,7]</li>
         *     </ul>
         * </p>
         * @return double[] newly created and transponded
         */
        public double[] createTranspondedArray() {

            double[] transponded = new double[origIcfrMag.length + 1];

            // copy last m elements, starting from the max value element
            System.arraycopy(origIcfrMag, maxPtr, transponded, 0, origIcfrMag.length - maxPtr);

            // guard-optimize in case that in the original array max value was in the first position of the array
            if (maxPtr > 0) {
                // copy first n elements, starting from the first position of the original array
                System.arraycopy(origIcfrMag, 0, transponded, origIcfrMag.length - maxPtr, maxPtr);
            }

            // copy max element once more at the transponded array's last position
            transponded[origIcfrMag.length] = max;

            return transponded;
        }
    }

    private final MicroReflectionSeverity microReflectionSeverity;
    private final double microReflection;

    /**
     * Create {@link Signature} with default CableLabs threshold recommendations.
     *
     * @param icfrMag double array of In Channel Frequency Response Magnitude
     */
    public Signature(final double[] icfrMag) {
        this(icfrMag, MicroReflectionSeverityThreshold.CABLE_LABS);
    }

    /**
     * Create {@link Signature} with provided threshold level.
     *
     * @param icfrMag        double array of In Channel Frequency Response Magnitude
     * @param thresholdLevel {@link MicroReflectionSeverityThreshold} provided threshold level
     */
    public Signature(final double[] icfrMag,
                     final MicroReflectionSeverityThreshold thresholdLevel) {

        SignatureArray minMaxArray = new SignatureArray(icfrMag);
        microReflection = calculateMicroReflection(minMaxArray.getPeakToPeak());
        microReflectionSeverity = calculateMicroReflectionSeverity(thresholdLevel);
    }

    private double calculateMicroReflection(final double peakToPeak) {
        double temp = Math.sqrt(Math.pow(10, peakToPeak / 10));
        return 10 * Math.log10(Math.pow((temp - 1) / (temp + 1), 2));
    }

    private MicroReflectionSeverity calculateMicroReflectionSeverity(final MicroReflectionSeverityThreshold thresholdLevel) {
        if (microReflection >= MicroReflectionSeverity.BAD.getThresholdForLevel(thresholdLevel)) {
            return MicroReflectionSeverity.BAD;
        } else if (microReflection >= MicroReflectionSeverity.MARGINAL.getThresholdForLevel(thresholdLevel)) {
            return MicroReflectionSeverity.MARGINAL;
        } else {
            return MicroReflectionSeverity.GOOD;
        }
    }

    /**
     * Returns calculated micro-reflection from In Channel Frequency Response data.
     * @return double value of the calculated micro-reflection
     */
    public double getMicroReflection() {
        return microReflection;
    }

    /**
     * Returns calculated micro-reflection severity from In Channel Frequency Response data.
     * @return {@link MicroReflectionSeverity} calculated from ICFR
     */
    public MicroReflectionSeverity getMicroReflectionSeverity() {
        return microReflectionSeverity;
    }

    @Override
    public String toString() {
        return "Signature{" +
                "microReflectionSeverity=" + microReflectionSeverity +
                ", microReflection=" + microReflection +
                '}';
    }
}
