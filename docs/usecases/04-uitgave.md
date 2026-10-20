# 04 — Uitgifte en bewijzen (inname/uitgifte)

Onderdeel: uitgifteproces. Schermen: het uitgiftescherm (`outputBean`, directe uitgifte
vanuit de voorraad); `reports/proofofissue` voor weergave (UC-RAP-02/03). Tabellen:
`proof_of_issues`, `proof_of_issue_lines`, neveneffecten op `equipment` (status, `owner_id`)
en `tickets` (status CLOSED).

**Koppeling ticket ↔ bewijs:** `proof_of_issues` heeft geen ticketkolom (schema is leidend).
Koppeling via `tickets.details.proof_of_issue_id` (invullen bij aanmaken) en via
`equipment_id` + `issue_date`.

**Webapp-variant:** het uitgiftescherm en de importwizard uit het prototype, aangevuld met
de domeinregels uit de eerdere bewijs-use-cases (regels, derde betaalt, ondertekening,
neveneffecten).

---

## UC-UIT-01 — Uitgifte uitvoeren (bewijs vastleggen)

**Actor:** Balie · **Rollen:** ROLE_BALIE, ROLE_ADMIN · **Doel:** apparaat overdragen aan een
klant: bewijs van uitgifte opmaken + status- en eigenaarseffecten. Vervangt de Access-flow
"Bewijs van inname/uitgifte" (handleiding §9).

### Scherm — Uitgave (twee panelen)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Uitgave                                                                  │
├──────────────────────────────────────────────────────────────────────────┤
│ 1. Kies een klant                                                        │
│ Voornaam [________ ]  Achternaam [________ ]  [ Zoeken ]                 │
│ Nr │ Voornaam │ Tussenv. │ Achternaam │ Woonplaats │  [ kiezen ▾ ]      │
│ Gekozen klant: Johanna de Vries (link naar CRUD)                         │
├──────────────────────────────────────────────────────────────────────────┤
│ 2. Kies een apparaat                                                     │
│ Nr │ Categorie │ Fabrikant │ Model │ Serienummer │  [ kiezen ▾ ]        │
│ Gekozen apparaat: 891 DESKTOP Dell Latitude                              │
├──────────────────────────────────────────────────────────────────────────┤
│ Uitgereikt door [ medewerker ▾ (default: ingelogde) ]                     │
│ [✓] Derde betaalt voor de ontvanger  (UC-UIT-03)                         │
│ Regels (UC-UIT-02):  # │ Omschrijving │ Aantal │ Eenheid │ Bedrag        │
│                      1 │ HP dc7800 — gereed │ 1,00 │ pcs │ € 80,00      │
│ [ Regel toevoegen ]  Totaal: € 80,00                                     │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Uitgeven ]              [ Bewijs van inname (intake) ▾ ]               │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Uitgifte uitvoeren

  Scenario: Uitgifte rondt uitgifte af
    Gegeven apparaat 891 met status SUITABLE_FOR_GIFT en ticket 4530 (ISSUE, nog niet CLOSED)
    Wanneer ik klant "Johanna de Vries", apparaat 891 en twee regels kies en op Uitgeven klik
    Dan bestaat een proof_of_issues-rij met issue_date nu, issued_by de medewerker,
      recipient_id 42 en equipment_id 891
    En wordt equipment.status van 891 GIFT_ISSUED
    En wordt equipment.owner_id 42
    En wordt ticket 4530 automatisch CLOSED (indien gekoppeld via details.proof_of_issue_id
      of via het apparaat) met een "[STATUS] … → Afgerond"-logregel
    En bevat details.proof_of_issue_id van dat ticket dit bewijs-id
    En verschijnt "Bewijs <id> vastgelegd; apparaat uitgegeven aan Johanna de Vries."

  Scenario: Zonder regels
    Gegeven er zijn nog geen regels
    Wanneer ik Uitgeven klik
    Dan wordt automatisch regel 1 "<fabrikant> <model> — uitgegeven", quantity 1,
      unit "pcs" aangemaakt
    En of verschijnt de waarschuwing "Dit bewijs bevat geen regels. Doorgaan?"

  Scenario: Voorraad toont alleen geschikte apparaten
    Dan bevat paneel 2 uitsluitend apparaten met status SUITABLE_FOR_GIFT of RESERVED
      (implementatie: filter op status; UC-EQP-05)

  Scenario: Niets gekozen
    Wanneer ik Uitgeven klik zonder klant of apparaat
    Dan verschijnt "Kies eerst een klant en een apparaat."

  Scenario: Apparaat niet beschikbaar
    Gegeven het apparaat heeft inmiddels status GIFT_ISSUED (ander scherm)
    Wanneer ik het alsnog kies en Uitgeven klik
    Dan verschijnt "Het gekozen apparaat is niet meer beschikbaar voor uitgifte."
      en wordt niets opgeslagen

  Scenario: Ontvanger gearchiveerd
    Wanneer de gekozen klant gearchiveerd is
    Dan blokkeert het scherm: "Deze klant is gearchiveerd. Heropen het record eerst."

  Scenario: Bewijs van inname zonder neveneffecten
    Wanneer ik soort "Bewijs van inname" kies (vanuit intake UC-INT-04)
    Dan wordt wél een proof_of_issues-rij vastgelegd met
      tickets.details.proof_of_issue_id koppeling
    En verandert de equipment-status niet en sluit geen ticket
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Klant | verplicht, persoon zonder `unregistered` | `Kies de ontvanger.` |
| Apparaat | verplicht, bestaand, niet-gearchiveerd, status geschikt | `Kies het apparaat.` |
| Uitgereikt door | verplicht (default huidige gebruiker) | `Kies wie het bewijs uitreikt.` |

### Technische notities

- Transactie: `proof_of_issues` → `proof_of_issue_lines` → `equipment`-update
  (status + owner_id) → (indien gekoppeld) `ticket_status` CLOSED + `[STATUS]`-logregel.
  Bij falen rolt alles terug.
- Soort bewijs zit niet als kolom in het schema: **afspraak** — uitgifte herkenbaar aan
  `equipment.status = GIFT_ISSUED` met bewijs in de periode; inname aan het gerelateerde
  REPAIR-ticket (conventies §4.4, `tickets.details.proof_of_issue_id`).
- Handtekening: na vastleggen kan de ondertekening worden vastgelegd (UC-UIT-04); afdrukken
  gebeurt via UC-RAP-02.

---

## UC-UIT-02 — Regels van een bewijs

**Actor:** Balie · **Doel:** `proof_of_issue_lines` beheren (vervangt de Access-factuurregels
en prijskaartgegevens).

### Scherm — regel toevoegen (deel van UC-UIT-01)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Regel toevoegen                                                           │
├──────────────────────────────────────────────────────────────────────────┤
│ Omschrijving [________________________________________ ] *               │
│ Aantal [ 1,00 ]  Eenheid [ pcs ▾ ]  ( pcs | stuk | set | huur )         │
│ Bedrag (optioneel) [ € ______,____ ]                                     │
│ Bron [ handmatig ▾ ] ( handmatig | prijslijst onderdelen | licentie )     │
│ Bron-selectie: [ kies item ▾ ]  →  vul omschrijving en bedrag automatisch │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Toevoegen ]                                      [ Annuleren ]          │
└──────────────────────────────────────────────────────────────────────────┘
```

```gherkin
Feature: Bewijsregels

  Scenario: Regel uit de vaste prijslijst
    Gegeven de vaste prijslijst-items (seed/beheerdata), bv.
      "toetsenbord + muis", € 5,00, grootboek 81550
    Wanneer ik Bron "prijslijst onderdelen" kies en "toetsenbord + muis" selecteer
    Dan vult Omschrijving "toetsenbord + muis" en Bedrag 5,00
    En voegt de regel toe met line_id oplopend (1, 2, 3 … per bewijs)

  Scenario: Handmatige regel
    Wanneer ik Omschrijving "Eizo-monitor, toetsenbord en muis", Aantal 1,
      Eenheid pcs, Bedrag 15,00 invoer
    Dan bestaat een proof_of_issue_lines-rij met die waarden
      (description = "Eizo-monitor, toetsenbord en muis | 15,00", zie notities)

  Scenario: Subsidieregel met negatief bedrag
    Wanneer ik omschrijving "Subsidie Project; Iedereen surft mee" en bedrag -95,00 invul
    Dan slaat de regel op (negatief bedrag toegestaan)
    En toont de som € 0,00

  Scenario: Omschrijving verplicht
    Wanneer ik Toevoegen klik zonder omschrijving
    Dan verschijnt "Vul een omschrijving in."

  Scenario: Aantal
    Wanneer ik Aantal 0 invoer
    Dan verschijnt "Aantal moet groter dan nul zijn."
    Wanneer ik Aantal 2,5 invoer
    Dan slaat de regel op met quantity 2.50 (DECIMAL(10,2))

  Scenario: Regels verwijderen vóór vastleggen
    Wanneer ik een regel in het concept-bewijs verwijder
    Dan verdwijnt de regel (pas ná vastleggen zijn regels onveranderlijk)
```

### Technische notities

- `proof_of_issue_lines` heeft FK alleen op `proof_of_issue_id`; `line_id` is een
  composite-PK-deel — genereer de volgende line_id per bewijs (`MAX(line_id)+1`).
- `unit` VARCHAR(50) met default "pcs"; de UI-eenheidlijst bevat pcs | stuk | set | huur.
  Een ontbrekende eenheid wordt als "pcs" opgeslagen.
- Prijzen: er is geen bedragkolom. **Afspraak:** omschrijving-formaat
  `<omschrijving> | <bedrag EUR formaat 0,00>` (bv. "Windows licentie | 15,00") bij een
  bekend bedrag; de UI parseert dit bij tonen en sommeert de bedragen. Bij ontbreken: "—".

---

## UC-UIT-03 — Derde betaalt (behalf_of)

**Actor:** Balie · **Doel:** vastleggen dat een instelling/stichting voor de cliënt betaalt
(handleiding §9.1).

```gherkin
Feature: Derde betaalt

  Scenario: Bewijs met derde
    Gegeven schenker "Stichting Leergeld" (nr 44)
    Wanneer ik "derde betaalt" aanvink, Leergeld kies en uitgeef
    Dan is proof_of_issues.behalf_of_id 44
    En bevat het gerelateerde ticket details.derde_betaalt {"donor_id": 44, …}

  Scenario: Vinkje uit zonder schenker
    Wanneer ik het vinkje niet aanzet
    Dan is behalf_of_id null

  Scenario: Vinkje aan zonder schenkerkeuze
    Wanneer ik het vinkje aanzet zonder schenker te kiezen en uitgeef
    Dan verschijnt "Kies de instantie die namens de ontvanger betaalt."

  Scenario: E-mail factuurkopie
    Wanneer ik "E-mail factuurkopie aan" invul
    Dan gaat het adres naar tickets.details.derde_betaalt.email_bewijs
      (mailen zelf is buiten scope; het veld wordt wel gevuld)
```

### Technische notities

- `proof_of_issues.behalf_of_id` → FK `donors`; na uitgifte toont het afdrukoverzicht
  "Handelt namens: <naam>" (UC-RAP-02).
- Koppeling persoon ↔ schenker (UI-hint bij keuze): UC-DON-04.

---

## UC-UIT-04 — Ondertekening vastleggen

**Actor:** Balie · **Doel:** het getekende bewijs bewaren in `signed_image` (LONGBLOB).

### Scherm — ondertekening

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Ondertekening bewijs 123                                                  │
├──────────────────────────────────────────────────────────────────────────┤
│ Getekend door ontvanger [✓]   Getekend door balie [✓]                     │
│ Scan/foto van het getekende bewijs:                                        │
│ [ Bestand kiezen … (png/jpg, max 10 MB) ]  [ Uploaden ]                    │
│ Status: nog niet geüpload                                                 │
└──────────────────────────────────────────────────────────────────────────┘
```

```gherkin
Feature: Ondertekening vastleggen

  Scenario: Upload
    Wanneer ik een jpg van max 10 MB upload
    Dan staat de afbeelding in proof_of_issues.signed_image
    En toont UC-RAP-02 de handtekening als afbeelding (data-URI)

  Scenario: Te groot bestand
    Wanneer ik een bestand > 10 MB kies
    Dan verschijnt "Het bestand is groter dan 10 MB."

  Scenario: Verkeerd type
    Wanneer ik een pdf of exe kies
    Dan verschijnt "Alleen png- of jpg-afbeeldingen zijn toegestaan."

  Scenario: Nog niet getekend
    Gegeven signed_image is null
    Dan toont UC-RAP-01 bij het bewijs "ongetekend"
    En blijft in UC-RAP-02 alleen de lege ondertekeningslijn zichtbaar

  Scenario: Vervangen
    Wanneer ik een nieuwe afbeelding upload
    Dan wordt de oude vervangen en verschijnt "Vervangen op <datumtijd>."
```

### Technische notities

- Multipart-limit is 10 MB (application.yml); valideer ook in de bean.
- `signed_image LONGBLOB`: opslaan als JPEG/PNG-bytes, lezen via JSF `StreamedContent`
  (`p:graphicImage`), nooit als base64 in de view.
- Retentie/bewaartermijn: buiten scope; veld blijft tot `proof_of_issues`-verwijdering
  (alleen UC-RAP-03).

---

## UC-UIT-05 — Werkopdracht verkoop/uitgifte (ISSUE)

**Actor:** Balie · **Doel:** de Access-werkopdracht "Klaar maken voor verkoop": een ticket type
ISSUE op een gereserveerd apparaat, vóór de uitgifte.

### Scherm — Werkopdracht aanmaken

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Werkopdracht aanmaken (uitgifte/verkoop)                                  │
├──────────────────────────────────────────────────────────────────────────┤
│ Apparaat [ zoek apparaat ▾ ]      Status apparaat: Gereserveerd           │
│ Ontvanger (persoon) [ zoek klant ▾ ]  [ nieuw ]                           │
│ Namens schenker [ zoek schenker ▾ ]  [✓] derde betaalt                    │
│ Onderwerp [ Klaar maken voor uitgifte ▾ ]                                  │
│   ( Klaar maken voor uitgifte | Laatste controle op werking en updates )  │
│ Wat wordt erbij geleverd [ Eizo-monitor, muis en toetsenbord ▾ ]         │
│   ( geen accessoires | Eizo-monitor, muis en toetsenbord | op maat )      │
│ Omschrijving [                                                        ]   │
│ Aangenomen door [ medewerker ▾ ]                                          │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Werkopdracht maken ]                                                   │
└──────────────────────────────────────────────────────────────────────────┘
```

```gherkin
Feature: Werkopdracht (ISSUE)

  Scenario: Projectcomputer klaarmaken
    Gegeven apparaat 890 met status RESERVED en "Project Iedereen surft mee"
      in specification.bijzonderheden
    Wanneer ik Werkopdracht aanmaak met ontvanger "Johanna de Vries",
      onderwerp "Klaar maken voor uitgifte", accessoires "Eizo-monitor, muis en toetsenbord"
    Dan wordt ticket_type ISSUE aangemaakt met equipment_id 890 en een OPEN-statusregel
    En bevat de eerste logregel "[OPMERKING] Werkopdracht: Klaar maken voor uitgifte;
      bijgeleverd: Eizo-monitor, muis en toetsenbord"

  Scenario: Apparaat niet gereserveerd
    Gegeven apparaat met status SUITABLE_FOR_GIFT
    Wanneer ik Werkopdracht maken klik
    Dan verschijnt "Reserveer het apparaat eerst (status Gereserveerd)."
    En biedt het scherm aan het apparaat direct te reserveren (UC-EQP-05)

  Scenario: Derde betaalt
    Wanneer ik "derde betaalt" aanvink en schenker "Gemeente" kies
    Dan bevat tickets.details.derde_betaalt {"donor_id": 31, …}
    En wordt dit bij de uitgifte (UC-UIT-03) voorgevuld als behalf_of_id

  Scenario: Ontvanger gearchiveerd
    Dan blokkeert het scherm met de melding uit UC-INT-04
```

### Technische notities

- Bij ISSUE blijft `equipment.owner_id` ongewijzigd tot de uitgifte (UC-UIT-01); de ontvanger
  staat in `tickets.details.klantnr_verkoop` — bij uitgifte volgt
  `proof_of_issues.recipient_id`.
- Access printte een werkopdracht; hier: optionele afdruk van het ticket (UI-aangelegenheid).
- Sluiting volgt uit UC-UIT-01 (automatisch CLOSED).
