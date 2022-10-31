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
     * Helper class wrapper for multiple values. The same class is used to encapsulate algorithm for detecting min / max values.
     */
    private static class MinMaxValues {
        private double min;
        private int minPtr;
        private double max;
        private int maxPtr;
        private final double peakToPeak;

        private MinMaxValues(final double[] icfrMag) {
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

        public double getMin() {
            return min;
        }

        public int getMinPtr() {
            return minPtr;
        }

        public double getMax() {
            return max;
        }

        public int getMaxPtr() {
            return maxPtr;
        }

        public double getPeakToPeak() {
            return peakToPeak;
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

        MinMaxValues minMaxValues = new MinMaxValues(icfrMag);
        microReflection = calculateMicroReflection(minMaxValues.getPeakToPeak());
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
