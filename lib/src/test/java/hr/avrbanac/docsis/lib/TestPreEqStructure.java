package hr.avrbanac.docsis.lib;

public class TestPreEqStructure {
    private final String preEqString;
    private final int mainTapIndex;
    private final int tapCount;
    private final int coefficientPerSymbol;
    private final long lMTE;
    private final long lMTNA;
    private final long lMTNE;
    private final long lPreMTE;
    private final long lPostMTE;
    private final long lTTE;
    private final double dMTC;
    private final double dMTR;
    private final double dNMTER;
    private final double dPreMTTER;
    private final double dPostMTTER;
    private final double dPPESR;
    private final double dPPTSR;
    private final double dTDR;
    private final double[] daICFR;
    private final double microReflection;
    private final String severity;

    /**
     * Private CTOR - too many parameters (unorganized!!!). Using builder pattern instead.
     */
    private TestPreEqStructure(final Builder builder) {
        this.preEqString = builder.preEqString;
        this.mainTapIndex = builder.mainTapIndex;
        this.tapCount = builder.tapCount;
        this.coefficientPerSymbol = builder.coefficientPerSymbol;
        this.lMTE = builder.lMTE;
        this.lMTNA = builder.lMTNA;
        this.lMTNE = builder.lMTNE;
        this.lPreMTE = builder.lPreMTE;
        this.lPostMTE = builder.lPostMTE;
        this.lTTE = builder.lTTE;
        this.dMTC = builder.dMTC;
        this.dMTR = builder.dMTR;
        this.dNMTER = builder.dNMTER;
        this.dPreMTTER = builder.dPreMTTER;
        this.dPostMTTER = builder.dPostMTTER;
        this.dPPESR = builder.dPPESR;
        this.dPPTSR = builder.dPPTSR;
        this.dTDR = builder.dTDR;
        this.daICFR = builder.daICFR;
        this.microReflection = builder.microReflection;
        this.severity = builder.severity;
    }

    public String getPreEqString() {
        return preEqString;
    }

    public int getMainTapIndex() {
        return mainTapIndex;
    }

    public int getTapCount() {
        return tapCount;
    }

    public int getCoefficientPerSymbol() {
        return coefficientPerSymbol;
    }

    public long getlMTE() {
        return lMTE;
    }

    public long getlMTNA() {
        return lMTNA;
    }

    public long getlMTNE() {
        return lMTNE;
    }

    public long getlPreMTE() {
        return lPreMTE;
    }

    public long getlPostMTE() {
        return lPostMTE;
    }

    public long getlTTE() {
        return lTTE;
    }

    public double getdMTC() {
        return dMTC;
    }

    public double getdMTR() {
        return dMTR;
    }

    public double getdNMTER() {
        return dNMTER;
    }

    public double getdPreMTTER() {
        return dPreMTTER;
    }

    public double getdPostMTTER() {
        return dPostMTTER;
    }

    public double getdPPESR() {
        return dPPESR;
    }

    public double getdPPTSR() {
        return dPPTSR;
    }

    public double getdTDR() {
        return dTDR;
    }

    public double[] getDaICFR() {
        return daICFR;
    }

    public double getMicroReflection() {
        return microReflection;
    }

    public String getSeverity() {
        return severity;
    }

    public static class Builder {
        private String preEqString;
        private int mainTapIndex;
        private int tapCount;
        private int coefficientPerSymbol;
        private long lMTE;
        private long lMTNA;
        private long lMTNE;
        private long lPreMTE;
        private long lPostMTE;
        private long lTTE;
        private double dMTC;
        private double dMTR;
        private double dNMTER;
        private double dPreMTTER;
        private double dPostMTTER;
        private double dPPESR;
        private double dPPTSR;
        private double dTDR;
        private double[] daICFR;
        private double microReflection;
        private String severity;
        private final boolean[] initialized = {
                false, false, false, false,
                false, false, false, false,
                false, false, false, false,
                false, false, false, false,
                false, false, false, false,
                false};

        public Builder setPreEqString(final String preEqString) {
            this.preEqString = preEqString;
            this.initialized[0] = true;
            return this;
        }

        public Builder setMainTapIndex(final int mainTapIndex) {
            this.mainTapIndex = mainTapIndex;
            this.initialized[1] = true;
            return this;
        }

        public Builder setTapCount(final int tapCount) {
            this.tapCount = tapCount;
            this.initialized[2] = true;
            return this;
        }

        public Builder setCoefficientPerSymbol(final int coefficientPerSymbol) {
            this.coefficientPerSymbol = coefficientPerSymbol;
            this.initialized[3] = true;
            return this;
        }

        public Builder setPreEqData(
                final String preEqString,
                final int mainTapIndex,
                final int tapCount,
                final int coefficientPerSymbol) {

            return this
                    .setPreEqString(preEqString)
                    .setMainTapIndex(mainTapIndex)
                    .setTapCount(tapCount)
                    .setCoefficientPerSymbol(coefficientPerSymbol);
        }

        public Builder setlMTE(final long lMTE) {
            this.lMTE = lMTE;
            this.initialized[4] = true;
            return this;
        }

        public Builder setlMTNA(final long lMTNA) {
            this.lMTNA = lMTNA;
            this.initialized[5] = true;
            return this;
        }

        public Builder setlMTNE(final long lMTNE) {
            this.lMTNE = lMTNE;
            this.initialized[6] = true;
            return this;
        }

        public Builder setlPreMTE(final long lPreMTE) {
            this.lPreMTE = lPreMTE;
            this.initialized[7] = true;
            return this;
        }

        public Builder setlPostMTE(final long lPostMTE) {
            this.lPostMTE = lPostMTE;
            this.initialized[8] = true;
            return this;
        }

        public Builder setlTTE(final long lTTE) {
            this.lTTE = lTTE;
            this.initialized[9] = true;
            return this;
        }

        public Builder setLongMetrics(
                final long lMTE,
                final long lMTNA,
                final long lMTNE,
                final long lPreMTE,
                final long lPostMTE,
                final long lTTE) {

            return this
                    .setlMTE(lMTE)
                    .setlMTNA(lMTNA)
                    .setlMTNE(lMTNE)
                    .setlPreMTE(lPreMTE)
                    .setlPostMTE(lPostMTE)
                    .setlTTE(lTTE);
        }

        public Builder setdMTC(final double dMTC) {
            this.dMTC = dMTC;
            this.initialized[10] = true;
            return this;
        }

        public Builder setdMTR(final double dMTR) {
            this.dMTR = dMTR;
            this.initialized[11] = true;
            return this;
        }

        public Builder setdNMTER(final double dNMTER) {
            this.dNMTER = dNMTER;
            this.initialized[12] = true;
            return this;
        }

        public Builder setdPreMTTER(final double dPreMTTER) {
            this.dPreMTTER = dPreMTTER;
            this.initialized[13] = true;
            return this;
        }

        public Builder setdPostMTTER(final double dPostMTTER) {
            this.dPostMTTER = dPostMTTER;
            this.initialized[14] = true;
            return this;
        }

        public Builder setdPPESR(final double dPPESR) {
            this.dPPESR = dPPESR;
            this.initialized[15] = true;
            return this;
        }

        public Builder setdPPTSR(final double dPPTSR) {
            this.dPPTSR = dPPTSR;
            this.initialized[16] = true;
            return this;
        }

        public Builder setDBMetrics(
                final double dMTC,
                final double dMTR,
                final double dNMTER,
                final double dPreMTTER,
                final double dPostMTTER,
                final double dPPESR,
                final double dPPTSR) {

            return this
                    .setdMTC(dMTC)
                    .setdMTR(dMTR)
                    .setdNMTER(dNMTER)
                    .setdPreMTTER(dPreMTTER)
                    .setdPostMTTER(dPostMTTER)
                    .setdPPESR(dPPESR)
                    .setdPPTSR(dPPTSR);
        }

        public Builder setdTDR(final double dTDR) {
            this.dTDR = dTDR;
            this.initialized[17] = true;
            return this;
        }

        public Builder setDaICFR(final double[] daICFR) {
            this.daICFR = daICFR;
            this.initialized[18] = true;
            return this;
        }

        public Builder setMicroReflection(final double microReflection) {
            this.microReflection = microReflection;
            this.initialized[19] = true;
            return this;
        }

        public Builder setSeverity(final String severity) {
            this.severity = severity;
            this.initialized[20] = true;
            return this;
        }

        public Builder setSignature(
                final double microReflection,
                final String severity) {

            return this.setMicroReflection(microReflection).setSeverity(severity);
        }

        public TestPreEqStructure build() {
            for (boolean b : initialized) {
                if (!b) throw PreEqException.TEST_ERROR;
            }
            return new TestPreEqStructure(this);
        }
    }
}
