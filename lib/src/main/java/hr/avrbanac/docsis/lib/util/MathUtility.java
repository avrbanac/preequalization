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
     * Parabolic interpolation enumeration with 2 different implementations.
     */
    public enum ParabolicInterpolation {
        /**
         * The algorithm was found in the official CableLabs documentation. It was unclear whether it should be used with tap energy ratio
         * values or nominal energy ratio values. Since nominal energy ratio uses nominal energy instead the calculated real value, it seems
         * that energy ratio is a better choice (more precise one).
         * <ul>
         *     <li>y = a*x^2 + b*x + c</li>
         *     <li>a = (y0 - 2*y1 + y2) / 2  (note: a should be negative or no peak exists)</li>
         *     <li>b = (y2 - y0) / 2</li>
         *     <li>c = y1</li>
         *     <li>xm = (y0 - y2) / (4*a)</li>
         *     <li>ym = -(y0 - y2)^2 / (16*a)</li>
         *     <li>Xout = x1 + xm</li>
         *     <li>Yout = y1 + ym</li>
         * </ul>
         */
        V1 {
            /**
             * @param left {@link Complex} complex number so (x0, y0) pair can be easily wrapped together
             * @param middle {@link Complex} complex number so (x1, y1) pair can be easily wrapped together
             * @param right {@link Complex} complex number so (x2, y2) pair can be easily wrapped together
             * @return double value of the X point (interpolated) - real part of the complex number
             */
            @Override
            public double doCalculate(Complex left, Complex middle, Complex right) {
                double diff = left.getImaginary() - right.getImaginary(); //v2
                double a = (left.getImaginary() - 2 * middle.getImaginary() + right.getImaginary()) / 2;
                double xm = diff / (4 * a);
                // this is needed only for y value: double ym = (-1d * (diff * diff)) / (16 * a)

                return middle.getReal() + xm;
            }
        },
        /**
         * The algorithm was extrapolated from existing third party pre-eq software partially written by CableLabs. It seems that in the
         * original code, energy ratio (calculated with TTE) was used instead of nominal values.
         */
        V2 {
            /**
             * @param left {@link Complex} complex number so (x0, y0) pair can be easily wrapped together
             * @param middle {@link Complex} complex number so (x1, y1) pair can be easily wrapped together
             * @param right {@link Complex} complex number so (x2, y2) pair can be easily wrapped together
             * @return double value of the X point (interpolated) - real part of the complex number
             */
            @Override
            public double doCalculate(Complex left, Complex middle, Complex right) {
                double x1 = left.getReal();
                double y1 = left.getImaginary();
                double x2 = middle.getReal();
                double y2 = middle.getImaginary();
                double x3 = right.getReal();
                double y3 = right.getImaginary();

                double u1 = x1 * x1 - x2 * x2;
                double u2 = x1 * x1 - x3 * x3;

                double v1 = x1 - x2;
                double v2 = x1 - x3;

                double w1 = y1 - y2;
                double w2 = y1 - y3;

                double k1 = u1 / v1 - u2 / v2;
                double m1 = w1 / v1 - w2 / v2;

                return -0.5d * (w2 * k1 / v2 / m1 - u2 / v2);
            }
        };

        abstract double doCalculate(final Complex left, final Complex middle, final Complex right);

        /**
         * Calculates Parabolic interpolation with one of the implementations.
         * @param left {@link Complex} complex number so (x0, y0) pair can be easily wrapped together
         * @param middle middle {@link Complex} complex number so (x1, y1) pair can be easily wrapped together
         * @param right right {@link Complex} complex number so (x2, y2) pair can be easily wrapped together
         * @return double value of the X point (interpolated) - real part of the complex number
         * @see ParabolicInterpolation#V1
         * @see ParabolicInterpolation#V2
         */
        public double calculate(
                final Complex left,
                final Complex middle,
                final Complex right) {

            return this.doCalculate(left, middle, right);
        }
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
     * Returns the calculated factor needed for TDR calculations. This factor includes speed of wave propagation for the given channel
     * symbol rate.
     * @param symbolRate float channel symbol rate
     * @return double factor value
     */
    public static double getTDRSpeedFactor(final float symbolRate) {
        return ChannelWidth.SPEED_OF_LIGHT * ChannelWidth.VELOCITY_OF_PROPAGATION / symbolRate / 1000000 / 2;
    }
}
