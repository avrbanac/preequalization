package hr.avrbanac.docsis.lib.analysis;

/**
 * Pre-eq ICFR signature correlation bond strength enumeration. Correlation between pairs will fall under one of these values.
 */
public enum CorrelationBond {
    /**
     * There is no correlation between pairs.
     */
    NONE    (-1),

    /**
     * There is a weak correlation between pairs.
     */
    WEAK    (0),

    /**
     * There is a strong correlation between pairs.
     */
    STRONG  (1);

    private final int numValue;

    CorrelationBond(final int numValue) {
        this.numValue = numValue;
    }

    /**
     * Returns numeric value of the correlation bond strength.
     * @return int value of the correlation bond
     */
    public int getNumValue() {
        return numValue;
    }
}
