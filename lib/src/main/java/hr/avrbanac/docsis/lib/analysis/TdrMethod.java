package hr.avrbanac.docsis.lib.analysis;

/**
 * Method used to estimate the delay of the dominant echo (micro-reflection) from the post-main pre-eq taps.
 *
 * @see PreEqAnalysis#getEchoEstimate(int, boolean, TdrMethod)
 */
public enum TdrMethod {

    /**
     * Default method. The complex taps are T-spaced samples of a response band-limited to the symbol rate, so the response between the
     * taps can be reconstructed with sinc (Whittaker-Shannon) interpolation. The main tap is removed first, the reconstructed post-main
     * response is searched on a fine grid and the peak is refined with a parabola on evenly spaced grid points. Uses magnitude and phase
     * of the taps and needs no special cases next to the main tap. Validated with synthetic single-echo strings (-15 to -30 dB, several
     * phases): error up to ~0.12 T for echoes between 0.9 and 1.5 T and up to ~0.08 T from 1.5 T on. Echoes estimated below 0.9 T are
     * reported as not resolved; true echo delays of 0.7-0.8 T may still be estimated at 0.9-1.0 T (error up to ~0.22 T).
     */
    BAND_LIMITED,

    /**
     * Method from CableLabs CM-GL-PNMP-V03, section 6.6.4: the strongest post-main tap and its two neighbours, magnitudes in dB, three-point
     * parabolic interpolation. The middle tap must be a local peak; for the first post-main tap the left neighbour is the main tap, so no
     * interpolation is possible and the echo is reported as not resolved (nominal 1 T); in practice this covers echoes up to ~1.5 T.
     * Coarser than {@link #BAND_LIMITED} (errors up to ~0.25 T), kept for comparability with the guideline.
     */
    CABLELABS_PARABOLIC
}
