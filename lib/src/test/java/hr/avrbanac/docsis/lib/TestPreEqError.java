package hr.avrbanac.docsis.lib;

/**
 * Helper pojo wrapper class for invalid pre-eq string test. It binds an invalid pre-eq string with the error class expected to be
 * reported when parsing it.
 */
public class TestPreEqError {
    private final String description;
    private final String preEqString;
    private final PreEqException.ErrorClass expectedErrorClass;

    public TestPreEqError(
            final String description,
            final String preEqString,
            final PreEqException.ErrorClass expectedErrorClass) {

        this.description = description;
        this.preEqString = preEqString;
        this.expectedErrorClass = expectedErrorClass;
    }

    /**
     * Returns stored short description of what is wrong with the string (used as assertion message).
     * @return {@link String} description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns stored invalid pre-eq string.
     * @return {@link String} pre-eq string
     */
    public String getPreEqString() {
        return preEqString;
    }

    /**
     * Returns stored expected error class.
     * @return {@link PreEqException.ErrorClass} expected to be reported
     */
    public PreEqException.ErrorClass getExpectedErrorClass() {
        return expectedErrorClass;
    }
}
