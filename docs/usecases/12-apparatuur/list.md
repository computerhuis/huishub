# 12 — Apparatuur: overzicht en voorraad

Onderdeel: apparatuur (giften, klanteigen apparaten, voorraad). Tabellen: `equipment`,
`persons` (eigenaar), `donors` (schenker/belanghebbende); `proof_of_issues` (leen-only voor de
koppeling uitgifte). View: `equipment_history` (alleen lezen; dossier en ticketkoppelingen,
zie `geschiedenis.md`). Schermen: het apparatuuroverzicht (`equipmentListBean`, het
overzicht met statusfilter) en het voorraadoverzicht (`equipmentStockBean`, de
voorraadlijst); het bijbehorende beheerformulier is het apparaatformulier
(`equipmentCrudBean`, zie `crud.md`) en het dossier is het apparaatdossier
(`equipmentHistoryBean`, zie `geschiedenis.md`). Kernwoorden uit de specificatie:
categorie-enum, statusstroom, uniek serienummer, `specification`-JSON — de enum- en
validatieregels staan in `crud.md` (Statusstroom-tabel bovenaan) en conventies §2/§4.3.

**Webapp-variant:** de vier prototypeschermen van het oude webapp-ontwerp (het
apparatuuroverzicht, het voorraadoverzicht, het apparaatformulier en het apparaatdossier)
— voor dit onderdeel hertekend tot twee domein-gebaseerde overzichts-use-cases
(UC-EQP-01 en UC-EQP-02); wijzigen, status en archiveren staan in `crud.md`, geschiedenis
in `geschiedenis.md`.

---

## UC-EQP-01 — Overzicht met statusfilter

**Actor:** Balie, Werkplaats, Rapportage · **Rollen:** ROLE_BALIE, ROLE_WERKPLAATS,
ROLE_RAPPORTAGE (uitsluitend lezen), ROLE_ADMIN · **Doel:** alle apparaten doorzoeken op
status, categorie, schenker en vrije tekst (fabrikant/model/serienummer) — het scherm
"Gekregen Computers" uit de Access-handleiding, gecombineerd met het overzicht uit het
prototype. Elke rij is de instap naar het apparaatdossier en naar wijzigen in het
apparaatformulier.

### Scherm — Apparatuuroverzicht

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Apparatuur                                                               [ Nieuw ]│
├──────────────────────────────────────────────────────────────────────────┤
│ Filters                                                                  │
│ Status [ alle ▾ ]  Categorie [ alle ▾ ]  Schenker [ zoek schenker ▾ ]     │
│ Zoek (fabrikant/model/serienummer) [____________________ ]  [ Zoeken ]    │
│ [✓] Toon gearchiveerd                                                     │
├──────────────────────────────────────────────────────────────────────────┤
│ # (id) │ Categorie │ Fabrikant │ Model │ Inschrijf datum │ Uitgeschreven  │
│        │           │           │       │ (registered)    │ datum          │
│        │           │           │       │                 │ (unregistered) │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Export (xlsx) ]                    50 resultaten per pagina ▾           │
└──────────────────────────────────────────────────────────────────────────┘
```

Rij-acties (pictogramknoppen per rij, conventies §6): `dossier` (opent het
apparaatdossier), `wijzigen` (opent het apparaatformulier), `status` (opent het
statusscherm uit UC-EQP-05). De kolom `#` toont `equipment.id` en is een link naar het
dossier.

### Acceptatiecriteria

```gherkin
Feature: Apparatuuroverzicht met statusfilter

  Scenario: Standaardweergave
    Gegeven er bestaan 120 niet-gearchiveerde apparaten en 12 gearchiveerde
    Wanneer ik het scherm open
    Dan toont de tabel alle niet-gearchiveerde apparaten (conventies §2:
      standaard worden gearchiveerde records uitgesloten)
    En staat er 10 per pagina met # aflopend
    En bevat de kolom Uitgeschreven datum voor elke rij een lege cel

  Scenario: Filteren op status
    Wanneer ik Status "Geschikt voor uitgifte" kies
    Dan ververst de tabel direct (twee p:ajax-regels: select + tabel)
      met uitsluitend apparaten met status SUITABLE_FOR_GIFT

  Scenario: Alles
    Wanneer ik Status "Alles" kies
    Dan toont de tabel weer alle (niet-gearchiveerde) apparaten

  Scenario: Categoriefilter
    Wanneer ik Categorie "LAPTOP" kies
    Dan bevat de lijst uitsluitend apparaten met category LAPTOP

  Scenario: Schenkerfilter
    Wanneer ik Schenker "Gemeente 's-Hertogenbosch" kies
    Dan bevat de lijst uitsluitend apparaten met donor_id van die schenker

  Scenario: Vrije tekst
    Gegeven apparaat 890 heeft fabrikant "HP", model "Compaq dc7800"
    Wanneer ik in Zoek "dc7800" intyp en Zoeken klik
    Dan bevat de lijst uitsluitend apparaat 890
    (implementatie: LIKE op manufacturer, model én serial_number)

  Scenario: Zoeken op serienummer
    Wanneer ik een volledig serienummer intyp
    Dan bevat de lijst uitsluitend dat apparaat

  Scenario: Zoekveld leeg
    Wanneer ik Zoeken klik met leeg zoekveld en geen andere filters
    Dan toont de lijst alle niet-gearchiveerde apparaten
      (leeg zoekveld = geen tekstfilter; de blokkade "Voer een zoekterm in."
       uit conventies §3 geldt alleen voor de personen-/schenkerzoekopdrachten)

  Scenario: Kleurindicator status
    Dan toont de tabel per rij een kleurindicator volgens de statusstroom
      (conventies en Statusstroom-tabel in `crud.md`):
      INCOMING_GIFT = rood, SUITABLE_FOR_GIFT = blauw, RESERVED = rood,
      CUSTOMER_OWNED = zwart, GIFT_ISSUED en SOLD = zwart/grijs, DEMOLITION = grijs

  Scenario: Toon gearchiveerd
    Gegeven apparaat 900 is gearchiveerd (unregistered gevuld)
    Wanneer ik "Toon gearchiveerd" aanvink
    Dan bevat de lijst ook apparaat 900, met de Uitgeschreven datum ingevuld
    En wanneer ik het vinkje weer uitzet verdwijnt het uit de lijst

  Scenario: Rij openen
    Wanneer ik op het dossier-pictogram van een rij klik
    Dan opent het apparaatdossier voor het betreffende record (UC-EQP-08)
    Wanneer ik op het wijzig-pictogram klik
    Dan opent het apparaatformulier voor het betreffende record (UC-EQP-04)

  Scenario: Nieuw apparaat
    Wanneer ik op "Nieuw" klik
    Dan opent het apparaatformulier in de modus Nieuw (UC-EQP-03)

  Scenario: Export
    Wanneer ik op Export klik
    Dan download ik een .xlsx van de géfilterde én de gefilterde lijst zoals
      getoond (kolommen zoals in het wireframe), volgens de kolomvolgorde van
      het scherm

  Scenario: Rapportage rol
    Gegeven ingelogd als ROLE_RAPPORTAGE
    Dan werken filters, export en dossier-weergave
    En zijn de knoppen Nieuw, wijzigen, status en Archiveren niet zichtbaar
      (conventies §1: alleen lezen)

  Scenario: Geen rechten
    Gegeven ingelogd zonder van de vier rollen uit conventies §1
    Wanneer ik het scherm aanvraag
    Dan volgt HTTP 403 en "U hebt geen rechten voor dit scherm."
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Status | enum-waarde uit `equipment.status` of "Alles" | `Kies een geldige status.` |
| Categorie | enum-waarde uit `equipment.category` of "Alles" | `Kies een geldige categorie.` |
| Schenker | bestaande `donors.id` (via autocomplete, conventies §8) | `Kies een bestaande schenker.` |
| Zoektekst | maximaal 255 tekens | `De zoektekst is te lang (maximaal 255 tekens).` |
| Toon gearchiveerd | checkbox; standaard uit (conventies §2) | — |

### Technische notities

- Query op `equipment` met `LEFT JOIN persons p ON p.id = e.owner_id` en
  `LEFT JOIN donors d ON d.id = e.donor_id`; standaardfilter
  `e.unregistered IS NULL`; sortering standaard `e.id DESC` (nieuwste eerst, "inschrijf
  datum aflopend" valt samen met # aflopend zolang AUTO_INCREMENT oploopt).
- Kolommen exact: `e.id`, `e.category`, `e.manufacturer`, `e.model`, `e.serial_number`,
  `e.registered`, `e.unregistered`, `e.status`, `e.owner_id`, `e.donor_id`,
  `e.behalf_of_id`, `e.specification`. "Inschrijf datum" = `registered`
  (UI NL-formaat `dd-MM-yyyy`, conventies §2); "Uitgeschreven datum" = `unregistered`
  (leeg = actief).
- Enum-weergave: `p:selectOneMenu` met de **volledige** enum-waardenlijst uit het schema en
  Nederlandse labels (conventies §8):
  `DESKTOP` = Desktop, `LAPTOP` = Laptop, `TABLET` = Tablet, `MOBILE` = Smartphone,
  `SIM` = Simkaart, `USB_STICK` = USB-stick; statuslabels zoals de Statusstroom-tabel in
  `crud.md`.
- Kleuren zijn presentatie-only (PrimeFaces `rowClass` op basis van `status`); nooit de
  enum-waarde zelf als kleur opslaan.
- Vrije tekst: `WHERE (manufacturer LIKE ? OR model LIKE ? OR serial_number LIKE ?)`; exacte
  serienummerzoekopdrachten matchen daardoor ook zonder wildcard.
- Pagineerder: 10 rijen per pagina (standaard), keuze 10/25/50; conventies §3 schrijft
  maximaal 50 voor bij zoekopdrachten — dezelfde bovengrens geldt hier.
- Export (POI beschikbaar, conventies §6): kolommen in schermvolgorde, bedragen uit
  `specification.prijs` NL-geformatteerd; gearchiveerde rijen alleen meenemen als het vinkje
  aan staat.
- `p:autoComplete` voor schenker (completeMethod op donors, zoekconventies op naam); nooit
  een volledige donors-tabel in een dropdown (conventies §8).
- De kolom Uitgeschreven datum toont `unregistered`; vastleggen daarvan gebeurt uitsluitend
  via Archiveren in UC-EQP-06 (conventies §2: `unregistered = NOW()`).

---

## UC-EQP-02 — Voorraadoverzicht

**Actor:** Balie, Werkplaats, Rapportage · **Rollen:** ROLE_BALIE, ROLE_WERKPLAATS,
ROLE_RAPPORTAGE (uitsluitend lezen), ROLE_ADMIN · **Doel:** de operationele voorraadlijst
"Klaar voor uitgifte" met totalen per status en uitgiftegeschiktheid per rij — de Access
statusbord/voorraadlijst, gecombineerd met het voorraadoverzicht uit het prototype. Voorraad =
uitgiftegeschikt: de lijst start op status SUITABLE_FOR_GIFT (implementatiekeuze van het
prototype) en toont desondanks alle statussen zodra een filter wordt gekozen, mét een
kolom Uitgiftegeschikt die doorverwijst naar UC-UIT-01.

### Scherm — Voorraad

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Voorraad apparaten                                                       [ Refresh ]│
├──────────────────────────────────────────────────────────────────────────┤
│ Totalen per status                                                       │
│ Binnengekomen gift 14 │ Geschikt 37 │ Gereserveerd 6 │ Uitgegeven 210    │
│ Verkocht 45 │ Sloop 12 │ Klanteigen 88   Som prijs voorraad: € 2.960,00  │
├──────────────────────────────────────────────────────────────────────────┤
│ Categorie [ alle ▾ ]  Status [ Geschikt voor uitgifte ▾ ]                 │
│ Schenker [ zoek schenker ▾ ]                                             │
│ Zoek (fabrikant/model/serienummer) [____________________ ]  [ Zoeken ]    │
│ [✓] Toon gearchiveerd                                                     │
├──────────────────────────────────────────────────────────────────────────┤
│ Nr │ Categorie │ Fabrikant/Model │ Serienummer │ Status │ Prijs │ Eigenaar│
│    │           │                 │             │(label) │       │/Schenker│
├──────────────────────────────────────────────────────────────────────────┤
│ [ Weergeven ] [ Status ] [ Reparaties ] [ Bewijs van uitgifte ]           │
│ [ Export (xlsx) ]                                                         │
└──────────────────────────────────────────────────────────────────────────┘
```

Rij-acties: `Weergeven` (dossier, UC-EQP-08), `Status` (UC-EQP-05), `Reparaties`
(gefilterde ticketlijst op dit apparaat), `Bewijs van uitgifte` (UC-UIT-01 met dit apparaat
voorgevuld — alleen actief bij uitgiftegeschikte apparaten).

### Acceptatiecriteria

```gherkin
Feature: Voorraadoverzicht

  Scenario: Standaard
    Wanneer ik het scherm open
    Dan staat Status op "Geschikt voor uitgifte" en toont de lijst uitsluitend
      apparaten met status SUITABLE_FOR_GIFT
    En de totalenregel toont per status een telling over alle (niet-gearchiveerde)
      apparaten, ongeacht het gekozen filter

  Scenario: Totalen per status
    Gegeven 14 apparaten INCOMING_GIFT, 37 SUITABLE_FOR_GIFT, 6 RESERVED,
      210 GIFT_ISSUED, 45 SOLD, 12 DEMOLITION en 88 CUSTOMER_OWNED
    Dan toont de totalenregel exact deze zeven aantallen met de Nederlandse
      statuslabels uit de Statusstroom-tabel (`crud.md`)
    En toont "Som prijs voorraad" de optelling van specification.prijs van de
      getoonde SUITABLE_FOR_GIFT-rijen (leeg = niet meegeteld)

  Scenario: Filter op status
    Wanneer ik Status "Binnengekomen gift" kies
    Dan bevat de lijst uitsluitend apparaten met status INCOMING_GIFT
    Wanneer ik Status "alle" kies
    Dan bevat de lijst alle niet-gearchiveerde apparaten

  Scenario: Standaardfilter
    Wanneer ik het scherm open zonder filters
    Dan bevat de lijst alle niet-gearchiveerde apparaten, gesorteerd op
      laatste wijziging aflopend (registered desc, conform conventies §2)

  Scenario: Uitgiftegeschiktheid
    Gegeven apparaat 890 heeft status SUITABLE_FOR_GIFT
    En apparaat 891 heeft status RESERVED
    En apparaat 892 heeft status INCOMING_GIFT
    Dan toont de kolom Uitgiftegeschikt bij 890 "ja"
    En bij 891 "ja, gereserveerd" (alleen met passende reserveringsmelding)
    En bij 892 "nee"
    En is de knop "Bewijs van uitgifte" alleen actief bij "ja"-rijen

  Scenario: Koppeling uitgifte
    Wanneer ik bij een uitgiftegeschikte rij op "Bewijs van uitgifte" klik
    Dan opent UC-UIT-01 met dit apparaat voorgevuld
    En na succesvolle uitgifte toont een verversing van dit scherm het apparaat
      niet meer in de standaardfilter (status GIFT_ISSUED)

  Scenario: Zoeken op serienummer
    Wanneer ik een volledig serienummer intyp
    Dan bevat de lijst uitsluitend dat apparaat

  Scenario: Kleurindicator
    Dan toont de kolom Status per rij een kleurindicator volgens:
      Binnengekomen gift = rood, Geschikt voor uitgifte = blauw,
      Gereserveerd = rood, Uitgegeven/Verkocht = zwart/grijs, Sloop = grijs,
      Klanteigen = zwart

  Scenario: Archiefweergave
    Gegeven apparaat 900 is gearchiveerd en was SUITABLE_FOR_GIFT
    Wanneer ik "Toon gearchiveerd" aanvink
    Dan verschijnt 900 in de lijst (indien het filter past)
    En nemen de totalen díé gearchiveerde rijen niet mee
      (totalen blijven gefilterd op unregistered IS NULL)

  Scenario: Reparaties-knop
    Wanneer ik bij een rij op "Reparaties" klik
    Dan toont de ticketlijst van UC-TIC-01 uitsluitend tickets met
      equipment_id = dit apparaat

  Scenario: Export
    Wanneer ik op Export klik
    Dan download ik een .xlsx van de gefilterde voorraadlijst inclusief
      totalenregel

  Scenario: Rapportage rol
    Gegeven ingelogd als ROLE_RAPPORTAGE
    Dan werken filters, totalen en export
    En zijn Status en Bewijs van uitgifte niet zichtbaar (conventies §1)
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Status | enum-waarde uit `equipment.status`, "Geschikt voor uitgifte" (default) of "alle" | `Kies een geldige status.` |
| Categorie | enum-waarde uit `equipment.category` of "alle" | `Kies een geldige categorie.` |
| Schenker | bestaande `donors.id` | `Kies een bestaande schenker.` |
| Zoektekst | maximaal 255 tekens | `De zoektekst is te lang (maximaal 255 tekens).` |

### Technische notities

- Totale telling per status: `SELECT status, COUNT(*) FROM equipment WHERE unregistered IS NULL GROUP BY status`; ontbrekende statussen tonen 0. De
  statuskolom is een MariaDB-ENUM: toon alle zeven waarden, niet alleen de voorkomende.
- "Som prijs voorraad": `specification->'$.prijs'` (JSON-extract), gecast naar
  DECIMAL; sleutel `prijs` volgt conventies §4.3; null-waarden tellen niet mee.
- Uitgiftegeschiktheid (implementatie): `status IN ('SUITABLE_FOR_GIFT','RESERVED')` — exact
  dezelfde filter als paneel 2 van UC-UIT-01 ("Voorraad toont alleen geschikte apparaten",
  statusfilter UC-EQP-05). RESERVED toont de reserveringstoelichting uit
  `specification.bijzonderheden` als tooltip/tekst.
- Kolommen: `e.id`, `e.category`, `e.manufacturer`, `e.model`, `e.serial_number`, `e.status`,
  `e.owner_id` (LEFT JOIN persons, naam), `e.donor_id` (LEFT JOIN donors, naam), `prijs`
  uit `specification`. Prijs is presentatie-only afgeleid; er is geen prijskolom in
  `equipment`.
- Dezelfde statuslabels, kleurklassen (`rowClass`) en enum-presentatie als UC-EQP-01 —
  implementeer dit als gedeelde presentatiecomponent zodat de twee schermen nooit
  afwijken.
- Rij-knop "Bewijs van uitgifte" navigeert naar het uitgiftescherm voor dat apparaat
  (UC-UIT-01); de uitgifte zelf valideert opnieuw de beschikbaarheid ("Apparaat niet
  beschikbaar" — een tussenkomende wijziging in een ander scherm wordt zo opgevangen).
- Export via POI (conventies §6); bewaar de kolomvolgorde van het scherm en voeg een
  statuslabel-kolom toe in plaats van de raw enum-waarde.
- Refresh-knop herlaadt totalen en tabel zonder paginering te verliezen; server-side
  `registered`-klok nooit vervangen door de browserklok (conventies §8).
