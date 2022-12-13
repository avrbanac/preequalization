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
    private static final MathUtility.ParabolicInterpolation PARABOLIC_INTERPOLATION = MathUtility.ParabolicInterpolation.V2;

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
     * Default overloaded method which initiates calculation with default parabolic interpolation defined for this class.
     * @param channelWidth {@link ChannelWidth} carrying the information about width of the channel and symbol rate
     * @param nearPostMainTapCount int count of the post-main energy taps considered near reflections
     * @param onlyFarReflections boolean value - true if near reflections should be left out of the calculation
     * @return double TDR value representing distance between the strongest MR point and first strong downstream reflection point
     * @see #getTDR(ChannelWidth, int, boolean, MathUtility.ParabolicInterpolation) 
     */
    public double getTDR(
            final ChannelWidth channelWidth,
            final int nearPostMainTapCount,
            final boolean onlyFarReflections) {

        return getTDR(channelWidth, nearPostMainTapCount, onlyFarReflections, PARABOLIC_INTERPOLATION);
    }

    /**
     * Time domain reflectometry calculation method.
     * <p>
     * When a propagating wave hits the impedance mismatch point, a micro-reflection (MR) occurs. This MR is reflected towards CM (wave
     * origin) until it hits another reflection point where it is reflected again towards target (US direction). As a result,
     * a superposition of the original wave and attenuated reflected one is perceived by the CMTS somewhere in the upstream. However, CMTS
     * will register reflected wave as a delayed wave in correlation to the original one. This time dilatation can be used together with the
     * known channel frequency and speed of the wave propagation to calculate the distance between two reflection points.
     * </p>
     * <p>
     * When addressing these MRs, there is a distinction: near and far reflections - assuming that the first n energy taps after the main
     * one are considered near to the CM device if the level of the MR is above certain threshold.
     * </p>
     * <p>
     * Calculation will be carried out with the highest post-main tap using hyperbolic interpolation. For this interpolation process, 3
     * points will be used: the highest MR tap, closest left and closest right tap value. Exception to this rule is calculating
     * interpolation for first post-main tap since the previous one cannot be used as left point (main tap is maxed out). Instead,
     * last pre-main tap will be used with according x-axis value (2 places offset instead of just one). Similarly, last post-main tap has
     * no right point, so imaginary right point with the same value as that to the right of the last one will be used, effectively fixing
     * calculation to the middle of the last tap. This will lower the precision of calculating interpolation for the last energy tap.
     * (This is the last tap and the time dilatation is the longest, so lowering the precision is not so big of a problem.)
     * </p>
     *
     * @param channelWidth {@link ChannelWidth} carrying the information about width of the channel and symbol rate
     * @param nearPostMainTapCount int count of the post-main energy taps considered near reflections
     * @param onlyFarReflections boolean value - true if near reflections should be left out of the calculation
     * @param parabolicInterpolation {@link hr.avrbanac.docsis.lib.util.MathUtility.ParabolicInterpolation} used for max point determination
     * @return double TDR value representing distance between the strongest MR point and first strong downstream reflection point
     */
    public double getTDR(
            final ChannelWidth channelWidth,
            final int nearPostMainTapCount,
            final boolean onlyFarReflections,
            final MathUtility.ParabolicInterpolation parabolicInterpolation) {

        long start = System.nanoTime();
        int mainTapIndex = preEqData.getMainTapIndex();
        int tapCount = preEqData.getTapCount();
        double maxReflection = preEqData.getTapEnergyRatioBoundary();
        List<Coefficient> coefficients = preEqData.getCoefficients();
        long tte = preEqData.getTTE();
        int ptr = onlyFarReflections ? mainTapIndex + nearPostMainTapCount : mainTapIndex;
        int maxTapPtr = 0;

        while (ptr < tapCount) {
            double currTapEnergyRatio = coefficients.get(ptr).getEnergyRatio(tte);
            if (currTapEnergyRatio > maxReflection) {
                maxReflection = currTapEnergyRatio;
                maxTapPtr = ptr;
            }
            ptr++;
        }

        if (maxTapPtr <= mainTapIndex - 1) throw PreEqException.TDR_CALCULATION_ERROR;

        Complex left = getLeftInterpolationPoint(coefficients, maxTapPtr, mainTapIndex, tte);
        Complex middle = new Complex(maxTapPtr - mainTapIndex + 1d, coefficients.get(maxTapPtr).getEnergyRatio(tte));
        Complex right = getRightInterpolationPoint(coefficients, maxTapPtr, mainTapIndex, tapCount, tte);

        double result = calculateInterpolatedTDR(left, middle, right, channelWidth.getSymRate(), parabolicInterpolation);
        elapsedTime += System.nanoTime() - start;

        return result;
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
     * Returns {@link Signature} calculated using current pre-eq analasis.
     *
     * @param channelWidth {@link ChannelWidth} provided so that symbol rate can be fetched
     * @param thresholdLevel {@link MicroReflectionSeverityThreshold} provided thresholdLevel for MR
     * @return {@link Signature} with wrapped calculated micro-reflection, severity and delay
     */
    public Signature getSignature(
            final ChannelWidth channelWidth,
            final MicroReflectionSeverityThreshold thresholdLevel) {

        long start = System.nanoTime();
        Signature signature = new Signature(getInChannelFrequencyResponseMagnitude(), channelWidth, thresholdLevel);
        elapsedTime += System.nanoTime() - start;

        return signature;
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

    /**
     * Helper method will return {@link Complex} wrapper where real value is a number of the tap (not an array index) and imaginary value is
     * the energy ratio of the coefficient left of the max reflection tap. There is a special case, when the max reflection tap is the first
     * one after the main tap. Then the calculation is done with the one left of the main tap taking into account it's fixed position.
     * @param coefficients {@link List} of the {@link Coefficient} with all the taps
     * @param middlePtr int an array index of the coefficient with max MR
     * @param mainTapIndex main tap index (not an array index)
     * @param tte long value of the total tap energy
     * @return {@link Complex} wrapper for left interpolation point
     */
    private Complex getLeftInterpolationPoint(
            final List<Coefficient> coefficients,
            final int middlePtr,
            final int mainTapIndex,
            final long tte) {

        return middlePtr == mainTapIndex
                ? new Complex(middlePtr - mainTapIndex - 1d, coefficients.get(middlePtr - 2).getEnergyRatio(tte))
                : new Complex(middlePtr - mainTapIndex * 1d, coefficients.get(middlePtr - 1).getEnergyRatio(tte));
    }

    /**
     * Helper method will return {@link Complex} wrapper where real value is a number of the tap (not an array index) and imaginary value is
     * the energy ratio of the coefficient right of the max reflection tap. There is a special case when the max reflection tap is the last
     * post-main tap. Then the calculation is done using the coefficient left of the last one (it's energy ratio) and taking into account
     * it's fixed position ("virtual" position right of the last one). This will result in targeting middle of the selected max reflection
     * tap.
     * @param coefficients {@link List} of the {@link Coefficient} with all the taps
     * @param middlePtr int an array index of the coefficient with max MR
     * @param mainTapIndex main tap index (not an array index)
     * @param tapCount int total tap count
     * @param tte long value of the total tap energy
     * @return {@link Complex} wrapper for right interpolation point
     */
    private Complex getRightInterpolationPoint(
            final List<Coefficient> coefficients,
            final int middlePtr,
            final int mainTapIndex,
            final int tapCount,
            final long tte) {

        return (middlePtr == tapCount - 1)
                ? new Complex(middlePtr - mainTapIndex + 2d, coefficients.get(middlePtr - 1).getEnergyRatio(tte))
                : new Complex(middlePtr - mainTapIndex + 2d, coefficients.get(middlePtr + 1).getEnergyRatio(tte));
    }

    /**
     * Helper method to interpolate and calculate TDR value from 3 points using parabolic interpolation. Method will also fix 3 point tilt
     * (which will very likely generate either negative or very large positive value) and inverted concavity cases.
     * There are 2 methods available for parabolic interpolation. Currently, using the one found in the third party pre-eq software.
     * @param left {@link Complex} number representing left point with (real, imag) values
     * @param middle {@link Complex} number representing middle point with (real, imag) values
     * @param right {@link Complex} number representing right point with (real, imag) values
     * @param symRate float symbol rate value needed for distance calculation
     * @return double interpolated value fixed if needed (not to produce negative values)
     * @see MathUtility.ParabolicInterpolation#calculate(Complex, Complex, Complex)
     */
    private double calculateInterpolatedTDR(
            final Complex left,
            final Complex middle,
            final Complex right,
            final float symRate,
            final MathUtility.ParabolicInterpolation interpolation) {

        double interpolated = interpolation.calculate(left, middle, right);

        // find the tilt - this is very likely to be negative or very large positive value, or inverted concavity
        if ((left.getImaginary() > middle.getImaginary() && middle.getImaginary() > right.getImaginary()) || (interpolated < 0)) {
            interpolated = 1d;
        }

        return interpolated * MathUtility.getTDRSpeedFactor(symRate);
    }
}
