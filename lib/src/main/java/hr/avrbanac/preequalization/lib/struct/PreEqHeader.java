package hr.avrbanac.preequalization.lib.struct;

/**
 * Pre-equalization header contains metadata for provided pre-equalization string.
 */
public interface PreEqHeader {

    /**
     * Returns the number of coefficients for the provided pre-equalization string.
     * @return integer number of energy taps (coefficients)
     */
    int getTapCount();

    /**
     * Returns the index of the main energy tap.
     * @return integer index of the main energy tap
     */
    int getMainTapIndex();

    /**
     * Returns number of coefficients per symbol.
     * @return integer number of coefficients per symbol
     */
    int getCoefficientPerSymbol();
}
