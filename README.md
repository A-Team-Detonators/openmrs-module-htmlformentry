# OpenMRS — Geharde CI/CD Omgeving

> **Mini-ISMS** | Beschrijft hoe omgevingen zijn ingericht, hoe testdata en productiedata gescheiden blijven, en hoe een nieuwe ontwikkelaar aan de slag kan.  
> NEN-7510-compliant pipeline voor het OpenMRS-project.

---

## Inhoudsopgave

1. [Omgevingen](#1-omgevingen)
2. [Branchstrategie](#2-branchstrategie)
3. [CI/CD Pipeline](#3-cicd-pipeline)
4. [Beveiliging & NEN-7510 compliance](#4-beveiliging--nen-7510-compliance)
5. [Gescheiden secrets](#5-gescheiden-secrets)
6. [Voorkomen van testdata in productie](#6-voorkomen-van-testdata-in-productie)
7. [Onboarding nieuwe ontwikkelaar](#7-onboarding-nieuwe-ontwikkelaar)
8. [Goedkeuringsproces voor productiedeploy](#8-goedkeuringsproces-voor-productiedeploy)

---

## 1. Omgevingen

Het project kent twee GitHub Environments met volledig gescheiden configuratie en secrets:

| Omgeving | Branch | URL | Approval vereist |
|---|---|---|---|
| **test** | `dev` | https://test.openmrs.example.com | Nee |
| **production** | `main` | https://openmrs.example.com | **Ja — minimaal 1 reviewer** |

### Hoe zijn de omgevingen ingericht?

```
.
├── environments/
│   ├── test/
│   │   └── .env.example          ← voorbeeldconfiguratie test
│   └── production/
│       └── .env.example          ← voorbeeldconfiguratie productie
├── docker/
│   ├── docker-compose.test.yml   ← teststack (poort 8081)
│   └── docker-compose.production.yml  ← productiestack (poort 8080)
└── .github/
    ├── workflows/
    │   ├── ci.yml                ← build, test, SAST, SBOM
    │   ├── deploy-test.yml       ← automatisch deploy naar test
    │   └── deploy-production.yml ← deploy naar productie met approval gate
    ├── dependabot.yml
    └── CODEOWNERS
```

**GitHub Environments** worden geconfigureerd via `scripts/setup-branch-protection.sh`:

```bash
export GITHUB_ORG=jouw-org
export GITHUB_REPO=openmrs-project
bash scripts/setup-branch-protection.sh
```

Dit script stelt in:
- Branch protection op `main` (2 reviewers, alle CI-checks verplicht, geen force push)
- Branch protection op `dev` (1 reviewer, CI-checks verplicht)
- GitHub Environment `test` (geen wachttijd, geen verplichte reviewer)
- GitHub Environment `production` (5 minuten wachttijd, verplichte reviewer uit het platform-team)

---

## 2. Branchstrategie

```
feature/* ──► dev ──► main
                │              │
           auto-deploy    approval-gate
             (test)        (productie)
```

| Branch | Doel | Direct push | Force push |
|---|---|---|---|
| `main` | Productiecode | ❌ Alleen via PR | ❌ |
| `dev` | Integratiebranch / test | ❌ Alleen via PR | ❌ |
| `feature/*` | Ontwikkeling | ✅ | ✅ (eigen branch) |

**Minimale vereisten om te mergen naar `main`:**

- [ ] Build geslaagd
- [ ] Alle tests geslaagd
- [ ] CodeQL SAST geslaagd (geen high/critical kwetsbaarheden)
- [ ] Dependency Review geslaagd (geen high/critical kwetsbare afhankelijkheden)
- [ ] Minimaal 2 goedkeurende reviews, waarvan 1 van een code-owner
- [ ] Alle review-commentaren opgelost

---

## 3. CI/CD Pipeline

### Elke push / pull request

```
push / PR
    │
    ├─► Build (Maven)
    ├─► Tests (JUnit/Surefire)
    ├─► CodeQL SAST ──────────────────► SARIF upload (Security tab)
    ├─► Dependency Review (alleen PR) ► commentaar op PR
    └─► SBOM genereren (CycloneDX) ───► OSV Scanner SCA
```

### Na merge naar `dev` → automatische deploy naar test

```
merge dev
    └─► deploy-test.yml
            ├─► Docker Compose up (test)
            └─► Health check
```

### Na merge naar `main` → deploy naar productie (met approval)

```
merge main
    └─► deploy-production.yml
            ├─► [APPROVAL GATE] ← reviewer klikt "Approve"
            ├─► CI-status validatie
            ├─► Docker Compose up (productie)
            ├─► Health check
            └─► GitHub Deployment record (audit trail)
```

### Wekelijkse geplande scan (elke maandag 03:00 UTC)

- CodeQL scan op volledige codebase
- SBOM genereren + OSV Scanner SCA
- Artefacten bewaard gedurende 365 dagen

---

## 4. Beveiliging & NEN-7510 compliance

Zie [`docs/NEN7510-controls.md`](docs/NEN7510-controls.md) voor de volledige controlematrix.

### Checklist compliante pipeline

| Control | Maatregel | Waar |
|---|---|---|
| ✅ Branch protection actief op `main` | Alleen via PR, 2 reviews verplicht | `scripts/setup-branch-protection.sh` |
| ✅ CI-checks verplicht vóór merge | Build, Test, SAST, Dependency Review | `ci.yml` |
| ✅ CodeQL SAST | Bij elke push én wekelijks gepland | `ci.yml`, `security-scheduled.yml` |
| ✅ Secret Scanning | Via GitHub Advanced Security (repo-instelling) | GitHub Settings > Security |
| ✅ Dependabot alerts & security updates | Wekelijks Maven, Actions, Docker | `.github/dependabot.yml` |
| ✅ Dependency Review Action | Gekoppeld aan PR's, blokkeert bij high/critical | `ci.yml` |
| ✅ SBOM (CycloneDX) + SCA | Gegenereerd bij elke CI-run + wekelijks | `ci.yml`, `security-scheduled.yml` |
| ✅ GitHub Environments met protection rules | Test + productie, approval gate op productie | `setup-branch-protection.sh` |
| ✅ Secrets gescheiden per environment | Zie sectie 5 | GitHub Environment Secrets |
| ✅ Pipeline-artefacten bewaard | Logs, SBOM, rapporten met retention | Alle workflow-bestanden |
| ✅ README beschrijft beleid (mini-ISMS) | Dit document | `README.md` |

### Secret Scanning activeren

Ga naar: **Settings → Security → Secret scanning → Enable**

Activeer ook **Push protection** om te voorkomen dat secrets per ongeluk gepusht worden.

---

## 5. Gescheiden secrets

Secrets worden **uitsluitend** beheerd via GitHub Environment Secrets. Ze zijn nooit in code, `.env`-bestanden of Docker Compose-bestanden opgenomen.

```
GitHub Repository Secrets     ← NIET gebruikt voor omgevingsspecifieke secrets
GitHub Environment Secrets
  ├── test
  │   ├── DB_PASSWORD_TEST
  │   └── OPENMRS_ADMIN_PASSWORD_TEST
  └── production
      ├── DB_PASSWORD_PROD
      ├── OPENMRS_ADMIN_PASSWORD_PROD
      └── SENTRY_DSN_PROD
```

**Instellen via GitHub CLI (eenmalig, door een beheerder):**

```bash
# Test-secrets
gh secret set DB_PASSWORD_TEST --env test --repo jouw-org/openmrs-project
gh secret set OPENMRS_ADMIN_PASSWORD_TEST --env test --repo jouw-org/openmrs-project

# Productie-secrets
gh secret set DB_PASSWORD_PROD --env production --repo jouw-org/openmrs-project
gh secret set OPENMRS_ADMIN_PASSWORD_PROD --env production --repo jouw-org/openmrs-project
gh secret set SENTRY_DSN_PROD --env production --repo jouw-org/openmrs-project
```

Een workflow die draait in de `test`-environment heeft **geen toegang** tot `production`-secrets, en vice versa.

---

## 6. Voorkomen van testdata in productie

Het systeem bevat meerdere lagen van bescherming om te voorkomen dat testdata in productie terechtkomt:

### Laag 1 — Configuratiescheiding

| Instelling | Test | Productie |
|---|---|---|
| `SEED_TEST_DATA` | `true` | **`false`** (hardcoded in Compose-file) |
| Seed-script gemonteerd | ✅ `test-seed.sql` | ❌ Geen seed-mount |
| `ENABLE_DEBUG_ENDPOINTS` | `true` | `false` |

### Laag 2 — Netwerkisolatie

Test- en productiecontainers draaien in gescheiden Docker-netwerken (`test-net` resp. `prod-net`). Ze kunnen elkaar **niet** bereiken.

### Laag 3 — Aparte databases

Elke omgeving heeft een eigen database-instantie met eigen credentials (zie sectie 5). Er is geen gedeeld database-volume.

### Laag 4 — CI-validatie

De `deploy-production.yml`-workflow bevat een expliciete stap die controleert of alle CI-checks geslaagd zijn vóór elke deploy.

### Laag 5 — Approval gate

Elke productiedeploy vereist handmatige goedkeuring van een lid van het platform-team (zie sectie 8).

### Laag 6 — Geanonimiseerde testdata

Alle testdata in `docker/seed/test-seed.sql` bestaat uitsluitend uit **gesynthetiseerde** (fictieve) patiëntrecords. Er worden nooit echte patiëntgegevens gebruikt als testdata.

---

## 7. Onboarding nieuwe ontwikkelaar

> Een nieuwe ontwikkelaar moet met alleen deze README aan de slag kunnen.

### Vereisten

- Git
- Docker Desktop (of Docker Engine + Docker Compose v2)
- Java 17 (JDK) — bijv. via [SDKMAN](https://sdkman.io/): `sdk install java 17-tem`
- GitHub CLI: `brew install gh` / [andere installatiemethode](https://cli.github.com/)

### Stap 1 — Repository klonen

```bash
git clone https://github.com/jouw-org/openmrs-project.git
cd openmrs-project
```

### Stap 2 — Authenticeren bij GitHub

```bash
gh auth login
```

### Stap 3 — Lokale omgevingsvariabelen instellen

```bash
cp environments/test/.env.example environments/test/.env
# Vul de waarden in via het wachtwoordbeheer van het team (bijv. 1Password / Bitwarden)
# NOOIT .env committen — het staat in .gitignore
```

### Stap 4 — Lokale test-omgeving starten

```bash
docker compose -f docker/docker-compose.test.yml --env-file environments/test/.env up -d
```

De applicatie is beschikbaar op http://localhost:8081/openmrs

### Stap 5 — Testen uitvoeren

```bash
mvn test
```

### Stap 6 — Feature branch aanmaken en werken

```bash
git checkout -b feature/mijn-feature
# ... code schrijven ...
git add .
git commit -m "feat: beschrijving van de wijziging"
git push origin feature/mijn-feature
# Open een Pull Request naar dev via de GitHub-interface of:
gh pr create --base dev --title "feat: beschrijving" --body "..."
```

### Stap 7 — CI controles begrijpen

Na het openen van een PR draaien automatisch:

1. **Build** — controleert of de code compileert
2. **Tests** — unit- en integratietests
3. **CodeQL** — statische beveiligingsanalyse
4. **Dependency Review** — controleert nieuwe afhankelijkheden op kwetsbaarheden
5. **SBOM** — genereert een software-stuklijst

Alle checks moeten groen zijn vóór merge. Rode checks worden direct in de PR getoond.

### Hulp nodig?

- Pipeline-problemen: maak een issue aan met label `ci-cd`
- Beveiligingsmeldingen: neem contact op met `@jouw-org/security-team`
- Toegangsproblemen (secrets, environments): neem contact op met een beheerder

---

## 8. Goedkeuringsproces voor productiedeploy

```
1. Ontwikkelaar opent PR naar main
2. Minimaal 2 reviewers keuren goed (waarvan 1 code-owner)
3. Alle CI-checks zijn groen
4. PR wordt gemerged naar main
5. GitHub Actions start deploy-production workflow
6. [WACHTTIJD 5 minuten]
7. Een lid van @jouw-org/platform-team keurt de deployment goed in de GitHub-interface:
   Actions → deploy-production → Review deployments → Approve
8. Deploy wordt uitgevoerd
9. Health check valideert de deployment
10. GitHub Deployment-record wordt aangemaakt (audit trail)
```

**Bij een mislukte deploy:** de workflow stopt automatisch. Voer een rollback uit via:

```bash
# Trigger een nieuwe deploy van de vorige stabiele tag
gh workflow run deploy-production.yml \
  -f version=<vorig-stabiel-sha>
```

---

*Laatste update: zie git history | Eigenaar: @jouw-org/platform-team*
