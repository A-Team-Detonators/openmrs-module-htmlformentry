## Trust Model

### Impliciet Vertrouwde Componenten

#### Vertrouwd: Form Author

De module gaat er impliciet van uit dat de auteur van een formulier betrouwbaar is.

Deze auteur mag:
- JavaScript toevoegen
- Programma-inschrijvingen wijzigen
- Patiëntstatus aanpassen
- Orders creëren
- Data manipuleren

Dit creëert een belangrijke trust boundary:

```
Form Author
     ↓ (trusted)
HTML Form Entry Engine
     ↓
OpenMRS Services
     ↓
Clinical Database
```

#### Vertrouwd: Client-side Data

Bij submit vertrouwt de module dat:
- verborgen velden niet gemanipuleerd zijn
- encounter-context correct is
- patiëntcontext geldig is

Dit vertrouwen moet worden gevalideerd door server-side controles.

#### Vertrouwd: OpenMRS Privilege Model

De module vertrouwt op privileges zoals:
- Form Entry
- Add Encounters
- Edit Encounters
- Program Management

Indien privileges verkeerd geconfigureerd zijn kan privilege escalation optreden.

### Attack Surface Overzicht

| Attack Surface | Beschrijving | Trust Boundary | Risico |
|---|---|---|---|
| Dependencies | Externe libraries, modules en frameworks waarvan HTML Form Entry afhankelijk is | OpenMRS ↔ Dependencies | High |
| Gebruikersinput | Alle invoervelden binnen formulieren (tekst, selecties, verborgen velden, uploads) | Browser ↔ OpenMRS | High |
| URL Parameters | Parameters zoals patientId, encounterId, returnUrl en andere requestparameters | Browser ↔ OpenMRS | High |
| JavaScript in Forms | JavaScript dat door formulierauteurs wordt toegevoegd aan formulieren | Form Author ↔ Browser | High |
| OpenMRS Core Services | Services voor patiënten, encounters, observaties, programma's en orders | Form ↔ OpenMRS | Medium |
| Zorgverleners | Gebruikers met klinische rollen die formulieren invullen of wijzigen | Browser ↔ OpenMRS | High |
| API Endpoints | REST-endpoints en controllers die formuliergegevens verwerken | Browser ↔ OpenMRS | Medium |