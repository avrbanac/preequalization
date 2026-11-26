package hr.avrbanac.docsis.lib.util;

import hr.avrbanac.docsis.lib.analysis.ChannelWidth;
import hr.avrbanac.docsis.lib.analysis.PreEqFFTInputFormat;
import org.apache.commons.math3.complex.Complex;

/**
 * Utility class for math purposes.
 */
public class MathUtility {
    private MathUtility() { }

    /**
     * Three-point parabolic interpolation of a local peak, as defined in CableLabs CM-GL-PNMP-V03, section 6.6.4. The three samples must
     * be <strong>evenly spaced by 1</strong> on the x-axis (x1 - 1, x1, x1 + 1); this is why only the middle x value is a parameter.
     * <ul>
     *     <li>y = a*x^2 + b*x + c</li>
     *     <li>a = (y0 - 2*y1 + y2) / 2  (a must be negative, otherwise no peak exists)</li>
     *     <li>xm = (y0 - y2) / (4*a)</li>
     *     <li>x_out = x1 + xm</li>
     * </ul>
     * Example from the guideline: x1 = 10, y = (35, 40, 29) gives 9.8125.
     * @param x1 double x position of the middle sample
     * @param y0 double value of the left sample (at x1 - 1)
     * @param y1 double value of the middle sample (at x1)
     * @param y2 double value of the right sample (at x1 + 1)
     * @return double interpolated x position of the peak, or {@link Double#NaN} if the samples do not form a peak (a &gt;= 0)
     */
    public static double parabolicPeak(
            final double x1,
            final double y0,
            final double y1,
            final double y2) {

        double a = (y0 - 2 * y1 + y2) / 2;
        if (a >= 0) {
            return Double.NaN;
        }

        return x1 + (y0 - y2) / (4 * a);
    }

    /**
     * Band-limited (Whittaker-Shannon, sinc) interpolation of uniformly spaced complex samples. T-spaced equalizer taps are samples of a
     * response band-limited to the symbol rate, so this reconstructs the response between the taps.
     * @param samples {@link Complex} array of samples
     * @param zeroIndex int array index of the sample located at t = 0
     * @param t double position (in sample periods, relative to the zero index) at which the value is calculated
     * @return {@link Complex} interpolated value at position t
     */
    public static Complex bandLimitedValue(
            final Complex[] samples,
            final int zeroIndex,
            final double t) {

        double re = 0d;
        double im = 0d;
        for (int k = 0; k < samples.length; k++) {
            double s = sinc(t - (k - zeroIndex));
            re += samples[k].getReal() * s;
            im += samples[k].getImaginary() * s;
        }

        return new Complex(re, im);
    }

    /**
     * Normalized sinc function: sin(pi*x) / (pi*x), with sinc(0) = 1.
     * @param x double argument
     * @return double sinc value
     */
    public static double sinc(final double x) {
        if (Math.abs(x) < 1e-12) {
            return 1d;
        }

        return Math.sin(Math.PI * x) / (Math.PI * x);
    }

    /**
     * Helper method determines how large size of input array for FFT is needed. Only 2^n sizes are valid. Depending on the
     * {@link PreEqFFTInputFormat} there are two methods of calculating this size. For case where main tap needs to be located in the middle
     * of the FFT input array, more complex algorithm is used. If input points are just sequentially positioned taps, it is only important
     * for this size to be greater than the total tap count.
     * @param tapCount int total number of taps
     * @param mainTapIndex int main tap index (not an array index)
     * @param preEqFFTInputFormat {@link PreEqFFTInputFormat} definition on how to fill up FFT input with tap data
     * @param minFFTInputSize int minimum test size for algorithm
     * @param maxFFTInputSize int maximum test size for algorithm
     * @return int min size of FFT input array or -1 if it could not be calculated between provided min and max FFT input size
     * @see PreEqFFTInputFormat
     */
    public static int getMinFFTSize(
            final int tapCount,
            final int mainTapIndex,
            final PreEqFFTInputFormat preEqFFTInputFormat,
            final int minFFTInputSize,
            final int maxFFTInputSize) {

        int halfSize;

        if (PreEqFFTInputFormat.FIRST_TAP_FIRST_POINT.equals(preEqFFTInputFormat)) {
            halfSize = tapCount / 2;
        } else {
            halfSize = (tapCount - mainTapIndex > tapCount / 2) ? tapCount - mainTapIndex : mainTapIndex;
        }

        // testing sizes only between provided min and max
        for (int i = minFFTInputSize; i <= maxFFTInputSize ; i *= 2) {
            if (i >= halfSize * 2) return i;
        }

        return -1;
    }

    /**
     * Returns the distance in meters that corresponds to an echo delay of one symbol period. The echo delay is a round trip between two
     * reflection points, so the distance between them is half of the traveled path (CableLabs CM-GL-PNMP-V03, section 6.6.2).
     * @param symbolRate float channel symbol rate in MHz
     * @return double meters per symbol period of echo delay
     */
    public static double getTDRSpeedFactor(final float symbolRate) {
        return ChannelWidth.SPEED_OF_LIGHT * ChannelWidth.VELOCITY_OF_PROPAGATION / symbolRate / 1000000 / 2;
    }
}
