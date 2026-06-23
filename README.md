# OpenMRS HtmlFormEntry — Geharde CI/CD Omgeving

> **Mini-ISMS** voor de `openmrs-module-htmlformentry`-module.  
> Beschrijft hoe de branchstrategie en CI-pipeline zijn ingericht, en hoe een nieuwe ontwikkelaar aan de slag kan.  
> NEN-7510-compliant pipeline.

---

## Inhoudsopgave

1. [Omgevingen & branchstrategie](#1-omgevingen--branchstrategie)
2. [CI/CD Pipeline](#2-cicd-pipeline)
3. [Bekende bouwbeperking (omod)](#3-bekende-bouwbeperking-omod)
4. [Beveiliging & NEN-7510 compliance](#4-beveiliging--nen-7510-compliance)
5. [Secrets](#5-secrets)
6. [Onboarding nieuwe ontwikkelaar](#6-onboarding-nieuwe-ontwikkelaar)
7. [Goedkeuringsproces voor productie](#7-goedkeuringsproces-voor-productie)
8. [GitHub Environments & branch protection instellen (eenmalig)](#8-github-environments--branch-protection-instellen-eenmalig)

---

## 1. Omgevingen & branchstrategie

Het project volgt een volledig **OTAP-model** met vier vaste hoofdbranches binnen de GitHub-organisatie **A-Team-Detonators**:

| Branch | Fase | Doel | Approval vereist |
|---|---|---|---|
| `dev` | Ontwikkeling | Actieve ontwikkeling, integratie van features | **Ja — minimaal 1 reviewer** |
| `test` | Test | Geautomatiseerde validatie na integratie | **Ja — minimaal 1 reviewer** |
| `acceptation` | Acceptatie | Acceptatietests door het team vóór productie | **Ja — minimaal 1 reviewer** |
| `production` | Productie | Live omgeving | **Ja — minimaal 1 reviewer** |

### Flow

```
feature/* ──► dev ──► test ──► acceptation ──► production
```

Merges verlopen **altijd via Pull Request**. Direct pushen naar `test`, `acceptation` of `production` is geblokkeerd door branch protection. CODEOWNERS zorgt voor automatische reviewertoewijzing via team **@A-Team-Detonators/C4-Detonators**.

### Mappenstructuur

```
.
├── .github/
│   ├── workflows/
│   │   ├── ci.yml                  ← build, test, coverage, CodeQL, SBOM, OSV, SonarQube
│   │   ├── dependency-review.yml   ← dependency review op PR's
│   │   └── security-scheduled.yml  ← wekelijkse CodeQL + SBOM scan (ma 03:00 UTC)
│   ├── actions/
│   │   └── setup-openmrs-maven/    ← composite action: Maven settings voor OpenMRS repo
│   ├── dependabot.yml              ← automatische dependency-updates naar dev (maandag)
│   ├── CODEOWNERS                  ← @A-Team-Detonators/C4-Detonators als verplichte reviewer
│   └── instructions/
│       └── aikido_rules.instructions.md
├── docs/
│   ├── onderhoudbaarheid/          ← SonarQube-bevindingen en refactordocumentatie
│   ├── SecurityImprovements/       ← beveiligingsverbeteringen
│   └── overige/                    ← CI-CD, logging, risicoanalyse, threat model, etc.
├── NEN7510-controls.md             ← volledige NEN-7510 controlematrix
└── README-module.md                ← module documentatie
└── README.md                       ← dit bestand (mini-ISMS)
```

---

## 2. CI/CD Pipeline

### Workflows overzicht

| Workflow | Bestand | Trigger |
|---|---|---|
| CI — Build, Test & Security | `ci.yml` | Push/PR op alle 4 branches |
| Dependency Review | `dependency-review.yml` | PR op production, acceptation, test, dev |
| Periodieke Beveiligingsscan | `security-scheduled.yml` | Elke maandag 03:00 UTC + handmatig |

### CI-pipeline (`ci.yml`) — bij elke push of pull request

De CI-pipeline bestaat uit zeven jobs. Build is de startvoorwaarde; Tests en CodeQL draaien parallel daarna; SBOM→OSV en SonarQube volgen op hun respectievelijke voorgangers:

```
push / PR
    │
    ▼
1. Build  ──────────────────────────────────────────────────────────────────┐
    │                                                                        │
    ├──► 2. Tests (JUnit + JaCoCo coverage)                                 │
    │         │                                                              │
    │         └──► 7. SonarQube (vereist coverage van stap 2)               │
    │                                                                        │
    ├──► 3. CodeQL SAST ──────────────────────────► SARIF → Security tab    │
    │                                                                        │
    ├──► 4. Dependency Review (alleen bij PR) ────► blokkeert bij high CVE  │
    │                                                                        │
    └──► 5. SBOM (anchore/sbom-action → CycloneDX JSON)                     │
              │                                                              │
              └──► 6. OSV Scan (scant SBOM op bekende kwetsbaarheden)       │
                                                                             │
    compiled-classes artifact ◄─────────────────────────────────────────────┘
```

#### Stap 1 — Build

Bouwt alle api-modules met Java 8 (Temurin) en Maven. De `omod`-module is uitgesloten (zie sectie 3). Produceert een `build-artifacts`-artifact (`.jar`-bestanden, 90 dagen bewaard).

#### Stap 2 — Tests & coverage

```bash
mvn verify jacoco:report -pl api,api-1.9,api-1.10,api-2.0,api-2.2,api-tests -am -P ci
```

Draait alle JUnit-tests via Maven Surefire. JaCoCo genereert een coverage-rapport per module. Beide worden als artifact opgeslagen (90 dagen) en doorgegeven aan SonarQube.

#### Stap 3 — CodeQL SAST

Statische beveiligingsanalyse van de Java-broncode. Resultaten (SARIF) zijn zichtbaar in de GitHub Security-tab en worden 90 dagen bewaard als artifact.

#### Stap 4 — Dependency Review

Draait **uitsluitend op pull requests**. Blokkeert de PR bij afhankelijkheden met `high` of `critical` CVE's, en bij verboden licenties (GPL-3.0, AGPL-3.0).

#### Stap 5 & 6 — SBOM + OSV Scan

`anchore/sbom-action` genereert een CycloneDX JSON SBOM direct van de broncode. Vervolgens scant `google/osv-scanner-action` de SBOM op bekende kwetsbaarheden. De SBOM wordt 90 dagen bewaard.

#### Stap 7 — SonarQube

SonarQube vereist Java 17 voor de scanner, terwijl de codebase op Java 8 is gebouwd. Beide versies worden gelijktijdig opgezet:

```
Java 8  → mvn verify (compileert de broncode opnieuw)
Java 17 → mvn sonar:sonar (draait de SonarQube scanner)
```

De scanner leest de JaCoCo coverage-rapporten uit stap 2. Resultaten zijn zichtbaar in SonarQube Cloud.

Benodigde secrets: `SONAR_TOKEN`, `PROJECT_KEY`, `ORGANIZATION_KEY`, `SONAR_HOST_URL`.

### Wekelijkse beveiligingsscan (`security-scheduled.yml`)

Elke maandag om 03:00 UTC (ook handmatig te starten via `workflow_dispatch`):
- Volledige CodeQL-analyse op de huidige broncode
- SBOM-generatie + OSV Scanner SCA

SBOM-artefacten van de wekelijkse scan worden **365 dagen** bewaard (NEN-7510 langetermijn auditvereiste).

---

## 3. Bekende bouwbeperking (omod)

De `omod`-submodule gebruikt `maven-openmrs-plugin:1.0.1`, opgehaald van `mavenrepo.openmrs.org`. Deze repository is vanuit GitHub Actions-runners niet betrouwbaar bereikbaar.

**Gevolg:** een volledige `mvn package` faalt in CI.

**Oplossing:** de `omod`-module wordt in alle CI-stappen uitgesloten via:

```bash
mvn ... -pl api,api-1.9,api-1.10,api-2.0,api-2.2,api-tests -am
```

De composite action `.github/actions/setup-openmrs-maven` configureert Maven met de juiste OpenMRS repository-instellingen voor de overige modules.

**Risicobeoordeling:** laag — de `omod` is een packaging-wrapper zonder eigen logica. Alle business-logica in de `api`-modules wordt volledig gebuild, getest en geanalyseerd.

**Lokaal builden** (inclusief omod): `mvn clean package` — werkt normaal met netwerktoegang tot de OpenMRS Nexus-repo.

---

## 4. Beveiliging & NEN-7510 compliance

Zie [`NEN7510-controls.md`](NEN7510-controls.md) voor de volledige controlematrix.

| Control | Maatregel | Locatie |
|---|---|---|
| ✅ Branch protection op `dev`, `test`, `acceptation` en `production` | Alleen via PR; reviews verplicht; force push geblokkeerd | GitHub Settings → Branches |
| ✅ CODEOWNERS | @A-Team-Detonators/C4-Detonators als verplichte reviewer op alles | `.github/CODEOWNERS` |
| ✅ CI-checks verplicht vóór merge | Build, Test, CodeQL, Dep. Review, SonarQube | `ci.yml` |
| ✅ CodeQL SAST | Bij elke push/PR + wekelijks gepland | `ci.yml`, `security-scheduled.yml` |
| ✅ SonarQube | Codekwaliteit + security na elke CI-run | `ci.yml` job `sonar` |
| ✅ JaCoCo coverage | Coverage-rapport per module, input voor SonarQube | `ci.yml` job `test` |
| ✅ Dependabot | Wekelijks Maven + Actions, PR's naar `dev` | `.github/dependabot.yml` |
| ✅ Dependency Review | Op elke PR; blokkeert bij high/critical CVE + verboden licenties | `dependency-review.yml`, `ci.yml` |
| ✅ SBOM (CycloneDX) + OSV Scan | Bij elke CI-run + wekelijks | `ci.yml`, `security-scheduled.yml` |
| ✅ GitHub Environments + approval gate | `production` vereist handmatige goedkeuring | GitHub Settings → Environments |
| ✅ Secrets gescheiden | Repository secrets (SonarQube) en environment secrets (toekomstig) | GitHub Settings → Secrets |
| ✅ Pipeline-artefacten bewaard | 90 dagen (dagelijks CI), 365 dagen (wekelijkse scan) | `retention-days` in alle workflows |
| ✅ Secret Scanning + Push Protection | Via GitHub Advanced Security | GitHub Settings → Security |

---

## 5. Secrets

Alle secrets worden beheerd via **GitHub Secrets** — nooit in code.

### Repository secrets (beschikbaar in alle workflows)

| Secret | Gebruik |
|---|---|
| `SONAR_TOKEN` | Authenticatie bij SonarQube Cloud |
| `PROJECT_KEY` | SonarQube projectsleutel |
| `ORGANIZATION_KEY` | SonarQube organisatiesleutel |
| `SONAR_HOST_URL` | URL van de SonarQube-instantie |

> Deployment-secrets (database, applicatie) zijn niet van toepassing zolang er geen geautomatiseerde deploy-workflows actief zijn. De `docker/`-bestanden en `environments/`-configuraties in de repository dienen als referentie voor toekomstige deploymentinrichting.

---

## 6. Onboarding nieuwe ontwikkelaar

### Vereisten

- **Git**
- **Java 8 JDK** — via [SDKMAN](https://sdkman.io/): `sdk install java 8.0.392-tem`
- **Maven 3.x** — `sdk install maven`
- **GitHub CLI** (optioneel) — [installatie](https://cli.github.com/)

### Stap 1 — Repository klonen

```bash
git clone https://github.com/A-Team-Detonators/openmrs-module-htmlformentry.git
cd openmrs-module-htmlformentry
```

### Stap 2 — Lokaal bouwen en testen

```bash
# Alleen api-modules (altijd werkend):
mvn clean test -pl api,api-1.9,api-1.10,api-2.0,api-2.2,api-tests -am

# Volledige build inclusief omod (vereist netwerktoegang tot OpenMRS Nexus):
mvn clean package
```

### Stap 3 — Feature branch aanmaken

```bash
git checkout dev
git pull origin dev
git checkout -b feature/mijn-feature

# ... code schrijven en testen ...

git add .
git commit -m "feat: beschrijving van de wijziging"
git push origin feature/mijn-feature

# PR openen naar dev:
gh pr create --base dev --title "feat: beschrijving" --body "..."
```

### Stap 4 — Flow naar productie

```
feature/* → PR → dev
dev       → PR → test
test      → PR → acceptation
acceptation → PR → production  (vereist approval van C4-Detonators)
```

### Stap 5 — CI begrijpen

Na het openen van een PR starten automatisch:

| Check | Wat het doet |
|---|---|
| **Build** | Compileert api-modules met Java 8 |
| **Tests** | Draait JUnit-tests + genereert JaCoCo coverage |
| **CodeQL** | Statische beveiligingsanalyse |
| **Dependency Review** | Controleert CVE's en licenties van nieuwe afhankelijkheden |
| **SBOM + OSV Scan** | Genereert CycloneDX SBOM en scant op kwetsbaarheden |
| **SonarQube** | Analyseert codekwaliteit en security (gebruikt JaCoCo coverage) |

Alle checks moeten groen zijn vóór merge.

### Hulp nodig?

- Pipeline-problemen → issue met label `ci-cd`
- Beveiligingsmeldingen → `@A-Team-Detonators/C4-Detonators`

---

## 7. Goedkeuringsproces voor productie

```
1. Ontwikkelaar opent PR: acceptation → production
2. @A-Team-Detonators/C4-Detonators wordt automatisch toegewezen via CODEOWNERS
3. Minimaal 1 reviewer keurt de PR goed
4. Alle CI-checks zijn groen (inclusief SonarQube)
5. PR wordt gemerged naar production
6. GitHub Deployment-record wordt aangemaakt (audit trail)
```

---

## 8. GitHub Environments & branch protection instellen (eenmalig)

```bash
export GITHUB_ORG=A-Team-Detonators
export GITHUB_REPO=openmrs-module-htmlformentry
bash scripts/setup-branch-protection.sh
```

Of handmatig via de GitHub-interface:

**Branch protection op `dev`, `test`, `acceptation` en `production`:**
- ✅ Require a pull request before merging
- ✅ Require approvals (1), dismiss stale reviews
- ✅ Require status checks: Build, Tests, CodeQL, SonarQube
- ✅ Block force pushes / Do not allow bypassing

**GitHub Environment `production`:**
- Settings → Environments → `production`
- Required reviewers: @A-Team-Detonators/C4-Detonators

---

*Laatste update: zie git history | Eigenaar: @A-Team-Detonators/C4-Detonators*
