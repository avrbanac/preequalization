package hr.avrbanac.docsis.app;

import hr.avrbanac.docsis.lib.struct.Coefficient;
import hr.avrbanac.docsis.lib.util.ParsingUtility;

/**
 * Helper pojo class for easy wrapping of the data needed for table "widget" in the pre-eq java FX application. Each table row will be
 * represented by one object instance of this class.
 */
public class TableCoefficient {
    private final int tapIndex;
    private final String realBytes;
    private final String imagBytes;
    private final int realDec;
    private final int imagDec;
    private final String realRelPwr;
    private final String imagRelPwr;
    private final String energyRatio;

    public TableCoefficient(
            final Coefficient coefficient,
            final int tapIndex,
            final long lMTNA,
            final long lMTNE,
            final int scale) {

        String formatting = "%." + scale + "f";
        this.tapIndex = tapIndex;
        byte[] bytes = coefficient.getBytes();
        this.realBytes = ParsingUtility.byteArrayToHexString(bytes, 0 , bytes.length / 2);
        this.imagBytes = ParsingUtility.byteArrayToHexString(bytes, bytes.length / 2, bytes.length);
        this.realDec = coefficient.getReal();
        this.imagDec = coefficient.getImag();
        this.realRelPwr = String.format(formatting, coefficient.getRelativePowerReal(lMTNA));
        this.imagRelPwr =  String.format(formatting, coefficient.getRelativePowerImag(lMTNA));
        this.energyRatio =  String.format(formatting, coefficient.getNominalEnergyRatio(lMTNE));
    }

    /**
     * Returns tap index cell for the table.
     * @return int value of the tap index
     */
    public int getTapIndex() {
        return tapIndex;
    }

    /**
     * Returns real bytes cell for the table.
     * @return {@link String} parsed real portion of the coefficient bytes
     */
    public String getRealBytes() {
        return realBytes;
    }

    /**
     * Returns imaginary bytes cell for the table.
     * @return {@link String} parsed imaginary portion of the coefficient bytes
     */
    public String getImagBytes() {
        return imagBytes;
    }

    /**
     * Returns decimal real value cell for the table.
     * @return int decimal value of the real portion of the coefficient bytes
     */
    public int getRealDec() {
        return realDec;
    }

    /**
     * Returns decimal imaginary value cell for the table.
     * @return int decimal value of the imaginary portion of the coefficient bytes
     */
    public int getImagDec() {
        return imagDec;
    }

    /**
     * Returns relative power of the value for the real portion of the coefficient.
     * @return {@link String} relative power value of the real portion of the coefficient
     */
    public String getRealRelPwr() {
        return realRelPwr;
    }

    /**
     * Returns relative power of the value for the imaginary portion of the coefficient.
     * @return {@link String} relative power value of the imaginary portion of the coefficient
     */
    public String getImagRelPwr() {
        return imagRelPwr;
    }

    /**
     * Returns energy ratio value of the coefficient for the table.
     * @return {@link String} value of the energy ratio for the coefficient
     */
    public String getEnergyRatio() {
        return energyRatio;
    }
}
