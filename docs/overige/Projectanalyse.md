# Projectanalyse OpenMRS HTML Form Entry Module (CIA/BIV)

## 1 Beschrijving van het project

De HTML Form Entry Module is een extensie van OpenMRS waarmee zorgverleners patiëntgegevens kunnen registreren via HTML-gebaseerde formulieren. De module wordt gebruikt voor het vastleggen van medische observaties, diagnoses, consultgegevens en andere patiëntgerelateerde informatie.

De module verwerkt gezondheidsgegevens en vormt daarmee een essentieel onderdeel van het elektronisch patiëntendossier (EPD) binnen OpenMRS.

### Referenties

- OpenMRS Documentation – HTML Form Entry Module
- OpenMRS Reference Application Documentation
- OpenMRS Data Model Documentation

## 2 Kroonjuwelen

Kroonjuwelen zijn de informatie-assets waarvan verlies, wijziging of openbaarmaking de grootste impact heeft op de organisatie.

| Kroonjuweel | Beschrijving | Gegevenssoort |
|---|---|---|
| Patiëntgegevens | Naam, geboortedatum, geslacht, patiënt-ID | Persoonsgegevens |
| Medische observaties | Bloeddruk, gewicht, symptomen, onderzoeksresultaten | Gezondheidsgegevens |
| Diagnoses | Vastgelegde medische aandoeningen | Gezondheidsgegevens |
| Encountergegevens | Consultinformatie, datum, locatie, behandelaar | Medische gegevens |
| Zorgverlenergegevens | Naam, gebruikersaccount, rol | Persoonsgegevens |
| Formulierdefinities | Configuratie van medische formulieren | Bedrijfskritische informatie |

### Waarom zijn dit kroonjuwelen?

Volgens de AVG vallen medische gegevens onder bijzondere persoonsgegevens. Daarnaast vormen diagnoses, observaties en consultgegevens de basis voor medische besluitvorming. Onjuiste of ontbrekende gegevens kunnen direct gevolgen hebben voor patiëntveiligheid.

## 3 BIV-classificatie

### Beschikbaarheid (B)

Beschikbaarheid betreft de mate waarin gegevens en systemen beschikbaar moeten zijn.

**Score: 4 (Hoog)**

Motivatie:
- Zorgverleners moeten patiëntgegevens kunnen registreren tijdens consulten.
- Uitval vertraagt zorgprocessen.
- Tijdelijke alternatieven (papierregistratie) zijn mogelijk.

### Integriteit (I)

Integriteit betreft de juistheid en volledigheid van gegevens.

**Score: 5 (Zeer Hoog)**

Motivatie:
- Medische beslissingen zijn afhankelijk van correcte gegevens.
- Verkeerde observaties of diagnoses kunnen leiden tot onjuiste behandeling.
- Manipulatie van patiëntgegevens kan ernstige gezondheidsschade veroorzaken.

### Vertrouwelijkheid (V)

Vertrouwelijkheid betreft bescherming tegen ongeautoriseerde toegang.

**Score: 5 (Zeer Hoog)**

Motivatie:
- De module verwerkt medische persoonsgegevens.
- Datalekken kunnen leiden tot AVG-overtredingen.
- Reputatieschade en juridische gevolgen zijn aanzienlijk.

### Samenvatting BIV

| Aspect | Score |
|---|---|
| Beschikbaarheid | 4 |
| Integriteit | 5 |
| Vertrouwelijkheid | 5 |

**Eindclassificatie: BIV = 4-5-5 (Hoog Kritisch)**
## 4 Risicocriteria

Voor de risicoanalyse wordt gewerkt met kans en impact.

### Kans

| Score | Beschrijving |
|---|---|
| 1 | Zeer onwaarschijnlijk |
| 2 | Onwaarschijnlijk |
| 3 | Mogelijk |
| 4 | Waarschijnlijk |
| 5 | Zeer waarschijnlijk |

### Impact

| Score | Beschrijving |
|---|---|
| 1 | Zeer onwaarschijnlijk |
| 2 | Onwaarschijnlijk |
| 3 | Mogelijk |
| 4 | Waarschijnlijk |
| 5 | Zeer waarschijnlijk |

### Impactfactoren

Bij de beoordeling van impact wordt gekeken naar:
- Patiëntveiligheid
- Schending van privacy
- Financiële schade
- Reputatieschade
- Beschikbaarheid van zorgprocessen
- Naleving van wet- en regelgeving (AVG, NEN 7510)

## 5 Scoreschaal

Risicoscore wordt berekend als:

**Risico = Kans × Impact**

| Score | Risiconiveau |
|---|---|
| 1-4 | Laag |
| 5-9 | Middel |
| 10-16 | Hoog |
| 17-25 | Kritiek |

## 6 Risicobereidheid (Risk Appetite)

Omdat OpenMRS medische gegevens verwerkt geldt een lage risicobereidheid.

| Risiconiveau | Acceptatie |
|---|---|
| Laag | Accepteren |
| Middel | Accepteren met monitoring |
| Hoog | Mitigatie verplicht |
| Kritiek | Niet acceptabel |

## 7 Grenswaarden

| Risicoscore | Actie |
|---|---|
| 1-4 | Accepteren |
| 5-9 | Monitoren |
| 10-16 | Beheersmaatregelen implementeren |
| 17-25 | Direct behandelen vóór productiegebruik |

## Conclusie

De OpenMRS HTML Form Entry Module verwerkt bijzondere persoonsgegevens en medische gegevens die essentieel zijn voor de patiëntenzorg. De belangrijkste kroonjuwelen zijn patiëntgegevens, medische observaties, diagnoses en consultinformatie. Op basis van de BIV-analyse krijgt de module de classificatie Beschikbaarheid = 4, Integriteit = 5 en Vertrouwelijkheid = 5. Gezien de aard van de gegevens wordt een lage risicobereidheid gehanteerd, waarbij alle risico's met een score van 10 of hoger verplicht moeten worden gemitigeerd.

Dit sluit goed aan op de volgende onderdelen van de sprint, zoals threat modeling, risicomatrices en het Risk Assessment Report.