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

    /**
     * Parsed pre-eq data with calculated key metrics.
     */
    private final PreEqData preEqData;
    /**
     * Size of the FFT input / output (number of points).
     */
    private final int fftSize;

    /**
     * Elapsed time in nanoseconds needed for FFT preparation and calculation.
     */
    private long elapsedTime = -1L;

    public PreEqAnalysis(final PreEqData preEqData) {
        this.preEqData = preEqData;
        this.fftSize = MathUtility.getMinFFTSize(
                preEqData.getTapCount(),
                preEqData.getMainTapIndex(),
                PRE_EQ_FFT_INPUT_FORMAT,
                MIN_FFT_INPUT_SIZE,
                MAX_FFT_INPUT_SIZE);

        if (fftSize < 0) {
            throw PreEqException.FFT_TAP_COUNT_ERROR;
        }
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
        long start = System.nanoTime();

        FastFourierTransformer transformer = new FastFourierTransformer(DFT_NORMALIZATION);
        Complex[] fftResult = createRotatedFFTArray(transformer.transform(prepareComplexFFTInput(), TRANSFORM_TYPE));

        elapsedTime = System.nanoTime() - start;

        return fftResult;
    }

    /**
     * Returns ICFR (In Channel Frequency Response) data as a result of forward FFT (Fast Fourier Transform).
     * Implicitly, this method will call {@link #getInChannelFrequencyResponse()} and convert values to magnitude values for plotting.
     * @return array of double values representing magnitudes of FFT output complex points
     */
    public double[] getInChannelFrequencyResponseMagnitude() {
        Complex[] fftResult = getInChannelFrequencyResponse();
        double[] result = new double[fftSize];

        for (int i = 0; i < fftSize; i++) {
            result[i] = 20 * Math.log10(Math.hypot(fftResult[i].getReal(), fftResult[i].getImaginary()));
        }

        return result;
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
     * interpolation for first post-main tap since we cannot use previous one as left point (main tap is maxed out). Instead, last pre-main
     * tap will be used with according x-axis value (2 places offset instead of just one). Similarly, last post-main tap has no right point,
     * so imaginary right point with the same value as that to the right of the last one will be used, effectively fixing calculation to the
     * middle of the last tap. This will lower the precision of calculating interpolation for the last energy tap. (This is the last tap and
     * the time dilatation is the longest, so lowering the precision is not so big of a problem.)
     * </p>
     *
     * @param channelWidth {@link ChannelWidth} carrying the information about width of the channel and symbol rate
     * @param nearPostMainTapCount int count of the post-main energy taps considered near reflections
     * @param onlyFarReflections boolean value - true if near reflections should be left out of the calculation
     * @return double TDR value representing distance between the strongest MR point and first strong downstream reflection point
     */
    public double getTDR(
            final ChannelWidth channelWidth,
            final int nearPostMainTapCount,
            final boolean onlyFarReflections) {

        int mainTapIndex = preEqData.getMainTapIndex();
        int tapCount = preEqData.getTapCount();
        double maxReflection = preEqData.getTapEnergyRatioBoundary();
        List<Coefficient> coefficients = preEqData.getCoefficients();
        long lMTNE = preEqData.getMTNE();
        int ptr = onlyFarReflections ? mainTapIndex + nearPostMainTapCount : mainTapIndex;
        int maxTapPtr = 0;

        while (ptr < tapCount) {
            double currTapEnergyRatio = coefficients.get(ptr).getEnergyRatio(lMTNE);
            if (currTapEnergyRatio > maxReflection) {
                maxReflection = currTapEnergyRatio;
                maxTapPtr = ptr;
            }
            ptr++;
        }

        if (maxTapPtr <= mainTapIndex - 1) throw PreEqException.TDR_CALCULATION_ERROR;

        Complex left = getLeftInterpolationPoint(coefficients, maxTapPtr, mainTapIndex - 1);
        Complex middle = new Complex(maxTapPtr + 1d, coefficients.get(maxTapPtr).getImag());
        Complex right = getRightInterpolationPoint(coefficients, maxTapPtr, tapCount);
        Complex interpolated = MathUtility.calculateParabolicInterpolation(left, middle, right);

        return (interpolated.getReal() - mainTapIndex) * MathUtility.getTDRSpeedFactor(channelWidth.getSymRate());
    }

    /**
     * Helper method will return {@link Complex} wrapper where real value is a number of the tap (not an array index) and imaginary value is
     * the energy ratio of the coefficient left of the max reflection tap. There is a special case, when the max reflection tap is the first
     * one after the main tap. Then the calculation is done with the one left of the main tap taking into account it's fixed position.
     * @param coefficients {@link List} of the {@link Coefficient} with all the taps
     * @param middlePtr int an array index of the coefficient with max MR
     * @param mainTapIndexPtr int an index array of the main tap
     * @return {@link Complex} wrapper for left interpolation point
     */
    private Complex getLeftInterpolationPoint(
            final List<Coefficient> coefficients,
            final int middlePtr,
            final int mainTapIndexPtr) {

        return middlePtr == mainTapIndexPtr + 1
                ? new Complex(middlePtr - 1d, coefficients.get(middlePtr - 2).getImag())
                : new Complex(middlePtr, coefficients.get(middlePtr - 1).getImag());
    }

    /**
     * Helper method will return {@link Complex} wrapper where real value is a number of the tap (not an array index) and imaginary value is
     * the energy ratio of the coefficient right of the max reflection tap. There is a special case when the max reflection tap is the last
     * post-main tap. Then the calculation is done using the coefficient left of the last one (it's energy ratio) and taking into account
     * it's fixed position ("virtual" position right of the last one). This will result in targeting middle of the selected max reflection
     * tap.
     * @param coefficients {@link List} of the {@link Coefficient} with all the taps
     * @param middlePtr int an array index of the coefficient with max MR
     * @param tapCount int total tap count
     * @return {@link Complex} wrapper for right interpolation point
     */
    private Complex getRightInterpolationPoint(
            final List<Coefficient> coefficients,
            final int middlePtr,
            final int tapCount) {

        return (middlePtr == tapCount - 1)
                ? new Complex(middlePtr + 2d, coefficients.get(middlePtr - 1).getImag())
                : new Complex(middlePtr + 2d, coefficients.get(middlePtr + 1).getImag());
    }



}
