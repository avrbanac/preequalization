package hr.avrbanac.docsis.lib.analysis;

/**
 * Pre-equalization strings carry the information of the line degradation. From pre-eq taps adjustments, using the FFT, a frequency domain
 * can be calculated out. This represents In Channel Frequency Response. By comparing multiple ICFRs from devices that share certain network
 * path, some similarities can be found. In other words, by comparing individual device response signatures, a correlation to bigger or
 * smaller degree can be found. If a correlation is strong enough for a group of devices on the same network path, it is considered a group
 * fault. The conclusion can be drawn that line degradation occurs in a shared network segment.
 * <p>
 * A micro-reflection signature is the pair (level, delay) of the dominant echo (CableLabs CM-GL-PNMP-V03, section 6.6.2):
 * </p>
 * <ul>
 *     <li>level: from the peak-to-peak amplitude ripple of the ICFR magnitude, converted with the micro-reflection calculator of
 *     CM-GL-PNMP-V03 Appendix IV (ripple -&gt; VSWR -&gt; reflection coefficient -&gt; dBc)</li>
 *     <li>delay: from the {@link EchoEstimate} of the post-main taps (see {@link PreEqAnalysis#getEchoEstimate(int, boolean)})</li>
 * </ul>
 *
 * @see PreEqAnalysis#getSignature(ChannelWidth)
 */
public class Signature {

    private final MicroReflectionSeverity microReflectionSeverity;
    private final double microReflection;
    private final double delay;
    private final boolean delayResolved;

    /**
     * Create {@link Signature} with default CableLabs threshold recommendations.
     *
     * @param icfrMag double array of In Channel Frequency Response Magnitude (dB)
     * @param echo {@link EchoEstimate} of the dominant echo
     * @param channelWidth {@link ChannelWidth} provided so that symbol rate can be fetched
     */
    public Signature(
            final double[] icfrMag,
            final EchoEstimate echo,
            final ChannelWidth channelWidth) {

        this(icfrMag, echo, channelWidth, MicroReflectionSeverityThreshold.CABLE_LABS);
    }

    /**
     * Create {@link Signature} with provided threshold level.
     *
     * @param icfrMag double array of In Channel Frequency Response Magnitude (dB)
     * @param echo {@link EchoEstimate} of the dominant echo
     * @param channelWidth {@link ChannelWidth} provided so that symbol rate can be fetched
     * @param thresholdLevel {@link MicroReflectionSeverityThreshold} provided threshold level
     */
    public Signature(
            final double[] icfrMag,
            final EchoEstimate echo,
            final ChannelWidth channelWidth,
            final MicroReflectionSeverityThreshold thresholdLevel) {

        microReflection = calculateMicroReflection(peakToPeak(icfrMag));
        microReflectionSeverity = calculateMicroReflectionSeverity(thresholdLevel);
        delay = echo.getDelayNs(channelWidth);
        delayResolved = echo.isResolved();
    }

    private static double peakToPeak(final double[] icfrMag) {
        double min = icfrMag[0];
        double max = icfrMag[0];
        for (double value : icfrMag) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        return max - min;
    }

    /**
     * Micro-reflection calculator (CM-GL-PNMP-V03 Appendix IV): ripple(dB) = 10 log10(VSWR^2), gamma = (VSWR - 1) / (VSWR + 1),
     * MR(dB) = 10 log10(gamma^2).
     */
    private static double calculateMicroReflection(final double peakToPeak) {
        double vswr = Math.sqrt(Math.pow(10, peakToPeak / 10));
        return 10 * Math.log10(Math.pow((vswr - 1) / (vswr + 1), 2));
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

    /**
     * Returns calculated micro-reflection from In Channel Frequency Response data.
     * @return double value of the calculated micro-reflection in dBc
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
     * Returns micro-reflection (echo) delay in nanoseconds. If {@link #isDelayResolved()} is false, the echo is too close to the main tap
     * to be located precisely and this is the nominal one-symbol delay (read as "about 1 T or less").
     * @return double echo delay in ns
     */
    public double getDelay() {
        return delay;
    }

    /**
     * Returns whether the echo delay was located with sub-tap precision.
     * @return boolean false if the delay is only the nominal one-symbol value
     */
    public boolean isDelayResolved() {
        return delayResolved;
    }

    @Override
    public String toString() {
        return "Signature{" +
                "microReflectionSeverity=" + microReflectionSeverity +
                ", microReflection=" + microReflection +
                ", delay=" + delay +
                ", delayResolved=" + delayResolved +
                '}';
    }
}
