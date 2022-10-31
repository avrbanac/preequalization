package hr.avrbanac.docsis.lib;

import hr.avrbanac.docsis.lib.struct.Coefficient;

/**
 * Helper pojo wrapper class for coefficient structure test.
 */
public class TestCoefficientStructure {
    private final Coefficient coefficient;
    private final int expectedReal;
    private final int expectedImag;

    public TestCoefficientStructure(
            final Coefficient coefficient,
            final int expectedReal,
            final int expectedImag) {

        this.coefficient = coefficient;
        this.expectedReal = expectedReal;
        this.expectedImag = expectedImag;
    }

    /**
     * Returns stored pre-eq coefficient.
     * @return {@link Coefficient} stored coefficient
     */
    public Coefficient getCoefficient() {
        return coefficient;
    }

    /**
     * Returns stored int value of the real part of the provided coefficient.
     * @return int parsed value of the real part of the provided coefficient
     */
    public int getExpectedReal() {
        return expectedReal;
    }

    /**
     * Returns stored int value of the imaginary part of the provided coefficient.
     * @return int parsed value of the imaginary part of the provided coefficient
     */
    public int getExpectedImag() {
        return expectedImag;
    }
}
