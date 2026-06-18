# Penetratietestplan HTML Form Entry Module (OpenMRS)

## Doel

Het doel is vaststellen of kwetsbaarheden binnen de module kunnen leiden tot schending van:
- Vertrouwelijkheid
- Integriteit
- Beschikbaarheid

van patiëntgegevens.

De test richt zich op de risico's met de hoogste risicoscore uit de risicoanalyse en security backlog.

## Scope

### In Scope
- HTML Form Entry Module
- Formulierbeheer
- Formuliervalidatie
- Gebruikersinvoer
- Autorisatiecontroles
- JavaScript-functionaliteit
- Integratie met OpenMRS permissies

### Out of Scope
- OpenMRS
- Productieomgeving
- Netwerkinfrastructuur
- Externe koppelingen
- Fysieke beveiliging

## Testmethodiek

Gebaseerd op:
- OWASP Web Security Testing Guide
- OWASP Top 10
- NEN-7510

## Risico's binnen scope

| ID | Risico | Binnen scope |
|---|---|---|
| R1 | Ongeautoriseerde inzage patiëntgegevens | Ja |
| R2 | Datalek medische informatie | Ja |
| R3 | Manipulatie medische gegevens | Ja |
| R4 | Verlies medische gegevens | Nee |
| R5 | Misbruik gebruikersaccounts | Nee |
| R6 | Foutieve autorisaties | Ja |
| R7 | Wijziging formulierdefinities | Ja |
| R8 | Privilege escalation | Ja |
| R9 | Uitval applicatie | Ja |
| R10 | Verouderde componenten | Ja |
| R11 | Malware/ransomware | Nee |
| R12 | Menselijke invoerfouten | Ja |
| R13 | Verwijderen gegevens | Ja |
| R14 | Phishing | Nee |
| R15 | Interne medewerkers | Ja |
| R16 | Back-up falen | Nee |
| R17 | Netwerkcompromittering | Nee |
| R18 | Onvoldoende encryptie | Nee |
| R19 | Integratiefouten | Ja |
| R20 | AVG/GDPR non-compliance | Ja |
| R21 | SQL Injection | Ja |
| R22 | Cross Site Scripting | Ja |

## Pen tests

### PT-01 Autorisatiecontrole

**Doel:** Controleren of gebruikers uitsluitend toegang hebben tot toegestane formulieren en functies.

**Risico's:** R1, R6, R8, R15

**NEN 7510:**
- 5.15 Toegangsbeheersing
- 8.2 Bevoorrechte toegangsrechten

**Test:**
- Gebruiker met lage rechten
- Directe URL-manipulatie
- Force browsing
- Manipuleren van requests

**Verwacht resultaat:** Geen toegang zonder juiste rol.

---

### PT-02 Formulierdefinitie manipulatie

**Doel:** Controleren of gebruikers uitsluitend toegang hebben tot toegestane formulieren en functies.

**Risico's:** R3, R7

**NEN 7510:**
- 8.32 Wijzigingsbeheer

**Test:** Analyse van htmlForm.jsp en formulierbeheer.

**Verwacht resultaat:** Gebruikers met beheerrechten kunnen direct formulierlogica wijzigen.

---

### PT-03 Cross-Site Scripting (XSS)

**Doel:** Controleren of invoer correct wordt gevalideerd.

**Risico's:** R1, R2, R22

**NEN 7510:**
- 8.28 Secure coding
- 8.29 Security testing

**Payload:**
```html
<script>alert('XSS')</script>
```

**Testlocaties:**
- Tekstvelden
- Formulierlabels
- Vrije tekst observaties

**Verwacht resultaat:** Payload wordt ge-escaped.

---

### PT-04 SQL Injection

**Doel:** Controleren op onveilige databasequery's.

**Risico's:** R21

**NEN 7510:**
- 8.28 Secure coding

**Payload:**
```sql
' OR '1'='1
```

**Testlocaties:**
- Zoekvelden
- Filtervelden
- Patiëntselectie

**Verwacht resultaat:** Geen invloed op query-uitvoering.

---

### PT-05 Verouderde componenten

**Doel:** Controleren of gebruikte softwarecomponenten geen bekende kwetsbaarheden bevatten en veilig worden beheerd.

**Risico's:** R10

**NEN 7510:**
- 8.8 Technical vulnerability management

**Test:** Analyse Maven dependencies.

**Verwacht resultaat:** Geen verouderde componenten waar vulnerabilities in zitten.

---

### PT-06 Beschikbaarheid

**Doel:** Controleren of de applicatie beschikbaar blijft bij fouten of verstoringen en of beveiligingsincidenten tijdig worden gedetecteerd.

**Risico:** R9

**NEN 7510:**
- 8.14 Redundantie
- 8.16 Monitoring

**Test:** Review foutafhandeling.

**Verwacht resultaat:** Applicatie handelt fouten gecontroleerd af zonder crash. Foutmeldingen geven geen gevoelige informatie vrij. Monitoring en logging signaleren beschikbaarheidsproblemen.

---

### PT-07 Integriteit medische gegevens

**Doel:** Controleren of medische gegevens correct worden gevalideerd, beschermd tegen ongeautoriseerde wijzigingen en betrouwbaar worden opgeslagen.

**Risico's:** R3, R12, R13

**NEN 7510:**
- 8.28 Secure coding
- 8.32 Change management

**Test:** Review van validatie- en opslaglogica.

**Verwacht resultaat:** Medische gegevens kunnen alleen door bevoegde gebruikers worden aangepast. Invoer wordt gevalideerd en wijzigingen worden gecontroleerd en gelogd. Ongeautoriseerde wijzigingen worden geblokkeerd.