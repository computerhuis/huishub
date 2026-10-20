# 00 — Conventies

Overkoepelende afspraken voor alle use-cases. Een implementatie is pas compleet als deze
conventies op elke use-case van toepassing zijn.

---

## 1. Rollen (autoriteit in `person_roles.authority`)

| Authority | Naam | Bevoegdheden |
|---|---|---|
| `ROLE_ADMIN` | Beheerder | Alles, inclusief rollen toekennen, archiveren ongedaan maken, activiteiten beheren |
| `ROLE_BALIE` | Balie | Klanten, schenkers, apparaten, tickets, uitgifte, bewijzen, aanwezigheid |
| `ROLE_WERKPLAATS` | Werkplaats | Tickets en status, apparaten (technische velden), aanwezigheid; **geen** uitgifte afronden |
| `ROLE_RAPPORTAGE` | Rapportage | Alleen lezen van overzichten en alle rapportages/exporten |

- Een gebruiker zonder autorisatie op een scherm krijgt HTTP 403 en een melding
  "U hebt geen rechten voor dit scherm."
- Personen met `ROLE_BALIE` of `ROLE_WERKPLAATS` mogen alle onder 1 genoemde mutaties doen;
  specifere rechten zijn niet nodig.
- "Actief ingelogd" = `persons.passhash` gevuld en `unregistered IS NULL`.

## 2. Algemene veldconventies

| Regel | Waarheid |
|---|---|
| Zachte verwijdering | Elke tabel met `unregistered` wordt nooit fysiek gewist door de applicatie. "Archiveren" = `unregistered = NOW()`. Gewijzigd heropenen = `unregistered = NULL`. |
| `registered` | Altijd `NOW()` bij aanmaak, niet wijzigbaar in de UI. |
| Telefoon (`mobile`, `telephone`, `CHAR(10)`) | Exact 10 tekens, cijfers alleen, formaat `0612345678` of `0731234567`. Leeg toegestaan. Invoer met spaties/puntjes/haakjes wordt door de UI geformatteerd naar 10 cijfers vóór opslaan. |
| Postcode (`persons.postal_code`, `VARCHAR(6)`) | Formaat `1234AB` (vier cijfers, twee letters, geen spatie, uppercase). Moet bestaan in `postal_codes.code`; anders blokkeert de opslag (FK). |
| Huisnummer-toevoeging (`VARCHAR(4)`) | Bijv. `A`, `B`, `12`, `bis`. Leeg als geen toevoeging. |
| Datums | `DATE` voor geboortedatum; `DATETIME` voor registered/unregistered/status/log/tijden. UI toont NL-formaat `dd-MM-yyyy`, tijd `HH:mm`. |
| Zoeken | Standaard worden gearchiveerde records (`unregistered IS NOT NULL`) uitgesloten; een filter "Toon gearchiveerd" voegt ze toe. |
| Optellingen | Nooit `DELETE`; overzichten zijn gefilterd op `unregistered IS NULL` tenzij anders vermeld. |
| Dubbele invoer | Bij aanmaak van persoon/apparaat/ticket controleert de applicatie op een fuzzy-dubbele en toont een waarschuwing, geen blokkade (behalve `serial_number`, zie UC-EQP-07). |

## 3. Zoekconventies (klant-/schenkerzoekopdrachten)

- Primair zoekveld: achternaam; secundair: voornaam, telefoon of e-mail.
- Zoeken op achternaam is **fonetisch**: resultaat bevat personen waarvan de achternaam of
  voornaam hetzelfde *klinkt* (implementatie: `SOUNDEX()` in MariaDB of equivalent), plus
  exacte treffers, exacte treffers eerst.
- Leeg zoekveld + "Zoeken" = foutmelding "Voer een zoekterm in." (geen volledige tabeldump).
- Maximum 50 resultaten per pagina; daarna pagina-knoppen.

## 4. JSON-registry (`msaccess`, `specification`, `details`)

De sleutelnamen hieronder zijn **bindend** en worden door alle use-cases en imports gebruikt.
Ontbrekende sleutels mogen `null` zijn; onbekende sleutels uit imports worden onder
`_overig` bewaard (behouden, niet weggooien).

### 4.1 `persons.msaccess`

```json
{
  "legacy_gebruikersnummer": 1234,
  "geslacht": "M | V | X",
  "vrienden_computerhuis_oost": true,
  "telefoon_3": "0731234567",
  "persoonlijke_email": "x@y.nl",
  "organisatie": "string",
  "contactpersoon": "string",
  "vrijwilligerswerk_start": "YYYY-MM-DD",
  "vrijwilligerswerk_eind": "YYYY-MM-DD",
  "afgemeld_nieuwsbrief": false,
  "website": "https://…",
  "postbus": "123",
  "postcode_postbus": "5211AB",
  "gekoppeld_gebruikersnummer": 4321,
  "vestigingsnummer": "string",
  "_overig": {}
}
```

### 4.2 `donors.msaccess`

```json
{
  "legacy_bedrijfsnummer": 567,
  "vestiging": "Vestiging 's-Hertogenbosch",
  "kvk_nummer": "12345678",
  "btw_nummer": "NL001234567B01",
  "website": "https://…",
  "postbus": "456",
  "postcode_postbus": "5201AA",
  "contactpersonen": [{ "naam": "string", "email": "string", "telefoon": "string" }],
  "_overig": {}
}
```

### 4.3 `equipment.specification`

```json
{
  "legacy_computernummer": 890,
  "type_kast": "TOWER | MINI | …",
  "type_computer": "string",
  "processor": "string",
  "geheugen": "string",
  "hdd_ssd": "string",
  "videokaart": "string",
  "optische_apparaten": "string",
  "losse_geluidskaart": "string",
  "cardreader": "string",
  "overige_apparaten": "string",
  "software": "string",
  "bijzonderheden": "string",
  "licentie": "string",
  "prijs": 80.00,
  "verkocht_door": "string",
  "datum_gift": "YYYY-MM-DD",
  "leergeld": { "laptop": false, "smartphone": false, "simkaart": false },
  "_overig": {}
}
```

### 4.4 `tickets.details`

```json
{
  "legacy_reparatienummer": 4521,
  "backup": "GEEN | GEHELE | DEEL | ONBEKEND",
  "bijgeleverde_accessoires": "string",
  "tafel": 3,
  "account_wachtwoord": "string",
  "email_gegevens": "string",
  "klantnr_verkoop": 999,
  "derde_betaalt": { "donor_id": 12, "email_bewijs": "string" },
  "proof_of_issue_id": 123,
  "_overig": {}
}
```

`account_wachtwoord` bevat een wachtwoord in **plaintext-veld** van de legacy; de UI toont dit
veld nooit terug, alleen als "ingevuld ja/nee".

## 5. Validatie- en foutafhandering

- Alle meldingen in het Nederlands, via PrimeFaces growl/messages.
- Veldvalidatie gebeurt in de JSF-laag (Bean Validation: `@NotNull`, `@Size`, `@Pattern`) én in
  de service-laag (FK-bestaan, enum-overgangen). Bij conflict wint de service-laag.
- Blokkerende situaties: `p:message` op het veld + samenvattende melding bovenaan het formulier.
- Niet-bestaande postcode bij invoer: "Postcode 1234AB komt niet voor in de postcodeverwijzing. "
  "Controleer de invoer of laat de beheerder de postcode toevoegen."
- Archiveren met kindrecords (bijv. apparaat met bewijs): wel toegestaan (soft delete cascadert
  niet), maar het scherm toont de waarschuwing "Archiveren verbergt het record in overzichten; "
  "bestaande tickets en bewijzen blijven zichtbaar."
- Optimistic locking is niet in het schema aanwezig: schermen die wijzigen tonen bij laden een
  "laatst gewijzigd"-tijdstip en waarschuwen bij een afwijkend tijdstip vóór opslaan.

## 6. Overzichtschermen (algemene opbouw)

- Eén `p:dataTable` met filters op de kolommen die de use-case noemt.
- Rij acties: pictogramknoppen per actie uit de use-case.
- "Nieuw"-knop opent het formulier inline (of dialog) — implementatiekeuze; velden zijn leidend.
- Exportknoppen (CSV/Excel) staan alleen in use-cases die export expliciet noemen (POI is
  beschikbaar).

## 7. Wireframe-legenda

```
┌─────────────────────────┐  kader = scherm of paneel
│ Label [ invoer ]        ]  [ knop ]   knop
│ Label [ select ▾ ]      │  selecties
│ Label [■■■■■ ] datum    │  datum/tijd-invoer
│ Label [✓]  ja/nee       │  checkbox
│ Label [lang tekst ]     │  textarea
├─────────────────────────┐  scheiding tussen zones
│ kol │ kol │ kol │ actie │  tabelkolommen
└─────────────────────────┘
```

Wireframes tonen uitsluitend **welke velden en knoppen** een scherm heeft, met vermelding van
het bijbehorende dataveld tussen haakjes waar dat onduidelijk is. Volgorde in het kader is de
gewenste invoervolgorde.

## 8. PrimeFaces-algemeen

- UI 100% PrimeFaces: formulieren `p:form`, tabellen `p:dataTable`, invoer via
  `p:inputText`, `p:datePicker` (datum/tijd), `p:selectOneMenu`, `p:selectBooleanCheckbox`,
  `p:autoComplete` (klant-/apparaatzoekopdrachten), `p:fileUpload` (bewijs-afbeelding),
  `p:commandButton` met `update`, `p:messages`/`p:growl` voor meldingen, `p:confirmDialog`
  voor destructieve/bevestigende acties.
- Zoekopdrachten naar personen/apparaten lopen altijd via `p:autoComplete` of een
  zoekformulier met `p:dataTable`; nooit een volledige tabel in een dropdown.
- Enum-velden worden gepresenteerd als `p:selectOneMenu` met de **volledige** enum-waardenlijst
  uit het schema; de weergegeven labels zijn Nederlands (zie per use-case).
- Het scherm vult na opslaan altijd `registered`/`NOW()` server-side; vertrouw nooit op de klok
  van de browser.
