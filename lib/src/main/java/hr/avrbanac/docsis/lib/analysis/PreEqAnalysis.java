package hr.avrbanac.docsis.lib.analysis;

import hr.avrbanac.docsis.lib.PreEqException;
import hr.avrbanac.docsis.lib.struct.Coefficient;
import hr.avrbanac.docsis.lib.struct.PreEqData;
import hr.avrbanac.docsis.lib.util.MathUtility;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.List;

/**
 * Main analysis class for parsed pre-equalization data.
 */
public class PreEqAnalysis {
    // Constants defined for FFT analysis (forward direction: from time domain to frequency domain)
    private static final TransformType TRANSFORM_TYPE = TransformType.FORWARD;
    private static final DftNormalization DFT_NORMALIZATION = DftNormalization.STANDARD;
    private static final PreEqFFTInputFormat PRE_EQ_FFT_INPUT_FORMAT = PreEqFFTInputFormat.FIRST_TAP_FIRST_POINT;
    private static final int MIN_FFT_INPUT_SIZE = 8;
    private static final int MAX_FFT_INPUT_SIZE = 128;
    // Echo delay (TDR) estimation
    private static final TdrMethod DEFAULT_TDR_METHOD = TdrMethod.BAND_LIMITED;
    /**
     * Fine grid density for the band-limited search (1/64 T is ~0.4 m at 6.4 MHz).
     */
    private static final int GRID_STEPS_PER_T = 64;
    /**
     * Band-limited search starts half a symbol after the main tap (main tap itself is removed).
     */
    private static final double BAND_LIMITED_SEARCH_START = 0.5d;
    /**
     * Echoes estimated closer than this (in T) are not resolved: simulation with synthetic single-echo strings shows that below ~0.9 T
     * the echo merges with the main tap (e.g. a 0.6 T echo is estimated at ~0.88 T).
     */
    private static final double SUB_T_RESOLUTION_LIMIT = 0.9d;
    private static final long ZERO_TAP_ENERGY_FLOOR = 1L;

    /**
     * Parsed pre-eq data with calculated key metrics.
     */
    private final PreEqData preEqData;
    /**
     * Size of the FFT input / output (number of points).
     */
    private final int fftSize;

    /**
     * Elapsed time in nanoseconds spent in calculation.
     */
    private long elapsedTime;
    /**
     * Complex array represents In Channel Frequency Response calculated only once, first time required.
     */
    private Complex[] fftICFR = null;
    /**
     * Double array represents In Channel Frequency Response Magnitude calculated only once, first time required.
     */
    private double[] fftICFRMag = null;

    public PreEqAnalysis(final PreEqData preEqData) {
        this.preEqData = preEqData;
        long start = System.nanoTime();
        this.fftSize = MathUtility.getMinFFTSize(
                preEqData.getTapCount(),
                preEqData.getMainTapIndex(),
                PRE_EQ_FFT_INPUT_FORMAT,
                MIN_FFT_INPUT_SIZE,
                MAX_FFT_INPUT_SIZE);

        if (fftSize < 0) {
            throw PreEqException.FFT_TAP_COUNT_ERROR;
        }

        elapsedTime = System.nanoTime() - start;
    }

    /**
     * Returns the number of input / output points for FFT.
     * @return int FFT size
     * @see MathUtility#getMinFFTSize(int, int, PreEqFFTInputFormat, int, int)
     */
    public int getFFTSize() {
        return fftSize;
    }

    /**
     * Returns elapsed time in nanoseconds (time needed for FFT preparation and calculation).
     * @return long elapsed time in ns
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Returns ICFR(In Channel Frequency Response) data as a result of forward FFT (Fast Fourier Transform).
     * As describe for {@link #prepareComplexFFTInput()}, after forward FFT, rotation needs to be done for the FFT output.
     * Returned array is an array of complex points. For plotting of the ICFR use {@link #getInChannelFrequencyResponseMagnitude()} method
     * instead.
     * @return {@link Complex} array in frequency domain
     */
    public Complex[] getInChannelFrequencyResponse() {
        if (fftICFR != null) return fftICFR;

        long start = System.nanoTime();
        FastFourierTransformer transformer = new FastFourierTransformer(DFT_NORMALIZATION);
        fftICFR = createRotatedFFTArray(transformer.transform(prepareComplexFFTInput(), TRANSFORM_TYPE));
        elapsedTime += System.nanoTime() - start;

        return fftICFR;
    }

    /**
     * Returns ICFR (In Channel Frequency Response) data as a result of forward FFT (Fast Fourier Transform).
     * Implicitly, this method will call {@link #getInChannelFrequencyResponse()} and convert values to magnitude values for plotting.
     * @return array of double values representing magnitudes of FFT output complex points
     */
    public double[] getInChannelFrequencyResponseMagnitude() {
        if (fftICFRMag != null) return fftICFRMag;

        Complex[] fftResult = getInChannelFrequencyResponse();
        fftICFRMag = new double[fftSize];

        long start = System.nanoTime();
        for (int i = 0; i < fftSize; i++) {
            fftICFRMag[i] = 20 * Math.log10(Math.hypot(fftResult[i].getReal(), fftResult[i].getImaginary()));
        }

        elapsedTime += System.nanoTime() - start;
        return fftICFRMag;
    }

    /**
     * Returns the distance in meters between the two reflection points of the dominant echo, using the default {@link TdrMethod}.
     * @param channelWidth {@link ChannelWidth} carrying the symbol rate
     * @param nearPostMainTapCount int count of the post-main taps considered near reflections
     * @param onlyFarReflections boolean value - true if near reflections should be left out of the calculation
     * @return double distance in meters; for an echo that is not resolved this is the nominal 1 T distance (see
     * {@link #getEchoEstimate(int, boolean)})
     */
    public double getTDR(
            final ChannelWidth channelWidth,
            final int nearPostMainTapCount,
            final boolean onlyFarReflections) {

        return getEchoEstimate(nearPostMainTapCount, onlyFarReflections).getDistance(channelWidth);
    }

    /**
     * Returns the distance in meters between the two reflection points of the dominant echo, using the given {@link TdrMethod}.
     * @param channelWidth {@link ChannelWidth} carrying the symbol rate
     * @param nearPostMainTapCount int count of the post-main taps considered near reflections
     * @param onlyFarReflections boolean value - true if near reflections should be left out of the calculation
     * @param method {@link TdrMethod} used for the echo delay estimation
     * @return double distance in meters; for an echo that is not resolved this is the nominal 1 T distance
     */
    public double getTDR(
            final ChannelWidth channelWidth,
            final int nearPostMainTapCount,
            final boolean onlyFarReflections,
            final TdrMethod method) {

        return getEchoEstimate(nearPostMainTapCount, onlyFarReflections, method).getDistance(channelWidth);
    }

    /**
     * Estimates the delay of the dominant echo using the default {@link TdrMethod#BAND_LIMITED} method.
     * @param nearPostMainTapCount int count of the post-main taps considered near reflections
     * @param onlyFarReflections boolean value - true if near reflections should be left out of the calculation
     * @return {@link EchoEstimate} with the delay in symbol periods
     * @see #getEchoEstimate(int, boolean, TdrMethod)
     */
    public EchoEstimate getEchoEstimate(
            final int nearPostMainTapCount,
            final boolean onlyFarReflections) {

        return getEchoEstimate(nearPostMainTapCount, onlyFarReflections, DEFAULT_TDR_METHOD);
    }

    /**
     * Estimates the delay of the dominant echo (micro-reflection) from the post-main taps.
     * <p>
     * A signal that hits an impedance mismatch is partly reflected back, hits a second mismatch and is reflected again in the original
     * direction. The CMTS receives the main signal plus a delayed, attenuated copy (echo). The pre-equalizer compensates for it with
     * energy in the post-main taps at (about) the echo delay. The delay is a round trip between the two reflection points, so the
     * distance between them is half of the traveled path (CableLabs CM-GL-PNMP-V03, sections 5.2.1 and 6.6.2).
     * </p>
     * <p>
     * The first {@code nearPostMainTapCount} post-main taps are considered near reflections; with {@code onlyFarReflections} set, echoes
     * within that region (up to nearPostMainTapCount + 0.5 T) are ignored.
     * </p>
     * @param nearPostMainTapCount int count of the post-main taps considered near reflections
     * @param onlyFarReflections boolean value - true if near reflections should be left out of the calculation
     * @param method {@link TdrMethod} used for the estimation
     * @return {@link EchoEstimate} with the delay in symbol periods
     * @throws PreEqException {@link PreEqException#TDR_CALCULATION_ERROR} if there is no post-main energy in the searched region
     */
    public EchoEstimate getEchoEstimate(
            final int nearPostMainTapCount,
            final boolean onlyFarReflections,
            final TdrMethod method) {

        long start = System.nanoTime();
        EchoEstimate estimate = TdrMethod.CABLELABS_PARABOLIC.equals(method)
                ? estimateEchoCableLabs(nearPostMainTapCount, onlyFarReflections)
                : estimateEchoBandLimited(nearPostMainTapCount, onlyFarReflections);
        elapsedTime += System.nanoTime() - start;

        return estimate;
    }

    /**
     * Returns {@link Signature} calculated using current pre-eq analysis.
     *
     * @param channelWidth {@link ChannelWidth} provided so that symbol rate can be fetched
     * @return {@link Signature} with wrapped calculated micro-reflection, severity and delay
     */
    public Signature getSignature(final ChannelWidth channelWidth) {
        return getSignature(channelWidth, MicroReflectionSeverityThreshold.CABLE_LABS);
    }

    /**
     * Returns {@link Signature} calculated using current pre-eq analysis. The micro-reflection level comes from the ICFR ripple, the delay
     * from {@link #getEchoEstimate(int, boolean)} over all post-main taps (default method).
     *
     * @param channelWidth {@link ChannelWidth} provided so that symbol rate can be fetched
     * @param thresholdLevel {@link MicroReflectionSeverityThreshold} provided thresholdLevel for MR
     * @return {@link Signature} with wrapped calculated micro-reflection, severity and delay
     */
    public Signature getSignature(
            final ChannelWidth channelWidth,
            final MicroReflectionSeverityThreshold thresholdLevel) {

        double[] icfrMag = getInChannelFrequencyResponseMagnitude();
        EchoEstimate echo = getEchoEstimate(0, false);
        long start = System.nanoTime();
        Signature signature = new Signature(icfrMag, echo, channelWidth, thresholdLevel);
        elapsedTime += System.nanoTime() - start;

        return signature;
    }

    /**
     * Band-limited echo delay estimation, see {@link TdrMethod#BAND_LIMITED}.
     */
    private EchoEstimate estimateEchoBandLimited(
            final int nearPostMainTapCount,
            final boolean onlyFarReflections) {

        List<Coefficient> coefficients = preEqData.getCoefficients();
        int mainPtr = preEqData.getMainTapIndex() - 1;
        Complex[] taps = new Complex[coefficients.size()];
        for (int k = 0; k < taps.length; k++) {
            // main tap removed: only the compensation of the echo remains
            taps[k] = k == mainPtr ? Complex.ZERO : new Complex(coefficients.get(k).getReal(), coefficients.get(k).getImag());
        }

        double from = onlyFarReflections ? nearPostMainTapCount + 0.5d : BAND_LIMITED_SEARCH_START;
        double to = preEqData.getTapCount() - preEqData.getMainTapIndex();
        int steps = (int) Math.round((to - from) * GRID_STEPS_PER_T);
        if (steps < 2) throw PreEqException.TDR_CALCULATION_ERROR;

        double[] mag = new double[steps + 1];
        int maxPtr = 0;
        for (int i = 0; i <= steps; i++) {
            mag[i] = MathUtility.bandLimitedValue(taps, mainPtr, from + i * (to - from) / steps).abs();
            if (mag[i] > mag[maxPtr]) maxPtr = i;
        }

        if (mag[maxPtr] == 0d) throw PreEqException.TDR_CALCULATION_ERROR;

        double step = (to - from) / steps;
        double delay = from + maxPtr * step;
        if (maxPtr > 0 && maxPtr < steps) {
            double refined = MathUtility.parabolicPeak(maxPtr, mag[maxPtr - 1], mag[maxPtr], mag[maxPtr + 1]);
            if (!Double.isNaN(refined)) delay = from + refined * step;
        }

        return new EchoEstimate(delay, delay >= SUB_T_RESOLUTION_LIMIT, TdrMethod.BAND_LIMITED);
    }

    /**
     * CableLabs three-point parabolic echo delay estimation, see {@link TdrMethod#CABLELABS_PARABOLIC}.
     */
    private EchoEstimate estimateEchoCableLabs(
            final int nearPostMainTapCount,
            final boolean onlyFarReflections) {

        List<Coefficient> coefficients = preEqData.getCoefficients();
        int mainPtr = preEqData.getMainTapIndex() - 1;
        int lastPtr = preEqData.getTapCount() - 1;
        int startPtr = mainPtr + 1 + (onlyFarReflections ? nearPostMainTapCount : 0);
        if (startPtr > lastPtr) throw PreEqException.TDR_CALCULATION_ERROR;

        int peakPtr = startPtr;
        for (int k = startPtr; k <= lastPtr; k++) {
            if (tapMagnitudeDb(coefficients, k) > tapMagnitudeDb(coefficients, peakPtr)) peakPtr = k;
        }

        if (coefficients.get(peakPtr).getEnergy() == 0L) throw PreEqException.TDR_CALCULATION_ERROR;

        double tapDelay = peakPtr - mainPtr;
        // left neighbour of the first post-main tap is the main tap: not a local peak, no interpolation possible (6.6.4)
        if (peakPtr == mainPtr + 1) return new EchoEstimate(tapDelay, false, TdrMethod.CABLELABS_PARABOLIC);
        // last tap has no right neighbour: tap resolution only
        if (peakPtr == lastPtr) return new EchoEstimate(tapDelay, true, TdrMethod.CABLELABS_PARABOLIC);

        double interpolated = MathUtility.parabolicPeak(
                tapDelay,
                tapMagnitudeDb(coefficients, peakPtr - 1),
                tapMagnitudeDb(coefficients, peakPtr),
                tapMagnitudeDb(coefficients, peakPtr + 1));

        return new EchoEstimate(Double.isNaN(interpolated) ? tapDelay : interpolated, true, TdrMethod.CABLELABS_PARABOLIC);
    }

    /**
     * Returns tap magnitude in dB (20 * log10 |tap|), with a floor for zero taps so that the value stays finite.
     */
    private static double tapMagnitudeDb(
            final List<Coefficient> coefficients,
            final int ptr) {

        return 10 * Math.log10(Math.max(coefficients.get(ptr).getEnergy(), ZERO_TAP_ENERGY_FLOOR));
    }

    /**
     * Helper method that prepares FFT input of {@link Complex} array structure. Depending on the defined {@link PreEqFFTInputFormat},
     * different methods are used. Either main tap is mapped to the middle input point, or taps are mapped sequentially in the input array
     * from the start to the last tap point and rest of the input array is zeroed.
     * @return {@link Complex} array structure for FFT input (calculated from pre-eq coefficients)
     * @see PreEqFFTInputFormat
     */
    private Complex[] prepareComplexFFTInput() {
        List<Coefficient> coefficients = preEqData.getCoefficients();
        Complex[] complexPoints = new Complex[fftSize];
        int tapCount = preEqData.getTapCount();
        long lMTNA = preEqData.getMTNA();

        if (PreEqFFTInputFormat.MAIN_TAP_MIDDLE.equals(PRE_EQ_FFT_INPUT_FORMAT)) {
            int mainTapIndex = preEqData.getMainTapIndex();
            for (int i = 0; i < fftSize; i++) {
                if ((i >= fftSize / 2 - mainTapIndex) &&(i < fftSize / 2 + (tapCount - mainTapIndex))) {
                    Coefficient coefficient = coefficients.get(i - mainTapIndex);
                    complexPoints[i] = new Complex(
                            coefficient.getRelativePowerReal(lMTNA),
                            coefficient.getRelativePowerImag(lMTNA));
                } else {
                    complexPoints[i] = Complex.ZERO;
                }
            }
        } else {
            for (int i = 0; i < fftSize; i++) {
                if (i < tapCount) {
                    Coefficient coefficient = coefficients.get(i);
                    complexPoints[i] = new Complex(coefficient.getRelativePowerReal(lMTNA), coefficient.getRelativePowerImag(lMTNA));
                } else {
                    complexPoints[i] = Complex.ZERO;
                }
            }
        }

        return complexPoints;
    }

    /**
     * Helper method returns new array (deep copy) of rotated FFT Complex array by rotation factor defined via {@link PreEqFFTInputFormat}.
     * @param rawFFTOutput {@link Complex} array as raw FFT result
     * @return new {@link Complex} array of rotated FFT output
     */
    private Complex[] createRotatedFFTArray(final Complex[] rawFFTOutput) {
        int rotationFactor = PRE_EQ_FFT_INPUT_FORMAT.getRotationFactor();
        Complex[] result = new Complex[fftSize];

        if (rotationFactor > fftSize || rotationFactor <= 0) {
            System.arraycopy(rawFFTOutput, 0, result, 0, fftSize);
        } else {
            int rotationIndex = fftSize / rotationFactor;
            System.arraycopy(rawFFTOutput, rotationIndex, result, 0, fftSize - rotationIndex);
            System.arraycopy(rawFFTOutput, 0, result, fftSize - rotationIndex, rotationIndex);
        }

        return result;
    }
}
