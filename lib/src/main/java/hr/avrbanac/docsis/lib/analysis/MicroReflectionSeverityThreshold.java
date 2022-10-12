package hr.avrbanac.docsis.lib.analysis;

/**
 * <p>
 * MicroReflectionSeverityThreshold enumeration has 3 distinct levels. This is due to the idea that different HFC providers need to
 * implement different thresholds. For networks new to the PNM idea, "low" thresholds should be used, while established networks with short
 * amplifier cascades may want to stick with CableLabs recommendations ("medium" thresholds). High thresholds should be considered when
 * modems switch to more robust modulation type such as 16-QAM (high noise?). This is a clear sign that something is broken, however metric
 * must change in pace with less demanding modulation type, or problem could be concealed.
 * </p>
 * <p>
 *     <table>
 *         <tr>
 *             <th>Threshold Category</th>
 *             <th>Enumeration Value</th>
 *             <th>Level</th>
 *             <th>Note</th>
 *         </tr>
 *         <tr>
 *             <td rowspan="2">LOW</td>
 *             <td>LOW</td>
 *             <td>-1</td>
 *             <td rowspan="2">For use with newly established networks (or new to the PNM idea)</td>
 *         </tr>
 *         <tr>
 *             <td>NEW_PNM</td>
 *             <td>-1</td>
 *         </tr>
 *         <tr>
 *             <td rowspan="4">MEDIUM</td>
 *             <td>MEDIUM</td>
 *             <td>0</td>
 *             <td rowspan="4">These default values are CableLabs recommendations.</td>
 *         </tr>
 *         <tr>
 *             <td>DEFAULT</td>
 *             <td>0</td>
 *         </tr>
 *         <tr>
 *             <td>CABLE_LABS</td>
 *             <td>0</td>
 *         </tr>
 *         <tr>
 *             <td>ESTABLISHED</td>
 *             <td>0</td>
 *         </tr>
 *         <tr>
 *             <td rowspan="3">HIGH</td>
 *             <td>HIGH</td>
 *             <td>1</td>
 *             <td rowspan="3">For use with more robust QAM.</td>
 *         </tr>
 *         <tr>
 *             <td>ROBUST_QAM</td>
 *             <td>1</td>
 *         </tr>
 *         <tr>
 *             <td>LOWERED_QAM</td>
 *             <td>1</td>
 *         </tr>
 *     </table>
 * </p>
 */
public enum MicroReflectionSeverityThreshold {
    // LOW threshold levels for use with newly established networks (new to the PNM idea).
    LOW(-1),
    NEW_PNM(-1),

    // MEDIUM threshold levels for use with established networks. These thresholds are CableLabs recommendations, thus a default choice.
    MEDIUM(0),
    DEFAULT(0),
    CABLE_LABS(0),
    ESTABLISHED(0),

    // HIGH threshold levels for use with lowered QAM situations. These thresholds should be used not to mask problems in such situations.
    HIGH(1),
    ROBUST_QAM(1),
    LOWERED_QAM(1);

    private final int thresholdLevel;

    MicroReflectionSeverityThreshold(final int thresholdLevel) {
        this.thresholdLevel = thresholdLevel;
    }

    public int getThresholdLevel() {
        return thresholdLevel;
    }
}
