package hr.avrbanac.docsis.lib.util;

import hr.avrbanac.docsis.lib.PreEqException;
import hr.avrbanac.docsis.lib.struct.PreEqData;

/**
 * Utility class for parsing purposes.
 */
public class ParsingUtility {
    private static final String LS = System.lineSeparator();

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

    /**
     * Validates provided string. There are 2 separated checks within this method:
     * <ul>
     *     <li>string is made of hex chars</li>
     *     <li>string is of required length (once normalized without separators)</li>
     * </ul>
     * @param preEqString {@link String} input string to check
     * @param size int size of the normalized string
     * @return boolean true if the string was successfully validated
     */
    public static boolean isPreEqStringValid(
            final String preEqString,
            final int size) {

        String normalized = preEqString.replace(":","").replace(" ","");
        if (normalized.length() != size) return false;

        for (char c : normalized.toCharArray()) {
            if (!isHex(c)) return false;
        }

        return true;
    }

    /**
     * Fast way to check whether character is a hex character.
     * @param c char input to test
     * @return boolean true if char is a hex char
     */
    private static boolean isHex(final char c) {
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return true;
            default:
                return false;
        }
    }

    /**
     * Converts byte array to hex string.
     * @param bytes array of bytes as input array
     * @param start int start position (included)
     * @param end int end position (excluded)
     * @return hex string
     */
    public static String byteArrayToHexString(
            final byte[] bytes,
            final int start,
            final int end) {

        if (start < end && end <= bytes.length) {
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < end; i++) {
                sb.append(String.format("%02x",bytes[i] & 0xff));
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * Returns string with formatted metrics.
     * @param preEqData {@link PreEqData} provided calculated data
     * @return {@link String} formatted metrics
     */
    public static String preEqDataToMetricsToString(final PreEqData preEqData) {
        return LS +
                "KEY METRICS:" + LS + LS +
                "    MTE (main tap energy): " + preEqData.getMTE() + LS +
                "    MTNA (main tap nominal amplitude): " + preEqData.getMTNA() + LS +
                "    MTNE (main tap nominal energy): " + preEqData.getMTNE() + LS +
                "    preMTE (pre-main tap energy): " + preEqData.getPreMTE() + LS +
                "    postMTE (post-main tap energy): " + preEqData.getPostMTE() + LS + LS +
                "    TTE (total tap energy): " + preEqData.getTTE() + " dB" + LS +
                "    MTC (main tap compression): " + preEqData.getMTC() + " dB" + LS +
                "    MTR (main tap ratio): " + preEqData.getMTR() + " dB" + LS +
                "    NMTER (non-main tap to total energy): " + preEqData.getNMTER() + " dB" + LS +
                "    preMTTER (pre-main tap to total energy): " + preEqData.getPreMTTER() + " dB" + LS +
                "    postMTTER (post-main tap to total energy): " + preEqData.getPostMTTER() + " dB" + LS +
                "    PPESR (pre-post energy symetry ratio): " + preEqData.getPPESR() + " dB" + LS +
                "    PPTSR (pre-post tap symetry ratio): " + preEqData.getPPTSR() + " dB" + LS;
    }
}
