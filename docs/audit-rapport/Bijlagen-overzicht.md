# Bijlagen-overzicht — Auditrapport HTML Form Entry Module

Dit bestand is een koppeltabel voor de bijlagen van `Auditrapport.md` / `Auditrapport.docx`. Vul de kolom **Bestand/Link** in zodra je de definitieve bestanden hebt klaargezet (lokaal pad, repository-link, of bijlage-nummer in de PDF/Word-export). Werk de links in het hoofdrapport bij zodra dit overzicht compleet is.

> **Let op:** elke bijlage moet vanuit de hoofdtekst van het auditrapport worden gerefereerd. Een bijlage zonder referentie heeft geen auditwaarde (zie WS06-richtlijn).

---

## Bijlage A — Traceability matrix

| Vereiste | Bestand/Link |
|---|---|
| Traceability matrix (NEN-7510 control → maatregel → vóór → aanpassing → na) | `[ link naar Traceability-Matrix.md / .pdf ]` |

*Beschikbare bron in project:* `docs/overige/Traceability-Matrix.md`

---

## Bijlage B — SBOM (CycloneDX JSON)

| Vereiste | Bestand/Link |
|---|---|
| Volledige SBOM JSON (build-tijd, dagelijkse CI-run) | `[ link naar sbom.cyclonedx.json ]` |
| SBOM van de wekelijkse geplande scan (indien afwijkend) | `[ link ]` |

*Beschikbare bronnen in project:* `docs/sbom/sbom.cyclonedx.json`, `artifacts/sbom-cyclonedx-anchore/sbom.cyclonedx.json`

---

## Bijlage C — SAST- en SCA-output

| Vereiste | Bestand/Link |
|---|---|
| CodeQL SARIF-output (eindmeting) | `[ link naar java.sarif ]` |
| OSV-Scanner SARIF-output (eindmeting) | `[ link naar results.sarif ]` |
| Dependabot alerts log | `[ link / export ]` |
| JaCoCo coverage-rapport (per module) | `[ link naar index.html / jacoco.xml ]` |

*Beschikbare bronnen in project:* `artifacts/codeql-sarif/java.sarif`, `artifacts/OSV Scanner SARIF file/results.sarif`, `artifacts/coverage-report/`

---

## Bijlage D — Risicomatrix en security backlog

| Vereiste | Bestand/Link |
|---|---|
| Risicomatrix — initieel | `[ link naar RiskMatrixHtmlformentryInitial.png ]` |
| Risicomatrix — residueel | `[ link naar RiskMatrixHtmlformentryResidual.png ]` |
| Risicoanalyse (deel 1 en 2) | `[ link naar RisicoAnalyseDeel1.png / Deel2.png ]` |
| Security backlog (volledige lijst SB-1 t/m SB-20) | `[ link naar Security-Requirements.md ]` |
| Risk Assessment Report | `[ link naar RiskAssesmentRapport.md ]` |

*Beschikbare bronnen in project:* `docs/overige/Risicomatrix/`, `docs/overige/Security-Requirements.md`, `docs/overige/RiskAssesmentRapport.md`

---

## Bijlage E — Bow-tie diagrammen / threat model

| Vereiste | Bestand/Link |
|---|---|
| Bow-tie: Toegang patiëntgegevens | `[ link naar bow-tie-Toegang-Patientgegevens.png ]` |
| Bow-tie: Wijziging medische gegevens | `[ link naar bow-tie-Wijziging-medische gegevens.png ]` |
| Bow-tie: Gebruik externe softwarecomponenten | `[ link naar bow-tie-gebruik-externe-softwarecomponenten.png ]` |
| Bow-tie: Gebruikersaccounts toegang medische gegevens | `[ link naar bow-tie-gebruikersacounts-toegang-medische-gegevens.png ]` |
| Threat model (IriusRisk, C4-diagrammen) | `[ link naar ThreatModel.md + images/ ]` |

*Beschikbare bronnen in project:* `docs/overige/Bow-Ties/`, `docs/overige/Threat model/`

---

## Bijlage F — Penetratietestplan en onderbouwing

| Vereiste | Bestand/Link |
|---|---|
| Penetratietestplan (PT-01 t/m PT-07) | `[ link naar penentrationtestplan.md ]` |
| Onderbouwing niet-uitvoering pentest | `[ link naar Onderbouwing-Pentesten.md ]` |
| Screenshot installatiefout (Liquibase) | `[ link naar Module-error.png ]` |

*Beschikbare bronnen in project:* `docs/overige/Pentesting/`

---

## Bijlage G — Gap-analyse

| Vereiste | Bestand/Link |
|---|---|
| Algemene gap-analyse (8.16, 8.28, 8.8) | `[ link naar gapAnalyse.md ]` |
| Gap-analyse logging (8.15, attack surface-koppeling) | `[ link — zelfde bestand, aparte sectie ]` |

*Beschikbare bron in project:* `docs/overige/gapAnalyse.md`

---

## Bijlage H — AI-tooling verantwoording

> Niet automatisch gegenereerd — vul dit zelf in volgens het format uit de WS06-slides ("Wat ik aan AI heb gevraagd", "Wat de AI heeft gegenereerd", "Wat ik zelf heb gecontroleerd", "Beslissingen die ik zelf heb gemaakt").

| Vereiste | Bestand/Link |
|---|---|
| AI-tooling verantwoordingsdocument | `[ zelf op te stellen ]` |

---

## Niet meegenomen (per instructie van de gebruiker)

De volgende bijlagen uit de standaard WS06-structuur zijn **niet** opgenomen omdat ze niet van toepassing zijn op dit project:

- **CRA-mapping als losse bijlage** — de CRA-koppeling is al inhoudelijk verwerkt in hoofdstuk 5.5 van het auditrapport zelf.
- **Snyk-rapport** — er is geen Snyk gebruikt; de SCA-functie wordt vervuld door Dependabot + OSV-Scanner (zie Bijlage C).

Indien je deze later alsnog wilt toevoegen (bijvoorbeeld als je team alsnog Snyk inzet), voeg dan een rij toe aan de bijlagentabel in `Auditrapport.md` sectie 7 en aan dit overzicht.
