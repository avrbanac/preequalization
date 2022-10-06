package hr.avrbanac.docsis.lib.analysis;

/**
 * Channel width enumeration with additional relevant information. Also, some constants are kept here since they will be used exclus
 */
public enum ChannelWidth {

    CW_US_3_2(
            "3.2MHz",
            3.2f,
            2.560f,
            0.391f,
            6.250f
    ),

    CW_US_6_4(
            "6.4MHz",
            6.4f,
            5.120f,
            0.195f,
            3.125f
    );

    public static final float VELOCITY_OF_PROPAGATION = 0.87f;
    public static final float SPEED_OF_LIGHT = 299792458.0f;

    private final String label;
    private final float value;
    private final float symRate;
    private final float symPeriod;
    private final float maxDelay;

    ChannelWidth(
            final String label,
            final float value,
            final float symRate,
            final float symPeriod,
            final float maxDelay) {

        this.label = label;
        this.value = value;
        this.symRate = symRate;
        this.symPeriod = symPeriod;
        this.maxDelay = maxDelay;
    }

    /**
     * Returns channel width label.
     * @return {@link String} channel width label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns channel width value.
     * @return float channel width value
     */
    public float getValue() {
        return value;
    }

    /**
     * Returns channel symbol rate.
     * @return float channel symbol rate
     */
    public float getSymRate() {
        return symRate;
    }

    /**
     * Returns channel symbol period duration in microseconds.
     * @return float symbol period duration
     */
    public float getSymPeriod() {
        return symPeriod;
    }

    /**
     * Returns channel max delay (16 T for post-main tap).
     * @return float channel max delay
     */
    public float getMaxDelay() {
        return maxDelay;
    }

}
