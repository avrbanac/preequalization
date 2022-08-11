package hr.avrbanac.preequalization.lib.struct;

import java.util.List;

/**
 * This is the representation of the parsed pre-equalization string data. It extends pre-equalization header data with energy tap
 * coefficients.
 */
public interface PreEqualization extends PreEqHeader {

    /**
     * Returns list of energy tap coefficients.
     * @return {@link List} of {@link Coefficient}
     */
    List<Coefficient> getCoefficients();
}
