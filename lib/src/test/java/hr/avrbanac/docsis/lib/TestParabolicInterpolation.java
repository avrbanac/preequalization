package hr.avrbanac.docsis.lib;

import org.apache.commons.math3.complex.Complex;

/**
 * Helper pojo wrapper class for testing parabolic interpolation data.
 */
public class TestParabolicInterpolation {
    private final Complex leftPoint;
    private final Complex middlePoint;
    private final Complex rightPoint;
    private final double expectedInterpolation;

    public TestParabolicInterpolation(
            final Complex leftPoint,
            final Complex middlePoint,
            final Complex rightPoint,
            final double expectedInterpolation) {

        this.leftPoint = leftPoint;
        this.middlePoint = middlePoint;
        this.rightPoint = rightPoint;
        this.expectedInterpolation = expectedInterpolation;
    }

    /**
     * Returns stored left point for parabolic interpolation input.
     * @return {@link Complex} left input point
     */
    public Complex getLeftPoint() {
        return leftPoint;
    }

    /**
     * Returns stored middle point for parabolic interpolation input.
     * @return {@link Complex} middle input point
     */
    public Complex getMiddlePoint() {
        return middlePoint;
    }

    /**
     * Returns stored right point for parabolic interpolation input.
     * @return {@link Complex} right input point
     */
    public Complex getRightPoint() {
        return rightPoint;
    }

    /**
     * Returns stored expected X coordinate of the interpolated point.
     * @return double value of the X coordinate of the interpolated point
     */
    public double getExpectedInterpolation() {
        return expectedInterpolation;
    }
}
