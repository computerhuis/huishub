# 17 — Instellingen: vrijwilligersoverzicht

Onderdeel: instellingen (vrijwilligers). Tabellen: `persons` (met `person_roles` en
`persons.msaccess`, conventies §4.1). Schermen: het vrijwilligersoverzicht
(`volunteerListBean`); wijzigen van vrijwilligersgegevens gebeurt in de personen-CRUD
(UC-PER-03 t/m 05). Dit overzicht vervangt de Access-lijsten "List_Vrijwilligers
afdrukken" en "proeftijd".

**Webapp-variant:** het vrijwilligersoverzicht uit het prototype, samengevoegd met het
periodefilter en de exportregels uit de domein-use-case; de start-/einddatum uit
`msaccess` krijgen hier wél kolommen (open punt uit het webapp-prototype, nu ingevuld).

---

## UC-SET-03 — Vrijwilligersoverzicht

**Actor:** Balie, Rapportage · **Rollen:** ROLE_BALIE, ROLE_RAPPORTAGE, ROLE_ADMIN ·
**Doel:** lijst van actieve en voormalige vrijwilligers, met start-/einddatum,
rollen, filter op periode en export.

### Scherm — Vrijwilligers

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Instellingen — Vrijwilligers                                             │
│ Periode van [ ■■-■■-████ ] tot [ ■■-■■-████ ]  [ Zoeken ] [ Wissen ]     │
├──────────────────────────────────────────────────────────────────────────┤
│ ⌕ │ Nr │ Vrijwilliger        │ Vanaf     │ Tot       │ Rollen   │ Status │
│ ⌕ │ 42 │ Johanna de Vries    │ 01-09-2020│ —         │ BALIE    │ actief │
│ ⌕ │ 57 │ Karel Ouwehand      │ 01-02-2018│ 30-06-2024│ WERKPLAATS│gestopt│
├──────────────────────────────────────────────────────────────────────────┤
│ [ Excel-export ]  [ CSV-export ]                                         │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Vrijwilligersoverzicht

  Background:
    Gegeven personen bestaan met msaccess.vrijwilligerswerk_start:
      "Johanna de Vries" (01-09-2020, geen einddatum, rol ROLE_BALIE),
      "Karel Ouwehand" (01-02-2018, einddatum 30-06-2024, rol ROLE_WERKPLAATS)
      en "Mieke Jansen" (vrijwilliger zonder person_roles-rol)
      en klant "Piet Bakker" zonder vrijwilligersgegevens

  Scenario: Actieve vrijwilligers (standaardweergave)
    Gegeven ik ben ingelogd als ROLE_BALIE
    Wanneer ik zonder periode op Zoeken klik
    Dan toont de lijst personen met msaccess.vrijwilligerswerk_start niet-null
      en (msaccess.vrijwilligerswerk_eind null of eind >= vandaag)
    En bevat de lijst "Johanna de Vries" en "Mieke Jansen"
    En bevat de lijst "Karel Ouwehand" niet (gestopt)
    En bevat de lijst "Piet Bakker" niet (geen vrijwilligersindicatie)

  Scenario: Periode-filter
    Wanneer ik periode 01-01-2018 tot 31-12-2018 invoer en zoek
    Dan toont de lijst vrijwilligers waarvan de startdatum binnen die periode viel

  Scenario: Periode inclusief gestopte vrijwilligers
    Gegeven ik heb een periode ingevuld
    Dan bevat het resultaat ook gestopte vrijwilligers (einddatum in het verleden)
      waarvan de startdatum in de periode valt

  Scenario: Kolommen
    Dan toont de tabel per vrijwilliger: zoekpictogram, Nr (persons.id),
      volledige naam (initials + first_name + infix + last_name),
      Vanaf (msaccess.vrijwilligerswerk_start), Tot (msaccess.vrijwilligerswerk_eind),
      Rollen (autoriteiten uit person_roles) en Status (actief/gestopt)

  Scenario: Start- en einddatum zichtbaar
    Gegeven start/einddatum zitten in persons.msaccess (conventies §4.1)
    Dan tonen de kolommen "Vanaf"/"Tot" die datums in NL-formaat dd-MM-yyyy (conventies §2)
    En toont "Tot" een streepje ("—") als einddatum null is

  Scenario: Vrijwilliger zonder rol
    Gegeven "Mieke Jansen" heeft een startdatum maar geen person_roles-rol
    Dan toont de kolom Rollen "—"
    En staat zij wel in het overzicht (startdatum is leidend, niet de rol)

  Scenario: Openen in personen-CRUD
    Wanneer ik op het zoekpictogram bij een vrijwilliger klik
    Dan opent het persoonsdossier van die vrijwilliger (UC-PER-03)

  Scenario: Excel-export
    Wanneer ik op "Excel-export" klik
    Dan download ik een .xlsx met kolommen Nr, Naam, Start, Eind, Rollen,
      E-mail, Telefoon
    En bevat de export exact het gefilterde resultaat van de lijst

  Scenario: Zoeken zonder periode toont ook stoppers via filter
    Wanneer ik "Wissen" klik
    Dan staat de lijst weer op de standaardweergave (alleen actieve vrijwilligers)

  Scenario: Geen rechten
    Gegeven een gebruiker zonder ROLE_BALIE/RAPPORTAGE/ADMIN opent het scherm
    Dan krijgt die HTTP 403 en de melding "U hebt geen rechten voor dit scherm."
      (conventies §1)

  Scenario: Gearchiveerde vrijwilliger
    Gegeven een vrijwilliger is gearchiveerd (persons.unregistered gevuld)
    Dan staat die standaard niet in de lijst (conventies §2)
```

### Validaties

| Situatie | Melding |
|---|---|
| Einddatum vóór startdatum | `De einddatum valt vóór de startdatum. Controleer de invoer.` (op personen-CRUD, UC-PER-03; in dit overzicht alleen waarschuwingsindicator) |
| Periode van > tot | `De begindatum valt na de einddatum.` (zoekopdracht wordt niet uitgevoerd) |
| Geen rechten | HTTP 403 + `U hebt geen rechten voor dit scherm.` (conventies §1) |
| Export zonder resultaat | `Geen vrijwilligers om te exporteren.` (geen leeg bestand downloaden) |

### Technische notities

- **Vrijwilligersindicatie (implementatie-afspraak):** `persons.msaccess` bevat
  `"vrijwilligerswerk_start"` niet-null (JSON-sleutel uit conventies §4.1), óf de persoon
  heeft minimaal één rij in `person_roles`. De startdatum uit `msaccess` is leidend voor
  de kolommen "Vanaf"/"Tot"; `person_roles.authority` voedt alleen de kolom Rollen.
- Query: `persons` LEFT JOIN `person_roles` (geaggregeerd, comma-gescheiden authorities),
  JSON-extractie op `msaccess -> '$.vrijwilligerswerk_start'` resp.
  `'$.vrijwilligerswerk_eind'` (MariaDB `JSON_EXTRACT`); standaardfilter
  `unregistered IS NULL` (conventies §2).
- Status afleiden: `eind IS NULL OR eind >= CURDATE()` → "actief", anders "gestopt"
  (Nederlandse label in de UI; geen enum in het schema).
- Periode-filter werkt op de **startdatum**: `start >= :van AND start <= :tot`;
  een ingevulde periode heft het "alleen actieve" filter op (gestopte vrijwilligers met
  startdatum in de periode verschijnen wel).
- Kolommen in de tabel: zoekpictogram (opent het persoonsdossier, UC-PER-03),
  `persons.id` (#), volledige naam (initials + first_name + infix + last_name, patroon
  UC-BAL-01), `msaccess.vrijwilligerswerk_start` (Vanaf), `msaccess.vrijwilligerswerk_eind`
  (Tot), Rollen (`person_roles.authority`, Nederlandse labels Beheerder/Balie/Werkplaats/
  Rapportage uit conventies §1), Status.
- Export via Apache POI (in projectopgenomen dependency); xlsx-kolommen: Nr, Naam, Start,
  Eind, Rollen, E-mail (`persons.email`), Telefoon (`persons.telephone`; valt `mobile` leeg,
  dan `persons.mobile` als aanvulling). De export volgt het actieve filter
  van de lijst; dit is de enige exportknop op dit scherm (conventies §6).
- Dit scherm is read-only: wijzigen van vrijwilligersgegevens (start/einddatum in
  `msaccess`, rollen) gebeurt in de personen-CRUD (UC-PER-03 t/m 05); het overzicht
  bevat geen bewerkvelden.
- `p:datePicker` voor de periode in NL-formaat dd-MM-yyyy; `p:dataTable` met
  kolomfilters en sortering op naam en datums (conventies §6/§8).
