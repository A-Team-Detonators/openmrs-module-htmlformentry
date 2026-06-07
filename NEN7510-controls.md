# NEN-7510 Controls — CI/CD Pipeline

> **Doel**: Aantoonbaar voldoen aan NEN-7510 (informatiebeveiligingsnorm voor de Nederlandse zorgsector) voor de CI/CD-pipeline van het OpenMRS-project.

---

## Controlematrix

| # | NEN-7510 Control | Maatregel in pipeline | Status | Bewijs |
|---|---|---|---|---|
| 1 | **A.9.4** — Toegangsbeveiliging systemen | Branch protection op `main` + verplichte PR-reviews | ✅ | `.github/CODEOWNERS`, branch-protection script |
| 2 | **A.12.1** — Operationele procedures | Aparte workflows per omgeving, scheiden van test/productie | ✅ | `deploy-test.yml`, `deploy-production.yml` |
| 3 | **A.12.6** — Beheer technische kwetsbaarheden | Dependabot alerts + security updates actief | ✅ | `.github/dependabot.yml` |
| 4 | **A.14.2** — Beveiliging in ontwikkeling | CodeQL SAST bij elke push/PR | ✅ | `ci.yml` — job `codeql` |
| 5 | **A.14.2** — Dependency-integriteit | Dependency Review Action op PR's | ✅ | `ci.yml` — job `dependency-review` |
| 6 | **A.14.2** — Software-samenstelling | SBOM (CycloneDX) + SCA (OSV Scanner) | ✅ | `ci.yml` — job `sbom` |
| 7 | **A.18.1** — Geheimbeheer | Secrets uitsluitend via GitHub Environment Secrets, nooit in code | ✅ | Secrets-tabel hieronder |
| 8 | **A.12.7** — Auditlogging | Pipeline-artefacten bewaard (logs, rapporten, SBOM) | ✅ | `retention-days` in alle workflows |
| 9 | **A.8.3** — Bescherming informatiedragers | Testdata nooit in productie (zie Data-isolatiebeleid) | ✅ | `SEED_TEST_DATA=false` in productie-config |
| 10 | **A.12.2** — Malwarebescherming | Secret Scanning + CodeQL actief op alle branches | ✅ | GitHub-instelling + `ci.yml` |
| 11 | **A.17.2** — Continuïteit | Health checks na elke deploy, rollback via `workflow_dispatch` | ✅ | Deploy-workflows |

---

## Secrets per omgeving

Alle secrets worden beheerd via **GitHub Environment Secrets** en zijn *niet* zichtbaar buiten de betreffende omgeving.

| Secret | Test (`test` environment) | Productie (`production` environment) |
|---|---|---|
| `DB_PASSWORD` | `DB_PASSWORD_TEST` | `DB_PASSWORD_PROD` |
| `OPENMRS_ADMIN_PASSWORD` | `OPENMRS_ADMIN_PASSWORD_TEST` | `OPENMRS_ADMIN_PASSWORD_PROD` |
| `SENTRY_DSN` | — | `SENTRY_DSN_PROD` |

**Instellen via GitHub CLI:**
```bash
gh secret set DB_PASSWORD_TEST \
  --env test \
  --repo jouw-org/openmrs-project

gh secret set DB_PASSWORD_PROD \
  --env production \
  --repo jouw-org/openmrs-project
```

---

## Data-isolatiebeleid

1. `SEED_TEST_DATA=true` staat **uitsluitend** in de test-omgeving.
2. De test-database draait op een apart netwerk (`test-net`), niet bereikbaar vanuit productie.
3. Testdata bestaat uitsluitend uit **gesynthetiseerde/geanonimiseerde** patiëntrecords (zie `docker/seed/test-seed.sql`).
4. Productie-Docker Compose monteert **geen** seed-script.
5. CI-pipeline bevat een expliciete check die afbreekt als `SEED_TEST_DATA` in een productiedeploy `true` is.

---

## Bewaarbeleid artefacten

| Artefact | Bewaartermijn | Reden |
|---|---|---|
| Build-artefact (`.war`) | 30 dagen | Reproduceerbaarheid |
| Testrapport | 30 dagen | Kwaliteitsaudit |
| CodeQL SARIF | 90 dagen | Beveiligingsaudit NEN-7510 |
| SBOM (CycloneDX) | 90 dagen (dagelijks), 365 dagen (wekelijks) | Compliance / leveranciersaudit |
| Deployment logs | 30 dagen (test), 90 dagen (productie) | Operationele audit |
