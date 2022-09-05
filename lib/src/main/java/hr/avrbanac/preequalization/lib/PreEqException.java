package hr.avrbanac.preequalization.lib;

/**
 * Custom {@link RuntimeException} for pre-eq lib purposes. This exception will be thrown in case something fails with API (lib) usage in
 * runtime. Since pre-eq lib will be used to parse pre-eq strings (which may or may not be of correct format) it is possible to fail either
 * in parsing phase, or in processing (calculation) phase.
 */
public class PreEqException extends RuntimeException {
    private static final long serialVersionUID = 20220810000000L;

    public static final PreEqException GENERAL_ERROR = new PreEqException(ErrorClass.GENERAL_ERROR);
    public static final PreEqException COEFFICIENT_MISMATCH_BYTE_SIZE = new PreEqException(ErrorClass.COEFFICIENT_MISMATCH_BYTE_SIZE);
    public static final PreEqException STRING_MISMATCH_BYTE_SIZE = new PreEqException(ErrorClass.STRING_MISMATCH_BYTE_SIZE);
    public static final PreEqException COEFFICIENT_PER_SYMBOL_MISMATCH = new PreEqException(ErrorClass.COEFFICIENT_PER_SYMBOL_MISMATCH);
    public static final PreEqException WRONG_TAP_COUNT = new PreEqException(ErrorClass.WRONG_TAP_COUNT);

    private final ErrorClass errorClass;

    private PreEqException(final ErrorClass errorClass) {
        super(errorClass.errorMessage);
        this.errorClass = errorClass;
    }

    public PreEqException(final String customPreEqErrorMessage) {
        super(ErrorClass.WRAPPER_ERROR.errorMessage + customPreEqErrorMessage);
        this.errorClass = ErrorClass.WRAPPER_ERROR;
    }

    public ErrorClass getErrorClass() {
        return errorClass;
    }

    public enum ErrorClass {

        /**
         * General unspecified pre-eq error occurred in API (lib) during either parsing or processing of the pre-eq string.
         */
        GENERAL_ERROR (1,
                "General error occurred (unspecified pre-equalization error)"),

        /**
         * Wrapper pre-eq error. Similar to the {@link #GENERAL_ERROR}, but with additional information provided.
         */
        WRAPPER_ERROR (100,
                "Following pre-equalization error occurred: "),

        /**
         * Pre-eq error occurred while trying to create PreEqData with the wrong byte array size (input error?).
         */
        STRING_MISMATCH_BYTE_SIZE(1000,
                "Provided string contains wrong byte size"),

        /**
         * Pre-eq error occurred while trying to create coefficient with the wrong byte array size (input error?).
         */
        COEFFICIENT_MISMATCH_BYTE_SIZE(1001,
                "Byte array of the wrong size provided for single coefficient"),

        /**
         * Pre-eq error occurred while parsing pre-eq string data: wrong coefficient per symbol rate.
         */
        COEFFICIENT_PER_SYMBOL_MISMATCH(1002,
                "Expected different value for coefficient per symbol, current implementation does not support this"),

        /**
         * Pre-eq error occurred while parsing pre-eq string data: wrong tap count information.
         */
        WRONG_TAP_COUNT(1003,
                "PreEqualization string contains header with wrong tap count information")
        ;

        private final int errorCode;
        private final String errorMessage;

        ErrorClass(
                final int errorCode,
                final String errorMessage) {

            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}