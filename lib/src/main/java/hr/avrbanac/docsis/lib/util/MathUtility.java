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
     * Parabola equations with 3 provided points (x0,y0), (x1, y1), (x2, y2):
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
     * </p>
     * @param left {@link Complex} complex number so (x0, y0) pair can be easily wrapped together
     * @param middle {@link Complex} complex number so (x1, y1) pair can be easily wrapped together
     * @param right {@link Complex} complex number so (x2, y2) pair can be easily wrapped together
     * @return {@link Complex} complex number so (Xout, Yout) pair can be easily wrapped together
     */
    public static Complex calculateParabolicInterpolation(
            final Complex left,
            final Complex middle,
            final Complex right) {

        double diff = left.getImaginary() - right.getImaginary();
        double a = (left.getImaginary() - 2 * middle.getImaginary() + right.getImaginary()) / 2;
        double xm = diff / (4 * a);
        double ym = (-1d * (diff * diff)) / (16 * a);

        return new Complex(middle.getReal() + xm, middle.getImaginary() + ym);
    }

    /**
     * Helper method determines how large size of input array for FFT is needed. Only 2^n sizes are valid. Depending on the
     * {@link PreEqFFTInputFormat} there are two methods of calculating this size. For case where main tap needs to be located in the middle
     * of the FFT input array, more complex algorithm is used. If input points are just sequentially positioned taps, it is only important
     * for this size to be greater than the total tap count.
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
