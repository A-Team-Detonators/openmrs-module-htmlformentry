## CI/CD Risico-evaluatie

Voor het CI/CD-proces is eerst een risico-evaluatie uitgevoerd. Daarbij zijn zes belangrijke risico's geïdentificeerd, zoals kwetsbare dependencies, onveilige code en fouten die niet door tests worden ontdekt.

Voor elk risico is de impact bepaald aan de hand van de NEN7510-impactmatrix. Daarbij is gekeken naar zes aspecten: patiëntveiligheid, privacy, financiële schade, reputatie, beschikbaarheid en compliance. Van deze zes scores is het gemiddelde genomen om de uiteindelijke impact te bepalen.

Vervolgens is die impact vermenigvuldigd met de kans dat het risico optreedt. Zo ontstond het initiële risico. Daarna is gekeken welke maatregelen al aanwezig zijn in de CI/CD-pipeline en opnieuw het residuele risico berekend.

Hieruit bleek dat het toevoegen van kwetsbare dependencies het meest kritieke risico is. Zonder maatregelen scoorde dit risico 12, maar door maatregelen zoals Dependency Review en OSV Scan daalt het residuele risico naar 4.
![Risicoanalyse_CI-CD](images/risicoanalyse_CI-CD.png)
## Bow-tie Analyse

Voor het meest kritieke risico is vervolgens een bow-tie-analyse gemaakt.

In het midden staat het top event: dat kwetsbare of onveilige software via de CI/CD-pipeline wordt opgeleverd.

Links zijn de bedreigingen te zien die dit kunnen veroorzaken, zoals kwetsbare dependencies, verboden licenties en onveilige code. Daarnaast staan de preventieve maatregelen die dit moeten voorkomen, zoals Dependency Review, CodeQL en Maven Tests.

Rechts staan de correctieve maatregelen. Deze beperken de gevolgen als het top event toch plaatsvindt. Denk aan OSV Scan, SARIF-opvolging en het beschikbaar hebben van een SBOM. Helemaal rechts staan de mogelijke consequenties, zoals een kwetsbaarheid in productie of auditproblemen.

De bow-tie laat duidelijk zien dat niet alleen wordt geprobeerd incidenten te voorkomen, maar dat er ook voorbereidingen zijn getroffen om de impact te beperken als er toch iets misgaat.
![Bow-Tie_CI-CD](images/bow-tie-CI-CD.png)
