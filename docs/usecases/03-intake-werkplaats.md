# 03 — Intake werkplaats (nieuwe reparatie)

Onderdeel: werkplaats-werkproces. Schermen: de intakewizard (`newWizardBean`,
3-steps wizard), startend vanaf `werkplaats/list` (UC-TIC-02). Schrijft in één transactie
`persons` (optioneel), `equipment`, `tickets` + `ticket_status`. Tabellen: `tickets`,
`ticket_status`, `ticket_log`, `equipment`, `persons`.

**Webapp-variant:** de intakewizard uit het prototype (3 stappen), aangevuld met de
domeinvalidaties (bestaande reparaties, dubbele klant, bewijs van inname).

---

## UC-INT-01 — Stap 1: klant

**Actor:** Werkplaats, Balie · **Rollen:** ROLE_WERKPLAATS, ROLE_BALIE, ROLE_ADMIN ·
**Doel:** bestaande klant zoeken en kiezen of een nieuwe registreren.

### Scherm — Nieuwe intake, stap 1

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Nieuwe intake                                                            │
│ ① Klant  ② Apparaat  ③ Werkzaamheden                                    │
├──────────────────────────────────────────────────────────────────────────┤
│ Klant gegevens                                                           │
│ Zoek klant: Voornaam [________ ] Achternaam [________ ]  [ Zoeken ]      │
│ Resultaten (5 per pagina) ▾  [ Deze klant kiezen ▾ ]                     │
│ [ Nieuwe klant ]                                                         │
├──────────────────────────────────────────────────────────────────────────┤
│ Klantgegevens (bewerkbaar na kiezen of bij Nieuwe klant):                │
│ Voornaam* [________ ]  Tussenvoegsels [____ ]  Achternaam* [________ ]   │
│ Telefoon [________ ]  Mobiel [________ ]  E-mailadres [________ ]        │
│ Straat [________ ]  Huisnummer [____ ]  Toevoeging [___ ]                │
│ Postcode [______ ]  Woonplaats [________ ]                               │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Annuleren ]                                    [ Volgende ]            │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Intake — klant

  Scenario: Bestaande klant zoeken en kiezen
    Wanneer ik voornaam/achternaam invul en Zoeken klik
    Dan toont de resultatenlijst (5 per pagina) de klanten volgens zoekconventies §3
    En kies ik er een met de pijl-knop; dan verschijnt het bewerkbare klantgegevensformulier

  Scenario: Nieuwe klant
    Wanneer ik op "Nieuwe klant" klik
    Dan verschijnt hetzelfde formulier leeg (velden zoals in het wireframe)

  Scenario: Verplichte velden en doorgaan
    Gegeven Voornaam en Achternaam zijn verplicht (required=true)
    Wanneer die zijn gevuld en ik klik Volgende
    Dan gaat het scherm naar stap 2

  Scenario: Mogelijke dubbele klant
    Gegeven er bestaat al "Johanna de Vries" met dezelfde geboorte-/adresgegevens niet gevraagd
    Wanneer ik een nieuwe klant aanmaak die overeenkomt met een bestaande
    Dan verschijnt "Mogelijk bestaat deze persoon al: <naam>. Controleer voor u doorgaat."
    En kan ik alsnog doorgaan

  Scenario: Kiezen is geen bewijs van aanwezigheid
    Dan slaat deze stap niets op; opslaan gebeurt pas bij UC-INT-04
```

### Technische notities

- Zoeken volgt dezelfde regels als UC-BAL-01 (`p:autoComplete` of zoekformulier).
- Validaties van het nieuwe-klantformulier: tabel uit UC-PER-02 (verplichte voornaam/
  achternaam, postcode-bestand, telefoon 10 cijfers, e-mail-formaat).

---

## UC-INT-02 — Stap 2: apparaat

**Doel:** bestaand apparaat van de klant kiezen of een nieuw apparaat vastleggen.

### Scherm — Nieuwe intake, stap 2

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Apparaat gegevens                                                        │
│ Apparaat [ zoek op fabrikant/model/serienummer ▾ ]  [ nieuw apparaat ]   │
├──────────────────────────────────────────────────────────────────────────┤
│ (alleen bij nieuw apparaat)                                              │
│ Type kast* [ DESKTOP ▾ ]   Fabrikant* [____________ ]                    │
│ Model nummer* [____________ ]   Serienummer [____________ ]              │
├──────────────────────────────────────────────────────────────────────────┤
│ Specificaties (één veld per sleutel uit equipment.specification):         │
│ Processor [________ ]  Geheugen [____ ]  HDD/SSD [________ ]             │
│ Software [________ ]  Bijzonderheden [________ ]  Prijs [______ ]        │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Vorige ]                                      [ Volgende ]             │
└──────────────────────────────────────────────────────────────────────────┘
```

```gherkin
Feature: Intake — apparaat

  Scenario: Bestaand apparaat kiezen
    Wanneer ik een apparaat van de klant selecteer
    Dan blijven categorie, fabrikant, model en serienummer ongewijzigd

  Scenario: Nieuw apparaat
    Wanneer ik "nieuw apparaat" kies en Type kast, Fabrikant en Model invul
    Dan wordt bij opslaan het apparaat opgeslagen met owner = de klant en
      status CUSTOMER_OWNED (statusstroom UC-EQP-05)

  Scenario: Verplichte velden
    Gegeven Type kast, Fabrikant en Model zijn verplicht
    Wanneer een van die ontbreekt en ik klik Volgende
    Dan verschijnen de veldmeldingen uit UC-EQP-03

  Scenario: Specificaties
    Dan toont "Specificaties" één invoerveld per sleutel uit
      equipment.specification (beginset uit conventies §4.3, dynamisch uitgebreid)
```

### Technische notities

- Uniek serienummer: dubbele invoer blokkeert met de melding uit UC-EQP-07.
- Kiezen van een apparaat dat al een OPEN-reparatie heeft blokkeert met de melding uit
  UC-INT-04.

---

## UC-INT-03 — Stap 3: werkzaamheden

**Doel:** het probleem vastleggen; dit wordt het ticket.

### Scherm — Nieuwe intake, stap 3

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Omschrijf werkzaamheden                                                  │
│ Type* [ REPAIR ▾ ]   ( REPAIR | ISSUE )                                  │
│ Onderwerp [________________________________ ]                            │
│ Omschrijving [                                                     ]      │
│                                                                          │
│ Details (één veld per sleutel uit tickets.details):                      │
│ Backup [ GEEN ▾ ]  Tafel [__ ]  Bijgeleverde accessoires [________ ]    │
│ Accountwachtwoord [ ingevuld ja/nee ]  E-mailgegevens [________ ]       │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Vorige ]                                   [ Opslaan ]                 │
└──────────────────────────────────────────────────────────────────────────┘
```

```gherkin
Feature: Intake — werkzaamheden

  Scenario: Ticketinvulling
    Dan bevat het paneel Type* (tickettype-enum), Onderwerp, Omschrijving* en
      één invoerveld per sleutel uit tickets.details (backup, tafel, accessoires …)

  Scenario: Backup-enum
    Wanneer ik Backup "DEEL" kies
    Dan bevat tickets.details.backup "DEEL"
      (enum: GEEN | GEHELE | DEEL | ONBEKEND, conventies §4.4)

  Scenario: Accountwachtwoord
    Gegeven account_wachtwoord een plaintext-veld van de legacy is
    Dan toont het scherm alleen "ingevuld ja/nee" en nooit de waarde (conventies §4.4)
```

---

## UC-INT-04 — Opslaan, validatie en afronding

**Actor:** Werkplaats, Balie · **Doel:** intake transactueel wegschrijven.

```gherkin
Feature: Intake opslaan

  Scenario: Opslaan
    Wanneer ik op "Opslaan" klik (stap 3)
    Dan wordt één transactie opgeslagen: persoon (nieuw of bestaand), apparaat
      (owner = persoon, status CUSTOMER_OWNED bij nieuw apparaat) en ticket type
      REPAIR met een OPEN-statusregel (volunteer_id = huidige gebruiker)
    En wordt bij een opgegeven omschrijving een "[SAMENVATTING] <omschrijving>"-logregel
      aangemaakt
    En keer ik terug naar het werkplaatsoverzicht (UC-TIC-02)

  Scenario: Bestaande reparaties controleren (handleiding-stap 3)
    Gegeven de klant heeft 2 open tickets
    Wanneer ik op "bestaande reparaties" open (optie in zoekstap)
    Dan toont een lijst die tickets (UC-TIC-01 gefilterd op deze klant)
    En kan ik een bestaand ticket openen in plaats van een nieuw aan te maken

  Scenario: Apparaat heeft al een open reparatie
    Gegeven het gekozen apparaat heeft een ticket met status OPEN of IN_PROGRESS
    Wanneer ik Opslaan klik
    Dan verschijnt "Dit apparaat heeft al een open reparatie (ticket <id>). Open die eerst."

  Scenario: Klant gearchiveerd
    Wanneer de gekozen klant gearchiveerd is
    Dan verschijnt "Deze klant is gearchiveerd. Heropen het record eerst (beheerder)."

  Scenario: Annuleren
    Wanneer ik op "Annuleren" klik (elke stap)
    Dan wordt de wizard afgebroken zonder iets op te slaan

  Scenario: Vorige
    Wanneer ik op "Vorige" klik (stap > 1)
    Dan keert het scherm terug met de ingevulde waarden behouden

  Scenario: Bewijs van inname na afloop
    Gegeven de intake is opgeslagen
    Wanneer ik "Bewijs van inname afdrukken" kies (optionele knop na opslaan)
    Dan opent UC-UIT-01 met klant, apparaat en ticket voorgevuld en soort "inname"
      (geen statusneveneffect; koppeling via details.proof_of_issue_id)
```

### Technische notities

- Volgorde in de transactie: person → equipment → tickets → ticket_status → ticket_log;
  bij falen rolt alles terug.
- "Aangenomen door" mag afwijken van de ingelogde gebruiker; beide personen moeten bestaan.
- Details-sleutels volgen conventies §4.4; onbekende sleutels bewaren onder `_overig`.
- Het prototype droeg geen "Bewijs van inname"-knop over; deze is conform de handleiding
  (2-voudig, getekend) toegevoegd als optionele eindstap.
