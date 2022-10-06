package hr.avrbanac.docsis.lib.struct;

import java.util.List;

/**
 * This is the representation of the parsed pre-equalization string data. It extends pre-equalization header data and metrics interface with
 * energy tap coefficients.
 */
public interface PreEqData extends PreEqHeader, PreEqMetrics {

    /**
     * Returns input pre-eq string in lower case without delimiters (whitespaces or colon)
     * @return {@link String} prepared input pre-eq string
     */
    String getPreEqString();

    /**
     * Returns list of energy tap coefficients.
     * @return {@link List} of {@link Coefficient}
     */
    List<Coefficient> getCoefficients();

    /**
     * Returns the lowest boundary (minimal possible value) of the tap energy ratio that the {@link Coefficient#getTapEnergyRatioBoundary()}
     * method can return. This method should just forward the same value unless the implementation has a superb reason to return some other
     * value.
     */
    double getTapEnergyRatioBoundary();

}
