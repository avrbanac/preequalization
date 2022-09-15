package hr.avrbanac.docsis.lib.util;

import hr.avrbanac.docsis.lib.PreEqException;

/**
 * Utility class for parsing purposes.
 */
public class ParsingUtility {

    private ParsingUtility() { }

    /**
     * Returns byte array from string containing that byte array. Input string must be a valid byte array representation without delimiters.
     * @param inputString {@link String} a valid string representation of the byte array
     * @return byte[] converted from value contained in the input string
     */
    public static byte[] hexStringToByteArray(final String inputString) {
        try {
            int length = inputString.length();
            byte[] data = new byte[length / 2];
            for (int i = 0; i < length; i += 2) {
                data[i / 2] = (byte) ((Character.digit(inputString.charAt(i), 16) << 4)
                        + Character.digit(inputString.charAt(i + 1), 16));
            }
            return data;
        } catch (Exception e) {
            throw PreEqException.STRING_NOT_BYTE_REPRESENTATION;
        }
    }
}
