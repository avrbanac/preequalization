package hr.avrbanac.docsis.lib.analysis;

/**
 * Micro-reflection severity enumerator for {@link Signature} purposes. Several micro-reflection thresholds are defined for each severity.
 * @see MicroReflectionSeverityThreshold
 */
public enum MicroReflectionSeverity {
    /**
     * If the value is lower (smaller) than the threshold.
     */
    GOOD (0, "noActionRequired", -22, -25, -32),
    /**
     * If the value is  higher (greater) than the threshold.
     */
    MARGINAL (1, "highMonitoringFrequency", -22, -25, -32),
    /**
     * If the value is higner (greater) than the threshold.
     */
    BAD (2, "immediateActionRequired", -11, -18, -25);

    private final int level;
    private final String name;
    private final int low;
    private final int medium;
    private final int high;

    MicroReflectionSeverity(
            final int level,
            final String name,
            final int low,
            final int medium,
            final int high) {

        this.level = level;
        this.name = name;
        this.low = low;
        this.medium = medium;
        this.high = high;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns "low" threshold for severity which should be used with operators new to the PNM idea.
     * @return int micro-reflection threshold boundary
     * @see MicroReflectionSeverityThreshold
     */
    public int getLow() {
        return low;
    }

    /**
     * Returns "medium" threshold for severity which should be used as part of CableLabs recommendation. All established networks should use
     * these default threshold values.
     * @return int micro-reflection threshold boundary
     * @see MicroReflectionSeverityThreshold
     */
    public int getMedium() {
        return medium;
    }

    /**
     * Returns "high" threshold for severity which should be used with lower / more robust modulation types.
     * @return int micro-reflection threshold boundary
     * @see MicroReflectionSeverityThreshold
     */
    public int getHigh() {
        return high;
    }

    /**
     * Returns threshold in accordance to the provided threshold level.
     * @param thresholdLevel {@link MicroReflectionSeverityThreshold} provided level
     * @return int threshold level
     */
    public int getThresholdForLevel(final MicroReflectionSeverityThreshold thresholdLevel) {
        switch (thresholdLevel) {
            case LOW:
            case NEW_PNM:
                return low;
            case HIGH:
            case ROBUST_QAM:
            case LOWERED_QAM:
                return high;
            default:
                return medium;
        }
    }

    @Override
    public String toString() {
        return "MicroReflectionSeverity{" +
                "level=" + level +
                ", name='" + name + '\'' +
                "} " + super.toString();
    }
}
