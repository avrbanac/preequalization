package hr.avrbanac.docsis.lib;

import org.apache.commons.math3.complex.Complex;

public class TestParabolicInterpolation {
    private final Complex leftPoint;
    private final Complex middlePoint;
    private final Complex rightPoint;
    private final Complex expectedInterpolation;

    public TestParabolicInterpolation(
            final Complex leftPoint,
            final Complex middlePoint,
            final Complex rightPoint,
            final Complex expectedInterpolation) {

        this.leftPoint = leftPoint;
        this.middlePoint = middlePoint;
        this.rightPoint = rightPoint;
        this.expectedInterpolation = expectedInterpolation;
    }

    public Complex getLeftPoint() {
        return leftPoint;
    }

    public Complex getMiddlePoint() {
        return middlePoint;
    }

    public Complex getRightPoint() {
        return rightPoint;
    }

    public Complex getExpectedInterpolation() {
        return expectedInterpolation;
    }
}
