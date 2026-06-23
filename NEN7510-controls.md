# NEN-7510 Controls — CI/CD Pipeline

> **Doel**: Aantoonbaar voldoen aan NEN-7510 (informatiebeveiligingsnorm voor de Nederlandse zorgsector) voor de CI/CD-pipeline van de `openmrs-module-htmlformentry` binnen GitHub-organisatie **A-Team-Detonators**.

---

## Controlematrix

| # | NEN-7510 Control | Maatregel | Status | Bewijs |
|---|---|---|---|---|
| 1 | **5.3** — Scheiding van taken | Branch protection op `dev`, `test`, `acceptation` en `production`; PR-reviews verplicht; initiator mag niet zelf goedkeuren (prevent self-review) | ✅ | GitHub Settings → Branches, `.github/CODEOWNERS` |
| 2 | **8.32** — Wijzigingsbeheer | Volledig OTAP-model `dev → test → acceptation → production`; alle wijzigingen via PR; force pushes geblokkeerd | ✅ | Branch protection rules, `.github/CODEOWNERS` |
| 3 | **8.8** — Technische kwetsbaarheden (dependencies) | Dependabot wekelijks voor Maven en GitHub Actions; PR's automatisch naar `dev` | ✅ | `.github/dependabot.yml` |
| 4 | **8.8** — Technische kwetsbaarheden (CVE-scanning) | Dependency Review blokkeert PR bij `high`/`critical` CVE's en verboden licenties (GPL-3.0, AGPL-3.0) | ✅ | `ci.yml` job `dependency-review`, `dependency-review.yml` |
| 5 | **8.8** — Technische kwetsbaarheden (componenten) | SBOM (CycloneDX via anchore/sbom-action) + OSV Scanner SCA bij elke CI-run en wekelijks gepland | ✅ | `ci.yml` jobs `sbom`, `osv-scan`; `security-scheduled.yml` |
| 6 | **8.28** — Veilig programmeren | CodeQL SAST bij elke push/PR op alle vijf branches + wekelijks gepland (maandag 03:00 UTC) | ✅ | `ci.yml` job `codeql`, `security-scheduled.yml` |
| 7 | **8.28 / 8.29** — Codekwaliteit & beveiligingstesten | SonarQube Cloud analyseert codekwaliteit, code smells, bugs en beveiligingskwetsbaarheden bij elke CI-run; JaCoCo coverage als input | ✅ | `ci.yml` job `sonar` |
| 8 | **8.29** — Beveiligingstesten tijdens ontwikkeling | JUnit-tests via Maven Surefire + JaCoCo coverage-rapportage per module | ✅ | `ci.yml` job `test` |
| 9 | **5.17** — Authenticatie-informatie / geheimbeheer | Secrets uitsluitend via GitHub Repository Secrets; nooit in code of configuratiebestanden | ✅ | Secrets-tabel hieronder |
| 10 | **8.15** — Logging / auditlogging | Alle pipeline-artefacten bewaard minimaal 90 dagen; GitHub Deployment-records als audit trail | ✅ | `retention-days: 90` in alle workflows |
| 11 | **8.9** — Configuratiebeheer | SBOM biedt volledig inzicht in gebruikte componenten en versies; build-artifacts 90 dagen bewaard | ✅ | `ci.yml` jobs `build`, `sbom` |
| 12 | **8.16** — Monitoringactiviteiten | Testrapporten (Surefire) en coverage-rapporten (JaCoCo) vastgelegd als artifact; wekelijkse scans voor continue monitoring | ✅ | `ci.yml`, `security-scheduled.yml` |

---

## Secrets

### Repository secrets (beschikbaar in alle workflows)

| Secret | Gebruik |
|---|---|
| `SONAR_TOKEN` | Authenticatie bij SonarQube Cloud |
| `PROJECT_KEY` | SonarQube projectsleutel |
| `ORGANIZATION_KEY` | SonarQube organisatiesleutel |
| `SONAR_HOST_URL` | URL van de SonarQube-instantie |

> Er zijn momenteel geen geautomatiseerde deploy-workflows actief. De `docker/`-bestanden en `environments/`-configuraties in de repository dienen als referentiedocumentatie voor toekomstige deploymentinrichting. Deployment-secrets (database, applicatie) worden ingericht zodra geautomatiseerde deployments worden geactiveerd.

---

## Bewaarbeleid artefacten

| Artefact | Bewaartermijn | Reden |
|---|---|---|
| Build-artefacten (`.jar`) | 90 dagen | Reproduceerbaarheid; beveiligingsaudit NEN-7510 |
| Testrapporten (Surefire) | 90 dagen | Kwaliteitsaudit NEN-7510 |
| Coverage-rapporten (JaCoCo) | 90 dagen | Kwaliteitsaudit; input voor SonarQube |
| CodeQL SARIF (dagelijks CI) | 90 dagen | Beveiligingsaudit NEN-7510 |
| Gecompileerde klassen (`compiled-classes`) | Sessieduur workflow | Doorgave tussen CI-jobs; geen bewaarplicht |
| SBOM (CycloneDX JSON, dagelijks CI) | 90 dagen | Compliance en leveranciersaudit |
| SBOM (CycloneDX JSON, wekelijkse scan) | 365 dagen | Langetermijn compliance NEN-7510 |

> Alle retentieperioden zijn vastgelegd via `retention-days` in de workflow-definities en zijn daarmee aantoonbaar en auditeerbaar.

---

## NEN-7510 mapping CI/CD-maatregelen

Gebaseerd op de CI/CD-documentatie (`docs/overige/CI-CD documentatie/CI-CD-Documentatie.md`):

| CI/CD-maatregel | NEN-7510:2024-2 control | Toelichting |
|---|---|---|
| Branch protection + PR-reviews | 5.3, 8.32 | Wijzigingen verlopen gecontroleerd via PR; functiescheiding door verplichte review |
| Approval gate productie (prevent self-review) | 5.3 | Vier-ogenprincipe; initiator mag niet zelf goedkeuren |
| CODEOWNERS | 5.3, 8.32 | Automatische toewijzing van verantwoordelijke reviewers per bestandspad |
| Deployment records + pipeline-logs | 8.15 | Alle CI-runs en deploys zijn traceerbaar en herleidbaar |
| CodeQL SAST | 8.28 | Broncode wordt geanalyseerd op beveiligingsproblemen en onveilige patronen |
| SonarQube | 8.28, 8.29 | Continue analyse van codekwaliteit en beveiligingskwetsbaarheden |
| Maven Tests + JaCoCo | 8.29 | Aantoonbare testdekking; automatische controle op regressies |
| Dependency Review | 8.8 | Nieuwe dependencies gecontroleerd op CVE's vóór merge |
| SBOM + OSV Scanner | 8.8, 8.9 | Volledig inzicht in componenten en bekende kwetsbaarheden |
| Dependabot | 8.8 | Automatische detectie en update van verouderde/kwetsbare dependencies |
| Artefactbeheer (retention-days) | 8.9, 8.15 | Herleidbaarheid en auditbaarheid van builds en beveiligingsrapporten |
| Secret Scanning + Push Protection | 5.17 | Voorkomt dat secrets per ongeluk in code terechtkomen |

---

## Bekende afwijking: omod-module uitgesloten van CI-build

De `omod`-module vereist `maven-openmrs-plugin:1.0.1` van `mavenrepo.openmrs.org`, die niet betrouwbaar bereikbaar is vanuit GitHub Actions-runners. De composite action `.github/actions/setup-openmrs-maven` configureert Maven met de OpenMRS-repository-instellingen voor de overige modules, maar lost de `omod`-beperking niet volledig op.

**Maatregel:** de `omod` wordt in alle CI-stappen uitgesloten via `-pl api,api-1.9,api-1.10,api-2.0,api-2.2,api-tests -am`.

**Risicobeoordeling:** laag — de `omod` is uitsluitend een packaging-wrapper die de gecompileerde api-modules bundelt tot een `.omod`-bestand. Alle business-logica bevindt zich in de `api`-modules, die volledig worden gebuild, getest, door CodeQL geanalyseerd en door SonarQube gescand.
