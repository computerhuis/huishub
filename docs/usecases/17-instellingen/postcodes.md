# 17 — Instellingen: postcodes

Onderdeel: instellingen (postcodeverwijzing). Tabel: `postal_codes` (PK `code`;
`persons.postal_code` heeft een FK met `ON DELETE CASCADE` op deze tabel — zie de
technische notities). Schermen: het postcodeoverzicht (`postalListBean`,
`p:fileUpload` CSV-import) en het postcodedetail (`postalCrudBean`,
uitsluitend lezen). De postcodeverwijzing voedt de auto-invul van plaats op het
personenformulier en de postcodevalidatie uit conventies §2.

Dit onderdeel vervangt het Access-idee van "postcodekaart bewerken": de verwijzing is
**beheer via CSV-import**, niet via handmatige wijzigingen in de UI.

**Webapp-variant:** het postcodeoverzicht uit het prototype (lijst + import) en het
postcodedetail (read-only), samengevoegd met de validatie- en transactieregels uit de
domein-use-cases.

---

## UC-SET-01 — Postcodeoverzicht en CSV-import

**Actor:** Beheerder · **Rollen:** ROLE_ADMIN · **Doel:** de postcodeverwijzing vullen en
actueel houden door periodiek een CSV-bestand (of meerdere) te importeren.

### Scherm — Postcodes, lijst met import

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Instellingen — Postcodes                                                  │
│ [ Importeer postcodelijst (meerdere bestanden, auto-upload) ]             │
├──────────────────────────────────────────────────────────────────────────┤
│ Postcode [________ ]  Huisnummer van [____ ] tot [____ ]  [ Zoeken ]     │
├──────────────────────────────────────────────────────────────────────────┤
│ ⌕ │ Postcode │ Huisnummer  │ Straat      │ Buurt     │ Wijk   │ Stad    │
│ ⌕ │ 5211AB   │ 1 – 25      │ Molenstraat │ Centrum   │ Noord  │ Den…    │
│ ⌕ │ 5212CD   │ 100 – 198   │ Dijklaat    │ Oost      │ Zuid   │ Den…    │
├──────────────────────────────────────────────────────────────────────────┤
│ Resultaat laatste import: 2 bestanden, 148 regels ingevoegd,             │
│ 37 regels geactualiseerd, 0 fouten                                      │
│ Postcode │ Straat      │ Status                                       │
│ 5211AB   │ Molenstraat │ [✓] geactualiseerd (groen)                   │
│ 5299XY   │ —           │ [✗] ongeldig postcodeformaat (rood)          │
└──────────────────────────────────────────────────────────────────────────┘
```

### CSV-formaat

| Eigenschap | Afspraak |
|---|---|
| Codering | UTF-8 (eventueel met BOM); regelafronding LF of CRLF |
| Kopregel | Verplicht; kolomnamen worden hoofdletterongevoelig herkend |
| Scheidingsteken | `;` of `,` — automatisch herkend op de kopregel |
| Verplichte kolommen | `postcode`, `stad`, `gemeente`, `provincie`, `website` |
| Optionele kolommen | `huisnummer_min`, `huisnummer_max`, `straat`, `buurt`, `wijk`, `postbus` |
| Onbekende kolommen | Bewaard onder `_overig`-principe: niet fout, wel loggen (conventies §4) |

`website` is verplicht omdat `postal_codes.url` NOT NULL is. `postbus` bevat `ja`/`nee`
en vult het boolean-veld `pobox`.

### Acceptatiecriteria

```gherkin
Feature: Postcodebeheer — overzicht en CSV-import

  Background:
    Gegeven de postcodeverwijzing bevat 5211AB (Molenstraat, Den Bosch)

  Scenario: Lijst tonen
    Gegeven ik ben ingelogd als ROLE_ADMIN
    Wanneer ik het scherm open
    Dan toont de tabel alle rijen uit postal_codes, gegroepeerd getoond op code
    En heeft elke rij een zoekpictogram naar het detail (UC-SET-02)

  Scenario: Zoeken in de lijst
    Wanneer ik postcode "5212" invul en op Zoeken klik
    Dan bevat de lijst alleen codes die beginnen met "5212"
    Wanneer ik een huisnummerbereik invul
    Dan bevat de lijst alleen rijen waarvan huisnummer_min <= ingevuld huisnummer
      en huisnummer_max >= ingevuld huisnummer

  Scenario: Importeren (upsert op code)
    Wanneer ik een postcodebestand kies (meerdere bestanden toegestaan, auto-upload)
    Dan worden alle regels in postal_codes ingevoegd of geactualiseerd (upsert op code)
    En verschijnt een growl met het aantal ingevoegde/geactualiseerde regels per bestand
    En toont het resultaatpaneel per regel een statuspictogram

  Scenario: Bestaande postcode bijwerken
    Gegeven 5211AB bestaat met straat "Molenstraat"
    En het bestand bevat 5211AB met straat "Oude Molenstraat"
    Dan wordt de bestaande rij bijgewerkt (geactualiseerd), niet gedupliceerd

  Scenario: Onjuist postcodeformaat
    Wanneer een regel postcode "5211 AB" of "5211 ab" of "AB1234" bevat
    Dan faalt die regel met "Postcode 5211 AB komt niet voor in de postcodeverwijzing.
      Controleer de invoer of laat de beheerder de postcode toevoegen." (conventies §5)
      of, bij formaatfout vóór de FK-check: "Postcode moet het formaat 1234AB hebben."
    En wordt de hele import van dat bestand teruggedraaid

  Scenario: Duplicaat in hetzelfde bestand
    Wanneer hetzelfde bestand 5211AB twee keer bevat
    Dan faalt de import met "Regel 27: postcode 5211AB komt meer dan eens voor in dit bestand."
    En wordt het hele bestand teruggedraaid (geen deelimport)

  Scenario: Verplichte kolom ontbreekt
    Wanneer de kopregel de kolom "gemeente" mist
    Dan verschijnt "Kolom "gemeente" ontbreekt in de kopregel. Import afgebroken."
    En wordt niets ingevoegd

  Scenario: Meerdere bestanden, fout in het tweede bestand
    Wanneer ik twee bestanden tegelijk importeer en het tweede bevat een foutregel
    Dan is het eerste bestand wél verwerkt
    En is het tweede bestand volledig teruggedraaid
    En vermeldt de growl beide uitkomsten apart

  Scenario: Wijzigen terwijl personen de postcode gebruiken
    Gegeven 5211AB is gekoppeld aan personen (persons.postal_code)
    Wanneer de import deze code actualiseert
    Dan blijven de koppelingen ongewijzigd (alleen adresvelden wijzigen)

  Scenario: Geen delete in de import
    Gegeven een bestand bevat een postcode die niet in de database staat
    Dan wordt die regel gewoon ingevoegd (insert)
    En kent de import geen "verwijderen"-modus: rijen die niet in het bestand staan
      blijven onaangetast
```

### Validaties

| Situatie | Melding |
|---|---|
| Postcode formaat ≠ `1234AB` | `Postcode moet het formaat 1234AB hebben (vier cijfers, twee letters, geen spatie).` |
| Code bestaat al in hetzelfde bestand | `Regel <n>: postcode <code> komt meer dan eens voor in dit bestand.` |
| Verplichte kolom ontbreekt | `Kolom "<naam>" ontbreekt in de kopregel. Import afgebroken.` |
| `huisnummer_min` > `huisnummer_max` | `Huisnummerbereik is onjuist: min is groter dan max (regel <n>).` |
| `huisnummer_min`/`max` niet numeriek | `Huisnummer moet een getal zijn (regel <n>).` |
| `postbus` ≠ `ja`/`nee`/`true`/`false` | `Postbus moet "ja" of "nee" bevatten (regel <n>).` |
| Lege `postcode` | `Postcode is verplicht (regel <n>).` |
| Bestand zonder herkenbare kopregel | `Kopregel niet herkend. Verwacht kolommen zoals postcode, stad, gemeente, provincie, website.` |
| Fout in een van de regels | `Import van bestand "<naam>" teruggedraaid: <n> regel(len) bevatten fouten.` (hele bestand, geen deelimport) |
| Geen rechten | HTTP 403 + `U hebt geen rechten voor dit scherm.` (conventies §1) |

### Technische notities

- **Schema (Liquibase, vast — niet wijzigen):** `postal_codes(code CHAR(6) PK, url
  VARCHAR(2048) NOT NULL, city, municipality, province VARCHAR(1024) NOT NULL,
  neighbourhood, district VARCHAR(1024) NULL, street VARCHAR(2048) NULL,
  house_number_min INT NULL, house_number_max INT NULL, pobox BOOLEAN NOT NULL
  DEFAULT false)`.
- De PK is uitsluitend `code`; er is dus **één rij per postcode**. `house_number_min`/
  `max` beschrijven één bereik per code — het CSV-formaat mag geen twee bereiken voor
  dezelfde code bevatten (duplicaatregel = fout, zie hierboven). Kolomkoppen in de UI:
  `#` (zoekpictogram), `code`, huisnummer min–max, `street`, `neighbourhood` (buurt),
  `district` (wijk), `city`.
- **Upsert per regel binnen één transactie per bestand:** `INSERT … ON DUPLICATE KEY
  UPDATE` op `postal_codes_pk`. Bij een enkele regelfout: rollback van dat hele bestand
  (`TransactionTemplate` rond de verwerking); andere bestanden in dezelfde upload worden
  onafhankelijk verwerkt (zelfde patroon als UC-IMP-01: geen transactie over
  meerdere bestanden).
- **Nooit `DELETE` op `postal_codes`:** `persons.postal_code` verwijst er naar toe met
  `ON UPDATE RESTRICT ON DELETE CASCADE`. Een verwijdering zou alle personen met die
  postcode stilzwijgend wissen (conventies §2: "Nooit DELETE"). De import kent daarom
  alleen insert/update; een "verwijder ontbrekende rijen"-optie mag niet gebouwd worden.
- `ON UPDATE RESTRICT`: `code`-updates vanuit de UI bestaan niet (read-only detail,
  UC-SET-02); de import werkt op de bestaande code.
- Normalisatie vóór validatie: uppercase van `code`, spaties verwijderen, `pobox`
  accepteert `ja/nee/true/false` (Nederlandse labels in de UI, boolean in de DB).
- Meldingen via `p:growl`; per-regelstatus in de tabel met `rowStyleClass`
  success/warn/error (patroon UC-IMP-01, stijlconventies uit §5).
- Het schema mag niet gewijzigd worden: kolommen als "bron van import" of "importdatum"
  kunnen niet toegevoegd worden; log de importresultaten in de applicatielogs.

---

## UC-SET-02 — Postcodedetail (alleen lezen)

**Actor:** Beheerder · **Rollen:** ROLE_ADMIN · **Doel:** één postcode controleren.
Er zijn **geen wijzigknoppen**: mutaties lopen uitsluitend via de CSV-import
(UC-SET-01).

### Scherm — Postcode, detail (alleen lezen)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Postcode 5211AB — alleen lezen                                           │
├──────────────────────────────────────────────────────────────────────────┤
│ Postcode       [ 5211AB            ]  (alleen lezen)                     │
│ Huisnummer van [ 1      ]  Huisnummer tot [ 25       ]  (alleen lezen)   │
│ Straat         [ Molenstraat       ]  (alleen lezen)                     │
│ Buurt          [ Centrum           ]  (alleen lezen)                     │
│ Wijk           [ Noord             ]  (alleen lezen)                     │
│ Stad           [ Den Bosch         ]  (alleen lezen)                     │
│ Gemeente       [ Den Bosch         ]  (alleen lezen)                     │
│ Provincie      [ Noord-Brabant     ]  (alleen lezen)                     │
│ Postbus        [✓]                                                        │
│ Website        https://postcode.example.nl/5211AB (klikbare link)        │
├──────────────────────────────────────────────────────────────────────────┤
│ Wijzigen? Gebruik "Importeer postcodelijst" op het overzicht (UC-SET-01).│
│ [ Sluiten ]                                                              │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Postcodedetail — alleen lezen

  Scenario: Detail openen
    Gegeven ik ben ingelogd als ROLE_ADMIN
    Wanneer ik op het zoekpictogram bij 5211AB in de lijst klik
    Dan opent het postcodedetail voor het betreffende record
    En zijn alle velden disabled: Postcode, Huisnummer min, Huisnummer max,
      Straat, Buurt, Wijk, Stad, Gemeente, Provincie en Website (klikbare link)

  Scenario: Geen wijzigacties
    Gegeven er staan geen bewerkvelden of opslaan-knoppen op het scherm
    Dan is het detail uitsluitend ter inzage
    En is er geen "Opslaan"-, "Verwijderen"- of "Archiveren"-knop

  Scenario: Wijzigen via import
    Gegeven ik wil 5211AB actualiseren
    Wanneer ik dat probeer op dit scherm
    Dan verwijst de melding "Wijzigen gebeurt via de postcode-import." naar UC-SET-01

  Scenario: Ongeldige of onbekende code
    Wanneer ik het postcodedetail voor postcode 9999ZZ (bestaat niet) open
    Dan verschijnt "Postcode 9999ZZ komt niet voor in de postcodeverwijzing.
      Controleer de invoer of laat de beheerder de postcode toevoegen." (conventies §5)
    En keer ik terug naar de lijst

  Scenario: Website tonen
    Dan toont "Website" de waarde uit postal_codes.url als klikbare link
      (target _blank, rel noopener)
```

### Technische notities

- Alle `p:inputText`-velden hebben `disabled="true"`; het scherm heeft geen
  `p:commandButton` voor opslaan — alleen "Sluiten" (terug naar
  het postcodeoverzicht). Veldlabels: Postcode (`code`), Huisnummer min
  (`house_number_min`), Huisnummer max (`house_number_max`), Straat (`street`), Buurt
  (`neighbourhood`), Wijk (`district`), Stad (`city`), Gemeente (`municipality`),
  Provincie (`province`), Postbus (`pobox`), Website (`url`).
- `f:viewParam name="code"` + `preRenderView` laden de rij via
  `postal_codes.code` (exacte match, uppercase-normalisatie vóór lookup); onbekende code
  = growl + outcome terug naar de lijst.
- Read-only: er wordt geen enkele UPDATE uitgevoerd; het scherm is een weergave van de
  import — geen validatielaag nodig buiten de code-lookup.
- Rollen: net als UC-SET-01 ROLE_ADMIN; anderen krijgen HTTP 403 (conventies §1).
