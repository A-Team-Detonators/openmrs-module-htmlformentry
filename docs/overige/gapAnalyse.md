# Gap-analyse HTML Form Entry-module OpenMRS

## Doel van de analyse

Het doel van deze gap-analyse is om te bepalen in hoeverre de HTML Form Entry-module van OpenMRS voldoet aan relevante beveiligingscontroles uit **NEN-7510:2024-2**.

De analyse kijkt naar:

- **8.16 – Monitoren van activiteiten**
- **8.28 – Veilig programmeren**
- **8.8 – Beheer van technische kwetsbaarheden**

Per control wordt onderzocht:

- De huidige implementatie van de module
- De gewenste situatie volgens NEN-7510
- Aanwezigheid van functionaliteit
- Bewijs vanuit de broncode
- Vastgestelde gaps en aanbevelingen


# Huidige situatie (AS-IS)

De HTML Form Entry-module verwerkt medische formulieren binnen OpenMRS. Hierbij worden onder andere verwerkt:

- patiëntgegevens
- medische observaties
- diagnoses
- encountergegevens

Uit de codeanalyse blijkt dat de module gebruikmaakt van bestaande OpenMRS-functionaliteit voor beveiliging.

Aanwezig:

- logging
- gebruikerscontext
- framework-functionaliteit

Veel beveiligingsmaatregelen zijn echter afhankelijk van het onderliggende OpenMRS-platform.

Er is geen volledige zelfstandige beveiligingslaag binnen de module zichtbaar voor alle security-aspecten.


# Gewenste situatie (TO-BE)

Volgens NEN-7510:2024-2 moet medische software passende maatregelen ondersteunen.

| Control | Gewenste situatie |
|---|---|
| 8.16 Monitoren van activiteiten | Security-relevante gebeurtenissen worden gelogd zodat acties herleidbaar zijn |
| 8.28 Veilig programmeren | Software wordt ontwikkeld volgens veilige programmeerprincipes inclusief bescherming tegen kwetsbaarheden |
| 8.8 Beheer technische kwetsbaarheden | Kwetsbaarheden in softwarecomponenten worden actief geïdentificeerd en beheerd |


# GAP-analyse

| Control | Gewenste situatie | Huidige situatie | Status | Bewijs | Gap | Aanbeveling |
|---|---|---|---|---|---|---|
| 8.16 Monitoren van activiteiten | Gebruikersactiviteiten en security-events moeten aantoonbaar worden gelogd | Module gebruikt Log4j en registreert technische gebeurtenissen zoals sessiecreatie | Gedeeltelijk aanwezig | `log4j.xml`, `FormEntrySession.java` bevat logging | Geen bewijs van volledige auditlogging voor toegang, wijzigingen en security-events | Audit logging uitbreiden met toegang tot patiëntgegevens, wijzigingen en toegangsweigeringen |
| 8.28 Veilig programmeren | Code moet bescherming bieden tegen beveiligingsrisico's zoals injecties en onveilige invoer | Module verwerkt gebruikersinvoer via HTML-formulieren en gebruikt OpenMRS-functionaliteit | Gedeeltelijk aanwezig | Formulierverwerking via FormEntrySession en OpenMRS-context | Geen volledig aantoonbare centrale validatie- en securitylaag | Secure coding toepassen, inputvalidatie standaardiseren en security reviews uitvoeren |
| 8.8 Beheer technische kwetsbaarheden | Kwetsbaarheden in software en dependencies moeten worden beheerd | Module gebruikt Java/Maven dependencies en externe libraries | Gedeeltelijk aanwezig | Maven projectstructuur (`pom.xml`) | Geen bewijs van automatische vulnerability scanning of CVE-beheer | Dependency scanning uitvoeren en periodiek kwetsbaarheden beoordelen |


# Detailanalyse per control

## A.8.16 Monitoren van activiteiten

### Bewijs uit code

**Bestand:** `api/src/main/resources/log4j.xml`

Aanwezig:
- loggerconfiguratie
- Log4j framework

**Bestand:** `api/src/main/java/org/openmrs/module/htmlformentry/FormEntrySession.java`

Voorbeeld:
```
log.info("FormEntrySession created: patient=" + ...)
```

### Analyse

De module beschikt over loggingfunctionaliteit. Hierdoor kunnen technische gebeurtenissen worden geregistreerd.

Er is echter beperkt bewijs gevonden voor volledige security-auditlogging zoals:
- succesvolle authenticaties
- mislukte authenticatiepogingen
- toegangsweigeringen
- wijzigingen aan patiëntgegevens
- wijzigingen aan formulieren

Hierdoor is de herleidbaarheid van beveiligingsincidenten beperkt.

### Conclusie

**Status:** gedeeltelijk aanwezig

Logging is aanwezig, maar onvoldoende aantoonbaar gericht op security monitoring.

---

## A.8.28 Veilig programmeren

### Bewijs uit code

**Bestand:** `api/src/main/java/org/openmrs/module/htmlformentry/FormEntrySession.java`

Aanwezig:
- verwerking van formulierdata
- gebruik van OpenMRS Context
- sessiebeheer rondom formulierinvoer

**Bestanden:** `api/src/main/java/org/openmrs/module/htmlformentry/api/*`

Aanwezig:
- gebruik van OpenMRS API-laag
- verwerking via bestaande services

### Analyse

De module gebruikt de OpenMRS API in plaats van directe database-interactie. Hierdoor wordt een deel van de beveiliging door het framework afgehandeld.

Er is echter geen duidelijk aantoonbare centrale beveiligingslaag gevonden voor alle invoerpunten.

Niet aantoonbaar:
- uniforme inputvalidatie
- expliciete XSS-bescherming
- volledige controle op alle gebruikersinvoer

Dit is relevant omdat de module HTML-formulieren verwerkt waarin gebruikers medische gegevens invoeren.

### Conclusie

**Status:** gedeeltelijk aanwezig

De module maakt gebruik van veilige frameworkcomponenten, maar secure coding maatregelen zijn niet volledig aantoonbaar.

---

## A.8.8 Beheer technische kwetsbaarheden

### Bewijs uit code

**Bestand:** `pom.xml`

Aanwezig:
- Maven dependencybeheer
- externe Java libraries

Voorbeeld:
```
    <dependecy>
    ...
    </dependecy>
```

### Analyse

De module gebruikt externe componenten die onderdeel zijn van de softwareketen.

Dependencybeheer is aanwezig doordat versies worden vastgelegd via Maven.

Er is echter geen bewijs gevonden van:
- automatische CVE-scanning
- dependency vulnerability checks
- periodieke kwetsbaarheidsbeoordeling

### Conclusie

**Status:** gedeeltelijk aanwezig

Het project beheert dependencies, maar actief vulnerability management is niet aantoonbaar.
markdown## Oorzakenanalyse

# Oorzakenanalyse

| Onderdeel | Mogelijke oorzaak |
|---|---|
| Monitoring | Logging is vooral technisch ingericht en niet volledig als audit logging |
| Veilig programmeren | Securitymaatregelen zijn deels afhankelijk van OpenMRS-framework |
| Kwetsbaarhedenbeheer | Geen zichtbare automatische controle op externe dependencies |

# Actieplan

| Actie | Prioriteit | Verwacht resultaat |
|---|---|---|
| Audit logging uitbreiden | Hoog | Betere controleerbaarheid van patiëntgegevens |
| Security-events loggen | Hoog | Detectie van misbruik verbeteren |
| Secure code review uitvoeren | Hoog | Verminderen van XSS/invoer risico's |
| Dependency scanning toevoegen | Middel | Sneller detecteren van kwetsbare libraries |
| Periodieke security review uitvoeren | Middel | Continue verbetering beveiliging |

# Samenvattende conclusie

De HTML Form Entry-module voldoet gedeeltelijk aan de onderzochte beveiligingscontroles.

De module bevat basisfunctionaliteit voor logging, gebruikt bestaande OpenMRS-beveiligingsmechanismen en maakt gebruik van externe dependencies. Er zijn echter verbeterpunten zichtbaar rondom:
- volledige audit logging
- aantoonbare secure coding maatregelen
- structureel beheer van technische kwetsbaarheden

## Eindbeoordeling

- **8.16 Monitoren van activiteiten** → gedeeltelijk aanwezig
- **8.28 Veilig programmeren** → gedeeltelijk aanwezig
- **8.8 Beheer technische kwetsbaarheden** → gedeeltelijk aanwezig

De belangrijkste verbeterpunten liggen bij het vergroten van aantoonbaarheid: niet alleen beveiliging






# Gap analyse logging

## Doel

Het doel van deze logging gap-analyse is om de logging binnen de HTML Form Entry-module van OpenMRS te inventariseren en te beoordelen tegen de eisen uit NEN-7510:2024-2 8.15 Logging.

Hierbij wordt gekeken naar:
- welke gebeurtenissen momenteel worden gelogd
- welke beveiligingsrelevante gebeurtenissen ontbreken
- welke gevoelige gegevens hierbij betrokken zijn
- het verschil tussen de huidige en gewenste situatie

## Huidige logging situatie

Uit de codeanalyse blijkt dat de HTML Form Entry-module gebruikmaakt van het bestaande OpenMRS loggingmechanisme via Log4j.

**Bewijs:**

**Bestand:** `api/src/main/resources/log4j.xml`

Aanwezig:
- Log4j configuratie
- logger instellingen

Daarnaast bevat:

**Bestand:** `api/src/main/java/org/openmrs/module/htmlformentry/FormEntrySession.java`

logging statements voor technische gebeurtenissen.

Voorbeeld:
```
log.info("FormEntrySession created: patient=" + ...)
```

Hieruit blijkt dat technische gebeurtenissen rondom formulierverwerking en sessies worden geregistreerd.

Er is echter geen aantoonbaar bewijs gevonden dat alle security-relevante gebeurtenissen worden gelogd.

## Logging inventarisatie gekoppeld aan attack surface

| Attack surface | Event | Gelogd? | Gevoelige data | Compliant met NEN-7510 8.15? |
|---|---|---|---|---|
| Formulierverwerking | FormEntrySession wordt aangemaakt | Ja | Mogelijk patiënt-ID | Gedeeltelijk |
| Applicatieproces | Technische fouten/exceptions | Ja | Systeeminformatie, mogelijk gebruikerscontext | Gedeeltelijk |
| Sessiebeheer | Starten formulierverwerking | Ja | Mogelijke patiëntcontext | Gedeeltelijk |
| Patiëntgegevens | Openen patiëntformulier | Niet aantoonbaar | Medische gegevens | Nee |
| Patiëntgegevens | Wijzigen formuliergegevens | Niet aantoonbaar | Gezondheidsinformatie | Nee |
| Patiëntgegevens | Opslaan medische gegevens | Niet aantoonbaar | Medische informatie | Nee |
| Authenticatie | Succesvolle login | Niet aantoonbaar binnen module | Accountgegevens | Nee |
| Authenticatie | Mislukte loginpoging | Niet aantoonbaar binnen module | Gebruikersnaam/IP | Nee |
| Autorisatie | Toegang geweigerd | Niet aantoonbaar | Gebruikersrechten | Nee |
| Security events | Verdachte invoer of aanvalspoging | Niet aantoonbaar | Ingevoerde gegevens | Nee |
| Configuratie | Wijzigingen aan module-instellingen | Niet aantoonbaar | Systeeminformatie | Nee |

## Analyse logging gap

De aanwezige logging binnen de module richt zich voornamelijk op technische werking en foutanalyse.

Aanwezig:
- technische logging via Log4j
- registratie van interne moduleprocessen
- foutmeldingen

Niet aantoonbaar aanwezig:
- volledige auditlogging van patiëntgegevens
- logging van gebruikersacties
- logging van autorisatiebesluiten
- logging van beveiligingsincidenten

Hierdoor ontbreekt een volledige audittrail.

Bij een incident is hierdoor beperkt vast te stellen:
- welke gebruiker een actie uitvoerde
- welke patiëntgegevens zijn geraadpleegd of gewijzigd
- wanneer een gebeurtenis plaatsvond

## Gat tussen huidige en gewenste situatie

| Onderdeel | Huidige situatie | Gewenste situatie volgens NEN-7510 8.15 | Gap |
|---|---|---|---|
| Technische logging | Aanwezig via Log4j | Technische gebeurtenissen moeten beschikbaar zijn voor monitoring | Klein |
| Auditlogging | Beperkt aantoonbaar | Acties moeten herleidbaar zijn tot gebruiker en moment | Groot |
| Patiëntgegevens logging | Niet aantoonbaar | Toegang en wijzigingen moeten traceerbaar zijn | Groot |
| Security logging | Niet aantoonbaar | Security-events moeten worden geregistreerd | Groot |
| Incidentonderzoek | Beperkt mogelijk | Volledige reconstructie mogelijk | Groot |

## Conclusie logging

De HTML Form Entry-module beschikt over basislogging, maar deze is voornamelijk gericht op technische ondersteuning.

Op basis van de analyse is de loggingfunctionaliteit gedeeltelijk aanwezig.

Het grootste verschil tussen de huidige en gewenste situatie is het ontbreken van aantoonbare audit- en securitylogging rondom patiëntgegevens, gebruikersactiviteiten en beveiligingsgebeurtenissen.