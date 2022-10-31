package hr.avrbanac.docsis.lib;

/**
 * Helper pojo wrapper for pre-eq data test structure.
 * @see hr.avrbanac.docsis.lib.struct.PreEqMetrics
 */
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

    /**
     * Returns stored pre-eq string.
     * @return {@link String} stored pre-eq string
     */
    public String getPreEqString() {
        return preEqString;
    }

    /**
     * Returns stored main tap index value (not an array index).
     * @return int main tap index value
     */
    public int getMainTapIndex() {
        return mainTapIndex;
    }

    /**
     * Returns stored total tap count.
     * @return int total tap count
     */
    public int getTapCount() {
        return tapCount;
    }

    /**
     * Returns stored coefficient per symbol value.
     * @return int coefficient per symbol value
     */
    public int getCoefficientPerSymbol() {
        return coefficientPerSymbol;
    }

    /**
     * Returns stored MTE key metric value.
     * @return long MTE value
     */
    public long getlMTE() {
        return lMTE;
    }

    /**
     * Returns stored MTNA key metric value.
     * @return long MTNA value
     */
    public long getlMTNA() {
        return lMTNA;
    }

    /**
     * Returns stored MTNE key metric value.
     * @return long MTNE value
     */
    public long getlMTNE() {
        return lMTNE;
    }

    /**
     * Returns stored pre-MTE key metric value.
     * @return long pre-MTE value
     */
    public long getlPreMTE() {
        return lPreMTE;
    }

    /**
     * Returns stored post-MTE key metric value.
     * @return long post-MTE value
     */
    public long getlPostMTE() {
        return lPostMTE;
    }

    /**
     * Returns stored TTE key metric value.
     * @return long TTE value
     */
    public long getlTTE() {
        return lTTE;
    }

    /**
     * Returns stored MTC ratio key metric value.
     * @return double MTC ratio in dB
     */
    public double getdMTC() {
        return dMTC;
    }

    /**
     * Returns stored MTR ratio key metric value.
     * @return double MTR ratio in dB.
     */
    public double getdMTR() {
        return dMTR;
    }

    /**
     * Returns stored NMTER ratio key metric value.
     * @return double NMTER ratio in dB
     */
    public double getdNMTER() {
        return dNMTER;
    }

    /**
     * Returns stored pre-MTTER ratio key metric value.
     * @return double pre-MTTER ratio in dB
     */
    public double getdPreMTTER() {
        return dPreMTTER;
    }

    /**
     * Returns stored post-MTTER ratio key metric value.
     * @return double post-MTTER ratio in dB
     */
    public double getdPostMTTER() {
        return dPostMTTER;
    }

    /**
     * Returns stored PPESR ratio key metric value.
     * @return double PPESR ratio in dB
     */
    public double getdPPESR() {
        return dPPESR;
    }

    /**
     * Returns stored PPTSR ratio key metric value.
     * @return double PPTSR ratio in dB.
     */
    public double getdPPTSR() {
        return dPPTSR;
    }

    /**
     * Returns stored TDR (time domain reflectometry) value.
     * @return double TDR value in m
     */
    public double getdTDR() {
        return dTDR;
    }

    /**
     * Returns stored ICFR (In Channel Frequency Response) data array.
     * @return double array of ICFR points.
     */
    public double[] getDaICFR() {
        return daICFR;
    }

    /**
     * Returns stored ICFR (In Channel Frequency Response) micro-reflection value.
     * @return double micro-reflection value
     */
    public double getMicroReflection() {
        return microReflection;
    }

    /**
     * Returns stored ICFR (In Channel Frequency Response) MR (micro-reflection) severity value.
     * @return {@link String} value of MR severity (name)
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * Helper builder class since the test structure has so many fields.
     */
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

        /**
         * Sets pre-eq string value for builder.
         * @param preEqString {@link String} pre-eq raw value
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setPreEqString(final String preEqString) {
            this.preEqString = preEqString;
            this.initialized[0] = true;
            return this;
        }

        /**
         * Sets main tap index value for builder.
         * @param mainTapIndex int main tap index (not an array index) value
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setMainTapIndex(final int mainTapIndex) {
            this.mainTapIndex = mainTapIndex;
            this.initialized[1] = true;
            return this;
        }

        /**
         * Sets total tap count number for builder.
         * @param tapCount int total tap number
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setTapCount(final int tapCount) {
            this.tapCount = tapCount;
            this.initialized[2] = true;
            return this;
        }

        /**
         * Sets coefficient per symbol value for the builder.
         * @param coefficientPerSymbol int value of the coefficient per symbol
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setCoefficientPerSymbol(final int coefficientPerSymbol) {
            this.coefficientPerSymbol = coefficientPerSymbol;
            this.initialized[3] = true;
            return this;
        }

        /**
         * Sets multiple values to make builder easier to use (this setter sets all the header data).
         * @param preEqString {@link String} pre-eq raw data string
         * @param mainTapIndex int main tap index (not an array index)
         * @param tapCount int total tap number
         * @param coefficientPerSymbol int value of the coefficient per symbol
         * @return {@link Builder} so the setters can be chained
         */
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

        /**
         * Sets the MTE value for the builder.
         * @param lMTE long MTE value
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setlMTE(final long lMTE) {
            this.lMTE = lMTE;
            this.initialized[4] = true;
            return this;
        }

        /**
         * Sets the MTNA value for the builder.
         * @param lMTNA long MTNA value
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setlMTNA(final long lMTNA) {
            this.lMTNA = lMTNA;
            this.initialized[5] = true;
            return this;
        }

        /**
         * Sets the MTNE value for the builder.
         * @param lMTNE long MTNE value
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setlMTNE(final long lMTNE) {
            this.lMTNE = lMTNE;
            this.initialized[6] = true;
            return this;
        }

        /**
         * Sets the pre-MTE value for the builder.
         * @param lPreMTE long pre-MTE value
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setlPreMTE(final long lPreMTE) {
            this.lPreMTE = lPreMTE;
            this.initialized[7] = true;
            return this;
        }

        /**
         * Sets the post-MTE value for the builder.
         * @param lPostMTE long post-MTE value
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setlPostMTE(final long lPostMTE) {
            this.lPostMTE = lPostMTE;
            this.initialized[8] = true;
            return this;
        }

        /**
         * Sets the TTE value for the builder.
         * @param lTTE long TTE value
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setlTTE(final long lTTE) {
            this.lTTE = lTTE;
            this.initialized[9] = true;
            return this;
        }

        /**
         * Helper setter for setting all key metric values at once for builder.
         * @param lMTE long MTE value
         * @param lMTNA long MTNA value
         * @param lMTNE long MTNE value
         * @param lPreMTE long pre-MTE value
         * @param lPostMTE long post-MTE value
         * @param lTTE long TTE value
         * @return {@link Builder} so the setters can be chained
         */
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

        /**
         * Sets the MTC ratio for the builder.
         * @param dMTC double MTC ratio in dB
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setdMTC(final double dMTC) {
            this.dMTC = dMTC;
            this.initialized[10] = true;
            return this;
        }

        /**
         * Sets the MTR ratio for the builder.
         * @param dMTR double MTR ratio in dB
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setdMTR(final double dMTR) {
            this.dMTR = dMTR;
            this.initialized[11] = true;
            return this;
        }

        /**
         * Sets the NMTER ratio for the builder.
         * @param dNMTER double NMTER ratio in dB
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setdNMTER(final double dNMTER) {
            this.dNMTER = dNMTER;
            this.initialized[12] = true;
            return this;
        }

        /**
         * Sets the pre-MTTER ratio for the builder.
         * @param dPreMTTER double pre-MTTER ratio in dB
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setdPreMTTER(final double dPreMTTER) {
            this.dPreMTTER = dPreMTTER;
            this.initialized[13] = true;
            return this;
        }

        /**
         * Sets the post-MTTER ratio for the builder.
         * @param dPostMTTER double post-MTTER ratio in dB
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setdPostMTTER(final double dPostMTTER) {
            this.dPostMTTER = dPostMTTER;
            this.initialized[14] = true;
            return this;
        }

        /**
         * Sets the PPESR ratio for the builder.
         * @param dPPESR double PPESR ratio in dB
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setdPPESR(final double dPPESR) {
            this.dPPESR = dPPESR;
            this.initialized[15] = true;
            return this;
        }

        /**
         * Sets the PPTSR ratio for the builder.
         * @param dPPTSR double PPTSR ratio in dB
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setdPPTSR(final double dPPTSR) {
            this.dPPTSR = dPPTSR;
            this.initialized[16] = true;
            return this;
        }

        /**
         * Helper setter for setting all key metric ratios at once for builder.
         * @param dMTC double MTC ratio in dB
         * @param dMTR double MTR ratio in dB
         * @param dNMTER double NMTER ratio in dB
         * @param dPreMTTER double pre-MTTER ratio in dB
         * @param dPostMTTER double post-MTTER ratio in dB
         * @param dPPESR double PPESR ratio in dB
         * @param dPPTSR double PPTSR ratio in dB
         * @return {@link Builder} so the setters can be chained
         */
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

        /**
         * Sets the TDR value for the builder.
         * @param dTDR double TDR value in m
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setdTDR(final double dTDR) {
            this.dTDR = dTDR;
            this.initialized[17] = true;
            return this;
        }

        /**
         * Sets the ICFR array for the builder.
         * @param daICFR double array as ICFR data
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setDaICFR(final double[] daICFR) {
            this.daICFR = daICFR;
            this.initialized[18] = true;
            return this;
        }

        /**
         * Sets the MR value for the builder.
         * @param microReflection double MR value
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setMicroReflection(final double microReflection) {
            this.microReflection = microReflection;
            this.initialized[19] = true;
            return this;
        }

        /**
         * Sets the severity string for the builder.
         * @param severity {@link String} value of the severity
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setSeverity(final String severity) {
            this.severity = severity;
            this.initialized[20] = true;
            return this;
        }

        /**
         * Helper setter for the signature data for the builder.
         * @param microReflection double MR value
         * @param severity {@link String} value of the severity
         * @return {@link Builder} so the setters can be chained
         */
        public Builder setSignature(
                final double microReflection,
                final String severity) {

            return this.setMicroReflection(microReflection).setSeverity(severity);
        }

        /**
         * Final builder method for building {@link TestPreEqStructure}.
         * @return {@link TestPreEqStructure} built data
         */
        public TestPreEqStructure build() {
            for (boolean b : initialized) {
                if (!b) throw PreEqException.TEST_ERROR;
            }
            return new TestPreEqStructure(this);
        }
    }
}
