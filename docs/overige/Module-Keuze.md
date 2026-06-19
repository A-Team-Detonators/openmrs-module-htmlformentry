## Gekozen module

**Naam:** HTML Form Entry (htmlformentry)
**Versie:** 3.10.0
**Broncode:** [OpenMRS HTML Form Entry GitHub Repository](https://github.com/openmrs/openmrs-module-htmlformentry)

## Motivatie voor de keuze

Voor dit onderzoek is gekozen voor de HTML Form Entry-module van OpenMRS. Deze module wordt gebruikt voor het definiëren en verwerken van medische formulieren binnen het elektronisch patiëntendossier en vormt daarmee een belangrijk onderdeel van de gegevensinvoer in OpenMRS.

De keuze voor deze module is gebaseerd op de volgende factoren:

- **Geschikte complexiteit:** De module bevat ongeveer 45.033 regels code en heeft een totale complexiteit van 6.139, waarvan 5.851 Java-complexiteit. Hierdoor is de module voldoende complex om interessante kwaliteits- en securityproblemen te bevatten, zonder dat de omvang onrealistisch groot wordt voor het onderzoek.
- **Voornamelijk Java-code:** Het grootste deel van de functionaliteit is geïmplementeerd in Java. Dit maakt de codebase relatief consistent en beter analyseerbaar voor refactoring, code quality-analyses en security-audits.
- **Kritieke functionaliteit:** De module verwerkt formulieren waarin patiëntgegevens worden ingevoerd en gevalideerd. Fouten of kwetsbaarheden in deze functionaliteit kunnen invloed hebben op de integriteit en betrouwbaarheid van medische gegevens.
- **Aanwezigheid van technische schuld:** Door de omvang, leeftijd en complexiteit van de module worden code smells, onderhoudsproblemen en mogelijke beveiligingsrisico's verwacht. Dit maakt de module geschikt voor het identificeren van verbeterpunten en het uitvoeren van refactoringvoorstellen.
- **Onderzoekswaarde:** De combinatie van complexe businesslogica, veel Java-code en kritieke functionaliteit maakt de module uitdagend genoeg om een diepgaande analyse van softwarekwaliteit en security uit te voeren.