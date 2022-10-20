package hr.avrbanac.docsis.app;

import hr.avrbanac.docsis.lib.struct.Coefficient;
import hr.avrbanac.docsis.lib.util.ParsingUtility;

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

    public int getTapIndex() {
        return tapIndex;
    }

    public String getRealBytes() {
        return realBytes;
    }

    public String getImagBytes() {
        return imagBytes;
    }

    public int getRealDec() {
        return realDec;
    }

    public int getImagDec() {
        return imagDec;
    }

    public String getRealRelPwr() {
        return realRelPwr;
    }

    public String getImagRelPwr() {
        return imagRelPwr;
    }

    public String getEnergyRatio() {
        return energyRatio;
    }
}
