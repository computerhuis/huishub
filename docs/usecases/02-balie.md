# 02 — Balie (klantdossier in vier stappen)

Onderdeel: balie-werkproces. Schermen: de baliewizard (`balieBean`, stepper
"Zoek klant · Klantgegevens · Apparatuur · Tickets"); snelkoppelingen naar het
uitgiftescherm (UC-UIT-01) en de intakewizard (UC-INT-01). Leest `persons`, `equipment`, `tickets`;
mutaties gebeuren in de CRUD-use-cases en de intake.

Dit is de thuisbasis van "Persoon weergeven": het dossier van één klant inclusief
samengevatte activiteiten (vervangt Access "Gebruikersgegevens" + "Laatste reparatie").

**Webapp-variant:** de baliewizard uit het prototype (4 stappen), samengevoegd met de
domeinregels fonetisch zoeken en dossierweergave.

---

## UC-BAL-01 — Stap 1: klant zoeken

**Actor:** Balie · **Doel:** bestaande klant vinden vóór reparatie, uitgifte of dossierweergave.

### Scherm — Balie, stap 1

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Balie                                              [ Uitgave ] [ Intake ] │
│ Zoek een klant en bekijk de gegevens, apparatuur en tickets              │
├──────────────────────────────────────────────────────────────────────────┤
│ Stap 1 Zoek klant                                                        │
│ Achternaam [____________________ ]  Voornaam [______________ ]           │
│ Telefoon/E-mail [____________________ ]  [✓] Toon gearchiveerd           │
│ [ Zoeken ]                                                               │
├──────────────────────────────────────────────────────────────────────────┤
│ Nr │ Voorl │ Naam            │ Adres           │ Postcode │ Telefoon     │
│ 42 │ J.    │ J. de Vries     │ Molenstraat 12  │ 5211AB   │ 0612345678  │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Deze klant kiezen ▾ ]                        [ Volgende ]              │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Balie — klant zoeken

  Background:
    Er bestaan personen: "Johanna", infix "de", achternaam "Vries", telefoon 0612345678,
    postcode 5211AB; en "Jan", achternaam "Vriesen", telefoon 0731234567.

  Scenario: Zoeken op achternaam, exacte treffers eerst
    Gegeven ik ben ingelogd als ROLE_BALIE
    Wanneer ik "vries" invul in Achternaam en op Zoeken klik
    Dan verschijnt "Johanna de Vries" als eerste resultaat
    En "Jan Vriesen" staat er ook bij als fonetische treffer
    En gearchiveerde personen staan er niet bij (conventies §3)

  Scenario: Zoeken op telefoon of e-mail
    Wanneer ik "0612345678" invul in Telefoon/E-mail en zoek
    Dan bevat het resultaat uitsluitend "Johanna de Vries"
    Wanneer ik "jvries@example.nl" invoer
    Dan bevat het resultaat uitsluitend personen met exact dit e-mailadres

  Scenario: Leeg zoeken
    Wanneer ik op Zoeken klik zonder enige zoekterm
    Dan verschijnt "Voer een zoekterm in." en er wordt geen lijst getoond

  Scenario: Zoekterm korter dan 2 tekens
    Wanneer ik "d" invul in Achternaam
    Dan verschijnt "Voer minimaal 2 tekens in."

  Scenario: Geen resultaten
    Wanneer ik "bestaatniet" zoek
    Dan verschijnt "Geen klanten gevonden voor "bestaatniet"."
      en de knop "Nieuwe klant registreren" (UC-PER-02)

  Scenario: Toon gearchiveerd
    Gegeven "Karel Ouwehand" is gearchiveerd (unregistered gevuld)
    Wanneer ik met vinkje zoek
    Dan bevat het resultaat "Karel Ouwehand" wel, met indicator "gearchiveerd"

  Scenario: Klant kiezen
    Wanneer ik bij een resultaat op "Deze klant kiezen" klik
    Dan staat "Gekozen klant: <volledige naam>" bovenaan
    En wordt de knop "Volgende" beschikbaar
    En gaat het scherm naar stap 2 bij Volgende
```

### Validaties en foutmeldingen

| Situatie | Melding |
|---|---|
| Geen zoekterm | `Voer een zoekterm in.` |
| Term < 2 tekens | `Voer minimaal 2 tekens in.` |
| Geen resultaten | `Geen klanten gevonden voor "<term>".` + knop `Nieuwe klant registreren` |

### Technische notities

- Query op `persons` waar `unregistered IS NULL` (tenzij filter); maximaal 50 rijen per pagina.
- Fonetisch zoeken: `SOUNDEX(last_name) = SOUNDEX(:term) OR SOUNDEX(first_name) =
  SOUNDEX(:term)` naast `last_name LIKE :term%`; exacte eerst. (Het webapp-prototype gebruikte
  alleen `LIKE`; de conventies §3 zijn leidend.)
- `p:autoComplete` is toegestaan in plaats van knop+lijst in schermen die een klant binnen een
  ander proces kiezen (intake, uitgifte); dezelfde zoekregels.
- Kolommen in de lijst: id, initials, naam (initials + first_name + infix + last_name), straat +
  huisnummer, postal_code, telephone, mobile.

---

## UC-BAL-02 — Stap 2: klantgegevens (dossier)

**Actor:** Balie · **Doel:** alle gegevens van de klant in beeld, met samengevatte activiteit.

### Scherm — Balie, stap 2

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Stap 2 Klantgegevens          Gekozen klant: Johanna de Vries            │
├───────────────────────────┬──────────────────────────────────────────────┤
│ Naam  Johanna de Vries (nr 42) [ open in CRUD ]                          │
│ Telefoon 0731234567   Mobiel 0612345678                                   │
│ E-mail jvries@example.nl (mailto-link)                                    │
│ Adres   Molenstraat 12, 5211AB Den Bosch                                  │
├───────────────────────────┼──────────────────────────────────────────────┤
│ Laatste reparatie : 12-05-2026 OPEN                                      │
│ Laatste bezoek    : 03-05-2026 Klik & Tik                                 │
│ Laatste uitgifte  : geen                                                 │
├───────────────────────────┴──────────────────────────────────────────────┤
│ [ Vorige ]                                        [ Volgende ]           │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Balie — klantgegevens

  Scenario: Gegevens tonen
    Dan toont het paneel naam, telefoon, mobiel, e-mail (mailto) en adres
      (straat huisnummer toevoeging, postcode plaats)
    En toont "Laatste activiteit" per categorie de meest recente datum

  Scenario: Wijzigen
    Wanneer ik op de naam-link klik
    Dan opent het persoonsdossier van die persoon (UC-PER-03)

  Scenario: Gearchiveerde klant
    Gegeven de klant is gearchiveerd
    Dan verschijnt "Deze klant is gearchiveerd. Heropen het record eerst (beheerder)."
    En is Volgende geblokkeerd (conventies §5)
```

### Technische notities

- "Laatste reparatie": laatste rij uit `ticket_overview` waar `equipment.owner_id = :person;
  "Laatste bezoek": laatste `timesheets`-rij; "Laatste uitgifte": laatste `proof_of_issues`
  waar `recipient_id = :person`.
- Read-only; mutaties lopen via person CRUD (UC-PER-03/04).

---

## UC-BAL-03 — Stap 3: apparatuur van de klant

### Scherm — Balie, stap 3

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Stap 3 Apparatuur                                                        │
├──────────────────────────────────────────────────────────────────────────┤
│ Nr (link) │ Categorie │ Fabrikant │ Model      │ Serienummer │ Status     │
│ 890       │ DESKTOP   │ HP        │ dc7800     │ —           │ Geschikt…  │
│ Geen apparatuur bekend (bij leeg)                                        │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Vorige ]                                        [ Volgende ]           │
└──────────────────────────────────────────────────────────────────────────┘
```

```gherkin
Feature: Balie — apparatuur van klant

  Scenario: Lijst apparatuur
    Dan toont de tabel alle apparaten waarvan owner_id de gekozen klant is,
      met status als tag (Nederlandse label uit UC-EQP-05)
    En "Geen apparatuur bekend" bij leeg

  Scenario: Apparaat openen
    Wanneer ik op het apparaatnummer klik
    Dan opent het apparaatformulier van dat apparaat (UC-EQP-04)
```

---

## UC-BAL-04 — Stap 4: tickets van de klant en afronden

### Scherm — Balie, stap 4

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Stap 4 Tickets                                                           │
├──────────────────────────────────────────────────────────────────────────┤
│ Nr (link) │ Apparaat (link) │ Type    │ Inname    │ Status   │ Onderwerp  │
│ 4521      │ 890             │ REPAIR  │ 01-04-2026│ OPEN     │ start niet │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Vorige ]                                        [ Afronden ]           │
└──────────────────────────────────────────────────────────────────────────┘
```

```gherkin
Feature: Balie — tickets van klant

  Scenario: Tickets tonen
    Dan toont de tabel alle tickets van de apparaten van de klant
      (via ticket_overview + equipment.owner_id)

  Scenario: Ticket openen
    Wanneer ik op het ticketnummer of apparaatnummer klik
    Dan openen respectievelijk het ticketformulier (UC-TIC-03) en
      het apparaatformulier

  Scenario: Afronden
    Wanneer ik op "Afronden" klik
    Dan sluit de wizard af en keert terug naar stap 1 met de klantgewist
```

---

## UC-BAL-05 — Snelkoppelingen

```gherkin
Feature: Balie — snelkoppelingen

  Scenario: Uitgave
    Wanneer ik op "Uitgave" klik
    Dan opent het uitgiftescherm (UC-UIT-01)

  Scenario: Nieuwe intake
    Wanneer ik op "Nieuwe intake" klik
    Dan opent de intakewizard voor een nieuwe reparatie (UC-INT-01)

  Scenario: Nieuwe klant zonder zoekresultaat
    Wanneer ik vanuit "Geen klanten gevonden" op "Nieuwe klant registreren" klik
    Dan opent het persoonformulier in de modus Nieuw (UC-PER-02) en keert de wizard na
      opslaan terug met de nieuwe klant gekozen
```
