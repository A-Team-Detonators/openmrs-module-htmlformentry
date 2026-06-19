# Auditrapport — OpenMRS HTML Form Entry Module

**Module:** htmlformentry (HTML Form Entry)
**Versie:** 3.10.0
**Repository:** github.com/A-Team-Detonators/openmrs-module-htmlformentry
**Normenkader:** NEN-7510:2024 Deel 2
**Rapportdatum:** 19 juni 2026
**Auditvorm:** Zelfevaluatie (interne audit door projectteam C4-Detonators)
**Opgesteld door:** [naam / teamnaam invullen]

---

## Inhoudsopgave

1. [Executive Summary](#1-executive-summary)
2. [Scope en Context](#2-scope-en-context)
3. [Audit Methodologie](#3-audit-methodologie)
4. [Risico-analyse en Bevindingen](#4-risico-analyse-en-bevindingen)
5. [SBOM en Supply Chain Security](#5-sbom-en-supply-chain-security)
6. [Conclusie en Advies](#6-conclusie-en-advies)
7. [Bijlagen](#7-bijlagen)

---

## 1. Executive Summary

**Status: 🟠 Oranje — significante bevindingen aanwezig, risico beheerst maar verbetering nodig**

De HTML Form Entry-module van OpenMRS is onderzocht op naleving van NEN-7510:2024-2. Deze module verwerkt patiëntgegevens, medische observaties en diagnoses, en vormt daarmee een kritiek onderdeel van het elektronisch patiëntendossier. De belangrijkste openstaande risico's zijn inmiddels gemitigeerd, maar een aantal aandachtspunten vraagt nog opvolging voordat de module zonder voorbehoud productierijp is.

**Top 3 risico's voor de organisatie**

1. **Ontbrekende audittrail bij inzage en wijziging van patiëntdossiers.** Tot voor kort kon niet worden vastgesteld wie welk patiëntdossier had ingezien of gewijzigd. Hierdoor kon de organisatie niet aantonen te voldoen aan de AVG-verantwoordingsplicht. Deze bevinding is inmiddels opgelost door audit-logging toe te voegen en met tests te borgen.
2. **Verouderde softwarecomponenten met bekende kritieke kwetsbaarheden.** In de gebruikte testketen zijn componenten aangetroffen met kritieke kwetsbaarheden (waaronder een kritieke Log4j-kwetsbaarheid en pad manipulatie-kwetsbaarheden in de OpenMRS-kernsoftware). Een aanvaller zou deze in theorie kunnen misbruiken om toegang te krijgen tot het systeem. Een deel hiervan is buiten de invloedssfeer van het projectteam (OpenMRS-kern), de overige onderdelen raken alleen de testomgeving en niet de productiecode.
3. **Geen praktische penetratietest uitgevoerd.** Door technische installatieproblemen kon de werking van de module in de praktijk niet end-to-end worden getest door een aanvaller-perspectief. Hierdoor steunt dit rapport op codeanalyse en geautomatiseerde scans, niet op een praktijkproef.

**Geprioriteerde roadmap**

| Prioriteit      | Actie                                                                                                                       | Termijn           |
| --------------- | --------------------------------------------------------------------------------------------------------------------------- | ----------------- |
| **Nu**          | Resterende SAST-aandachtspunten (null-checks, resource leaks) beoordelen en triagen                                         | Direct            |
| **Deze sprint** | Inputvalidatie en autorisatiecontroles centraal en aantoonbaar maken; testdekking van de hoofdmodule verhogen               | Lopende sprint    |
| **Later**       | Praktische penetratietest uitvoeren zodra een werkende testomgeving beschikbaar is; CRA-meldproces (ENISA) formeel beleggen | Volgende kwartaal |

De module toont een duidelijke positieve ontwikkeling: een eerdere gap-analyse op logging is opgevolgd met concrete code-aanpassingen en automatische tests, de CI/CD-pijplijn bevat inmiddels CodeQL, dependency-scanning en een SBOM-generator, en de laatste scans laten geen actieve SAST- of supply-chain-bevindingen meer zien in de productiecode. Met de hierboven genoemde vervolgacties kan de module richting een groene status bewegen.

---

## 2. Scope en Context

### 2.1 Wat is beoordeeld

| Onderdeel  | Beschrijving                                                |
| ---------- | ----------------------------------------------------------- |
| Module     | HTML Form Entry (`htmlformentry`)                           |
| Versie     | 3.10.0                                                      |
| Repository | `github.com/A-Team-Detonators/openmrs-module-htmlformentry` |
| Platform   | OpenMRS (ondersteunde platformversies: 1.9, 1.10, 2.0, 2.2) |
| Taal       | Java (Maven multi-module project), circa 45.033 regels code |

### 2.2 Beoordelingsperiode

De audit bestrijkt het volledige projecttraject (workshops WS01 tot en met WS06), met als eindmeting de scanresultaten en codestatus van **18-19 juni 2026**.

### 2.3 Testomgeving

De beoordeling is uitgevoerd op:

- **Statische code-analyse:** rechtstreeks op de broncode in de `dev`-branch, los van een draaiende omgeving.
- **CI/CD-pijplijn:** GitHub Actions binnen de organisatie A-Team-Detonators (Ontwikkel-fase van het OTAP-model: `dev` → `test` → `acceptation` → `production`).
- **Dynamische test (pentest):** niet uitgevoerd — zie 2.5.

### 2.4 Wat is buiten scope

- **Productie-infrastructuur** van OpenMRS-implementaties bij daadwerkelijke zorginstellingen.
- **OpenMRS-kernplatform (`openmrs-web`, `openmrs-core`)** zelf, behalve waar de module er direct gebruik van maakt. Kwetsbaarheden in OpenMRS-kerncode worden benoemd, maar vallen onder verantwoordelijkheid van het OpenMRS-project.
- **Overige OpenMRS-modules** die niet door het projectteam zijn ontwikkeld of aangepast.
- **Netwerkinfrastructuur, fysieke beveiliging en externe koppelingen** (bijv. apotheeksystemen).

### 2.5 Wat is niet getest, en waarom

Er is een penetratietestplan opgesteld [(zie Bijlage F)](#7-bijlagen) met zeven geplande testscenario's (autorisatie, formulierdefinitie-manipulatie, XSS, SQL-injectie, verouderde componenten, beschikbaarheid, integriteit). De daadwerkelijke uitvoering van deze tests is **niet gelukt**: bij het opzetten van een werkende OpenMRS + htmlformentry-testomgeving trad consistent een Liquibase-foutmelding op tijdens de database-migratie, ook na meerdere installatiepogingen met verschillende OpenMRS- en Java-versies (in totaal circa 1,5–2 dagen besteed). Na afstemming met de docent is besloten de beschikbare tijd in te zetten op codeanalyse in plaats van verdere troubleshooting. Dit betekent dat de bevindingen in dit rapport zijn gebaseerd op **statische analyse (SAST/SCA/code review)**, niet op dynamisch geverifieerd aanvallers gedrag. Dit is een erkende beperking van de huidige auditcyclus (zie Conclusie en Advies).

### 2.6 Normenkader en wetgeving

| Kader                           | Toepassing                                                                                                                |
| ------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| **NEN-7510:2024 Deel 2**        | Primair normenkader voor deze audit                                                                                       |
| **AVG (GDPR)**                  | Relevant voor verwerking van medische persoonsgegevens en de verantwoordingsplicht (art. 30)                              |
| **Cyber Resilience Act (CRA)**  | Aanvullend van toepassing omdat de module een "product met digitale elementen" is; zie sectie 5.4 voor de CRA-koppeling   |
| **NIS2 / Cyberbeveiligingswet** | Niet direct getoetst in deze audit; relevant voor de zorginstelling die de software exploiteert, niet voor de module zelf |

---

## 3. Audit Methodologie

### 3.1 Aanpak

De audit is uitgevoerd als zelfevaluatie door het projectteam, met als doel aan te tonen in hoeverre de module voldoet aan NEN-7510:2024-2. De aanpak volgde de structuur: gap-analyse → risicoanalyse → mitigatie → hertest → rapportage, zoals toegepast in de voorgaande workshops van dit traject.

### 3.2 Gebruikte technieken en tools

| Techniek                  | Tool                                                          | Wanneer                                                       | Resultaat                                             |
| ------------------------- | ------------------------------------------------------------- | ------------------------------------------------------------- | ----------------------------------------------------- |
| SAST                      | GitHub CodeQL (GitHub Actions)                                | Bij elke pull request + wekelijks gepland + eindmeting        | SARIF-rapport, zie [Bijlage C](#7-bijlagen)           |
| SCA / dependency-scanning | Dependabot + Dependency Review Action + OSV-Scanner           | Wekelijks + bij elke CI-run + eindmeting                      | Alerts/SARIF, zie [Bijlage C](#7-bijlagen)            |
| SBOM-generatie            | CycloneDX (anchore/sbom-action)                               | Build-tijd, bij elke CI-run                                   | `sbom.cyclonedx.json`, zie [Bijlage B](#7-bijlagen)   |
| Codekwaliteit             | SonarQube Cloud                                               | Bij elke CI-run                                               | Code smells, bugs, coverage-koppeling                 |
| Testdekking               | JaCoCo + Maven Surefire                                       | Bij elke CI-run                                               | Coverage-rapport, zie [Bijlage C](#7-bijlagen)        |
| Code review               | Handmatig (peer review via verplichte PR-reviews)             | Doorlopend, WS05                                              | PR-commentaar, CODEOWNERS-toewijzing                  |
| Risicoanalyse             | ISO 27005-methode (kans × impact, kwalitatief + kwantitatief) | WS03                                                          | Risicomatrix, zie [Bijlage D](#7-bijlagen)            |
| Threat modeling           | IriusRisk (C4-model: System Context, Container, Component)    | WS03                                                          | Threat model, zie [Bijlage E](#7-bijlagen)            |
| Bow-tie analyse           | Handmatig, gekoppeld aan NEN-7510-controls                    | WS03                                                          | Bow-tie diagrammen, zie [Bijlage E](#7-bijlagen)      |
| Penetratietest            | OWASP Web Security Testing Guide + OWASP Top 10 (gepland)     | Niet uitgevoerd — zie [2.5](#25-wat-is-niet-getest-en-waarom) | Testplan + onderbouwing, zie [Bijlage F](#7-bijlagen) |

### 3.3 Toelichting CI/CD-inrichting

De CI/CD-pijplijn draait bij elke push en pull request op de branches `dev`, `test`, `acceptation` en `production` en voert build, test en security-scanning gescheiden van elkaar uit. Branch protection rules voorkomen directe pushes naar beschermde branches; wijzigingen verlopen verplicht via pull requests met minimaal één review goedkeuring (CODEOWNERS-gestuurd) en zonder zelf-goedkeuring (vier-ogenprincipe). Build- en scan-artefacten worden 90 dagen bewaard conform de NEN-7510-bewaarplicht voor auditeerbaarheid; SBOM's uit de wekelijkse geplande scan worden 365 dagen bewaard. De `omod`-module wordt om technische redenen (afhankelijkheid van een niet-bereikbare OpenMRS-Maven-repository binnen GitHub Actions) uitgesloten van de geautomatiseerde build; dit risico is als laag beoordeeld omdat de `omod` uitsluitend een packaging-wrapper is zonder eigen businesslogica — alle daadwerkelijke code wordt wel gebouwd, getest en gescand.

### 3.4 Omgang met false positives

Bevindingen van CodeQL en OSV-Scanner worden eerst handmatig geverifieerd in de context van het daadwerkelijke gebruik door de module, voordat ze als false positive worden gemarkeerd. Bevindingen worden nooit zonder onderbouwing genegeerd; zie [Bijlage C](#7-bijlagen) voor de volledige onderbouwing per bevinding (bijvoorbeeld de CodeQL-meldingen over command-line constructie en log-injectie, die na analyse als laag risico of false positive zijn beoordeeld met expliciete motivatie).

---

## 4. Risico-analyse en Bevindingen

### 4.1 Samenvatting bevindingen

| #     | Bevinding                                                                 | Ernst                | NEN-7510                             | Status                                  |
| ----- | ------------------------------------------------------------------------- | -------------------- | ------------------------------------ | --------------------------------------- |
| B-001 | Ontbrekende audit-logging bij inzage/wijziging patiëntgegevens            | Hoog (contextueel)   | 8.15 Log-registratie                 | **Opgelost**                            |
| B-002 | SQL-injectie via string-concatenatie in database query's                  | Kritiek              | 8.28 Veilig programmeren             | **Opgelost**                            |
| B-003 | Pad manipulatie (path traversal) bij lezen van formulierbestanden         | Hoog                 | 8.28 Veilig programmeren             | **Opgelost**                            |
| B-004 | Kritieke kwetsbaarheden in afhankelijkheden (Log4j, OpenMRS-web, Jackson) | Kritiek/Hoog         | 8.8 Beheer technische kwetsbaarheden | **Deels opgelost / deels buiten scope** |
| B-005 | Lage testdekking in de hoofdmodule (`api`)                                | Middel               | 8.29 Beveiligingstesten              | Open                                    |
| B-006 | Geen uitgevoerde penetratietest                                           | Middel (contextueel) | 8.29 Beveiligingstesten              | Open                                    |

Hieronder worden de vier kernbevindingen (B-001 tot en met B-004) volgens het vaste audit-format uitgewerkt; B-005 en B-006 zijn als aanvullende observaties opgenomen.

---

### Bevinding B-001 — Ontbrekende audit-logging bij inzage/wijziging patiëntgegevens

| Veld                  | Waarde                                                                                               |
| --------------------- | ---------------------------------------------------------------------------------------------------- |
| **Bevinding-ID**      | B-001                                                                                                |
| **Titel**             | Ontbrekende audit-logging bij inzage en wijziging van patiëntdossiers                                |
| **Datum gevonden**    | Gap-analyse logging, huidige projectfase                                                             |
| **Status**            | **Opgelost**                                                                                         |
| **CVSS-score**        | 6.5 (Medium) — AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:N/A:N                                                   |
| **Contextuele score** | 7.5 (Hoog) — hogere impact in zorgcontext omdat dit de AVG-verantwoordingsplicht (art. 30) blokkeert |
| **NEN-7510 control**  | 8.15 Logregistratie (gerelateerd: 8.16 Monitoringactiviteiten)                                       |

**Beschrijving**

Uit de gap-analyse logging bleek dat `FormEntrySession.java` weliswaar technische logging bevatte (sessiecreatie via Log4j), maar geen aantoonbare audittrail voor security-relevante gebeurtenissen: het openen van een patiëntformulier, het opslaan of wijzigen van medische gegevens, en mislukte formulierindieningen werden niet apart geregistreerd. Hierdoor kon bij een incident niet worden gereconstrueerd welke gebruiker welk patiëntdossier had ingezien of gewijzigd. De oorspronkelijk aanwezige logregel logde bovendien de gebruikersnaam, wat een onnodige verwerking van persoonsgegevens in logbestanden betekende.

**Kwetsbare situatie (vóór)**

```
log.info("FormEntrySession created: user="
    + Context.getAuthenticatedUser().getUsername()
    + ", patientId=" + patient.getPatientId());
```

**Gecorrigeerde situatie (na)**

Zes audit-logregels zijn toegevoegd verdeeld over `FormEntrySession.java` en `FormSubmissionController.java`, met een uniform formaat (`AUDIT | action=... | userId=... | ...`) en uitsluitend identifiers in plaats van namen:

```
log.info("AUDIT | action=FORM_OPEN"
    + " | userId=" + Context.getAuthenticatedUser().getId()
    + " | patientId=" + patient.getPatientId()
    + " | mode=" + mode.name());
```

Aanvullend zijn events toegevoegd voor `ENCOUNTER_CREATE`, `ENCOUNTER_EDIT`, `ENCOUNTER_VOID`, `ENCOUNTER_VOID_FAILED` en `FORM_SUBMIT_FAILED`.

**Bewijs van oplossing**

- Code: `api/src/main/java/org/openmrs/module/htmlformentry/FormEntrySession.java` (regels 146, 575, 682, 688, 698) en `FormSubmissionController.java` (regel 88)
- Unit tests: `api/src/test/java/org/openmrs/module/htmlformentry/AuditLoggingTest.java` — 5 tests, allemaal geslaagd:
  - `formOpen_shouldLogAuditEntry` — succesvolle actie wordt gelogd
  - `formOpen_shouldLogPatientIdButNotPatientName` — patientId wél, naam niet in de log
  - `validateSubmission_withErrors_shouldLogSubmitFailed` — mislukte actie wordt gelogd
  - `validateSubmission_withoutErrors_shouldNotLogSubmitFailed` — geen valse meldingen bij succes
  - `auditLog_shouldContainUserIdNotUsername` — userId wél, gebruikersnaam niet in de log
- Traceability matrix: zie [Bijlage A](#7-bijlagen)

**Restrisico en aanbeveling**

De huidige logging dekt formulier-gerelateerde acties. Authenticatie-events (succesvolle/mislukte login) en autorisatieweigeringen op moduleniveau zijn nog niet expliciet gedekt en blijven gedeeltelijk afhankelijk van OpenMRS-kernlogging. Aanbeveling: in een volgende sprint de logging van toegangsweigeringen op formulierniveau (SB-2 uit de security backlog) en wijzigingen aan formulierdefinities (SB-3) toevoegen.

---

### Bevinding B-002 — SQL-injectie via string-concatenatie in databasequery's

| Veld                 | Waarde                                                           |
| -------------------- | ---------------------------------------------------------------- |
| **Bevinding-ID**     | B-002                                                            |
| **Titel**            | SQL-injectie door directe string-concatenatie in databasequery's |
| **Status**           | **Opgelost**                                                     |
| **CVSS-score**       | 9.8 (Kritiek) — AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H              |
| **NEN-7510 control** | 8.28 Veilig programmeren                                         |
| **Gevonden door**    | Code review / SAST-analyse                                       |

**Beschrijving**

Databasequery's binnen de backend werden opgebouwd via directe string-concatenatie van invoerwaarden (zoals rolnaam en attribuut), in plaats van geparametriseerde queries. Dit maakte het in theorie mogelijk voor een aanvaller om arbitraire SQL te injecteren via formuliervelden die uiteindelijk in deze queries terechtkomen.

**Gecorrigeerde situatie (na)**

De queries zijn herschreven naar geparametriseerde queries met named placeholders:

```
// Vóór: query.setString(1, "WHERE name = '" + userInput + "'")
// Na:
q.setString("roleName", roleName);
q.setString("attribute", attribute);
```

**Bewijs van oplossing**

- Code-aanpassing gedocumenteerd in `docs/SecurityImprovements/Improvements-code-security.md`
- Herhaalde CodeQL-scan op de huidige `dev`-branch (eindmeting): **0 findings** in de java-analyse (zie [Bijlage C](#7-bijlagen), `codeql-sarif/java.sarif`)

**Restrisico en aanbeveling**

Geen aantoonbaar restrisico in de geanalyseerde bestanden. Aanbeveling: bij toekomstige wijzigingen aan DAO-klassen een linting-regel of CodeQL-query specifiek op string-concatenatie in SQL-context actief houden, zodat regressie vroegtijdig wordt gedetecteerd.

---

### Bevinding B-003 — Padmanipulatie (path traversal) bij het lezen van formulierbestanden

| Veld                 | Waarde                                                                                                     |
| -------------------- | ---------------------------------------------------------------------------------------------------------- |
| **Bevinding-ID**     | B-003                                                                                                      |
| **Titel**            | Uncontrolled data used in path expression — toegang tot bestanden buiten de bedoelde directory             |
| **Status**           | **Opgelost**                                                                                               |
| **CVSS-score**       | 7.1 (Hoog) — geschat op basis van vereiste privilege (Manage Forms) en impact (bestandslezen op de server) |
| **NEN-7510 control** | 8.28 Veilig programmeren                                                                                   |
| **Gevonden door**    | CodeQL SAST (4 findings: #9, #10, #11, #12)                                                                |

**Beschrijving**

Een gebruikersgestuurd bestandspad werd gebruikt om bestanden op het OpenMRS-serverbestandssysteem te lezen, zonder validatie van het uiteindelijke pad. Hoewel toegang beperkt was tot gebruikers met het privilege _Manage Forms_, hadden deze gebruikers hierdoor potentieel toegang tot bestanden buiten de bedoelde formulierdirectory.

**Gecorrigeerde situatie (na)**

Bestandstoegang is beperkt tot de OpenMRS application data-directory waarin HTML/XML-formulieren worden opgeslagen, met een canonical-path-controle om directory traversal te voorkomen, plus een bestandstype-restrictie (alleen `.html`/`.xml`).

**Bewijs van oplossing**

- Beschrijving en screenshots: `docs/SecurityImprovements/Improvements-code-security.md`
- Herhaalde CodeQL-scan: 0 findings in de eindmeting ([Bijlage C](#7-bijlagen))

**Restrisico en aanbeveling**

Geen aantoonbaar restrisico. Aanbeveling: vergelijkbare canonical-path-validatie als standaardpatroon documenteren voor toekomstige bestand-gerelateerde functionaliteit binnen de module.

---

### Bevinding B-004 — Kritieke kwetsbaarheden in afhankelijkheden (Supply chain)

| Veld                 | Waarde                                                                                    |
| -------------------- | ----------------------------------------------------------------------------------------- |
| **Bevinding-ID**     | B-004                                                                                     |
| **Titel**            | Gebruik van componenten met bekende kritieke kwetsbaarheden in de softwareketen           |
| **Status**           | **Deels opgelost / deels buiten scope van de module**                                     |
| **CVSS-score**       | 10.0 (Kritiek) voor de meest ernstige onderliggende kwetsbaarheid (Log4j, CVE-2021-44228) |
| **NEN-7510 control** | 8.8 Beheer van technische kwetsbaarheden (gerelateerd: 5.22 Monitoring leveranciers)      |
| **Gevonden door**    | Dependency-scan / security backlog-analyse                                                |

**Beschrijving**

Bij de dependency-analyse zijn meerdere kritieke en hoge kwetsbaarheden geïdentificeerd, onder andere deserialisatie-kwetsbaarheden in Log4j 1.x, SQL-injectie in Log4j 1.2.x, en path traversal/Zip Slip-kwetsbaarheden in `org.openmrs.web:openmrs-web`. Zie [Bijlage B](#7-bijlagen) (SBOM) en [Bijlage D](#7-bijlagen) (volledige bevindingenlijst) voor het complete overzicht.

**Analyse en classificatie**

| Component                                                                                | Scope                                                         | Productierisico                                                             |
| ---------------------------------------------------------------------------------------- | ------------------------------------------------------------- | --------------------------------------------------------------------------- |
| `log4j:log4j`, `org.codehaus.groovy:groovy`, `mysql:mysql-connector-java`, `junit:junit` | Uitsluitend test-/release-test-scope (`release-tests`-module) | **Geen** — niet verpakt in het productieartefact                            |
| `org.openmrs.web:openmrs-web` (Zip Slip, path traversal)                                 | OpenMRS-kernplatform                                          | **Niet oplosbaar binnen de module** — vereist upstream patch vanuit OpenMRS |
| `org.codehaus.jackson:jackson-mapper-asl` (deserialisatie, XXE)                          | Geërfd via OpenMRS-platformstack                              | Vereist nadere analyse of de module deze API direct aanroept                |

**Bewijs**

- `docs/SecurityImprovements/Improvements-code-security.md` (risicoclassificatie per dependency-groep)
- `docs/overige/RiskAssesmentRapport.md` sectie 6 (volledige SCA-bevindingenlijst)
- SBOM: `docs/sbom/sbom.cyclonedx.json` ([Bijlage B](#7-bijlagen))
- Actuele OSV-scan (eindmeting): **0 findings** op de huidige dependency-set van de `dev`-branch ([Bijlage C](#7-bijlagen))

**Restrisico en aanbeveling**

Het residuele risico in de productiecode van de module is laag, omdat de kritieke kwetsbaarheden zich concentreren in test-scoped dependencies en in OpenMRS-kerncode die buiten de wijzigingsbevoegdheid van het projectteam valt. Aanbeveling:

1. Test-/release-test-dependencies (Log4j 1.x, oude Groovy-versie) moderniseren zodra de testinfrastructuur wordt herzien — dit is geen productierisico, maar wel technische schuld.
2. OpenMRS-platformupgrades volgen zodra een patch voor `openmrs-web` beschikbaar komt.
3. Verifiëren of `jackson-mapper-asl` direct door de module wordt aangeroepen; zo niet, dit expliciet documenteren als geërfd platformrisico (reeds gedaan, zie bewijsdocument).

---

### Aanvullende observatie B-005 — Lage testdekking in de hoofdmodule

| Veld                 | Waarde                                       |
| -------------------- | -------------------------------------------- |
| **Status**           | Open                                         |
| **NEN-7510 control** | 8.29 Beveiligingstesten tijdens ontwikkeling |

De JaCoCo-coverage-rapportage van de eindmeting toont een instructiedekking van circa 16% in de hoofd-`api`-module, tegenover de in de CI/CD-documentatie vastgelegde drempel van 60%. Submodules zoals `api-1.10` halen wel een hoge dekking (~89%). Een lage dekking in de hoofdmodule betekent dat een deel van de legacy-code niet automatisch wordt gevalideerd bij wijzigingen, wat het risico op onopgemerkte regressies vergroot. **Aanbeveling:** de coverage-drempel per module laten afdwingen door de pipeline (in plaats van alleen op projectniveau) en gericht testen toevoegen aan de meest kritieke, nog ongeteste klassen.

### Aanvullende observatie B-006 — Geen uitgevoerde penetratietest

| Veld                 | Waarde                                                     |
| -------------------- | ---------------------------------------------------------- |
| **Status**           | Open                                                       |
| **NEN-7510 control** | 8.29 Beveiligingstesten tijdens ontwikkeling en acceptatie |

Zoals toegelicht in sectie 2.5 kon de geplande penetratietest niet worden uitgevoerd door een blijvend installatieprobleem in de testomgeving. Dit betekent dat aannames als "payload wordt ge-escaped" (XSS) en "geen invloed op query-uitvoering" (SQLi) uit het testplan niet dynamisch zijn geverifieerd, ook al ondersteunt de statische analyse deze aannames. **Aanbeveling:** zodra een werkende OpenMRS-testomgeving beschikbaar is, het bestaande penetratietestplan ([Bijlage F](#7-bijlagen)) alsnog uitvoeren, met prioriteit voor PT-03 (XSS) en PT-04 (SQL-injectie) ter dynamische bevestiging van B-002.

---

## 5. SBOM en Supply Chain Security

### 5.1 Doel

De Software Bill of Materials (SBOM) vormt de basis voor kwetsbaarheidsbeheer (weten welke versie waar draait), CRA-compliance (SBOM beschikbaar stellen is een CRA-verplichting) en leveranciersbeheer (NEN-7510 5.22). De SBOM wordt automatisch gegenereerd bij elke CI-run via de `anchore/sbom-action` in CycloneDX-formaat en aansluitend gescand met OSV-Scanner.

### 5.2 SBOM-kerngegevens

| Eigenschap         | Waarde                                                             |
| ------------------ | ------------------------------------------------------------------ |
| Formaat            | CycloneDX 1.6 (JSON)                                               |
| Gegenereerd op     | 18 juni 2026                                                       |
| Aantal componenten | 81 (inclusief 48 Maven-/Java-dependencies en GitHub Actions)       |
| Bewaartermijn      | 90 dagen (reguliere CI-run) / 365 dagen (wekelijkse geplande scan) |

### 5.3 Top dependencies op risico (hoogste CVSS-score, uit security-backlog-analyse)

| Component                                 | Versie              | Licentie               | Bekende CVE('s)                                               | CVSS        | Status                                |
| ----------------------------------------- | ------------------- | ---------------------- | ------------------------------------------------------------- | ----------- | ------------------------------------- |
| `log4j:log4j` (test-scope)                | 1.2.x               | Apache-2.0             | Meerdere (deserialisatie, SQL-injectie, JMSAppender RCE, DoS) | tot 9.8     | Test-scope, geen productierisico      |
| `org.openmrs.web:openmrs-web`             | div. (1.9.x–1.10.x) | OpenMRS Public License | Module Upload Path Traversal (Zip Slip)                       | Kritiek     | Upstream/OpenMRS-verantwoordelijkheid |
| `org.openmrs.web:openmrs-web`             | div.                | OpenMRS Public License | ModuleResourcesServlet Path Traversal                         | Hoog        | Upstream/OpenMRS-verantwoordelijkheid |
| `mysql:mysql-connector-java` (test-scope) | 5.1.8               | GPL-2.0                | Privilege escalation, improper access control                 | Hoog–Middel | Test-scope, geen productierisico      |
| `org.codehaus.jackson:jackson-mapper-asl` | 1.5.0               | Apache-2.0             | Deserialization of Untrusted Data, XXE                        | Hoog        | Geërfd platformrisico, analyse loopt  |
| `org.codehaus.groovy:groovy` (test-scope) | 1.8.3               | Apache-2.0             | Deserialisatie, output-neutralisatie                          | Kritiek     | Test-scope, geen productierisico      |
| `xalan:xalan`                             | 2.7.1               | Apache-2.0             | XSLT integer truncation, improper authorization               | Hoog        | Vereist analyse                       |

> Voor het volledige overzicht van alle 29 gerapporteerde dependency-kwetsbaarheden: zie [Bijlage D](#7-bijlagen) (Security backlog / RiskAssessmentRapport sectie 6) en [Bijlage B](#7-bijlagen) (volledige SBOM JSON).

### 5.4 Resultaat eindmeting

De meest recente OSV-Scanner run op de huidige `dev`-branch (zie [Bijlage C](#7-bijlagen)) toont 0 actieve findings. Dit bevestigt dat de dependency-set die daadwerkelijk in de huidige module-versie wordt meegeleverd, op het moment van deze audit geen door OSV-Scanner gedetecteerde bekende kwetsbaarheden bevat. De eerder genoemde kwetsbaarheden in tabel 5.3 zijn voor het merendeel afkomstig uit test-scoped dependencies of OpenMRS-kerncode buiten de productie-classpath van de module.

### 5.5 CRA-koppeling

| CRA-verplichting                                       | NEN-7510:2024-2 control                                 | Status binnen dit project                                                                                                                       |
| ------------------------------------------------------ | ------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| Software leveren zonder bekende actieve kwetsbaarheden | 8.8 Beheer van technische kwetsbaarheden                | Grotendeels aangetoond; restpunten in tabel 5.3                                                                                                 |
| SBOM beschikbaar stellen aan gebruikers                | 8.8 + 5.22 Monitoring leveranciers                      | SBOM wordt automatisch gegenereerd en bewaard                                                                                                   |
| Beveiligingsupdates leveren gedurende de levensduur    | 8.8 Patch-management                                    | Dependabot actief; geen geautomatiseerd patch-beleid voor majeure versies                                                                       |
| Secure by design                                       | 8.25 Beveiligen tijdens de ontwikkelcyclus              | Branch protection, verplichte reviews, CodeQL bij elke PR                                                                                       |
| Actief misbruikte kwetsbaarheden melden aan ENISA      | 6.8 Rapportage van informatiebeveiligingsgebeurtenissen | **Nog niet formeel belegd** — geen meldproces gedocumenteerd binnen dit project                                                                 |
| Logging en monitoring                                  | 8.15 + 8.16                                             | Aangetoond via B-001 en CI/CD-auditlog                                                                                                          |
| Toegangscontrole voor beheerinterfaces                 | 8.2 Bevoorrechte toegangsrechten                        | Branch protection + CODEOWNERS voor de pipeline; module-niveau autorisatie nog niet volledig aantoonbaar (zie security backlog SB-14 t/m SB-16) |

---

## 6. Conclusie en Advies

### 6.1 Beantwoording van de audit vraag

**Voldoet de OpenMRS HTML Form Entry-module aan de relevante NEN-7510:2024-2 controls en CRA-verplichtingen?**

**Grotendeels, met aantoonbare verbetering en een aantal resterende aandachtspunten.** De drie in de gap-analyse onderzochte controls (8.15/8.16 Logregistratie en monitoring, 8.28 Veilig programmeren, 8.8 Beheer technische kwetsbaarheden) waren bij aanvang van dit traject slechts gedeeltelijk aantoonbaar. Gedurende het project zijn concrete maatregelen genomen — audit-logging met tests, geparametriseerde queries, pad validatie, een geautomatiseerde SAST/SCA/SBOM-pijplijn — die met verifieerbaar bewijs (commits, hertest-resultaten, geslaagde unit tests) aantonen dat deze controls nu grotendeels zijn ingevuld. De CI/CD-pijplijn zelf ondersteunt daarnaast aantoonbaar de controls 5.3, 8.32, 5.17 en 8.9.

Twee structurele beperkingen blijven echter bestaan: de **lage testdekking** in de hoofdmodule (B-005) en het **ontbreken van een dynamisch geverifieerde penetratietest** (B-006). Beide zijn geen nieuwe kwetsbaarheden, maar beperken de mate van zekerheid die dit rapport kan bieden over de daadwerkelijke, praktijk getoetste veiligheid van de module.

### 6.2 Geprioriteerde aanbevelingen

| Prioriteit      | Criterium                                   | Actie                                                                                                                                                               |
| --------------- | ------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Nu**          | Kritieke of hoge bevinding, nog open        | Geen kritieke bevindingen meer open in productiecode; wel: triage van resterende CodeQL-notes (null-checks, resource leaks) ter voorkoming van toekomstige bugs     |
| **Deze sprint** | Gemiddelde bevinding, plan aanwezig         | Testdekking hoofdmodule verhogen richting de vastgestelde 60%-norm; logging van toegangsweigeringen en formulierdefinitie-wijzigingen toevoegen (SB-2, SB-3)        |
| **Later**       | Lage bevinding of lange-termijn verbetering | Penetratietest alsnog uitvoeren zodra testomgeving werkt; CRA ENISA-meldproces beleggen; modernisering van test-scoped legacy-dependencies (Log4j 1.x, oude Groovy) |

### 6.3 Eindoordeel

Op basis van het beschikbare bewijs wordt de module beoordeeld als **🟠 Oranje**: er zijn geen onopgeloste kritieke risico's in de productiecode aangetroffen, de belangrijkste eerder geïdentificeerde bevindingen zijn met bewijs opgelost, maar de combinatie van beperkte testdekking en het ontbreken van dynamische verificatie betekent dat volledige "in control"-status (Groen) nog niet kan worden vastgesteld. Met de in 6.2 genoemde acties is dat binnen afzienbare termijn haalbaar.

---

## 7. Bijlagen

| Bijlage | Beschrijving                                            | Bestand / locatie                                                              |
| ------- | ------------------------------------------------------- | ------------------------------------------------------------------------------ |
| **A**   | Traceability matrix                                     | `Traceability-Matrix.md`                                                       |
| **B**   | SBOM (CycloneDX JSON)                                   | `sbom.cyclonedx.json`                                                          |
| **C**   | SAST-output (CodeQL) + SCA-output (OSV-Scanner)         | `codeql-java.sarif`, `osv-scanner-results.sarif`                               |
| **D**   | Risicomatrix (initieel + residueel) en security backlog | `Risicomatrix-Initial.png`, `Risicomatrix-Residual.png`, `Security-Backlog.md` |
| **E**   | Bow-tie diagrammen / threat model                       | `Bow-Tie-*.png`, `ThreatModel.md`                                              |
| **F**   | Penetratietestplan + onderbouwing niet-uitvoering       | `Pentest-Plan.md`, `Pentest-Onderbouwing.md`                                   |
| **G**   | Gap-analyse logging en algemene gap-analyse             | `Gap-Analyse.md`                                                               |
| **H**   | Overzicht AI-tooling verantwoording                     | `Onderhoudbaarheid`                                                            |

> Zie het bestand **[Bijlagen-overzicht.md](Bijlagen-overzicht.md)** voor een tabel met directe koppelingen waar je de definitieve bestanden aan kunt linken.

---

_Einde auditrapport._
