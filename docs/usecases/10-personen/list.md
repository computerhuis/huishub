# 10 — Personen: zoeken en overzicht

Onderdeel: personen (klanten, vrijwilligers en medewerkers). Tabellen: `persons`,
`postal_codes` (FK-doel van `persons.postal_code`), `person_roles`, koppeltabel
`donor_persons`; **geen eigen view** — overzichten query'en direct op `persons` (de tabbladen
van het dossier lezen wél views: `ticket_overview`, `timesheet_overview`). Schermen
(webapp-prototype): het personenoverzicht (`personListBean`) als enig overzichts- en
zoekscherm; het beheerformulier (nieuw, weergeven, wijzigen) hoort bij het persoonformulier
(`personCrudBean`) en is beschreven in `10-personen/crud.md`
(UC-PER-02 t/m 07). Rollen: conventies §1; zoekregels: conventies §3; soft delete en
"Toon gearchiveerd": conventies §2; wireframe-legenda: conventies §7.

**Webapp-variant:** het personenoverzicht uit het prototype (kolomlijst, kolomfilters,
paginering) is hier samengevoegd met de domeinregels voor fonetisch zoeken, foutmeldingen,
status-/archiefweergave en rijacties uit de eerdere klantzoek-use-case; openen en muteren
gebeurt via het persoonformulier.

---

## UC-PER-01 — Klant zoeken

**Actor:** Balie, Werkplaats, Rapportage (alleen lezen) · **Rollen:** ROLE_BALIE,
ROLE_WERKPLAATS, ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:** een bestaande persoon vinden vóór
aanmaak van een nieuw record, reparatie, uitgifte, dossierweergave of koppeling — de
centrale toegang tot `persons`. Vervangt het Access-zoekscherm "Gebruikers zoeken" én het
webapp-personenoverzicht in één scherm.

### Scherm — Klant zoeken (personenoverzicht)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Personen — klant zoeken                              [ Vernieuwen ]       │
├──────────────────────────────────────────────────────────────────────────┤
│ Achternaam [____________________ ]  Voornaam [______________ ]           │
│ Telefoon/E-mail [____________________ ]  [✓] Toon gearchiveerd           │
│ [ Zoeken ]                                                               │
├──────────────────────────────────────────────────────────────────────────┤
│ # (link) │ Voorl │ Voornaam │ Tussenv. │ Achternaam │ Adres │ Postcode    │
│ Woonplaats │ Telefoon (tel:) │ Mobiel (tel:) │ E-mail (mailto:)           │
│ Inschrijf datum (registered) │ Uitgeschreven datum (unregistered)         │
│ Status │ rijacties                                                        │
├──────────────────────────────────────────────────────────────────────────┤
│ 42 │ J. │ Johanna │ de │ Vries │ Molenstraat 12 │ 5211AB │ Den Bosch     │
│    │ 0731234567 │ 0612345678 │ jvries@example.nl │ 01-02-2024 │ — │ actief│
├──────────────────────────────────────────────────────────────────────────┤
│ Rijacties: [ Weergeven ] [ Wijzigen ] [ Bewijs/uitgifte ] [ Reparaties ]  │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Eerste ] [ Vorige ] [ 1 ] [ 2 ] [ Volgende ] [ Laatste ]  (10 per pag.)│
├──────────────────────────────────────────────────────────────────────────┤
│ Geen personen gevonden voor "bestaatniet".                                │
│ [ Nieuwe klant registreren ]                                              │
└──────────────────────────────────────────────────────────────────────────┘
```

Geen permanente "Nieuw"-knop op de lijst: een nieuw record start via het persoonformulier in
de modus Nieuw (UC-PER-02); alleen bij nul resultaten verschijnt de knop
"Nieuwe klant registreren".

### Acceptatiecriteria

```gherkin
Feature: Klant zoeken (personenoverzicht)

  Background:
    Er bestaan personen: "Johanna", infix "de", achternaam "Vries", voorletters "J.",
    e-mail jvries@example.nl, telefoon 0731234567, mobiel 0612345678, postcode 5211AB,
    straat Molenstraat 12, registered 01-02-2024; en "Jan", achternaam "Vriesen",
    telefoon 0731234567.

  Scenario: Tonen zonder zoekterm is niet toegestaan
    Gegeven ik ben ingelogd als ROLE_BALIE
    En ik ben op het scherm "Personen — klant zoeken"
    Wanneer ik op Zoeken klik zonder enige zoekterm
    Dan verschijnt de melding "Voer een zoekterm in."
    En er wordt geen lijst getoond (geen volledige tabeldump, conventies §3)

  Scenario: Zoeken op achternaam, exacte treffers eerst
    Wanneer ik "vries" invul in Achternaam en op Zoeken klik
    Dan verschijnt "Johanna de Vries" als eerste resultaat
    En "Jan Vriesen" staat er ook bij als fonetische treffer
    En gearchiveerde personen staan er niet bij (conventies §3)

  Scenario: Zoeken op achternaam én voornaam verfijnt
    Wanneer ik "vries" in Achternaam en "jan" in Voornaam invul en zoek
    Dan bevat het resultaat uitsluitend "Jan Vriesen"

  Scenario: Zoeken op telefoon (ook met opmaak)
    Wanneer ik "0612345678" invul in Telefoon/E-mail en op Zoeken klik
    Dan bevat het resultaat uitsluitend personen met mobiel of telefoon 0612345678
    Wanneer ik "06 1234 5678" invoer
    Dan wordt de term genormaliseerd naar 10 cijfers en is het resultaat hetzelfde
      (conventies §2)

  Scenario: Zoeken op e-mail
    Wanneer ik "jvries@example.nl" invul in Telefoon/E-mail en op Zoeken klik
    Dan bevat het resultaat uitsluitend personen met exact dit e-mailadres

  Scenario: Zoekterm korter dan 2 tekens
    Wanneer ik "d" invul in Achternaam en op Zoeken klik
    Dan verschijnt de melding "Voer minimaal 2 tekens in."

  Scenario: Geen resultaten
    Wanneer ik "bestaatniet" zoek
    Dan verschijnt "Geen personen gevonden voor "bestaatniet"."
    En verschijnt de knop "Nieuwe klant registreren" (UC-PER-02)

  Scenario: Toon gearchiveerd
    Gegeven "Karel Ouwehand" is gearchiveerd (unregistered gevuld, 30-06-2024)
    Wanneer ik "ouwehand" zoek zonder vinkje
    Dan bevat het resultaat "Karel Ouwehand" niet
    Wanneer ik "ouwehand" zoek mét vinkje
    Dan bevat het resultaat "Karel Ouwehand" wel, met Status "gearchiveerd"
      en de Uitgeschreven datum 30-06-2024

  Scenario: Kolomweergave en status
    Wanneer de lijst laadt
    Dan staat de rij met hoogste # bovenaan (# aflopend, koppeling naar het dossier)
    En toont Status "actief" zolang unregistered null is en "gearchiveerd" daarna
    En toont een lege cel met "—" voor onbekende waarden (telefoon, e-mail, toevoeging)

  Scenario: Links in de rij
    Wanneer ik op jvries@example.nl klik
    Dan opent de mailclient via mailto:jvries@example.nl
    Wanneer ik op 0612345678 klik
    Dan opent de bel-app via tel:0612345678
    En zijn lege telefoon-/e-mailvelden geen link

  Scenario: Zoekresultaat openen
    Wanneer ik bij een resultaat op "Weergeven" klik
    Dan opent het scherm "Persoon weergeven" (UC-PER-03) in leesmodus met alle velden
    Wanneer ik op "Wijzigen" klik
    Dan opent hetzelfde formulier in wijzigmodus (UC-PER-04)

  Scenario: Rijactie Bewijs/uitgifte
    Wanneer ik bij "Johanna de Vries" op "Bewijs/uitgifte" klik
    Dan opent het uitgiftescherm met deze klant alvast gekozen (UC-UIT-01)

  Scenario: Rijactie Reparaties
    Wanneer ik op "Reparaties" klik
    Dan opent het dossier UC-PER-03 met tabblad Tickets actief
    (vanuit de balie werkt de eigen stappenwizard: UC-BAL-04)

  Scenario: Paginering
    Gegeven er staan 123 personen in de lijst
    Dan toont de tabel 10 rijen per pagina met pagina-knoppen
    En nooit meer dan 50 rijen op één pagina (conventies §3)

  Scenario: Zonder Nieuw-knop
    Gegeven de lijst bevat geen "Nieuw"-knop
    Wanneer ik het persoonformulier in de modus Nieuw open
    Dan toont het scherm een leeg formulier met id en Inschrijf datum leeg (UC-PER-02)

  Scenario: Alleen-lezen rol
    Gegeven ik ben ingelogd als ROLE_RAPPORTAGE
    Dan werken Zoeken, Weergeven en Reparaties (lezen)
    En zijn Wijzigen en Bewijs/uitgifte niet zichtbaar
    En levert elke mutatie-request HTTP 403 "U hebt geen rechten voor dit scherm."
      (conventies §1)

  Scenario: Geen enkele autorisatie
    Gegeven de ingelogde persoon heeft geen rij in person_roles
    Dan verschijnt "U hebt geen rechten voor dit scherm." (conventies §1)
```

### Validaties

| Situatie | Melding |
|---|---|
| Geen zoekterm (alle drie de velden leeg) | `Voer een zoekterm in.` |
| Term < 2 tekens | `Voer minimaal 2 tekens in.` |
| Geen resultaten | `Geen personen gevonden voor "<term>".` + knop `Nieuwe klant registreren` |
| Zoekterm op telefoonveld niet genormaliseerd naar 10 cijfers | `Telefoonnummer moet 10 cijfers bevatten, bijvoorbeeld 0731234567.` |
| Aanroep zonder autorisatie op dit scherm | `U hebt geen rechten voor dit scherm.` (HTTP 403) |

### Technische notities

- Query: `SELECT … FROM persons WHERE (unregistered IS NULL OR :toonGearchiveerd) AND …`
  met AND-combinatie van de gevulde velden; `ORDER BY id DESC` als standaardsortering,
  kolommen sorteerbaar; `LIMIT/OFFSET` met standaard 10 rijen per pagina en hard maximum 50
  (conventies §3).
- Fonetisch zoeken (conventies §3, leidend boven het prototype dat alleen `LIKE` gebruikte):
  `WHERE last_name LIKE :term% OR SOUNDEX(last_name) = SOUNDEX(:term)
  OR SOUNDEX(first_name) = SOUNDEX(:term)`; exacte treffers (`last_name LIKE :term%`) sorteren
  op `last_name`, fonetische treffers daarna. Implementatie `SOUNDEX()` in MariaDB of equivalent;
  gebruik `soundslike()`/een fonetische index als die beschikbaar is — dezelfde uitkomst.
- Secundaire velden: Voornaam → `first_name LIKE :term%`; Telefoon → na normalisatie naar 10
  cijfers `telephone = :genormaliseerd OR mobile = :genormaliseerd`; E-mail →
  `email = :term` (collation `utf8_unicode_ci`, dus hoofdletterongevoelig). Een gevuld
  Achternaam-veld is nooit verplicht als Telefoon/E-mail een geldige term bevat (conventies §3:
  achternaam primair, voornaam/telefoon/e-mail secundair).
- Kolommen en datavelden: # → `id` (link naar het persoonsdossier van het betreffende record,
  leesmodus = UC-PER-03),
  Voorl → `initials`, Voornaam → `first_name`, Tussenv. → `infix`, Achternaam → `last_name`,
  Adres → `street` + `house_number` + `house_number_addition`, Postcode → `postal_code`,
  Woonplaats → `city`, Telefoon → `telephone`, Mobiel → `mobile`, E-mail → `email`,
  Inschrijf datum → `registered`, Uitgeschreven datum → `unregistered`
  (NL-formaat `dd-MM-yyyy`, conventies §2), Status → afgeleid uit `unregistered`.
- Rijacties en zichtbaarheid (conventies §1): Weergeven/Reparaties voor alle rollen (lezen);
  Wijzigen voor ROLE_BALIE en ROLE_ADMIN (ROLE_WERKPLAATS opent een beperkt formulier, zie
  UC-PER-04); Bewijs/uitgifte alleen ROLE_BALIE/ROLE_ADMIN (werkplaats mag uitgifte niet
  afronden). Rollenvinkjes/acties ontbreken volledig voor ROLE_RAPPORTAGE.
- `p:dataTable` met `p:column`-filters op Achternaam, Woonplaats, Inschrijf datum en Status;
  zoekzone erboven is een `p:form` met `p:commandButton` + `update` op de tabel en
  `p:messages`; `p:autoComplete` is toegestaan in schermen die een persoon binnen een ander
  proces kiezen (intake, uitgifte, tickets) — dezelfde zoekregels, nooit een volledige tabel
  in een dropdown (conventies §8).
- Geen exportknop op dit scherm (conventies §6): export van personen bestaat uitsluitend in
  UC-RAP-05 (mailinglijst) en UC-SET-03 (`17-instellingen/vrijwilligers.md`, vrijwilligers
  met Excel-export).
- Archiefvolgorde: standaard gefilterd op `unregistered IS NULL` (conventies §2); het vinkje
  "Toon gearchiveerd" voegt de gearchiveerde rijen toe met indicator — nooit `DELETE`, geen
  tellende "verwijderen"-actie op dit scherm (archiveren = UC-PER-05).
