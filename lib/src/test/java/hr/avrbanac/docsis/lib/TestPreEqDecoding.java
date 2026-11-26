package hr.avrbanac.docsis.lib;

/**
 * Helper pojo wrapper class for pre-eq string decoding test. It binds the whole pre-eq string with expected decoded values of a single
 * tap, so that the per-string choice between 3-nibble and 4-nibble decoding can be verified.
 */
public class TestPreEqDecoding {
    private final String preEqString;
    private final int tapIndex;
    private final int expectedReal;
    private final int expectedImag;

    public TestPreEqDecoding(
            final String preEqString,
            final int tapIndex,
            final int expectedReal,
            final int expectedImag) {

        this.preEqString = preEqString;
        this.tapIndex = tapIndex;
        this.expectedReal = expectedReal;
        this.expectedImag = expectedImag;
    }

    /**
     * Returns stored pre-eq string.
     * @return {@link String} pre-eq string
     */
    public String getPreEqString() {
        return preEqString;
    }

    /**
     * Returns stored tap index to check (not an array index).
     * @return int tap index
     */
    public int getTapIndex() {
        return tapIndex;
    }

    /**
     * Returns stored expected real part of the checked tap.
     * @return int expected real part
     */
    public int getExpectedReal() {
        return expectedReal;
    }

    /**
     * Returns stored expected imaginary part of the checked tap.
     * @return int expected imaginary part
     */
    public int getExpectedImag() {
        return expectedImag;
    }
}
