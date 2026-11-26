# Reference documentation

Background material collected (2026-11-26) for fixing and evolving the PreEq library.

The documents themselves are **not committed** (third-party, copyrighted). Run `./fetch.sh` to download them into `raw/` and
extract searchable text into `text/` (needs `curl`, `git`, `pdftotext`). Both folders are git-ignored.

## Documents

| Priority | File (in `raw/`) | What it is | Use for |
|:--:|---|---|---|
| ★★★ | `CM-GL-PNMP-V03-160725.pdf` | CableLabs, *PNM Best Practices: HFC Networks (DOCSIS 3.0)*, V03, 2016-07-25, 180 pp. Third-party mirror (volpefirm.com) of the official guideline. | **Primary source** for nearly everything the lib does: decoding, key metrics, micro-reflection, interpolation, severity, CM matching. |
| ★★☆ | `rfc4546.txt` | IETF RFC 4546, DOCSIS 2.0 RF MIB (`DOCS-IF-MIB`). | Authoritative `DocsEqualizerData` byte layout (header + coefficients). |
| ★★☆ | `DOCS-IF3-MIB.mib` | CableLabs DOCSIS 3.0 MIB (current). | `docsIf3CmStatusUsEqData` / `docsIf3CmtsCmUsStatusEqData` (per-upstream-channel EQ data, same format). |
| ★★☆ | `CM-SP-PHYv3.0-I09-101008.pdf` | CableLabs DOCSIS 3.0 PHY spec, I09. | §6.2.15/16 *Transmit Pre-Equalizer*: filter structure, tap spacing, coefficient normalization. |
| ★★☆ | `CM-GL-PNM-3.1-V02-210114.pdf` | CableLabs *Primer for PNM Best Practices (DOCSIS 3.1)*, V02, 2021. | Future evolution: OFDMA pre-equalization, D3.1 PNM data formats. |
| ★☆☆ | `scte-Volpe-paper.pdf` | B. Volpe, *DOCSIS Pre-Equalization: Vastly Powerful, Often Undervalued* (SCTE). | Readable overview; practical interpretation of the metrics. |
| ★☆☆ | `scte-FTF22-Hranac.pdf` | R. Hranac & J. Rupe, *How Will PNM Change Under DOCSIS 4.0?* (SCTE 2022). | Roadmap context (D3.1/D4.0). |
| ★☆☆ | `cablelabs-PNM-webinar-2017.pdf` | SCTE/CableLabs PNM webinar slides (2017). | Overview slides. |
| ★☆☆ | `cablelabs-60177.pdf`, `cablelabs-60203.pdf` | CableLabs invention disclosures: pre-eq based PNM process model (single / multiple US channels). | Original idea; multi-channel considerations. |
| ★☆☆ | `patent-US9735867-MR-delay-estimation.pdf` | US 9,735,867 *Microreflection delay estimation in a CATV network*. Scanned (no text layer). | Alternative MR delay algorithm (relevant to `Signature` delay). |
| ★☆☆ | `patent-US8861577-fault-id-preeq.pdf` | US 8,861,577 *Fault identification using pre-equalization coefficients*. Scanned. | Fault classification ideas. |
| ★☆☆ | `broadbandlibrary-how-adaptive-equalization-works.html` | R. Hranac, Broadband Library article. | Tutorial on adaptive EQ / taps. |
| ★☆☆ | `mrsjk-preeq/` | Open-source Perl parser for DOCS-IF3 pre-eq data (GitHub `mrsjk/preeq`). | Independent implementation to compare decoding/FFT against. |

Not obtained: the original 2008 Cable-Tec Expo paper (Campos, Cardona, Raman, *Pre-equalization Based Pro-active Network
Maintenance Methodology*), NCTA 2009 *Optimizing Upstream Throughput Using Equalization Coefficient Analysis* (download page
only returns HTML), the DOCSIS 3.0 MULPI spec (RNG-RSP TLV definition). The RFC 4546 text covers the relevant format.

## Where to look in CM-GL-PNMP-V03 (the main reference)

| Section (page) | Topic | Lib counterpart |
|---|---|---|
| 5.2.1 (27) | Micro-reflection types | `Signature`, TDR |
| 5.5 (37) | Limits of pre-eq compensation | `ChannelWidth.maxDelay` |
| 5.6 / 5.6.1 (38-39) | Pre-eq MIBs, data structure | `DefaultPreEqData` header parsing |
| 6.2 (42), Fig. 14, Table 3 | Format, vendor max amplitudes (2047/1023/511) | `DefaultPreEqData`, `calculateMTNA` |
| 6.2.1-6.2.3 (43) | 4-nibble / 3-nibble 2's complement, universal decoding | `DefaultCoefficient.calculateValue`, `is3NibbleEncoding` |
| 6.3.1-6.3.11 (44-46) | MTE, MTNA/MTNE, Pre/PostMTE, TTE, MTC, MTR, NMTER, PreMTTER, PostMTTER, PPESR, PPTSR | `PreEqMetrics` / `DefaultPreEqData` |
| 6.3.12 (46-51) | Group delay distortion | not implemented |
| 6.6.2 (62) | Micro-reflection signature = (level, delay); delay is round trip (2T), distance uses half | `Signature`, `getTDR` |
| 6.6.4 (65) | Parabolic interpolation (3-point, dB values); worked example 9/10/11 → x = 9.8125 | `MathUtility.ParabolicInterpolation.V1`; it is the `TestBook` interpolation case |
| 6.7 (67-71), Tables 16-18 | Severity thresholds: new PNM 22/11, established 25/18, lower QAM 32/25 (MTR dB) | `MicroReflectionSeverity(+Threshold)` |
| 6.7.2 (71) | Static MR severity: ≤ -25 dBc green, ≥ -18 dBc red | `Signature.calculateMicroReflectionSeverity` |
| 6.7.5-6.7.6 (74-80), Table 21 | **Signature matching / CM correlation**: FD division, 32-point IFFT, match if MTR of quotient ≥ threshold (18-25 dB), match matrix, zero close-in taps for in-home echoes | `CorrelationBond`: roadmap item "correlation algorithm" |
| 7.3 (101) | Time response from IFFT without phase data | possible TDR alternative |
| App. IV (162) | Micro-reflection calculator: ripple ↔ VSWR ↔ Γ ↔ dBc | `Signature.calculateMicroReflection` |
| App. VI (166) | Reference software pipeline (parse → normalize → FFT → analyzers vs threshold tables) | overall lib architecture |
| App. VIII (169) | CableLabs TDR (IFFT of magnitude with zero phase) | `getTDR` |

## Cross-check: lib vs. documentation

These are findings from comparing the lib code with the sources above. They are input for the bug-fixing phase.

**Matches the documentation**
- Key-metric formulas (MTC, MTR, NMTER, PreMTTER, PostMTTER, PPESR, PPTSR) are identical to PNMP §6.3.
- `Signature.calculateMicroReflection` is exactly App. IV (VSWR = √(10^(ripple/10)), MR = 10·log10(Γ²)).
- `ParabolicInterpolation.V1` is exactly §6.6.4. The test value 9.8125 is the guideline's own example.
- Severity thresholds (-22/-25/-32 and -11/-18/-25) correspond to Tables 16-18 and §6.7.2.
- TDR divides the delay by 2 (round trip), consistent with §6.6.2.

**Discrepancies and bugs to address**
1. **[fixed in v0.4.1] Nibble-format detection** (`DefaultPreEqData.is3NibbleEncoding`). The intent (PNMP §6.2.2) is "use 3-nibble only if the first
   nibble of every value is 0". The code masks a sign-extended `byte` with `0xF000`, so it only detects high bytes ≥ 0x80. A value
   such as `0x1234` or `0x7FFF` (MTNA 32767 modems) is treated as 3-nibble, and `0x7FFF` then decodes as **negative**. The
   correct check is `(bytes[i] & 0xF0) != 0`.
2. **[fixed in v0.4.1] Header byte 3 is not reserved.** Per RFC 4546 it is the number of *reverse* taps `m`. Byte 1 is forward taps per symbol
   (1, 2 or 4), byte 2 is forward tap count `n`. Total length = 4 + 4·(n+m), max 64 taps (260 bytes). The lib ignores byte 3
   and hardcodes 200 hex chars. That is fine for D2.0/3.0 24-tap T-spaced data (m = 0), but byte 3 should at least be validated
   to be 0.
3. **[fixed in v0.4.1] Main tap position.** PNMP §6.3.1: typically 8, but "some CMTS implementations can have it as low as the sixth position"
   (DOCSIS 1.1: 4). The lib reads the index from the header, which is correct. PPTSR/TDR edge cases (main tap at 1 or 24)
   should fail cleanly. `bytes[0]` is a signed byte with no range check against 1..tapCount.
4. **Universal decoding vs. MTNA > 2047.** PNMP §6.2.3 says the first nibble can always be dropped because the maximum is ≤ 2047.
   The 2016 guideline predates modems with MTNA 32767, which the lib already handles (CHANGELOG v0.1.0). Keep 4-nibble as the
   default and add test strings for 511, 1023, 2047 and 32767 amplitudes.
5. **FFT input layout.** PNMP §6.7.6.2 zero-pads 24 → 32 and barrel-rotates the main tap to index 0. The lib uses
   `FIRST_TAP_FIRST_POINT` plus an output rotation. Equivalent for the magnitude, but worth aligning when implementing matching
   (which needs complex FD values).
6. **Group delay metrics** (§6.3.12) are not implemented.

6b. **[fixed in v0.5.0] TDR / echo delay.** Pre-0.5.0 TDR interpolated over a 2-tap gap next to the main tap (~2x too short);
   replaced by band-limited interpolation, validated with ground-truth synthetic echoes. The §6.6.4 method is kept as
   `TdrMethod.CABLELABS_PARABOLIC`.

**Roadmap material**
- Correlation / signature matching: §6.7.5-6.7.6 gives the full procedure (FD division method and time-domain subtraction
  method, thresholds, match matrix, grouping).
- DOCSIS 3.1 OFDMA pre-equalization: `CM-GL-PNM-3.1-V02-210114.pdf`.
