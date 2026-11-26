package hr.avrbanac.docsis.lib;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * <p>
 * Generator of synthetic pre-eq strings with known ground truth. It models an upstream channel with a single echo and computes the
 * 24-tap T-spaced pre-equalizer a CM would end up with, so that analysis results (micro-reflection level, delay, distance) can be
 * checked against the physical parameters the string was built from, instead of against past outputs of the code.
 * </p>
 * <p>
 * Model:
 * </p>
 * <ul>
 *     <li>overall pulse = raised cosine with roll-off 0.25 (square-root raised cosine at both CM and CMTS, CM-SP-PHYv3.0 6.2.17)</li>
 *     <li>channel sampled once per symbol: c[n] = p(n) + r * e^(j*phi) * p(n - tau), tau in symbol periods T (may be fractional)</li>
 *     <li>equalizer: 24 T-spaced taps (CM-SP-PHYv3.0 6.2.16), least squares (slightly regularized) so that channel * equalizer is a
 *     unit impulse at the main tap</li>
 *     <li>coefficients normalized so the main tap is real with the given amplitude (e.g. 2047), rounded, 4-nibble 2's complement</li>
 * </ul>
 * <p>
 * The equalizer is the inverse of the channel, so an echo of level r at delay tau shows up as roughly -r at tau (and +r^2 at 2*tau).
 * </p>
 */
public final class EchoChannelGenerator {
    private static final double ROLL_OFF = 0.25d;
    private static final int TAP_COUNT = 24;
    private static final int CHANNEL_START = -16;
    private static final int CHANNEL_END = 48;
    private static final double REGULARIZATION = 1e-4d;

    private EchoChannelGenerator() { }

    /**
     * Creates pre-eq string for a channel with a single echo, main tap 8 and main tap amplitude 2047.
     * @param echoDelay double echo delay in symbol periods (T)
     * @param echoLevelDb double echo level relative to main signal in dB (e.g. -20)
     * @param echoPhase double echo phase in radians
     * @return {@link String} 200 hex chars pre-eq string
     */
    public static String singleEcho(
            final double echoDelay,
            final double echoLevelDb,
            final double echoPhase) {

        return singleEcho(echoDelay, echoLevelDb, echoPhase, 8, 2047);
    }

    /**
     * Creates pre-eq string for a channel with a single echo.
     * @param echoDelay double echo delay in symbol periods (T)
     * @param echoLevelDb double echo level relative to main signal in dB (e.g. -20)
     * @param echoPhase double echo phase in radians
     * @param mainTapIndex int main tap index (not an array index)
     * @param mainTapAmplitude int amplitude of the main tap after normalization (e.g. 511, 1023, 2047)
     * @return {@link String} 200 hex chars pre-eq string
     */
    public static String singleEcho(
            final double echoDelay,
            final double echoLevelDb,
            final double echoPhase,
            final int mainTapIndex,
            final int mainTapAmplitude) {

        Complex echo = ComplexUtils.polar2Complex(Math.pow(10, echoLevelDb / 20), echoPhase);
        int channelLength = CHANNEL_END - CHANNEL_START;
        Complex[] channel = new Complex[channelLength];
        for (int i = 0; i < channelLength; i++) {
            int n = CHANNEL_START + i;
            channel[i] = new Complex(raisedCosine(n)).add(echo.multiply(raisedCosine(n - echoDelay)));
        }

        Complex[] taps = solveEqualizer(channel, mainTapIndex - 1);
        Complex norm = new Complex(mainTapAmplitude).divide(taps[mainTapIndex - 1]);

        StringBuilder sb = new StringBuilder(String.format("%02x01%02x00", mainTapIndex, TAP_COUNT));
        for (Complex tap : taps) {
            Complex scaled = tap.multiply(norm);
            sb.append(String.format("%04x", Math.round(scaled.getReal()) & 0xffff));
            sb.append(String.format("%04x", Math.round(scaled.getImaginary()) & 0xffff));
        }

        return sb.toString();
    }

    /**
     * Raised cosine pulse (time in symbol periods), including the removable singularity at t = +-1/(2 * roll-off).
     */
    static double raisedCosine(final double t) {
        double sinc = Math.abs(t) < 1e-12 ? 1d : Math.sin(Math.PI * t) / (Math.PI * t);
        double den = 1 - Math.pow(2 * ROLL_OFF * t, 2);
        if (Math.abs(den) < 1e-9) {
            double x = 1 / (2 * ROLL_OFF);
            return Math.PI / 4 * Math.sin(Math.PI * x) / (Math.PI * x);
        }

        return sinc * Math.cos(Math.PI * ROLL_OFF * t) / den;
    }

    /**
     * Least squares (Tikhonov regularized) zero-forcing equalizer: minimizes |C*w - d|^2 + lambda*|w|^2, where C is the convolution
     * matrix of the channel and d a unit impulse placed so that the main tap carries the direct path. Complex system is solved as an
     * equivalent real system of twice the size.
     */
    private static Complex[] solveEqualizer(
            final Complex[] channel,
            final int mainTapPtr) {

        int rows = channel.length + TAP_COUNT - 1;
        Complex[][] conv = new Complex[rows][TAP_COUNT];
        for (int r = 0; r < rows; r++) {
            for (int k = 0; k < TAP_COUNT; k++) {
                int idx = r - k;
                conv[r][k] = (idx >= 0 && idx < channel.length) ? channel[idx] : Complex.ZERO;
            }
        }

        int targetRow = -CHANNEL_START + mainTapPtr;
        RealMatrix a = new Array2DRowRealMatrix(2 * TAP_COUNT, 2 * TAP_COUNT);
        RealVector b = new ArrayRealVector(2 * TAP_COUNT);
        for (int i = 0; i < TAP_COUNT; i++) {
            for (int j = 0; j < TAP_COUNT; j++) {
                Complex sum = Complex.ZERO;
                for (int r = 0; r < rows; r++) {
                    sum = sum.add(conv[r][i].conjugate().multiply(conv[r][j]));
                }
                if (i == j) {
                    sum = sum.add(REGULARIZATION);
                }
                a.setEntry(i, j, sum.getReal());
                a.setEntry(i, j + TAP_COUNT, -sum.getImaginary());
                a.setEntry(i + TAP_COUNT, j, sum.getImaginary());
                a.setEntry(i + TAP_COUNT, j + TAP_COUNT, sum.getReal());
            }
            Complex rhs = conv[targetRow][i].conjugate();
            b.setEntry(i, rhs.getReal());
            b.setEntry(i + TAP_COUNT, rhs.getImaginary());
        }

        RealVector x = new LUDecomposition(a).getSolver().solve(b);
        Complex[] taps = new Complex[TAP_COUNT];
        for (int k = 0; k < TAP_COUNT; k++) {
            taps[k] = new Complex(x.getEntry(k), x.getEntry(k + TAP_COUNT));
        }

        return taps;
    }
}
