# Risk Assessment Report — OpenMRS HTML Form Entry Module

## 1. Inleiding

### 1.1 Doel

Dit Risk Assessment Report beschrijft de geïdentificeerde beveiligingsrisico's binnen de OpenMRS HTML Form Entry Module.

De risicoanalyse is opgesteld op basis van:
- Security scan resultaten
- Security backlog
- Gap analyse
- Bow-tie analyses
- Security requirements

Het doel van dit rapport is om gevonden kwetsbaarheden te beoordelen, passende mitigatiemaatregelen vast te stellen en deze te koppelen aan relevante maatregelen uit NEN 7510:2024-2.

## 2. Scope

### 2.1 Systeem

De analyse richt zich op de OpenMRS HTML Form Entry Module.

De module wordt gebruikt voor het maken, beheren en invullen van medische formulieren binnen het OpenMRS-platform.

De module verwerkt onder andere:
- patiëntinformatie
- medische observaties
- diagnoses
- consultgegevens
- formulierconfiguraties

### 2.2 Gebruikers

De module wordt gebruikt door:
- Zorgverleners
- Beheerders
- Geautoriseerde gebruikers

## 3. Verwerkte gevoelige gegevens

Op basis van de kroonjuwelenanalyse zijn de volgende gevoelige gegevens geïdentificeerd.

| Kroonjuweel | Gegevens | Gegevenssoort |
|---|---|---|
| Patiëntgegevens | Naam, geboortedatum, geslacht, patiënt-ID | Persoonsgegevens |
| Medische observaties | Bloeddruk, gewicht, symptomen, onderzoeksresultaten | Gezondheidsgegevens |
| Diagnoses | Vastgelegde medische aandoeningen | Gezondheidsgegevens |
| Encountergegevens | Consultinformatie, datum, locatie, behandelaar | Medische gegevens |
| Zorgverlenergegevens | Naam, gebruikersaccount, rol | Persoonsgegevens |
| Formulierdefinities | Configuratie medische formulieren | Bedrijfskritische informatie |

### Risico bij verlies of misbruik

Wanneer deze gegevens onvoldoende beveiligd zijn kan dit leiden tot:
- Ongeautoriseerde toegang tot patiëntgegevens
- Manipulatie van medische gegevens
- Privacyincidenten
- Verstoring van zorgprocessen

## 4. Security bevindingen

De onderstaande risico's zijn afkomstig uit de security backlog.

| ID | Vulnerability / risico | Bron | Impact | Prioriteit |
|---|---|---|---|---|
| SB-1 | Ontbrekende audit logging wijzigingen patiëntgegevens | Gap analyse 8.16 | Manipulatie medische gegevens / datalek | Hoog |
| SB-2 | Ontbrekende logging toegangsweigeringen | Gap analyse 8.16 | Ongeautoriseerde inzage | Hoog |
| SB-5 | Onvoldoende beveiligde formulierverwerking | Gap analyse 8.28 | SQL Injection / XSS | Hoog |
| SB-6 | Ontbrekende standaard inputvalidatie | Gap analyse 8.28 | Manipulatie gegevens / XSS | Hoog |
| SB-10 | Kwetsbare externe dependencies | Gap analyse 8.8 | Supply-chain risico | Hoog |
| SB-14 | Onvoldoende autorisatiecontrole | Bow-tie analyse | Ongeautoriseerde toegang | Hoog |
| SB-17 | Onvoldoende herleidbaarheid gebruikersacties | Bow-tie + 8.16 | Geen controleerbaarheid | Hoog |
| SB-20 | Ontbrekend dependency patchproces | Bow-tie + 8.8 | Technische kwetsbaarheden | Hoog |

## 5. Risicoanalyse en mitigaties

### SB-1 Audit logging patiëntgegevens

**Vulnerability:** Wijzigingen aan patiëntgegevens worden onvoldoende geregistreerd.

**Risico:** Gebruikers of aanvallers kunnen medische gegevens aanpassen zonder dat achteraf vastgesteld kan worden wie de wijziging heeft uitgevoerd.

**Impact:**

| Aspect | Impact |
|---|---|
| Confidentiality | Hoog |
| Integrity | Hoog |
| Availability | Middel |

**Mitigatie:** Implementeren van audit logging waarbij wordt vastgelegd:
- Welke gebruiker actie uitvoerde
- Datum en tijd
- Oude en nieuwe waarde
- Type wijziging

**NEN 7510:2024-2 koppeling:**

| Maatregel | Beschrijving |
|---|---|
| 8.16 Monitoringactiviteiten | Beveiligingsrelevante gebeurtenissen moeten worden gelogd en gecontroleerd |

**Security backlog:** SB-1

---

### SB-5 Formulierverwerking beveiligen

**Vulnerability:** Formulierinvoer kan mogelijk leiden tot SQL Injection of Cross-Site Scripting.

**Risico:** Aanvallers kunnen kwaadaardige invoer plaatsen waardoor gegevens worden ingezien of aangepast.

**Impact:**

| Aspect | Impact |
|---|---|
| Confidentiality | Hoog |
| Integrity | Hoog |
| Availability | Middel |

**Mitigatie:**
- Inputvalidatie toepassen
- Output encoding toepassen
- Prepared statements gebruiken
- Security code review uitvoeren

**NEN 7510:2024-2 koppeling:**

| Maatregel | Beschrijving |
|---|---|
| 8.28 Secure coding | Beveiligde ontwikkeling en voorkomen van programmeerfouten |

**Security backlog:** SB-5, SB-6, SB-7, SB-8

---

### SB-10 Dependency vulnerabilities

**Vulnerability:** Externe libraries kunnen bekende kwetsbaarheden bevatten.

**Risico:** Kwetsbare componenten kunnen misbruikt worden om toegang tot het systeem te verkrijgen.

**Mitigatie:**
- Dependency scanning uitvoeren
- CVE-controles uitvoeren
- Libraries updaten
- Patchproces uitvoeren

**NEN 7510:2024-2 koppeling:**

| Maatregel | Beschrijving |
|---|---|
| 8.8 Beheer van technische kwetsbaarheden | Identificeren en oplossen van technische kwetsbaarheden |

**Security backlog:** SB-10, SB-11, SB-12, SB-13, SB-20

---

### SB-14 Autorisatiecontrole

**Vulnerability:** Gebruikersrollen kunnen mogelijk meer rechten hebben dan noodzakelijk.

**Risico:** Een gebruiker kan patiëntgegevens bekijken of aanpassen zonder juiste toestemming.

**Mitigatie:**
- RBAC testen
- Least privilege toepassen
- Autorisaties per actie controleren

**NEN 7510:2024-2 koppeling:**

| Maatregel | Beschrijving |
|---|---|
| 5.15 Toegangsbeveiliging | Beheren van toegangsrechten tot informatie |

**Security backlog:** SB-14, SB-15, SB-16

## 6. Security scan resultaten

### SAST — CodeQL Findings Overview

| Rule | Findings | Category | Severity | Language |
|---|---|---|---|---|
| Inconsistent equals and hashCode | 2 | Reliability | Error | Java |
| Array index out of bounds | 3 | Reliability | Error | Java |
| Synchronization on boxed types or strings | 1 | Reliability | Error | Java |
| Container contents are never accessed | 1 | Maintainability | Error | Java |
| Dereferenced variable may be null | 14 | Reliability | Warning | Java |
| Potential input resource leak | 4 | Reliability | Warning | Java |
| Boxed variable is never null | 40 | Maintainability | Warning | Java |
| Useless null check | 16 | Maintainability | Warning | Java |
| Confusing overloading of methods | 1 | Maintainability | Note | Java |
| Ignored error status of call | 1 | Reliability | Note | Java |
| Inefficient primitive constructor | 17 | Reliability | Note | Java |
| Inefficient empty string test | 27 | Maintainability | Note | Java |
| Inefficient String constructor | 1 | Maintainability | Note | Java |
| Exposing internal representation | 7 | Reliability | Note | Java |
| Possible confusion of local and field | 8 | Maintainability | Note | Java |
| Unread local variable | 15 | Maintainability | Note | Java |
| Missing Override annotation | 57 | Maintainability | Note | Java |
| Inner class could be static | 6 | Maintainability | Note | Java |
| Use of String#replaceAll with a first argument which is not a regular expression | 6 | Reliability | Note | Java |
| Missing catch of NumberFormatException | 23 | Reliability | Note | Java |
| Spurious Javadoc @param tags | 25 | Maintainability | Note | Java |
| Useless parameter | 42 | Maintainability | Note | Java |
| Useless toString on String | 4 | Maintainability | Note | Java |
| Semicolon insertion | 1 | Maintainability | Note | JavaScript |
| Unused variable, import, function or class | 1 | Maintainability | Note | JavaScript |

### SCA / Dependency scan

| Alert # | Vulnerability | Severity | Dependency | File(s) |
|---|---|---|---|---|
| #7 | Improper Neutralization of Special Elements in Output Used by a Downstream Component in Apache Groovy | Critical (Development) | org.codehaus.groovy:groovy | pom.xml |
| #25 | Deserialization of Untrusted Data in Log4j | Critical | log4j:log4j | release-tests/pom.xml |
| #11 | Deserialization of Untrusted Data in Groovy | Critical (Development) | org.codehaus.groovy:groovy | pom.xml |
| #17 | SQL Injection in Log4j 1.2.x | Critical | log4j:log4j | release-tests/pom.xml |
| #9 | Deserialization of Untrusted Data in org.codehaus.jackson:jackson-mapper-asl | Critical | org.codehaus.jackson:jackson-mapper-asl | pom.xml |
| #24 | Deserialization of Untrusted Data in Apache Log4j | Critical | log4j:log4j | release-tests/pom.xml |
| #29 | OpenMRS Module Upload Vulnerable to Path Traversal (Zip Slip) | Critical | org.openmrs.web:openmrs-web | release-tests/pom.xml |
| #13 | OpenMRS Module Upload Vulnerable to Path Traversal (Zip Slip) | Critical | org.openmrs.web:openmrs-web | pom.xml |
| #4 | OpenMRS Module Upload Vulnerable to Path Traversal (Zip Slip) | Critical | org.openmrs.web:openmrs-web | api-1.9/pom.xml |
| #2 | OpenMRS Module Upload Vulnerable to Path Traversal (Zip Slip) | Critical | org.openmrs.web:openmrs-web | api-1.10/pom.xml |
| #26 | MySQL Connectors takeover vulnerability | High | mysql:mysql-connector-java | release-tests/pom.xml |
| #19 | Improper Privilege Management in MySQL Connectors Java | High | mysql:mysql-connector-java | release-tests/pom.xml |
| #16 | Deserialization of Untrusted Data in Log4j 1.x | High | log4j:log4j | release-tests/pom.xml |
| #22 | Improper Access Control in MySQL Connectors Java | High | mysql:mysql-connector-java | release-tests/pom.xml |
| #28 | OpenMRS ModuleResourcesServlet has Path Traversal that Leads to Arbitrary File Read | High | org.openmrs.web:openmrs-web | release-tests/pom.xml |
| #12 | OpenMRS ModuleResourcesServlet has Path Traversal that Leads to Arbitrary File Read | High | org.openmrs.web:openmrs-web | pom.xml |
| #3 | OpenMRS ModuleResourcesServlet has Path Traversal that Leads to Arbitrary File Read | High | org.openmrs.web:openmrs-web | api-1.9/pom.xml |
| #1 | OpenMRS ModuleResourcesServlet has Path Traversal that Leads to Arbitrary File Read | High | org.openmrs.web:openmrs-web | api-1.10/pom.xml |
| #15 | JMSAppender in Log4j 1.2 is vulnerable to deserialization of untrusted data | High | log4j:log4j | release-tests/pom.xml |
| #10 | Apache Xalan Java XSLT library integer truncation issue when processing malicious XSLT stylesheets | High (Development) | xalan:xalan | pom.xml |
| #5 | Improper Restriction of XML External Entity Reference in jackson-mapper-asl | High | org.codehaus.jackson:jackson-mapper-asl | pom.xml |
| #27 | Apache Log4j 1.x (EOL) allows Denial of Service (DoS) | High | log4j:log4j | release-tests/pom.xml |
| #8 | Improper Authorization in Apache Xalan-Java | High (Development) | xalan:xalan | pom.xml |
| #18 | Improper Handling of Insufficient Permissions or Privileges in MySQL Connectors Java | Moderate | mysql:mysql-connector-java | release-tests/pom.xml |
| #21 | Exposure of Sensitive Information to an Unauthorized Actor in Oracle MySQL Connectors Java | Moderate | mysql:mysql-connector-java | release-tests/pom.xml |
| #14 | Privilege escalation in mysql-connector-java | Moderate | mysql:mysql-connector-java | release-tests/pom.xml |
| #23 | Improper Access Control in MySQL Connectors Java | Moderate | mysql:mysql-connector-java | release-tests/pom.xml |
| #20 | Exposure of Sensitive Information to an Unauthorized Actor in Oracle MySQL Connectors Java | Low | mysql:mysql-connector-java | release-tests/pom.xml |

### Penetration test

**NOT IMPLEMENTED**

## 7. Kostenraming

### Resources

| Rol | Taken | Inzet |
|---|---|---|
| Developer | Code aanpassen | ± €32 – €40 per uur |
| Security Engineer | Security controle | ± €36 – €48 per uur |
| Tester | Validatie | ± €28 – €36 per uur |

### Tijdinschatting

| Activiteit | Tijd |
|---|---|
| Analyse vulnerabilities | Één dag |
| Implementeren fixes | Twee dagen |
| Testen | Één dag |

### Budget

| Onderdeel | Kosten |
|---|---|
| Personeel | €4.600 |
| Tooling | €50 |
| Overig | €200 |
| **Totaal** | **€4.850** |

## 8. Conclusie

Op basis van de security backlog zijn meerdere beveiligingsrisico's geïdentificeerd binnen de OpenMRS HTML Form Entry Module.

De belangrijkste risico's bevinden zich rondom:
- Logging en monitoring
- Inputvalidatie
- Secure coding
- Dependency management
- Autorisatie

Door de voorgestelde mitigaties uit te voeren worden de risico's verminderd en wordt de beveiliging van patiënt- en medische gegevens verbeterd.