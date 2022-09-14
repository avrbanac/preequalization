package hr.avrbanac.preequalization.lib.analysis;

import hr.avrbanac.preequalization.lib.PreEqException;
import hr.avrbanac.preequalization.lib.struct.Coefficient;
import hr.avrbanac.preequalization.lib.struct.PreEqData;
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
        this.fftSize = getMinFFTSize(preEqData.getTapCount(), preEqData.getMainTapIndex());

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
     * Helper method determines how large size of input array for FFT is needed. Only valid sizes are 2^n. Depending on the
     * {@link PreEqFFTInputFormat} there are two methods of calculating this size. For case where main tap needs to be located in the middle
     * of the FFT input array, more complex algorithm is used. If input points are just sequentially positioned taps, it is only important
     * for this size to be greater than the total tap count.
     * @return int min size of FFT input array
     * @see PreEqFFTInputFormat
     */
    private int getMinFFTSize(
            final int tapCount,
            final int mainTapIndex) {

        int halfSize;

        if (PreEqFFTInputFormat.FIRST_TAP_FIRST_POINT.equals(PRE_EQ_FFT_INPUT_FORMAT)) {
            halfSize = tapCount / 2;
        } else {
            halfSize = (tapCount - mainTapIndex > tapCount / 2) ? tapCount - mainTapIndex : mainTapIndex;
        }

        // there is no need for testing of sizes smaller than MIN and greater than MAX defined by this class private static members.
        for (int i = MIN_FFT_INPUT_SIZE; i <= MAX_FFT_INPUT_SIZE ; i *= 2) {
            if (i >= halfSize * 2) return i;
        }

        return -1;
    }

    /**
     * Returns the number of input / output points for FFT.
     * @return int FFT size
     * @see #getMinFFTSize(int, int)
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
        double[]  result = new double[fftSize];

        for (int i = 0; i < fftSize; i++) {
            result[i] = 20 * Math.log10(Math.hypot(fftResult[i].getReal(), fftResult[i].getImaginary()));
        }

        return result;
    }
}
