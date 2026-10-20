# 11 — Donateurs: overzicht

Onderdeel: schenkers/donateurs — bedrijven, organisaties en particulieren die apparaten
schenken of namens cliënten betalen ("derde betaalt"). Dit bestand beschrijft de **lees- en
zoekslag**; aanmaken, wijzigen, archiveren en koppelen staan in `crud.md` (UC-DON-02 t/m 04).
Tabellen: `donors` (donateurs), `donor_persons` (koppeling naar `persons`), voor
postcodevalidatie `postal_codes`, en lezend `equipment` (`donor_id`, `behalf_of_id`) en
`proof_of_issues.behalf_of_id`. Views: **geen** — de viewset in
`src/main/resources/db/changelog` (`ticket_overview`, `ticket_status_overview`,
`ticket_log_overview`, `timesheet_overview`, `equipment_history`) kent geen donor-view, dus
alle overzichten query'en direct op `donors` respectievelijk `equipment`. Schermen:
het schenkeroverzicht (`donorListBean`), het schenkerformulier (`donorCrudBean`,
leesmodus vanuit dit bestand) en de apparatuurlijst van de schenker
(`donorEquipmentListBean`).

**Webapp-variant:** de prototypeschermen voor het schenkeroverzicht en de apparatuurlijst
van de schenker, samengevoegd met de domeinregels (zoekgedrag, rollen,
archiefstatus) uit de schenkersuse-cases.

---

## UC-DON-01 — Schenker zoeken

**Actor:** Balie, Rapportage (alleen lezen) · **Rollen:** ROLE_BALIE, ROLE_RAPPORTAGE,
ROLE_ADMIN · **Doel:** bedrijf, organisatie of particuliere schenker vinden vóór giften- of
uitgifteverwerking, en het donateursoverzicht raadplegen.

### Scherm — Schenkers (donateurs)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Schenkers (donateurs)                                                    │
├──────────────────────────────────────────────────────────────────────────┤
│ Naam [_________________________________ ]  [✓] Toon gearchiveerd         │
│ [ Zoeken ]   [ Nieuwe schenker ]                                         │
├──────────────────────────────────────────────────────────────────────────┤
│ #  │ Naam                  │ Rapportage │ Inschrijf  │ Uitgeschreven     │
│ 31 │ Gemeente 's-Hertogenb │ [✓]        │ 12-03-2024 │ —                 │
│ 12 │ Ros-Kabelkrant        │ [ ]        │ 04-01-2023 │ 01-06-2025        │
│ 44 │ Stichting Digimaat    │ [✓]        │ 21-09-2022 │ 14-02-2026  (arch)│
├──────────────────────────────────────────────────────────────────────────┤
│  # = donors.id (link naar CRUD, aflopend) · Rapportage = donors.reporting │
│  (vinkje, disabled) · Inschrijf = registered · Uitgeschreven = unregistered│
├──────────────────────────────────────────────────────────────────────────┤
│ 1-10 van 87    Per pagina [ 10 ▾ ]    [ vorige ] [ volgende ]            │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Weergeven ] [ Wijzigen ] [ Apparatuur ] [ Personen ] [ Giften ]        │
│ [ Cliënten (uitgifte) ]                                                   │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Donateursoverzicht en schenker zoeken

  Background:
    Er bestaan schenkers: "Ros-Kabelkrant" (id 12, reporting false, plaats Den Bosch),
    "Gemeente 's-Hertogenbosch" (id 31, reporting true) en "Stichting Digimaat"
    (id 44, reporting true, unregistered gevuld).

  Scenario: Tonen van alle donateurs
    Gegeven ik ben ingelogd als ROLE_BALIE
    En ik sta op het schenkeroverzicht
    Dan toont de tabel alle niet-gearchiveerde schenkers, 10 per pagina
    En staat de hoogste id bovenaan (sortering # aflopend)

  Scenario: Kolomopbouw
    Dan bevat elke rij: # (id, link naar de CRUD), Naam, Rapportage-vinkje (disabled),
      Inschrijfdatum (registered), Uitgeschreven datum (unregistered) en de
      pictogramknop "Apparatuurlijst"

  Scenario: Zoeken op deel van naam
    Wanneer ik "ros" invul in Naam en op Zoeken klik
    Dan bevat de lijst "Ros-Kabelkrant"
    En staan treffers waarin "ros" de hele naam is boven resultaten die het deel
      "ros" alleen bevatten

  Scenario: Zoeken op bedrijfsnaam zonder resultaat
    Wanneer ik "bestaatniet" zoek
    Dan verschijnt "Geen schenkers gevonden." en de knop "Nieuwe schenker"
      (opent UC-DON-02)

  Scenario: Leeg zoekveld zonder vinkje
    Wanneer ik op Zoeken klik zonder zoekterm en zonder "Toon gearchiveerd"
    Dan verschijnt "Voer een zoekterm in." en wordt geen lijst getoond
      (conventies §3)

  Scenario: Leeg zoekveld met vinkje gearchiveerd
    Wanneer ik alleen het vinkje "Toon gearchiveerd" aanzet en zoek met lege naam
    Dan toont de lijst uitsluitend gearchiveerde schenkers
    En draagt elke rij het label "gearchiveerd"

  Scenario: Zoeken met vinkje gearchiveerd
    Gegeven "Stichting Digimaat" is gearchiveerd (unregistered gevuld)
    Wanneer ik met vinkje op "digimaat" zoek
    Dan bevat het resultaat "Stichting Digimaat" wél, met label "gearchiveerd"
    Wanneer ik zonder vinkje zoek
    Dan bevat het resultaat "Stichting Digimaat" niet (conventies §2)

  Scenario: Rapportagevinkje is leesbaar
    Dan staat het vinkje in de kolom Rapportage disabled
    En telt een schenker met reporting true mee in het schenkersrapport (UC-RAP-06)
    En is wijzigen van het vinkje alleen mogelijk in de CRUD (UC-DON-03)

  Scenario: Apparatuurlijst per donateur
    Wanneer ik op de pictogramknop in de kolom Apparatuurlijst klik
    Dan opent de apparatuurlijst van de schenker met de koptekst van die donateur (UC-DON-05)

  Scenario: Rij openen in de CRUD
    Wanneer ik op het #-nummer in een rij klik
    Dan opent het schenkerformulier in leesmodus (UC-DON-03)

  Scenario: Gearchiveerde schenker openen
    Gegeven de gekozen schenker is gearchiveerd
    Wanneer ik op Weergeven of Wijzigen klik
    Dan toont het scherm de status "gearchiveerd" en blokkeert opslaan tot het record
      heropend is (ROLE_ADMIN, procedure UC-PER-05)

  Scenario: Gekoppelde personen tonen
    Wanneer ik bij een resultaat op Personen of Cliënten (uitgifte) klik
    Dan opent de koppellijst van die schenker (UC-DON-04)
    En opent de gekoppelde persoon in het persoonsdossier (UC-PER-03)

  Scenario: Giften van de schenker
    Wanneer ik op Giften klik
    Dan opent de apparatuurlijst van die schenker (UC-DON-05)
    En verwijst het scherm door naar het schenkersrapport (UC-RAP-06) voor
      jaarcijfers en export

  Scenario: Nieuwe schenker
    Wanneer ik op Nieuwe schenker klik (ook vanuit "Geen schenkers gevonden.")
    Dan opent het schenkerformulier in de modus Nieuw (UC-DON-02)

  Scenario: Alleen lezen als ROLE_RAPPORTAGE
    Gegeven ik ben ingelogd als ROLE_RAPPORTAGE
    Dan staan "Nieuwe schenker" en "Wijzigen" niet ter beschikking
    En openen Personen en Cliënten (uitgifte) de koppellijst uitsluitend leesbaar
      (koppelen en verwijderen disabled, UC-DON-04)
    En blijven Zoeken, Weergeven, #, Apparatuurlijst en Giften beschikbaar
```

### Validaties

| Situatie | Melding |
|---|---|
| Leeg zoekveld zonder "Toon gearchiveerd" | `Voer een zoekterm in.` |
| Geen resultaten | `Geen schenkers gevonden.` + knop `Nieuwe schenker` |
| Zoeken met alleen het vinkje "Toon gearchiveerd" | geen foutmelding; lijst toont uitsluitend `unregistered IS NOT NULL` |
| Geen autorisatie op het scherm | `U hebt geen rechten voor dit scherm.` (HTTP 403, conventies §1) |
| Paginagrens overschreden | geen melding; pagina-knoppen na de eerste 50 rijen (conventies §3) |

### Technische notities

- Query op `donors`; standaard `WHERE unregistered IS NULL` (conventies §2/§6), met het filter
  "Toon gearchiveerd" als `OR unregistered IS NOT NULL` (dan `unregistered IS NOT NULL` als
  uitsluitende selectie bij leeg zoekveld, zie AC).
- Naam zoeken **niet-fonetisch**: `name LIKE '%<term>%'` met exacte treffers vóór
  deeldelen (`name LIKE '<term>%'` eerst sorteren). De fonetiekregel uit conventies §3 geldt
  voor personen (`persons`); bedrijfsnamen worden op letters getoetst.
- Kolommen in de lijst: `id`, `name`, `reporting`, `registered`, `unregistered`, `city`
  (Plaats); de kolommen `email`, `mobile`, `telephone` uit het eerste schermodel worden in de
  detailweergave getoond — niet alle zes naast elkaar in de tabel (kolombreedte).
- Sortering standaard `id DESC`; kolomkoppen sorteerbaar; paginering 10 rijen per pagina
  (webapp), maximum 50 per pagina conform conventies §3; daarna pagina-knoppen.
- `reporting BOOLEAN DEFAULT FALSE NOT NULL` — het vinkje in de tabel is **disabled**
  (leesbaar, niet wijzigbaar); wijzigen gebeurt uitsluitend in UC-DON-03. De vlag is het
  filter van UC-RAP-06 ("Alleen schenkers met rapportage aan").
- Pictogramknop per rij → UC-DON-05 (`equipment.donor_id = :id`, index `equipment_i_2`).
- Rijlink `#` → het schenkerformulier voor het betreffende record; zonder gekozen record
  opent het schenkerformulier in de modus Nieuw (UC-DON-02).
- Geen exportknoppen op dit scherm: export van donateursgegevens staat in UC-RAP-06
  (conventies §6 — alleen expliciet genoemd).
- PrimeFaces: één `p:dataTable` met `p:column`-filters op Naam en Inschrijfdatum,
  `p:selectBooleanCheckbox` voor "Toon gearchiveerd", `p:commandButton` voor Zoeken en
  Nieuwe schenker, `p:growl` voor meldingen; voor "Personen"/"Cliënten (uitgifte)" volstaat een
  `p:commandButton` in de rijacties. Zoeken binnen andere processen (uitgifte UC-UIT-03,
  koppeling UC-DON-04) gebruikt `p:autoComplete` op `donors` met exact dezelfde
  naamzoekregels, nooit een volledige tabel in een dropdown (conventies §8).
- Rollen: ROLE_BALIE en ROLE_ADMIN mogen alles op dit scherm; ROLE_RAPPORTAGE is lezend
  (conventies §1); ROLE_WERKPLAATS heeft hier niets te zoeken en krijgt 403.

---

## UC-DON-05 — Apparatuurlijst per donateur

**Actor:** Balie, Rapportage (alleen lezen), Werkplaats (alleen lezen) · **Rollen:**
ROLE_BALIE, ROLE_WERKPLAATS, ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:** read-only overzicht van
alle apparaten die een donateur heeft geschonken (en, optioneel, die namens hem zijn
uitgegeven), met doorlink naar het apparaatscherm.

### Scherm — Apparatuur van donateur

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Apparatuur — Ros-Kabelkrant (donateur nr 12)                  [ Terug ]   │
├──────────────────────────────────────────────────────────────────────────┤
│ [✓] Ook apparaten "namens" deze schenker (derde betaalt)                  │
│ [✓] Toon gearchiveerd                                                    │
├──────────────────────────────────────────────────────────────────────────┤
│ # (id) │ Categorie │ Fabrikant │ Model       │ Inschrijf  │ Uitgeschreven │
│ 890    │ DESKTOP   │ HP        │ dc7800      │ 12-03-2024 │ —             │
│ 891    │ LAPTOP    │ Dell      │ Latitude    │ 04-04-2024 │ 19-05-2026    │
├──────────────────────────────────────────────────────────────────────────┤
│ Geen apparatuur bekend (bij leeg resultaat)                              │
│ 1-2 van 2    Per pagina [ 10 ▾ ]    [ vorige ] [ volgende ]              │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Open apparaat ]  (of klik op het apparaatnummer)                       │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Apparatuur per donateur

  Background:
    Gegeven donateur "Ros-Kabelkrant" (id 12) heeft apparaten 890 (DESKTOP, HP dc7800)
      en 891 (LAPTOP, Dell Latitude), waarvan 891 gearchiveerd is
    En apparaat 903 heeft behalf_of_id 12 (uitgegeven namens deze schenker)

  Scenario: Tonen
    Dan toont de tabel alle apparaten met donor_id = deze donateur
      en unregistered IS NULL, met de koptekst = donatienaam

  Scenario: Vanuit donateur CRUD
    Wanneer ik in het schenkerformulier op "Apparatuurlijst" klik
    Dan opent de apparatuurlijst van de schenker met het id van die donateur

  Scenario: Vanuit het donateursoverzicht
    Wanneer ik op de pictogramknop in de kolom Apparatuurlijst van UC-DON-01 klik
    Dan opent dezelfde lijst met dat id

  Scenario: Apparaat openen
    Wanneer ik op het apparaatnummer of "Open apparaat" klik
    Dan opent het apparaatformulier voor het betreffende record (apparatuurbeheer, UC-EQP-08)

  Scenario: Lege lijst
    Gegeven donateur 31 heeft geen apparaten met donor_id
    Dan verschijnt "Geen apparatuur bekend." en blijft de knop Terug actief

  Scenario: Gearchiveerde apparaten
    Gegeven "Toon gearchiveerd" staat uit
    Dan bevat de lijst apparaat 891 niet
    Wanneer ik het vinkje aanzet
    Dan bevat de lijst 891 wel, met label "gearchiveerd"

  Scenario: Apparaten namens de schenker
    Gegeven "Ook apparaten namens deze schenker" staat standaard uit
    Wanneer ik het vinkje aanzet
    Dan bevat de lijst ook apparaten waarvan behalf_of_id = 12
      (label "namens schenker" op die rij)

  Scenario: Gearchiveerde donateur
    Gegeven de donateur is gearchiveerd
    Dan toont het scherm de melding "Deze schenker is gearchiveerd; de apparatuurlijst
      is leesbaar en bestaande apparaten blijven zichtbaar."
    En blijft het openen van een apparaat toegestaan

  Scenario: Onbekend id
    Wanneer de apparatuurlijst van de schenker voor donateur 999999 (bestaat niet)
      openstaat
    Dan verschijnt "Donateur niet gevonden." en biedt Terug terug naar
      het schenkeroverzicht

  Scenario: Alleen lezen
    Gegeven ik ben ingelogd als ROLE_WERKPLAATS of ROLE_RAPPORTAGE
    Dan bevat het scherm geen wijzigknoppen; uitsluitend lezen en doorlinken
```

### Validaties

| Situatie | Melding |
|---|---|
| Onbekend of leeg donateur-id | `Donateur niet gevonden.` |
| Geen apparaten | `Geen apparatuur bekend.` |
| Geen autorisatie | `U hebt geen rechten voor dit scherm.` (HTTP 403, conventies §1) |

### Technische notities

- Query: `SELECT id, category, manufacturer, model, registered, unregistered, status
  FROM equipment WHERE donor_id = :id AND unregistered IS NULL`
  (index `equipment_i_2` op `equipment.donor_id`); met filter
  `OR unregistered IS NOT NULL`. De optionele tweede bron:
  `OR behalf_of_id = :id` — kolom `behalf_of_id` heeft FK naar `donors`
  (ON UPDATE RESTRICT, ON DELETE CASCADE; nooit verwijderen, conventies §2).
- Geen eigen view: de lijst query't direct op `equipment`. `specification` (JSON) wordt niet
  in de kolommen getoond maar wel bij het openen van het apparaat (UC-EQP-08); `datum_gift`
  uit `specification` is de giftdatum voor UC-RAP-06.
- Kolommen: `id`, `category` (ENUM DESKTOP/LAPTOP/TABLET/MOBILE/SIM/USB_STICK, weergegeven
  als enum-waarde zoals in het voorraadoverzicht), `manufacturer`, `model`, `registered`
  (Inschrijfdatum), `unregistered` (Uitgeschreven datum) en optioneel `status`
  (Nederlandse statuslabel uit UC-EQP-05) als tag.
- Read-only: geen bewerkvelden, geen exportknoppen (conventies §6); enige actie is doorlinken
  naar het apparaatformulier (UC-EQP-08), waar `equipment.donor_id` en
  `equipment.behalf_of_id` te wijzigen zijn.
- Koptekst toont `donors.name` van de opgevraagde `id`; bij een gearchiveerde donateur wordt
  `unregistered` als label getoond (conventies §5-waarschuwing is hier tekst, geen blokkade:
  de lijst is lezend).
- PrimeFaces: `p:dataTable` met `p:selectBooleanCheckbox` voor de twee filters,
  `p:commandButton` voor Terug en Open apparaat; `p:outputLabel` voor de donatienaam.
  Sortering standaard `id ASC` (oplopend, in tegenstelling tot het donateursoverzicht);
  paginering 10 per pagina, maximum 50 (conventies §3).
