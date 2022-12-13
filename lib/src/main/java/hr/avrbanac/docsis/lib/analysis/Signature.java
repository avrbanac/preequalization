package hr.avrbanac.docsis.lib.analysis;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Helper class to hold value from ICFR array bound together with original array index.
     */
    private static class DelayElement {
        private int index;
        private double value;

        DelayElement(
                final int index,
                final double value) {

            this.index = index;
            this.value = value;
        }

        /**
         * Index getter.
         * @return int value of the original index
         */
        public int getIndex() {
            return index;
        }

        /**
         * Value getter.
         * @return double original ICFR mag. value
         */
        public double getValue() {
            return value;
        }

        /**
         * Setter for delay element. This method forces providing a pair of arguments as new values.
         * @param index int value of the original ICFR mag. array element index
         * @param value double value of the original ICFR mag. array element
         */
        public void set(
                final int index,
                final double value) {

            this.index = index;
            this.value = value;
        }

        /**
         * Returns newly created {@link DelayElement}. Returned object is a newly created deep copy.
         * @return newly created deep copy of the current {@link DelayElement}
         */
        public DelayElement copy() {
            return new DelayElement(index, value);
        }
    }

    /**
     * This value is a threshold to define peak-to-valley transition.
     */
    private static final float MIN_PEAK_VALLEY_PERC = 0.5f;
    private static final int MR_DELAY_BOUND = -18;
    private final MicroReflectionSeverity microReflectionSeverity;
    private final double microReflection;
    private final double delay;

    /**
     * Create {@link Signature} with default CableLabs threshold recommendations.
     *
     * @param icfrMag double array of In Channel Frequency Response Magnitude
     * @param channelWidth {@link ChannelWidth} provided so that symbol rate can be fetched
     */
    public Signature(
            final double[] icfrMag,
            final ChannelWidth channelWidth) {

        this(icfrMag, channelWidth, MicroReflectionSeverityThreshold.CABLE_LABS);
    }

    /**
     * Create {@link Signature} with provided threshold level.
     *
     * @param icfrMag        double array of In Channel Frequency Response Magnitude
     * @param channelWidth {@link ChannelWidth} provided so that symbol rate can be fetched
     * @param thresholdLevel {@link MicroReflectionSeverityThreshold} provided threshold level
     */
    public Signature(final double[] icfrMag,
                     final ChannelWidth channelWidth,
                     final MicroReflectionSeverityThreshold thresholdLevel) {

        SignatureArray sigArray = new SignatureArray(icfrMag);
        microReflection = calculateMicroReflection(sigArray);
        microReflectionSeverity = calculateMicroReflectionSeverity(thresholdLevel);
        delay = calculateDelay(sigArray, channelWidth);
    }

    private double calculateMicroReflection(final SignatureArray signatureArray) {
        double temp = Math.sqrt(Math.pow(10, signatureArray.getPeakToPeak() / 10));
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

    private double calculateDelay(
            final SignatureArray signatureArray,
            final ChannelWidth channelWidth) {

        double[] transArray = signatureArray.createTranspondedArray();
        double minPeakValley = MIN_PEAK_VALLEY_PERC * signatureArray.getPeakToPeak();
        int length = transArray.length;
        List<DelayElement> peaks = new ArrayList<>(length);
        List<DelayElement> valleys = new ArrayList<>(length);

        // define prevSign as diff sign between first two elements
        double prevSign = Math.signum(transArray[1] - transArray[0]);
        double currSign;

        // first peak is at index point 0 (array is transponded beforehand)
        peaks.add(new DelayElement(0, transArray[0]));

        // iterate array and search for sign change (means either peak or valley is found)
        for (int i = 1; i < length - 1; i++) {
            currSign = Math.signum(transArray[i + 1] - transArray[i]);

            if (prevSign != currSign) {
                if (currSign < 0) {
                    peaks.add(new DelayElement(i, transArray[i]));
                } else {
                    valleys.add(new DelayElement(i, transArray[i]));
                }
                prevSign = currSign;
            }
        }

        // fake initial valley value
        DelayElement prevValley = new DelayElement(0, transArray[0]);
        int currPtr = 1;

        // remove high order MRs
        while ((currPtr < peaks.size()) && (currPtr - 1 < valleys.size())) {
            int index = valleys.get(currPtr - 1).getValue() <= prevValley.getValue()
                    ? valleys.get(currPtr - 1).getIndex()
                    : prevValley.getIndex();
            double value = Math.min(prevValley.getValue(), valleys.get(currPtr - 1).getValue());
            prevValley = new DelayElement(index, value);

            if (Math.abs(peaks.get(currPtr).getValue() - valleys.get(currPtr - 1).getValue()) < minPeakValley) {
                peaks.remove(currPtr);
                valleys.remove(currPtr - 1);
            } else {
                index = valleys.get(currPtr - 1).getValue() <= prevValley.getValue()
                        ? valleys.get(currPtr - 1).getIndex()
                        : prevValley.getIndex();
                value = Math.min(valleys.get(currPtr - 1).getValue(), prevValley.getValue());

                valleys.set(currPtr - 1, new DelayElement(index, value));
                prevValley.set(0, peaks.get(0).getValue());
                currPtr++;
            }
        }

        List<Double> delayPeaks = new ArrayList<>(length);
        double delayValue;

        // populate delay peak list by calculating horizontal distances between peaks (array index diff), but taking into account length
        for (int i = 0; i < peaks.size() - 1; i++) {
            int firstIndex = peaks.get(i).getIndex();
            int secondIndex = peaks.get(i + 1).getIndex();

            delayValue = ((secondIndex > length / 2) && (firstIndex < length / 2))
                    ? 1.0f * length / (length - Math.abs(secondIndex - firstIndex))
                    : 1.0f * length / Math.abs(secondIndex - firstIndex);
            delayPeaks.add(delayValue);
        }

        float symRate = channelWidth.getSymRate() * 1000;
        if (delayPeaks.size() > 0) {
            // for future enhancement on heuristics (to calculate delay); now return first item from list
            return delayPeaks.get(0) / symRate;
        } else {
            // since there is only one peak, delay is below 1T, indicated as T (nsec) - 1 for clarity
            return (microReflection > MR_DELAY_BOUND)
                    ? 1.0f * ((int)(1 / symRate) - 1)
                    : 0f;
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

    /**
     * Returns calculated micro-reflection delay from In Channel Frequency Response data.
     * @return double value delay calculated from ICFR
     */
    public double getDelay() {
        return delay;
    }

    @Override
    public String toString() {
        return "Signature{" +
                "microReflectionSeverity=" + microReflectionSeverity +
                ", microReflection=" + microReflection +
                ", delay=" + delay +
                '}';
    }
}
