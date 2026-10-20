# 16 — Rapportage: uitgiftbewijzen

Onderdeel: rapportage. Tabellen: `proof_of_issues`, `proof_of_issue_lines`; gekoppeld
`persons` (recipient_id, issued_by), `donors` (behalf_of_id), `equipment`; koppeling met
tickets via `tickets.details.proof_of_issue_id` (conventies §4.4) en via `equipment_id` +
`issue_date`. Schermen: het uitgiftbewijzenoverzicht (`proofOfIssueListBean`);
resultaat van "Openen"/"Afdrukken": het uitgiftbewijsdocument (UC-RAP-02).
Aanmaken, regels en ondertekening van bewijzen staan in 04 — Uitgave (UC-UIT-01 t/m 04);
hieronder uitsluitend het doorzoeken en beheren van het overzicht.

**Webapp-variant:** het uitgiftbewijzenoverzicht en het uitgiftbewijsdocument uit het
webapp-prototype; de veld- en domeinregels in dit bestand zijn leidend boven de weergave
van het prototype.

---

## UC-RAP-01 — Uitgiftbewijzen zoeken

**Actor:** Balie, Rapportage · **Rollen:** ROLE_BALIE, ROLE_RAPPORTAGE, ROLE_ADMIN ·
**Doel:** alle bewijzen van inname en uitgifte doorzoeken op nummer, persoon, apparaat,
periode, soort en ondertekening, en van daaruit openen, afdrukken, ondertekening uploaden
of (als beheerder) verwijderen. Vervangt het Access-overzicht van bewijzen; het
webapp-prototype leverde de vrij zoekopdracht en paginering.

### Scherm — Uitgiftbewijzen

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Uitgiftbewijzen                                                          │
├──────────────────────────────────────────────────────────────────────────┤
│ Zoeken [ Uitgiftenummer, ontvanger, uitgever of datum (dd-mm-jjjj) ]      │
│ [ Zoeken ]  [ Wissen ]                                                    │
├──────────────────────────────────────────────────────────────────────────┤
│ Soort [ alle ▾ ] ( alle | Bewijs van inname | Bewijs van uitgifte )       │
│ Periode: Van [ __-__-____ ]  Tot [ __-__-____ ]                           │
│ Ontvanger [ zoek klant ▾ ]     Uitgereikt door [ zoek medewerker ▾ ]      │
│ Apparaat [ zoek apparaat ▾ ]   Schenker (handelt namens) [ alle ▾ ]       │
│ [✓] Alleen ongetekend                                                     │
├──────────────────────────────────────────────────────────────────────────┤
│ Nr │ Datum │ Soort │ Ontvanger │ Uitgegeven door │ Apparaat │ Namens │    │
│ Getekend │                                                                    │
│ 123 │ 07-04-2026 │ Uitgifte │ J. de Vries │ K. Bakker │ 891 │ Leergeld │   │
│ [ Openen ] [ Afdrukken ] [ Upload ondertekening ] [ Verwijderen ]         │
├──────────────────────────────────────────────────────────────────────────┤
│ Totaal: 37 bewijzen (pagina 1 van 4)              [ Export (.xlsx) ]      │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Uitgiftbewijzen — overzicht

  Scenario: Alle bewijzen
    Wanneer ik het scherm open
    Dan toont de tabel alle bewijzen, 10 per pagina, uitgiftenummer aflopend

  Scenario: Zoeken op nummer
    Wanneer ik "123" invoer en zoek
    Dan bevat de lijst het bewijs met dit uitgiftenummer

  Scenario: Zoeken op naam
    Wanneer ik "Vries" invoer
    Dan bevat de lijst bewijzen waarvan ontvanger of uitgever "Vries" bevat

  Scenario: Zoeken op datum
    Wanneer ik "07-04-2026" invoer
    Dan bevat de lijst bewijzen met deze uitgiftedatum

  Scenario: Zoeken op meerdere termen
    Wanneer ik "123 vries 07-04-2026" invoer
    Dan behandelt het scherm elk deel afzonderlijk (id-getal / datum / LIKE op naam)
      En bevat de lijst uitsluitend bewijzen die aan alle delen voldoen

  Scenario: Wissen
    Wanneer ik op Wissen klik
    Dan is het zoekveld leeg, zijn de filters gereset en toont de lijst weer alles

  Scenario: Filter op periode
    Wanneer ik 01-01-2026 tot 31-03-2026 invoer
    Dan bevat de lijst uitsluitend bewijzen met issue_date in die periode

  Scenario: Filter op soort
    Wanneer ik Soort "Bewijs van uitgifte" kies
    Dan bevat de lijst uitsluitend uitgiftebewijzen
      (implementatie: equipment.status = GIFT_ISSUED met bewijs in de periode,
       zie de afspraak in de technische notities)

  Scenario: Filter op ontvanger en apparaat
    Wanneer ik ontvanger "Johanna de Vries" kies
    Dan bevat de lijst uitsluitend bewijzen met recipient_id 42
    Wanneer ik apparaat 891 kies
    Dan bevat de lijst uitsluitend bewijzen met equipment_id 891

  Scenario: Filter niet-ongetekend
    Wanneer ik het vinkje "Alleen ongetekend" aanzet
    Dan bevat de lijst alleen bewijzen met signed_image null
    En toont de kolom "Getekend" bij die bewijzen de badge "ongetekend"
      (de ondertekening zelf wordt vastgelegd in UC-UIT-04)

  Scenario: Badge ongetekend
    Gegeven bewijs 124 met signed_image null en bewijs 123 met een geüploade ondertekening
    Dan toont de kolom "Getekend" bij 124 de badge "ongetekend" en bij 123 "ja"

  Scenario: Openen
    Wanneer ik bij een resultaat op Openen klik
    Dan toont het detail (UC-RAP-02) alle velden, de regels (met gesommeerd bedrag) en
      de ondertekening indien aanwezig

  Scenario: Afdrukken
    Wanneer ik op Afdrukken klik
    Dan opent het afdrukdocument van UC-RAP-02 direct in de printweergave

  Scenario: Upload ondertekening
    Wanneer ik op "Upload ondertekening" klik
    Dan opent het ondertekeningsscherm van UC-UIT-04 voor dit bewijs

  Scenario: Verwijderen (alleen beheerder)
    Gegeven ingelogd als ROLE_ADMIN
    Wanneer ik op Verwijderen klik
    Dan opent de bevestiging van UC-RAP-03

  Scenario: Verwijderen zonder adminrechten
    Gegeven ingelogd als ROLE_BALIE of ROLE_RAPPORTAGE
    Dan is de knop "Verwijderen" niet zichtbaar

  Scenario: Export
    Wanneer ik Export klik
    Dan download ik een .xlsx van de gefilterde lijst met regeldetails

  Scenario: Geen resultaat
    Dan toont de tabel "Geen uitgiftbewijzen gevonden"

  Scenario: Lege periode
    Wanneer Van na Tot ligt
    Dan verschijnt "De begindatum ligt na de einddatum." en wordt niets gezocht
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Periode Van/Tot | indien allebei gevuld: Van ≤ Tot | `De begindatum ligt na de einddatum.` |
| Zoeken | vrije term mag leeg zijn (toont dan alles) | — |
| Ontvanger/Apparaat | bestaand, niet-gearchiveerd (conventies §2) | `Kies een bestaand record.` |

### Technische notities

- Query op `proof_of_issues` + `LEFT JOIN persons` (recipient_id → naam ontvanger,
  issued_by → naam uitgever), `LEFT JOIN donors` (behalf_of_id → "handelt namens"),
  `LEFT JOIN equipment`; regels via `proof_of_issue_lines`.
- Vrij zoekveld: de zoekopdracht wordt op leegtes gesplitst; elk deel wordt behandeld als
  id-getal (op `proof_of_issues.id`), datum in `dd-mm-jjjj` (op `issue_date`) of
  `LIKE`-term op ontvanger- of uitgeversnaam. Het veld werkt op de volledige set; daarna
  pagineert de tabel 10 rijen per pagina (maximum 50, conventies §3).
- Soort: `proof_of_issues` kent geen soortkolom (schema is leidend). **Afspraak** (idem
  UC-UIT-01): uitgifte herkenbaar aan `equipment.status = GIFT_ISSUED` met een bewijs in de
  periode; inname aan het gerelateerde REPAIR-ticket via
  `tickets.details.proof_of_issue_id` (conventies §4.4). Leg dit vast in codecomments.
- Badge "ongetekend": kolom toont badge bij `signed_image IS NULL`, anders "ja"; het vinkje
  "Alleen ongetekend" filtert op `signed_image IS NULL`. De afbeelding zelf wordt pas in
  UC-UIT-04 vastgelegd.
- `proof_of_issues` heeft geen `unregistered`-kolom: de zachte verwijdering uit
  conventies §2 is hier niet van toepassing. Fysiek verwijderen kan uitsluitend in
  UC-RAP-03.
- Bedragensom (kolom Waarde in de export): parse de suffixconventie uit UC-UIT-02
  (`<omschrijving> | <bedrag EUR>`) of toon "—" als geen bedrag.
- Export (POI): `.xlsx` met de kolommen van de gefilterde lijst plus regeldetails
  (omschrijving, aantal, eenheid, bedrag); identieke kolomvolgorde als het scherm.
- Rollen: `ROLE_RAPPORTAGE` is lees-only (conventies §1); knoppen met een mutatie
  ("Upload ondertekening", "Verwijderen") volgen de rollen van UC-UIT-04 respectievelijk
  UC-RAP-03.
