package hr.avrbanac.preequalization.lib.struct;

import hr.avrbanac.preequalization.lib.PreEqException;
import hr.avrbanac.preequalization.lib.util.ParseUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Default pre-equalization data class is a 24 energy tap implementation of the {@link PreEqData}. Coefficients of this class represent CM
 * complex coefficients F1 through F24 of the 24-tap linear transversal filter structure for the upstream equalizer.
 * </p>
 * <hr/>
 *
 * <h3>
 * PRE-EQ EXPLANATION (short version):
 * </h3>
 *
 * <p>
 * Each tap is coupled with delay element (for DOCSIS 2.0 and 3.0 - symbol period T; and for DOCSIS 1.1 it was T/2 and T/4).
 * In the ranging process the CM sends a ranging request message (RNG-REQ) to the CMTS, which uses a known portion of this message
 * (preamble) to determine the quality of the received signal and also the adjustments needed to better compensate the upstream distortion.
 * Thus, the response message is sent (RNG-RSP), containing a set of 24 coefficients and a parameter that defines whether it is a set
 * (initial ranging) or adjust instruction. This process can take multiple steps (adjustments). In the end adjustment instruction for all
 * taps (except main tap) is set to zero.
 * </p>
 *
 * <p>
 * RNG-RSP message is a TLV encoded message with following information: timing, frequency, power level, equalization adjustment, set
 * or adjust parameter and ranging status. For DOCSIS 2.0 and 3.0 equalization coefficients are identified as type 09 (DOCSIS 1.1 has
 * type 04). RNG-RSP and RNG-REQ messages are linked by the SID number (service ID) - present in both messages. Additionally, channel ID is
 * also provided.
 * </p>
 *
 * <p>
 * RNG-REQ message is a TLV encoded message with known portion of the message (preamble) which is used to determine needed adjustments.
 * Roughly, RNG-REQ message is divided into 3 parts: ramp-up with preamble, DOCSIS payload and FEC info/parity woth ramp-down. Docsis
 * payload part carries SID, channel ID and pending till complete info (used to inform CMTS that ranging adjustment is not yet completed).
 * </p>
 *
 * <p>
 * Historically, DOCSIS 1.0 and 1.1 had some problems with pre-eq but that is beyond the scope of this project.
 * </p>
 * <hr/>
 * <h3>
 * LIMITATION:
 * </h3>
 *
 * <p>
 * The maximum delay compensation that can be achieved using pre-eq is equal to the delay between adaptive equalizer's main tap and last
 * adaptive equalizer tap. For DOCSIS 2.0 and 3.0, the delay or spacing between each adaptive equalizer tap location is equal to the symbol
 * period. Typical implementations have main eq. tap in eight. position out of 24-tap delay line. This gives us max delay of 16T.
 * </p>
 *
 * <table>
 *     <tr>
 *         <th>Symbol Rate [MHz]</th>
 *         <th>Symbol Period (T) [msecs]</th>
 *         <th>16*T [msecs]</th>
 *     </tr>
 *     <tr>
 *         <td>5.12</td>
 *         <td>0.195</td>
 *         <td>3.125</td>
 *     </tr>
 *     <tr>
 *         <td>2.56</td>
 *         <td>0.391</td>
 *         <td>6.250</td>
 *     </tr>
 *     <tr>
 *         <td>1.28</td>
 *         <td>0.781</td>
 *         <td>12.500</td>
 *     </tr>
 * </table>
 *
 * <p>
 * Given values are approximates. Depending on the type of the micro-reflection and energy dissipation across all taps, final limits can be
 * lower than table values.
 * </p>
 * <hr/>
 * <h3>
 * SOURCE OF INFORMATION:
 * </hr>
 *
 * <ul>
 *     <li>DOCS-IF-MIB (RFC4546)</li>
 *     <li>DOCS-IF-3-MIB (DOCSISv3.0)</li>
 * </ul>
 *
 * <p>
 * Pre-equalization strings can be collected from equipment via SNMP.
 * </p>
 *
 * <table>
 *     <tr>
 *         <th>MIB</th>
 *         <th>Target</th>
 *         <th>Docsis</th>
 *         <th>Description</th>
 *     </tr>
 *     <tr>
 *         <td>docsIfCmtsCmStatusEqualizationData</td>
 *         <td>CMTS</td>
 *         <td>2.0</td>
 *         <td>provides the adjustment needed to update the CM coefficients</td>
 *     </tr>
 *     <tr>
 *         <td>docsIf3CmtsCmUsStatusTable</td>
 *         <td>CMTS</td>
 *         <td>3.0</td>
 *         <td>provides the adjustment (per channel) needed to update the CM coefficients</td>
 *     </tr>
 *     <tr>
 *         <td>docsIfCmStatusEqualizationData</td>
 *         <td>CM</td>
 *         <td>2.0</td>
 *         <td>indicates the current pre-distortion that is applied to the upstream</td>
 *     </tr>
 *     <tr>
 *         <td>docsIf3CmStatusUsTable</td>
 *         <td>CM</td>
 *         <td>3.0</td>
 *         <td>indicates the current pre-distortion (per ch.) that is applied to the upstream</td>
 *     </tr>
 * </table>
 */
public class DefaultPreEqData implements PreEqData {

    private static final int TAP_COUNT = 24;
    private static final int COEFFICIENT_PER_SYMBOL = 1;

    /**
     * Normalized pre-eq input string.
     */
    private final String preEqString;

    /**
     * Main tap index fetched from pre-eq header data. THIS IS AN ACTUAL INDEX, NOT AN ARRAY INDEX (use 1 less for array).
     */
    private final int mainTapIndex;

    /**
     * Parsed complex coefficients corresponding to 24 energy taps.
     */
    private final List<Coefficient> coefficients = new ArrayList<>();

    // The following are the key metrics:
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

    public DefaultPreEqData(final String rawInputPreEqString) {
        this.preEqString = rawInputPreEqString
                .toLowerCase()
                .replace(":","")
                .replace(" ","");

        if (this.preEqString.length() != (TAP_COUNT + 1) * 8) {
            throw PreEqException.STRING_MISMATCH_BYTE_SIZE;
        }

        byte[] bytes = ParseUtility.hexStringToByteArray(this.preEqString);
        this.mainTapIndex = bytes[0];

        if (bytes[1] != COEFFICIENT_PER_SYMBOL) {
            throw PreEqException.COEFFICIENT_PER_SYMBOL_MISMATCH;
        }

        if (bytes[2] != TAP_COUNT) {
            throw PreEqException.WRONG_TAP_COUNT;
        }

        for (int i = 4; i < bytes.length; i += 4) {
            coefficients.add(new DefaultCoefficient(Arrays.copyOfRange(bytes, i, i + 4), i / 4));
        }

        lMTE = coefficients.get(mainTapIndex - 1).getEnergy();
        lPreMTE = calculateEnergyForTaps(1, mainTapIndex - 1);
        lPostMTE = calculateEnergyForTaps(mainTapIndex + 1, TAP_COUNT);
        lTTE = lPreMTE + lMTE + lPostMTE;
        dMTC = 10 * Math.log10(1d * lTTE / lMTE);
        dMTR = 10 * Math.log10(1d * lMTE / (lPreMTE + lPostMTE));
        dNMTER = 10 * Math.log10(1d * (lPreMTE + lPostMTE) / lTTE);
        dPreMTTER = 10 * Math.log10(1d * lPreMTE / lTTE);
        dPostMTTER = 10 * Math.log10(1d * lPostMTE / lTTE);
        dPPESR = 10 * Math.log10(1d * lPreMTE / lPostMTE);
        dPPTSR = 10 * Math.log10(1d * coefficients.get(mainTapIndex - 2).getEnergy() / coefficients.get(mainTapIndex).getEnergy());

        lMTNA = Math.round(Math.pow(2, Math.ceil(Math.log(Math.sqrt(lTTE)) / Math.log(2))) - 1);
        lMTNE = lMTNA * lMTNA;
    }

    public long calculateEnergyForTaps(
            final int startTap,
            final int endTap) {

        long energy = 0L;
        for (int i = startTap - 1; i < endTap; i++) {
            energy += coefficients.get(i).getEnergy();
        }

        return energy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPreEqString() {
        return preEqString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Coefficient> getCoefficients() {
        return coefficients;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTapCount() {
        return TAP_COUNT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMainTapIndex() {
        return mainTapIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCoefficientPerSymbol() {
        return COEFFICIENT_PER_SYMBOL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMTE() {
        return lMTE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMTNA() {
        return lMTNA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMTNE() {
        return lMTNE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPreMTE() {
        return lPreMTE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPostMTE() {
        return lPostMTE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTTE() {
        return lTTE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMTC() {
        return dMTC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMTR() {
        return dMTR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNMTER() {
        return dNMTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPreMTTER() {
        return dPreMTTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPostMTTER() {
        return dPostMTTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPPESR() {
        return dPPESR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPPTSR() {
        return dPPTSR;
    }
}
