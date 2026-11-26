#!/usr/bin/env bash
# Re-downloads the reference documents into raw/ and extracts searchable text into text/.
# Both folders are git-ignored (third-party, copyrighted material). Requires curl, git, pdftotext (poppler).
set -u
cd "$(dirname "$0")"
mkdir -p raw text
UA="Mozilla/5.0 (X11; Linux x86_64) Gecko/20100101 Firefox/130.0"

dl() { # <file> <url>
  if [ -s "raw/$1" ]; then echo "skip $1"; return; fi
  curl -sSL --http1.1 -A "$UA" --max-time 120 -o "raw/$1" "$2" && echo "ok   $1" || echo "FAIL $1 <- $2"
}

dl CM-GL-PNMP-V03-160725.pdf                 https://volpefirm.com/wp-content/uploads/2017/01/CM-GL-PNMP-V03-160725.pdf
dl CM-GL-PNM-3.1-V02-210114.pdf              https://account.cablelabs.com/server/alfresco/c021877d-cce7-434a-b3ac-9cc466d6c601
dl CM-SP-PHYv3.0-I09-101008.pdf              https://account.cablelabs.com/server/alfresco/887e1bcb-0bf0-40d1-aaae-65f254078670
dl rfc4546.txt                               https://www.rfc-editor.org/rfc/rfc4546.txt
dl DOCS-IF3-MIB.mib                          https://mibs.cablelabs.com/MIBs/DOCSIS/DOCS-IF3-MIB.mib
dl scte-Volpe-paper.pdf                      https://www.scte.org/documents/6852/Volpe_Paper.pdf
dl scte-FTF22-Hranac.pdf                     https://www.scte.org/documents/5732/FTF22_OP03_Hranac_3885.pdf
dl cablelabs-PNM-webinar-2017.pdf            https://www.cablelabs.com/wp-content/uploads/2017/12/Proactive-Network-Maintenance-Webinar.pdf
dl cablelabs-60177.pdf                       https://www.cablelabs.com/wp-content/uploads/2014/04/60177-published.pdf
dl cablelabs-60203.pdf                       https://www.cablelabs.com/wp-content/uploads/2014/04/60203-published.pdf
dl patent-US8861577-fault-id-preeq.pdf       https://image-ppubs.uspto.gov/dirsearch-public/print/downloadPdf/8861577
dl patent-US9735867-MR-delay-estimation.pdf  https://image-ppubs.uspto.gov/dirsearch-public/print/downloadPdf/9735867
dl broadbandlibrary-how-adaptive-equalization-works.html https://broadbandlibrary.com/how-adaptive-equalization-works/

[ -d raw/mrsjk-preeq ] || git clone -q --depth 1 https://github.com/mrsjk/preeq.git raw/mrsjk-preeq

for f in raw/*.pdf; do
  out="text/$(basename "${f%.pdf}").txt"
  [ -s "$out" ] || pdftotext -layout "$f" "$out"
done
echo "done"
