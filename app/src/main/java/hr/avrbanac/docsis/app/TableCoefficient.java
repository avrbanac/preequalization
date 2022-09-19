package hr.avrbanac.docsis.app;

import hr.avrbanac.docsis.lib.struct.Coefficient;
import hr.avrbanac.docsis.lib.util.ParsingUtility;

public class TableCoefficient {
    private final int tapIndex;
    private final String realBytes;
    private final String imagBytes;
    private final int realDec;
    private final int imagDec;
    private final double realRelPwr;
    private final double imagRelPwr;
    private final double energyRatio;

    public TableCoefficient(
            final Coefficient coefficient,
            final int tapIndex,
            final long lMTNA,
            final long lMTNE) {

        this.tapIndex = tapIndex;
        byte[] bytes = coefficient.getBytes();
        this.realBytes = ParsingUtility.byteArrayToHexString(bytes, 0 , bytes.length / 2);
        this.imagBytes = ParsingUtility.byteArrayToHexString(bytes, bytes.length / 2, bytes.length);
        this.realDec = coefficient.getReal();
        this.imagDec = coefficient.getImag();
        this.realRelPwr = coefficient.getRelativePowerReal(lMTNA);
        this.imagRelPwr = coefficient.getRelativePowerImag(lMTNA);
        this.energyRatio = coefficient.getEnergyRatio(lMTNE);
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

    public double getRealRelPwr() {
        return realRelPwr;
    }

    public double getImagRelPwr() {
        return imagRelPwr;
    }

    public double getEnergyRatio() {
        return energyRatio;
    }
}