# 15 — Tijdsregistratie: overzichten

Onderdeel: tijdsregistratie en aanwezigheid. Tabellen: `timesheets`, `activities`, `persons`;
view: `timesheet_overview` (timesheets + activiteitsnaam + naam van de persoon). Schermen:
het tijdregistratieoverzicht (`timesheetListBean`: de twee overzichten hieronder) en
het tijdregistratieformulier (`timesheetCrudBean`: registreren en corrigeren — zie
`15-tijdsregistratie/crud.md`, UC-TID-03 t/m UC-TID-05).

**Semantiek (onveranderd):** een bezoek = één `timesheets`-rij: `registered` = binnenkomst
(verplicht, deel van de composite PK), `unregistered` = vertrek (`null` zolang de persoon er
nog is). Vervangt Access `Tbl_Tijdsregistratie` (Tijd Binnengekomen / Tijd weggegaan +
Activiteit).

**Webapp-variant:** het tijdregistratieoverzicht (overzicht + per persoon) ·
het tijdregistratieformulier (nieuwe registratie en correctie).

---

## UC-TID-01 — Tijdregistratie-overzicht

**Actor:** Balie, Rapportage · **Rollen:** ROLE_BALIE, ROLE_WERKPLAATS, ROLE_RAPPORTAGE
(alleen lezen), ROLE_ADMIN · **Doel:** alle bezoeken in een periode in beeld, met filters op
persoon, activiteit en periode, totalen uren en export (view `timesheet_overview`; vervangt
Access "Aanwezigheidsoverzicht").

### Scherm — Tijdregistratie

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Tijdregistratie                             [ Bezoek registreren ]        │
├──────────────────────────────────────────────────────────────────────────┤
│ Van [ ■■■■■■■■ ]  Tot [ ■■■■■■■■ ]  Bezoeker [ zoek klant ▾ ]             │
│ Activiteit [ alle ▾ ]  [✓] Alleen aanwezig nu  [✓] Toon gearchiveerd      │
│ [ Zoeken ]                                        [ Excel-export ]         │
├──────────────────────────────────────────────────────────────────────────┤
│ Datum/tijd   │ Bezoeker [🔍]      │ Activiteit │ Binnen │ Uit  │ Duur │ actie │
│ 03-05 10:40  │ Johanna de Vries    │ Klik & Tik │ 10:40  │ 11:35│ 0:55 │ [✎]  │
│ 03-05 10:12  │ Mark Jansen         │ Vrije Inl. │ 10:12  │ —    │ nu   │ [uit] │
├──────────────────────────────────────────────────────────────────────────┤
│ 1204 rijen · Totaal duur 612:40 · waarvan open 3 (meetelt tot nu: 1:12)   │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Tijdsregistratie-overzicht

  Background:
    Gegeven bezoeken: "Johanna de Vries" op "Klik & Tik" 03-05-2026 10:40–11:35,
      "Mark Jansen" op "Vrije Inloop" 03-05-2026 10:12–(null) en
      "Mark Jansen" op "Werkplaats" 01-01-2026 09:00–12:00

  Scenario: Standaardperiode
    Wanneer ik het scherm open
    Dan staat Van op 1 januari van dit jaar en Tot op vandaag

  Scenario: Tonen
    Dan toont de tabel alle timesheets-rijen uit de periode via timesheet_overview,
      10 rijen per pagina, gesorteerd op registered aflopend

  Scenario: Duur berekenen
    Gegeven binnenkomst 10:40 en vertrek 11:35
    Dan toont de kolom Duur "0:55"
    En bij unregistered null toont die "nu"

  Scenario: Totaaluren
    Wanneer ik het resultaat heb opgehaald
    Dan toont de voetregel "Totaal duur" als som van alle gefilterde rijen, in uren:minuten
    En vermeldt "waarvan open" het aantal rijen met unregistered null plus hun
      opgetelde duur tot het huidige tijdstip
    En worden de totalen over de volledige selectie berekend, niet over de huidige pagina

  Scenario: Filter op periode
    Wanneer ik Van 01-05-2026 en Tot 03-05-2026 invul en Zoeken klik
    Dan bevat de lijst uitsluitend rijen waarvan registered binnen [Van 00:00, Tot 23:59:59] valt

  Scenario: Periode ongeldig
    Wanneer ik Van 03-05-2026 en Tot 01-05-2026 invoer
    Dan verschijnt "De begindatum ligt na de einddatum."
    En wordt de vorige selectie behouden

  Scenario: Filter op bezoeker
    Wanneer ik "jansen" in Bezoeker zoek en kies (p:autoComplete, zoekregels UC-BAL-01)
    Dan bevat de lijst uitsluitend bezoeken van die persoon
    En toont "Tijdregistratie van Mark Jansen" als verfijning boven de tabel

  Scenario: Filter op activiteit
    Wanneer ik bij Activiteit "Klik & Tik" kies
    Dan bevat de lijst uitsluitend rijen met die activity_id

  Scenario: Alleen aanwezig nu
    Wanneer ik "Alleen aanwezig nu" aanzet
    Dan bevat de lijst uitsluitend rijen met unregistered null
    En toont de voetregel uitsluitend "waarvan open"

  Scenario: Toon gearchiveerd
    Gegeven persoon "Karel Ouwehand" is gearchiveerd en heeft één bezoek
    Wanneer ik met "Toon gearchiveerd" zoek
    Dan bevat de lijst zijn bezoek wel, met indicator "bezoeker gearchiveerd"

  Scenario: Per persoon openen
    Wanneer ik op het zoekpictogram bij een bezoeker klik
    Dan opent het tijdregistratieformulier voor die persoon (UC-TID-02)

  Scenario: Bezoek registreren
    Wanneer ik op "Bezoek registreren" klik
    Dan opent het tijdregistratieformulier in de modus Nieuw (UC-TID-03)

  Scenario: Rij corrigeren
    Gegeven ingelogd als ROLE_ADMIN
    Wanneer ik bij een rij op het bewerkpictogram klik
    Dan opent het tijdregistratieformulier voor het betreffende record (UC-TID-05)
    En bij ROLE_BALIE, ROLE_WERKPLAATS of ROLE_RAPPORTAGE is dat pictogram er niet

  Scenario: Uitchecken vanuit het overzicht
    Gegeven ingelogd als ROLE_BALIE en een open bezoek van vandaag
    Wanneer ik bij die rij op [uit] klik
    Dan opent UC-TID-04 met die rij voorgevuld

  Scenario: Export
    Wanneer ik op Excel-export klik
    Dan download ik een .xlsx met de kolommen Datum, Tijd binnen, Tijd uit, Duur,
      Bezoeker, Activiteit (volledige selectie, niet alleen de pagina)

  Scenario: Gegevens uit de view
    Dan komen bezoeker-, activiteits- en persoonsnamen uit timesheet_overview
      (niet uit een eigen join in de query)

  Scenario: Leeg resultaat
    Gegeven geen enkele rij voldoet aan de filters
    Dan verschijnt "Geen bezoeken gevonden in de geselecteerde periode."
    En blijft de voetregel op "0:00"

  Scenario: Rollen
    Gegeven ingelogd als ROLE_RAPPORTAGE
    Dan zijn "Bezoek registreren", het bewerkpictogram en [uit] niet aanwezig
    En zijn filters, duurkolom, totalen en Excel-export wel beschikbaar
```

### Validaties

| Situatie | Melding / gedrag |
|---|---|
| Van > Tot | `De begindatum ligt na de einddatum.` (niets opnieuw ophalen) |
| Geen resultaten | `Geen bezoeken gevonden in de geselecteerde periode.` |
| Bezoeker-filter leeg of niet gekozen | Alle personen in de periode |
| Activiteit-filter `alle` | Geen filtering op `activity_id` |
| Mutaties door ROLE_RAPPORTAGE | Niet beschikbaar; directe toegang tot het formulier → HTTP 403, `U hebt geen rechten voor dit scherm.` (conventies §1) |

### Technische notities

- Query uitsluitend op de view:
  `SELECT ts.person_id, ts.activity_id, ts.registered, ts.unregistered, ts.activity_name,
  ts.first_name, ts.infix, ts.last_name FROM timesheet_overview ts
  WHERE ts.registered BETWEEN :van AND :tot + 1 dag [+ filters] ORDER BY ts.registered DESC`.
  De view levert exact deze kolommen (`timesheets.*` + `activity_name`, `first_name`,
  `infix`, `last_name`).
- **`dco`-schema-prefix:** de view bevat `LEFT JOIN dco.persons i ON i.id = ts.person_id`.
  Als de applicatieconnectie niet op schema `dco` zit, faalt deze view. Dit wordt
  expliciet overgenomen als technische notitie: **schema niet wijzigen** — de oplossing ligt
  in de omgeving/connectie (SPECIFICATIE §6.7 punt 7; schema `dco` is in productie
  verondersteld).
- Duur wordt in de applicatielaag berekend; de view bevat geen duurkolom:
  `unregistered - registered`, weergave `H:mm` (of `HH:mm`); open bezoek → label "nu" en
  duur tot `NOW()`. De kolom toont nooit een negatieve duur (correctiemelding: UC-TID-05).
- Totaallen moeten over de **volledige gefilterde set** lopen (aparte tellende query of
  aggregatie), niet over de huidige pagina; anders klopt "Totaal duur" niet bij paginering.
- Filters: `registered` binnen [Van 00:00:00, Tot 23:59:59]; `person_id = :person`;
  `activity_id = :activity`; "Alleen aanwezig nu" → `unregistered IS NULL`. De
  "Toon gearchiveerd"-regel uit conventies §2 geldt hier op de **bezoeker**; bezoeken zelf
  worden nooit standaard gewist (conventies §2: optellingen).
- PrimeFaces: `p:datePicker` op Van/Tot, `p:autoComplete` op Bezoeker (zoekregels UC-BAL-01:
  max 50 resultaten; dit is een personenzoekopdracht), `p:selectOneMenu` op Activiteit
  (alleen actieve activiteiten, UC-EDU-01), `p:selectBooleanCheckbox` voor "Alleen aanwezig
  nu", `p:dataTable` met kolomkoppen sorteerbaar, `p:commandButton` Zoeken/Excel-export.
- Activiteiten zonder `reporting`-vlag zijn hier gewoon zichtbaar; de vlag bepaalt alleen
  of ze in UC-RAP-04 meetellen.
- Export via POI (conventies §6 noemt export alleen waar de use-case het expliciet noemt;
  dit is zo'n use-case).

---

## UC-TID-02 — Tijdregistratie per persoon

**Actor:** Balie, Werkplaats, Rapportage (alleen lezen) · **Rollen:** ROLE_BALIE,
ROLE_WERKPLAATS, ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:** het volledige bezoekverloop van één
persoon in beeld. Dit scherm wordt bereikt vanaf UC-PER-03 ("Persoon weergeven", tab
Aanwezigheid) en via de zoekknop per rij in UC-TID-01.

### Scherm — Tijdregistratie per persoon

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Tijdregistratie — Johanna de Vries (nr 42)  [ naar persoon ]             │
├──────────────────────────────────────────────────────────────────────────┤
│ Activiteit-id (link) │ Activiteit │ Inschrijf datum  │ Uitgeschreven  │ Duur │
│ 1 (link)             │ Klik & Tik │ 03-05-2024 10:15 │ 03-05-2024 11:00│ 0:45 │
│ 1 (link)             │ Klik & Tik │ 03-05-2026 10:40 │ 03-05-2026 11:35│ 0:55 │
│ 2 (link)             │ Vrije Inloop│ 03-05-2026 13:00 │ — (aanwezig)    │ nu   │
├──────────────────────────────────────────────────────────────────────────┤
│ 14 bezoeken · Totaal duur 12:35 · waarvan open 1                         │
│ [ Bezoek registreren ]  [ Registratie corrigeren ]  [ Excel-export ]     │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Tijdregistratie per persoon

  Background:
    Gegeven "Johanna de Vries" (nr 42) heeft 14 timesheets-rijen, waarvan 1 open

  Scenario: Tonen
    Wanneer ik het scherm open
    Dan toont de tabel de bezoeken van deze persoon, gesorteerd op registered aflopend

  Scenario: Openen vanuit het overzicht
    Wanneer ik in UC-TID-01 op het zoekpictogram bij "Johanna de Vries" klik
    Dan opent dit scherm met titel "Tijdregistratie — Johanna de Vries (nr 42)"

  Scenario: Openen vanuit het klantdossier
    Wanneer ik in UC-PER-03 tab Aanwezigheid kies en "Tijdregistratie" open
    Dan toont dit scherm dezelfde lijst

  Scenario: Naar persoon
    Wanneer ik op "naar persoon" klik
    Dan opent het persoonformulier van die persoon (UC-PER-03)

  Scenario: Activiteit openen
    Wanneer ik op het activiteit-id in de eerste kolom klik
    Dan opent het activiteitformulier voor die activiteit (UC-EDU-02, alleen ROLE_ADMIN;
      voor andere rollen is het getal geen link)

  Scenario: Duur per rij
    Gegeven binnenkomst 10:40 en vertrek 11:35
    Dan toont de kolom Duur "0:55"
    En bij open bezoek "nu" plus een indicator "aanwezig"

  Scenario: Totalen
    Dan toont de voetregel het aantal bezoeken, de totale duur en "waarvan open"
      (berekend over alle rijen van deze persoon, niet over de pagina)

  Scenario: Bezoek toevoegen
    Wanneer ik op "Bezoek registreren" klik
    Dan opent het tijdregistratieformulier voor die persoon (UC-TID-03)
      met de bezoeker voorgevuld

  Scenario: Vertrek vastleggen
    Gegeven een rij met Uitgeschreven datum "— (aanwezig)"
    Wanneer ik op "Registratie corrigeren" klik en die rij kies
    Dan opent UC-TID-04/UC-TID-05 met die rij voorgevuld

  Scenario: Leeg
    Gegeven de persoon heeft nog geen bezoeken
    Dan verschijnt "Deze persoon heeft nog geen bezoeken geregistreerd."
    En is "Bezoek registreren" beschikbaar

  Scenario: Gearchiveerde persoon
    Gegeven de persoon is gearchiveerd
    Dan toont het scherm "Deze klant is gearchiveerd. Heropen het record eerst (beheerder)."
    En is "Bezoek registreren" geblokkeerd (conventies §5)

  Scenario: Export
    Wanneer ik Excel-export klik
    Dan download ik een .xlsx met de bezoeken van deze persoon
      (kolommen als in het overzicht)
```

### Validaties

| Situatie | Melding / gedrag |
|---|---|
| Onbekend `personId` | `Persoon <id> bestaat niet.` + terug naar UC-TID-01 |
| Persoon gearchiveerd | Waarschuwing; nieuwe registratie geblokkeerd (UC-TID-03) |
| Geen bezoeken | `Deze persoon heeft nog geen bezoeken geregistreerd.` |
| Open bezoek aanwezig | Rij met indicator "aanwezig"; geen blokkade voor andere acties |

### Technische notities

- Query: `SELECT ts.*, ts.activity_name FROM timesheet_overview ts WHERE ts.person_id = :person
  ORDER BY ts.registered DESC` — of rechtstreeks op `timesheets` met dezelfde kolomnamen
  (`person_id`, `activity_id`, `registered`, `unregistered`); namen komen uit de view
  (met de `dco.persons`-prefix zoals bij UC-TID-01 genoteerd).
- De kolom "Activiteit-id (link)" gebruikt `activity_id` en linkt naar UC-EDU-02; de view
  levert ook `activity_name`
  (null als de activiteit fysiek verwijderd is — de FK `ON DELETE CASCADE` op
  `timesheets.activity_id` kan dan bezoeken laten verdwijnen; rapporteer een ontbrekende
  naam als "— (gewist)").
- Het webapp-prototype gaf op deze pagina knoppen "Annuleren" en "Opslaan" en bewerkte de
  rijen inline (Activiteit, Inschrijf datum, Uitgeschreven datum); die aanpak is hier
  vervangen door lezen + acties naar UC-TID-03 (nieuwe registratie) en UC-TID-04/05
  (vertrek vastleggen en corrigeren), zodat validaties op één plek zitten. De prototype-
  pagina verwees bovendien naar `timesheetListBean.list` en `personCrudBean.save`; de
  implementatie gebruikt de eigen timesheet-beans (bugfix uit de bron, hier overgenomen).
- PrimeFaces: `p:dataTable` met paginering, `p:commandButton` voor de drie acties,
  `p:exporter` of POI-knop voor Excel; geen invoervelden op dit scherm (lezen).
