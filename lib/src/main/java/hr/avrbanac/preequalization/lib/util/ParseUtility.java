package hr.avrbanac.preequalization.lib.util;

/**
 * Utility class for parsing purposes.
 */
public class ParseUtility {

    private ParseUtility() { }

    public static byte[] hexStringToByteArray(final String inputString) {
        int length = inputString.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(inputString.charAt(i), 16) << 4)
                    + Character.digit(inputString.charAt(i+1), 16));
        }
        return data;
    }
}
