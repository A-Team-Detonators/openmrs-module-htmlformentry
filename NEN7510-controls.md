# NEN-7510 Controls — CI/CD Pipeline

> **Doel**: Aantoonbaar voldoen aan NEN-7510 (informatiebeveiligingsnorm voor de Nederlandse zorgsector) voor de CI/CD-pipeline van de `openmrs-module-htmlformentry`.

---

## Controlematrix

| # | NEN-7510 Control | Maatregel | Status | Bewijs |
|---|---|---|---|---|
| 1 | **A.9.4** — Toegangsbeveiliging | Branch protection op `production` + PR-reviews verplicht | ✅ | `.github/CODEOWNERS`, branch-protection via GitHub Settings |
| 2 | **A.12.1** — Operationele procedures | Gescheiden workflows per omgeving; OTAP-flow via `dev → test → production` | ✅ | `deploy-test.yml`, `deploy-production.yml` |
| 3 | **A.12.6** — Technische kwetsbaarheden | Dependabot wekelijks, PR's geopend naar `dev` | ✅ | `.github/dependabot.yml` |
| 4 | **A.14.2** — Beveiliging in ontwikkeling | CodeQL SAST bij elke push/PR + wekelijks gepland | ✅ | `ci.yml` job `codeql`, `security-scheduled.yml` |
| 5 | **A.14.2** — Dependency-integriteit | Dependency Review Action op elke PR, blokkeert bij high/critical | ✅ | `ci.yml` job `dependency-review` |
| 6 | **A.14.2** — Software-samenstelling | SBOM (CycloneDX via anchore/sbom-action) + OSV Scanner SCA | ✅ | `ci.yml` job `sbom`, `security-scheduled.yml` |
| 7 | **A.18.1** — Geheimbeheer | Secrets uitsluitend via GitHub Environment Secrets; nooit in code | ✅ | Secrets-tabel hieronder |
| 8 | **A.12.7** — Auditlogging | Alle pipeline-artefacten bewaard met retention-days | ✅ | `retention-days` in alle workflows |
| 9 | **A.8.3** — Bescherming informatiedragers | Testdata nooit in productie (meerdere lagen isolatie) | ✅ | `docker-compose.production.yml`, `README.md` sectie 6 |
| 10 | **A.12.2** — Bescherming tegen malware | Secret Scanning + CodeQL actief op alle branches | ✅ | GitHub Settings → Security |
| 11 | **A.17.2** — Continuïteit | Rollback via `workflow_dispatch`, deployment-records als audit trail | ✅ | `deploy-production.yml` |

---

## Secrets per omgeving

| Secret | `test` environment | `production` environment |
|---|---|---|
| `DB_PASSWORD` | `DB_PASSWORD_TEST` | `DB_PASSWORD_PROD` |
| `OPENMRS_ADMIN_PASSWORD` | `OPENMRS_ADMIN_PASSWORD_TEST` | `OPENMRS_ADMIN_PASSWORD_PROD` |
| `SENTRY_DSN` | — | `SENTRY_DSN_PROD` |

Een workflow in `test` heeft **geen toegang** tot `production`-secrets en vice versa.

---

## Data-isolatiebeleid

1. `SEED_TEST_DATA=true` staat **uitsluitend** in `docker-compose.test.yml`.
2. `docker-compose.production.yml` bevat **geen** seed-script mount en heeft `SEED_TEST_DATA=false` hardcoded.
3. Test- en productiecontainers draaien in gescheiden Docker-netwerken.
4. `deploy-production.yml` controleert of alle CI-checks geslaagd zijn vóór elke deploy.
5. Productiedeploy vereist handmatige goedkeuring via GitHub Environments.
6. Testdata bestaat uitsluitend uit gesynthetiseerde (fictieve) patiëntrecords.

---

## Bewaarbeleid artefacten

| Artefact | Bewaartermijn | Reden |
|---|---|---|
| Build-artefact (`.jar`) | 30 dagen | Reproduceerbaarheid |
| Testrapporten (Surefire) | 30 dagen | Kwaliteitsaudit |
| CodeQL SARIF | 90 dagen | Beveiligingsaudit NEN-7510 |
| SBOM (CycloneDX JSON) | 90 dagen (dagelijks), 365 dagen (wekelijks) | Compliance / leveranciersaudit |
| Deployment logs | 30 dagen (test), 90 dagen (productie) | Operationele audit |

---

## Bekende afwijking: omod-module uitgesloten van CI-build

De `omod`-module vereist `maven-openmrs-plugin:1.0.1` via de OpenMRS Nexus-repository, die niet betrouwbaar bereikbaar is vanuit GitHub Actions. De `omod` is een packaging-wrapper zonder eigen logica.

**Maatregel**: de `omod` wordt uitgesloten via `-pl api,api-1.9,...` in CI. Alle business-logica (in de `api`-modules) wordt wél gebuild, getest en geanalyseerd.

**Risicobeoordeling**: laag — de `omod` bevat geen code die de beveiliging beïnvloedt.
