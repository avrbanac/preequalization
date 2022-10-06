package hr.avrbanac.docsis.lib;

import hr.avrbanac.docsis.lib.struct.Coefficient;

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

    public Coefficient getCoefficient() {
        return coefficient;
    }

    public int getExpectedReal() {
        return expectedReal;
    }

    public int getExpectedImag() {
        return expectedImag;
    }
}
