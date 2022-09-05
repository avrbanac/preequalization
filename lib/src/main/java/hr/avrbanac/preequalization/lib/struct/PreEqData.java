package hr.avrbanac.preequalization.lib.struct;

import java.util.List;

/**
 * This is the representation of the parsed pre-equalization string data. It extends pre-equalization header data with energy tap
 * coefficients.
 */
public interface PreEqData extends PreEqHeader {

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
     * Returns adaptive equalizer main tap energy.
     * @return long value of MTE
     */
    long getMTE();
}
