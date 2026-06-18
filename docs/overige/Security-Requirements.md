## Security Backlog

| ID | Security Backlog item | Bron | Risico | Prioriteit |
|---|---|---|---|---|
| SB-1 | Audit logging implementeren voor wijzigingen aan patiëntgegevens | Gap analyse 8.16 | Manipulatie medische gegevens, Datalek medische informatie | Hoog |
| SB-2 | Logging van toegangsweigeringen implementeren | Gap analyse 8.16 | Ongeautoriseerde inzage patiëntgegevens | Hoog |
| SB-3 | Logging van wijzigingen aan formulierdefinities toevoegen | Gap analyse 8.16 | Wijziging formulierdefinities | Hoog |
| SB-4 | Security-events definiëren die verplicht gelogd moeten worden | Gap analyse 8.16 | Onvoldoende logging | Middel |
| SB-5 | Security code review uitvoeren op formulierverwerking | Gap analyse 8.28 | SQL Injection, XSS | Hoog |
| SB-6 | Inputvalidatie van gebruikersinvoer standaardiseren | Gap analyse 8.28 | Manipulatie medische gegevens, XSS | Hoog |
| SB-7 | XSS-testen uitvoeren op invoervelden en formulieroutput | Gap analyse 8.28 | Cross-Site Scripting | Hoog |
| SB-8 | SQL Injection testen uitvoeren op invoervelden | Gap analyse 8.28 | SQL Injection | Hoog |
| SB-9 | Secure coding richtlijnen opstellen voor toekomstige wijzigingen | Gap analyse 8.28 | Diverse ontwikkelrisico's | Middel |
| SB-10 | Dependency vulnerability scan uitvoeren op Maven dependencies | Gap analyse 8.8 | Verouderde softwarecomponenten | Hoog |
| SB-11 | Inventarisatie maken van alle externe libraries en versies | Gap analyse 8.8 | Supply-chain risico | Middel |
| SB-12 | Proces opstellen voor periodieke CVE-controles | Gap analyse 8.8 | Technische kwetsbaarheden | Middel |
| SB-13 | Procedure opstellen voor dependency updates | Gap analyse 8.8 | Verouderde softwarecomponenten | Middel |
| SB-14 | RBAC (Role Based Access Control) testen voor alle formulierfunctionaliteit | Bow-tie + toegangsrisico | Ongeautoriseerde toegang tot patiëntgegevens | Hoog |
| SB-15 | Autorisatiecontroles per formulieractie documenteren | Bow-tie + toegangsrisico | Te ruime rechten / privilege misbruik | Hoog |
| SB-16 | Least privilege controle uitvoeren op gebruikersrollen | Bow-tie | Misbruik gebruikersrechten | Hoog |
| SB-17 | Logging uitbreiden zodat gebruikersacties herleidbaar zijn (wie, wat, wanneer) | Bow-tie + 8.16 | Onvoldoende controleerbaarheid | Hoog |
| SB-18 | Monitoring uitbreiden voor verdachte gebruikersactiviteiten | Bow-tie + 8.16 | Accountmisbruik / ongeautoriseerde acties | Middel |
| SB-19 | Validatiecontroles toevoegen om foutieve medische registratie te voorkomen | Bow-tie + 8.28 | Verkeerde patiëntgegevens / medische fouten | Middel |
| SB-20 | Dependency patchproces opstellen voor kwetsbare componenten | Bow-tie + 8.8 | Kwetsbare softwarecomponenten | Hoog |