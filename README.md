# OpenMRS HtmlFormEntry — Geharde CI/CD Omgeving

> **Mini-ISMS** voor de `openmrs-module-htmlformentry`-module.  
> Beschrijft hoe omgevingen zijn ingericht, hoe testdata en productiedata gescheiden blijven, en hoe een nieuwe ontwikkelaar aan de slag kan.  
> NEN-7510-compliant pipeline.

---

## Inhoudsopgave

1. [Omgevingen & branchstrategie](#1-omgevingen--branchstrategie)
2. [CI/CD Pipeline](#2-cicd-pipeline)
3. [Bekende bouwbeperking (omod)](#3-bekende-bouwbeperking-omod)
4. [Beveiliging & NEN-7510 compliance](#4-beveiliging--nen-7510-compliance)
5. [Gescheiden secrets](#5-gescheiden-secrets)
6. [Voorkomen van testdata in productie](#6-voorkomen-van-testdata-in-productie)
7. [Onboarding nieuwe ontwikkelaar](#7-onboarding-nieuwe-ontwikkelaar)
8. [Goedkeuringsproces voor productiedeploy](#8-goedkeuringsproces-voor-productiedeploy)
9. [GitHub Environments instellen (eenmalig)](#9-github-environments-instellen-eenmalig)

---

## 1. Omgevingen & branchstrategie

Het project volgt een **OTAP-achtige** branchstrategie met drie vaste branches:

| Branch | Omgeving | Doel | Approval vereist |
|---|---|---|---|
| `dev` | — | Actieve ontwikkeling, integratie van features | Nee |
| `test` | **test** | Validatie en acceptatie door het team | Nee |
| `production` | **production** | Live omgeving | **Ja — minimaal 1 reviewer** |

### Flow

```
feature/* ──► dev ──► test ──► production
                        │           │
                   auto-deploy  approval-gate
                    (test env)  (prod env)
```

Merges verlopen **altijd via Pull Request**. Direct pushen naar `test` of `production` is geblokkeerd door branch protection.

### Mappenstructuur

```
.
├── .github/
│   ├── workflows/
│   │   ├── ci.yml                  ← build, test, SAST, SBOM (alle branches)
│   │   ├── deploy-test.yml         ← auto-deploy na merge naar test
│   │   ├── deploy-production.yml   ← deploy naar prod met approval gate
│   │   └── security-scheduled.yml  ← wekelijkse CodeQL + SBOM scan
│   ├── dependabot.yml              ← automatische dependency-updates → dev
│   └── CODEOWNERS                  ← verplichte reviewers per pad
├── docker/
│   ├── docker-compose.test.yml
│   └── docker-compose.production.yml
├── NEN7510-controls.md             ← volledige controlematrix
└── README.md                       ← dit bestand (mini-ISMS)
```

---

## 2. CI/CD Pipeline

### Bij elke push of pull request (alle branches)

```
push / PR
    │
    ├─► Build        (Maven, alleen api-modules — zie sectie 3)
    ├─► Tests        (JUnit via Surefire)
    ├─► CodeQL SAST  ──────────────────► SARIF → Security tab
    ├─► Dependency Review (alleen PR)  ► blokkeert bij high/critical CVE
    └─► SBOM (anchore/sbom-action) ───► OSV Scanner SCA
```

### Na merge naar `test`

```
merge → test
    └─► deploy-test.yml
            └─► GitHub Environment: test (geen approval)
```

### Na merge naar `production`

```
merge → production
    └─► deploy-production.yml
            ├─► [APPROVAL GATE] ← reviewer klikt "Approve" in Actions
            ├─► CI-statusvalidatie (alle checks moeten groen zijn)
            └─► GitHub Deployment record (audit trail)
```

### Wekelijkse geplande scan (maandag 03:00 UTC)

- Volledige CodeQL-analyse
- Nieuwe SBOM + OSV Scanner SCA
- Artefacten bewaard 365 dagen (NEN-7510)

---

## 3. Bekende bouwbeperking (omod)

De `omod`-submodule gebruikt de `maven-openmrs-plugin` (versie 1.0.1). Deze plugin wordt opgehaald van de OpenMRS Nexus-repository (`mavenrepo.openmrs.org`), die vanuit GitHub Actions-runners **niet betrouwbaar bereikbaar** is.

**Gevolg:** de volledige `mvn package` faalt in CI met:

```
Unresolveable build extension: Plugin org.openmrs.maven.plugins:maven-openmrs-plugin:1.0.1
```

**Oplossing in CI:** de `omod`-module wordt uitgesloten via de Maven `-pl`-flag:

```bash
mvn clean package -pl api,api-1.9,api-1.10,api-2.0,api-2.2,api-tests -am
```

Dit bouwt en test alle `api`-modules — de code die er voor security-analyse toe doet. De `omod` is alleen een packaging-wrapper en bevat zelf geen logica.

**Lokaal builden** (inclusief omod): werkt normaal via `mvn clean package` als je netwerktoegang hebt tot de OpenMRS Nexus-repo.

---

## 4. Beveiliging & NEN-7510 compliance

Zie [`NEN7510-controls.md`](NEN7510-controls.md) voor de volledige controlematrix.

| Control | Maatregel | Locatie |
|---|---|---|
| ✅ Branch protection op `production` | Alleen via PR, reviews verplicht | GitHub Settings → Branches |
| ✅ CI-checks verplicht vóór merge | Build, Test, SAST, Dep. Review | `ci.yml` |
| ✅ CodeQL SAST | Bij elke push + wekelijks | `ci.yml`, `security-scheduled.yml` |
| ✅ Secret Scanning | Via GitHub Advanced Security | GitHub Settings → Security |
| ✅ Dependabot | Wekelijks Maven + Actions, PR naar `dev` | `.github/dependabot.yml` |
| ✅ Dependency Review | Op elke PR, blokkeert bij high/critical | `ci.yml` |
| ✅ SBOM (CycloneDX) + SCA | Bij elke CI-run + wekelijks | `ci.yml`, `security-scheduled.yml` |
| ✅ GitHub Environments + approval | `test` en `production` geconfigureerd | GitHub Settings → Environments |
| ✅ Secrets gescheiden per environment | Zie sectie 5 | GitHub Environment Secrets |
| ✅ Pipeline-artefacten bewaard | Logs, SBOM, rapporten | `retention-days` in alle workflows |
| ✅ README als mini-ISMS | Dit document | `README.md` |

### Secret Scanning activeren

Ga naar: **Settings → Security → Secret scanning → Enable**  
Activeer ook **Push protection** zodat secrets nooit per ongeluk gepusht kunnen worden.

---

## 5. Gescheiden secrets

Alle secrets worden beheerd via **GitHub Environment Secrets** — nooit in code of Docker Compose-bestanden.

```
GitHub Environment Secrets
  ├── test
  │   ├── DB_PASSWORD_TEST
  │   └── OPENMRS_ADMIN_PASSWORD_TEST
  └── production
      ├── DB_PASSWORD_PROD
      ├── OPENMRS_ADMIN_PASSWORD_PROD
      └── SENTRY_DSN_PROD
```

Een workflow die draait in de `test`-environment heeft **nooit toegang** tot `production`-secrets.

**Instellen via GitHub CLI (eenmalig, door beheerder):**

```bash
gh secret set DB_PASSWORD_TEST             --env test       --repo jouw-org/openmrs-module-htmlformentry
gh secret set OPENMRS_ADMIN_PASSWORD_TEST  --env test       --repo jouw-org/openmrs-module-htmlformentry
gh secret set DB_PASSWORD_PROD             --env production --repo jouw-org/openmrs-module-htmlformentry
gh secret set OPENMRS_ADMIN_PASSWORD_PROD  --env production --repo jouw-org/openmrs-module-htmlformentry
gh secret set SENTRY_DSN_PROD              --env production --repo jouw-org/openmrs-module-htmlformentry
```

---

## 6. Voorkomen van testdata in productie

| Laag | Maatregel |
|---|---|
| **Configuratie** | `SEED_TEST_DATA=true` alleen in `docker-compose.test.yml`; hardcoded `false` in productie |
| **Netwerk** | Test- en productiecontainers draaien in gescheiden Docker-netwerken (`test-net` / `prod-net`) |
| **Database** | Elke omgeving heeft eigen DB-instantie en eigen credentials |
| **CI-validatie** | `deploy-production.yml` controleert of alle CI-checks groen zijn vóór deploy |
| **Approval gate** | Productiedeploy vereist handmatige goedkeuring (zie sectie 8) |
| **Testdata** | Alle testdata in `docker/seed/test-seed.sql` is volledig gesynthetiseerd — nooit echte patiëntgegevens |

---

## 7. Onboarding nieuwe ontwikkelaar

> Met alleen deze README kun je aan de slag.

### Vereisten

- **Git**
- **Java 8 JDK** — via [SDKMAN](https://sdkman.io/): `sdk install java 8.0.392-tem`
- **Maven 3.x** — `sdk install maven`
- **Docker Desktop** (optioneel, voor lokale omgeving)
- **GitHub CLI** — [installatie](https://cli.github.com/): `brew install gh`

### Stap 1 — Repository klonen

```bash
git clone https://github.com/jouw-org/openmrs-module-htmlformentry.git
cd openmrs-module-htmlformentry
```

### Stap 2 — Lokaal builden en testen

```bash
# Alleen api-modules (werkt altijd):
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
feature/* → PR → dev    (jouw feature)
dev       → PR → test   (klaar voor validatie)
test      → PR → production  (klaar voor productie + approval)
```

### Stap 5 — CI begrijpen

Na het openen van een PR starten automatisch:

| Check | Wat het doet |
|---|---|
| **Build** | Compileert api-modules (omd uitgesloten — zie sectie 3) |
| **Tests** | Draait alle JUnit-tests via Surefire |
| **CodeQL SAST** | Statische beveiligingsanalyse van de Java-code |
| **Dependency Review** | Controleert nieuwe afhankelijkheden op CVE's |
| **SBOM** | Genereert software-stuklijst + SCA-scan |

Alle checks moeten groen zijn vóór merge naar `test` of `production`.

### Hulp nodig?

- Pipeline-problemen → issue met label `ci-cd`
- Beveiligingsmeldingen → `@jouw-org/security-team`
- Toegang / secrets → beheerder

---

## 8. Goedkeuringsproces voor productiedeploy

```
1.  Ontwikkelaar opent PR: test → production
2.  Minimaal 1 reviewer keurt goed (code-owner verplicht)
3.  Alle CI-checks zijn groen
4.  PR wordt gemerged naar production
5.  GitHub Actions start deploy-production workflow
6.  [WACHTTIJD — configureerbaar in Environment settings]
7.  Reviewer gaat naar:
      Actions → deploy-production → Review deployments → Approve
8.  Deploy-stappen worden uitgevoerd
9.  GitHub Deployment-record aangemaakt (audit trail)
```

**Rollback:**

```bash
gh workflow run deploy-production.yml -f version=<vorig-stabiel-sha>
```

---

## 9. GitHub Environments instellen (eenmalig)

Voer dit eenmalig uit als repo-beheerder:

```bash
export GITHUB_ORG=jouw-org
export GITHUB_REPO=openmrs-module-htmlformentry
bash scripts/setup-branch-protection.sh
```

Of handmatig via de GitHub-interface:

**Branch protection op `production`:**
- Settings → Branches → Add rule → `production`
- ✅ Require a pull request before merging
- ✅ Require approvals (1)
- ✅ Require status checks: Build, Tests, CodeQL SAST, Dependency Review
- ✅ Do not allow bypassing the above settings

**Branch protection op `test`:**
- Settings → Branches → Add rule → `test`  
- ✅ Require a pull request before merging
- ✅ Require status checks: Build, Tests, CodeQL SAST

**Environments:**
- Settings → Environments → New environment: `test` (geen approval)
- Settings → Environments → New environment: `production` (Required reviewers: jouw team)

---

*Laatste update: zie git history | Eigenaar: @jouw-org/platform-team*
