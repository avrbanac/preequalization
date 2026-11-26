package hr.avrbanac.docsis.lib.analysis;

import hr.avrbanac.docsis.lib.util.MathUtility;

/**
 * Estimated delay of the dominant echo (micro-reflection), independent of the channel width. The delay is expressed in symbol periods
 * (T) and can be converted to nanoseconds or to the distance between the two reflection points for a given {@link ChannelWidth}.
 * <p>
 * An echo that is too close to the main tap cannot be located precisely from T-spaced taps (its energy merges with the main tap). Such
 * an estimate is marked as not resolved and its delay is the nominal value of {@link #UNRESOLVED_DELAY} (1 T), which should be read as
 * "about one symbol period or less".
 * </p>
 */
public class EchoEstimate {

    /**
     * Nominal delay (in T) reported for an echo that is not resolved.
     */
    public static final double UNRESOLVED_DELAY = 1d;

    private final double delay;
    private final boolean resolved;
    private final TdrMethod method;

    /**
     * Creates echo estimate.
     * @param delay double echo delay in symbol periods (T)
     * @param resolved boolean false if the echo is too close to the main tap to be located precisely
     * @param method {@link TdrMethod} used for the estimation
     */
    public EchoEstimate(
            final double delay,
            final boolean resolved,
            final TdrMethod method) {

        this.delay = resolved ? delay : UNRESOLVED_DELAY;
        this.resolved = resolved;
        this.method = method;
    }

    /**
     * Returns echo delay in symbol periods (T).
     * @return double echo delay in T
     */
    public double getDelay() {
        return delay;
    }

    /**
     * Returns whether the echo was located with sub-tap precision.
     * @return boolean false if the echo is too close to the main tap and the delay is only the nominal value
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * Returns the method used for the estimation.
     * @return {@link TdrMethod} used
     */
    public TdrMethod getMethod() {
        return method;
    }

    /**
     * Returns echo delay in nanoseconds for the given channel width (T = 1 / symbol rate).
     * @param channelWidth {@link ChannelWidth} providing the symbol rate
     * @return double echo delay in ns
     */
    public double getDelayNs(final ChannelWidth channelWidth) {
        return delay * 1000d / channelWidth.getSymRate();
    }

    /**
     * Returns the distance in meters between the two reflection points that cause the echo (half of the round trip, cable velocity of
     * propagation {@link ChannelWidth#VELOCITY_OF_PROPAGATION}).
     * @param channelWidth {@link ChannelWidth} providing the symbol rate
     * @return double distance in meters
     */
    public double getDistance(final ChannelWidth channelWidth) {
        return delay * MathUtility.getTDRSpeedFactor(channelWidth.getSymRate());
    }

    @Override
    public String toString() {
        return "EchoEstimate{" +
                "delay=" + delay +
                ", resolved=" + resolved +
                ", method=" + method +
                '}';
    }
}
