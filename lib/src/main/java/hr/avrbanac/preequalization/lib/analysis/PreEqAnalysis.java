package hr.avrbanac.preequalization.lib.analysis;

import hr.avrbanac.preequalization.lib.PreEqException;
import hr.avrbanac.preequalization.lib.struct.PreEqData;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;


/**
 * Main analysis class for parsed pre-equalization data.
 */
public class PreEqAnalysis {
    // Constants defined for FFT analysis (forward direction: from time domain to frequency domain)
    private static final TransformType TRANSFORM_TYPE = TransformType.FORWARD;
    private static final DftNormalization DFT_NORMALIZATION = DftNormalization.STANDARD;
    private static final int MIN_FFT_INPUT_SIZE = 8;
    private static final int MAX_FFT_INPUT_SIZE = 256;

    /**
     * Parsed pre-eq data with calculated key metrics.
     */
    private final PreEqData preEqData;

    /**
     * Elapsed time in nanoseconds needed for FFT preparation and calculation.
     */
    private long elapsedTime = -1L;

    public PreEqAnalysis(final PreEqData preEqData) {
        this.preEqData = preEqData;
    }

    /**
     * Returns ICFR(In Channel Frequency Response) data as a result of forward FFT (Fast Fourier Transform).
     * @return {@link Complex} array in frequency domain
     */
    public Complex[] getInChannelFrequencyResponse() {
        long start = System.nanoTime();

        FastFourierTransformer transformer = new FastFourierTransformer(DFT_NORMALIZATION);
        Complex[] fftResult = transformer.transform(prepareDoubleArrayFFTInput(), TRANSFORM_TYPE);

        elapsedTime = System.nanoTime() - start;

        return fftResult;
    }

    /**
     * Returns elapsed time in nanoseconds (time needed for FFT preparation and calculation).
     * @return long elapsed time in ns
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Helper method that prepares FFT input double array structure. For FFT input, main tap is so allocated, it corresponds to the middle
     * point of the FFT input. The rest of the points are defined according to tap values, and remaining input points that would stay unused
     * are zeroed.
     * @return double[] value structure for FFT input (calculated from pre-eq coefficients)
     */
    private double[] prepareDoubleArrayFFTInput() {
        int mainTapIndex = preEqData.getMainTapIndex();
        int inputArraySize = getMinFFTSize(preEqData.getTapCount(), mainTapIndex);

        if (inputArraySize < 0) {
            throw PreEqException.FFT_TAP_COUNT_ERROR;
        }

        // this array is zeroed as default doubles are positive 0.0
        double[] inputPoints = new double[inputArraySize];
        long lMTNE = preEqData.getMTNE();

        for (int i = inputArraySize / 2 - mainTapIndex; i < inputArraySize; i++) {
            inputPoints[i] = preEqData.getCoefficients().get(i - mainTapIndex).getEnergyRatio(lMTNE);
        }

        return inputPoints;
    }

    /**
     * Helper method determines how large size of input array for FFT is needed. Only valid sizes are 2^n. Additional problem is that main
     * tap needs to be in the middle of the input range. Algorithm needs to determine larger "half" of the tap array and make sure it can
     * fit into half of the FFT input array.
     * @return int min size of FFT input array
     */
    private int getMinFFTSize(
            final int tapCount,
            final int mainTapIndex) {

        int halfSize = (tapCount - mainTapIndex > tapCount / 2) ? tapCount - mainTapIndex : mainTapIndex;

        // there is no need for testing of sizes smaller than 8 and greater than 256
        for (int i = MIN_FFT_INPUT_SIZE; i <= MAX_FFT_INPUT_SIZE ; i *= 2) {
            if (i >= halfSize * 2) return i;
        }

        return -1;
    }

}
