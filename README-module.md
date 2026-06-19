# HTML Form Entry Module

[![CI — Build, Test & Security](https://github.com/A-Team-Detonators/openmrs-module-htmlformentry/actions/workflows/ci.yml/badge.svg)](https://github.com/A-Team-Detonators/openmrs-module-htmlformentry/actions/workflows/ci.yml)

## Overzicht

De HTML Form Entry module stelt iedereen met basiskennis van HTML en OpenMRS in staat om formulieren te maken. Het is een alternatief voor de InfoPath FormEntry-module in veel (maar niet alle) gevallen.

Het kernidee is dat je alleen HTML hoeft te schrijven — met speciale tags voor OpenMRS-specifieke velden — en de module automatisch afhandelt wat er gebeurt wanneer de gebruiker op de submit-knop klikt.

Een formulierinzending maakt één encounter aan voor één patiënt.

## Vereisten

- OpenMRS 1.9.9+, 1.10.2+ of 1.11.3+
- Java 8

## Installatie

1. Download de module (`.omod`-bestand) vanuit de repository.
2. Installeer de module via OpenMRS Administration → Manage Modules.
3. Ga naar **Manage HTML Forms** onder de administatiepagina.
4. Maak een nieuw formulier aan via **New HTML Form**.
5. Vul naam, omschrijving, versie en encounter type in.
6. Sla op en pas de HTML naar wens aan.

Zie de [HTML Form Entry Module documentatie](https://wiki.openmrs.org/display/docs/HTML+Form+Entry+Module) voor een volledige referentie van beschikbare tags.

## Globale eigenschappen

| Property | Beschrijving |
|---|---|
| `htmlformentry.dateFormat` | Datumnotatie voor alle datumvelden (bijv. `dd-MMM-yyyy`). Zie [Java SimpleDateFormat](http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html). |
| `htmlformentry.showDateFormat` | Stel in op `true` om de datumnotatie als statische tekst naast datumvelden te tonen. |

## Lokaal bouwen

```bash
# Alleen api-modules (werkt altijd, ook zonder OpenMRS Nexus):
mvn clean test -pl api,api-1.9,api-1.10,api-2.0,api-2.2,api-tests -am

# Volledige build inclusief omod-packaging:
mvn clean package
```

> **Let op:** de volledige build vereist netwerktoegang tot `mavenrepo.openmrs.org`. In CI wordt de `omod`-module uitgesloten vanwege een netwerklimitation op GitHub Actions-runners. Zie [README.md](README.md) sectie 3 voor details.

## CI/CD & beveiliging

Deze repository maakt gebruik van een volledige OTAP-branchstrategie (`dev` → `test` → `acceptation` → `production`) met een geautomatiseerde CI-pipeline die bij elke push en pull request draait.

De pipeline omvat:
- Maven build & JUnit tests met JaCoCo coverage
- CodeQL statische beveiligingsanalyse (SAST)
- Dependency Review (blokkeert bij high/critical CVE's)
- SBOM-generatie (CycloneDX) + OSV Scanner
- SonarQube codekwaliteits- en securityanalyse

Zie [README.md](README.md) voor de volledige CI/CD- en beveiligingsdocumentatie.

## Projectlinks

- [OpenMRS Wiki — HTML Form Entry Module](https://wiki.openmrs.org/display/docs/HTML+Form+Entry+Module)
- [HTML Tag Reference](http://archive.openmrs.org/wiki/HTML_Form_Entry_Module_HTML_Reference)
- [Broncode op GitHub](https://github.com/OpenMRS/openmrs-module-htmlformentry)
- [Module downloads](http://modules.openmrs.org/modules/view.jsp?module=htmlformentry)
