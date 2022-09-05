package hr.avrbanac.preequalization.lib.struct;

import hr.avrbanac.preequalization.lib.PreEqException;
import hr.avrbanac.preequalization.lib.util.ParseUtility;

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
 * Each tap is coupled with delay element (for docsis 2.0 and 3.0 - symbol period T; and for docsis 1.1 it was T/2 and T/4).
 * In the ranging process the CM sends a ranging request message (RNG-REQ) to the CMTS, which uses a known portion of this message
 * (preamble) to determine the quality of the received signal and also the adjustments needed to better compensate the upstream distortion.
 * Thus, the response message is sent (RNG-RSP), containing a set of 24 coefficients and a parameter that defines whether it is a set
 * (initial ranging) or adjust instruction. This process can take multiple steps (adjustments). In the end adjustment instruction for all
 * taps (except main tap) is set to zero.
 * </p>
 *
 * <p>
 * RNG-RSP message is a TLV encoded message with following information: timing, frequency, power level, equalization adjustment, set
 * or adjust parameter and ranging status. For docsis 2.0 and 3.0 equalization coefficients are identified as type 09 (docsis 1.1 has
 * type 04). RNG-RSP and RNG-REQ messages are linked by the SID number (service ID) - present in both messages. Additionally, channel ID is
 * also provided.
 * </p>
 *
 * <p>
 * RNG-REQ message is a TLV encoded message with known portion of the message (preamble) which is used to determine needed adjustments.
 * Roughly, RNG-REQ message is divided into 3 parts: ramp-up with preamble, docsis payload and FEC info/parity woth ramp-down. Docsis
 * payload part carries SID, channel ID and pending till complete info (used to inform CMTS that ranging adjustment is not yet completed).
 * </p>
 *
 * <p>
 * Historically, docsis 1.0 and 1.1 had some problems with pre-eq but that is beyond the scope of this project.
 * </p>
 * <hr/>
 * <h3>
 * LIMITATION:
 * </h3>
 *
 * <p>
 * The maximum delay compensation that can be achieved using pre-eq is equal to the delay between adaptive equalizer's main tap and last
 * adaptive equalizer tap. For docsis 2.0 and 3.0, the delay or spacing between each adaptive equalizer tap location is equal to the symbol
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

    private final byte[] bytes;
    private final int mainTapIndex;

    public DefaultPreEqData(final String preEqString) {
        preEqString
                .replace(":","")
                .replace(" ","");

        if (preEqString.length() != (TAP_COUNT + 1) * 8) {
            throw PreEqException.STRING_MISMATCH_BYTE_SIZE;
        }

        this.bytes = ParseUtility.hexStringToByteArray(preEqString);
        this.mainTapIndex = bytes[0];

        if (bytes[1] != COEFFICIENT_PER_SYMBOL) {
            throw PreEqException.COEFFICIENT_PER_SYMBOL_MISMATCH;
        }

        if (bytes[2] != TAP_COUNT) {
            throw PreEqException.WRONG_TAP_COUNT;
        }
    }

    @Override
    public List<Coefficient> getCoefficients() {
        return null;
    }

    @Override
    public int getTapCount() {
        return TAP_COUNT;
    }

    @Override
    public int getMainTapIndex() {
        return mainTapIndex;
    }

    @Override
    public int getCoefficientPerSymbol() {
        return COEFFICIENT_PER_SYMBOL;
    }

}
