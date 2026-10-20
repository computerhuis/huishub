# 16 — Rapportage: uitgiftbewijs

Onderdeel: rapportage. Tabellen: `proof_of_issues`, `proof_of_issue_lines`; gekoppeld
`persons` (recipient_id, issued_by), `donors` (behalf_of_id), `equipment`; koppeling met
tickets via `tickets.details.proof_of_issue_id` (conventies §4.4). Schermen:
het uitgiftbewijsdocument (`proofOfIssueBean`). Het overzicht waaruit een bewijs
wordt geopend staat in UC-RAP-01; aanmaken en ondertekening in 04 — Uitgave
(UC-UIT-01 t/m 04).

**Webapp-variant:** het uitgiftbewijzenoverzicht en het uitgiftbewijsdocument uit het
webapp-prototype; de veld- en domeinregels in dit bestand zijn leidend boven de weergave
van het prototype.

---

## UC-RAP-02 — Uitgiftbewijs bekijken en afdrukken

**Actor:** Balie, Rapportage · **Rollen:** ROLE_BALIE, ROLE_RAPPORTAGE, ROLE_ADMIN ·
**Doel:** één bewijs als volledig, afdrukbaar document tonen — partijen, goederenregels met
bedragen, ondertekening — ter vervanging van de papieren 2-oudige uitgifteafdracht uit de
handleiding (§9).

### Scherm — Uitgiftbewijs (afdrukdocument)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Uitgiftbewijs 123                                             [ Afdrukken ]│
├──────────────────────────────────────────────────────────────────────────┤
│ UITGIFTEBEWIJS (Proof of Goods Issued – Geen Kosten)                     │
│ Uitgevende organisatie: Computerhuis 's-Hertogenbosch                    │
│ Handelt namens: Stichting Leergeld (alleen indien behalf_of_id gevuld)   │
├──────────────────────────────────────────────────────────────────────────┤
│ Uitgiftenummer [ U-123 ]        Uitgiftedatum [ 07-04-2026 ]             │
│ Ontvanger [ Johanna de Vries, Molenstraat 12, 5211AB Den Bosch ]         │
│ Uitgegeven door [ Klaas Bakker ]                                         │
├──────────────────────────────────────────────────────────────────────────┤
│ Uitgeleverde goederen                                                     │
│ Omschrijving                    │ Aantal │ Eenheid │ Bedrag               │
│ HP dc7800 — uitgegeven          │ 1,00   │ pcs     │ € 80,00              │
│ Windows licentie                │ 1,00   │ pcs     │ € 15,00              │
│ Totaal                                                         € 95,00    │
├──────────────────────────────────────────────────────────────────────────┤
│ Opmerkingen: de goederen zijn kosteloos ter beschikking gesteld.         │
├──────────────────────────────────────────────────────────────────────────┤
│ Ontvanger: naam [ Johanna de Vries ]                                     │
│            handtekening [ afbeelding of lege lijn ]  datum [ __-__-____ ]│
│ Uitgever : naam [ Klaas Bakker ]      handtekening [ afbeelding of      │
│                                              lege lijn ]                 │
├──────────────────────────────────────────────────────────────────────────┤
│ "Bewaar dit bewijs goed. Bij verlies of schade kunt u geen aanspraak     │
│  maken."                              "Kopie voor eigen administratie"   │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Uitgiftbewijs — weergave

  Scenario: Document tonen
    Gegeven bewijs id 123 met ontvanger "Johanna de Vries", uitgever "Klaas Bakker",
      apparaat 891 en twee regels
    Dan toont het scherm het volledige document zoals hierboven

  Scenario: Niet gevonden
    Gegeven onbekend id
    Dan toont het scherm paneel "Niet gevonden — Er is geen uitgiftbewijs gevonden
      met dit nummer."

  Scenario: Handtekening
    Gegeven signed_image gevuld
    Dan toont de handtekeninglyn de afbeelding (data-URI)
    En bij leeg blijft alleen de lege lijn zichtbaar

  Scenario: Nog niet getekend
    Gegeven signed_image is null (conventies: vastgelegd in UC-UIT-04)
    Dan bevat het document een lege ondertekeningslijn met ruimte voor naam,
      handtekening en datum van de ontvanger

  Scenario: Namens een donateur
    Gegeven behalf_of_id gevuld met schenker "Stichting Leergeld" (nr 44)
    Dan toont "Handelt namens" de donateursnaam
    En wanneer behalf_of_id null is, blijft de regel "Handelt namens" weg

  Scenario: Goederenregels
    Gegeven bewijsregels met aantallen en eenheden
    Dan toont de tabel "Uitgeleverde goederen" elke regel (omschrijving, aantal,
      eenheid); bij geen regels "Geen goederen geregistreerd"

  Scenario: Bedragen uit de suffixconventie
    Gegeven regel 1 met description "HP dc7800 — uitgegeven | 80,00" en
      regel 2 met description "Windows licentie | 15,00" (conventie UC-UIT-02)
    Dan toont de kolom Bedrag € 80,00 respectievelijk € 15,00
    En toont de regel Totaal € 95,00

  Scenario: Regel zonder bedrag
    Gegeven een regel zonder suffix ("Bewijs van inname — toetsenbord")
    Dan toont de kolom Bedrag bij die regel "—"
    En telt die regel niet mee in het totaal

  Scenario: Subsidieregel met negatief bedrag
    Gegeven regel "Subsidie Project; Iedereen surft mee | -95,00"
    Dan toont de kolom Bedrag € -95,00
    En verlaagt de regel het totaal

  Scenario: Afdrukken
    Wanneer ik op "Afdrukken" klik
    Dan opent de browserprintdialoog zonder menubalk, broodkruimel en groeimeldingen

  Scenario: Doel van uitgifte
    Gegeven het bijbehorende ISSUE-ticket (koppeling tickets.details.proof_of_issue_id)
      met een doeltekst in tickets.details respectievelijk een eerste regelomschrijving
      met prefix "Doel:"
    Dan toont het document het doel in de opmerkingenzone
    En wanneer geen van beide bestaat blijft de regel "Doel:" weg
```

### Validaties

| Situatie | Melding |
|---|---|
| Onbekend bewijs-id | `Niet gevonden — Er is geen uitgiftbewijs gevonden met dit nummer.` |
| Bewijs zonder regels | tabel toont `Geen goederen geregistreerd` (geen fout) |
| Bedrag-suffix niet parsebaar | kolom toont `—`; regel telt niet mee in het totaal |

### Technische notities

- Documentnummer: `documentNumber` bestaat niet als kolom (schema is leidend) —
  implementeer als `"U-" + id`. Uitgiftedatum = `proof_of_issues.issue_date`, NL-formaat
  `dd-MM-yyyy` (conventies §2).
- "Doel van uitgifte" (`purpose`): komt uit `tickets.details` van het bijbehorende
  ISSUE-ticket (koppeling `tickets.details.proof_of_issue_id`, conventies §4.4) of bij
  ontbreken uit de eerste regelomschrijving met prefix "Doel:".
- Regels: `proof_of_issue_lines` op `proof_of_issue_id`, gesorteerd op `line_id`;
  `quantity` DECIMAL(10,2) en `unit` met UI-labels pcs | stuk | set | huur.
- Bedragen: er is geen bedragkolom. Parse de suffixconventie uit UC-UIT-02:
  omschrijving-formaat `<omschrijving> | <bedrag EUR formaat 0,00>`; toon "—" bij
  ontbreken en sommeer de geparseerde bedragen (negatief toegestaan, zie UC-UIT-02).
- Ondertekening: `signed_image LONGBLOB` (vastgelegd in UC-UIT-04) lezen via JSF
  `StreamedContent` (`p:graphicImage`); in de printweergave als data-URI, nooit als base64
  in de lijstweergave. Bij `signed_image IS NULL` blijft alleen de lege lijn zichtbaar.
- "Handelt namens": `proof_of_issues.behalf_of_id` → FK `donors`; toon `donors.name`
  (afdrachtregel uit handleiding §9.1, zie UC-UIT-03).
- Afdrukken: `window.print()` op een printview; per CSS (`@media print`) zijn menubalk,
  broodkruimelnavigatie en growl-/groemeldingen verborgen. De twee vaste voetteksten uit
  de handleiding ("Bewaar dit bewijs goed…" en "Kopie voor eigen administratie") staan
  altijd onderaan.
- Read-only scherm: wijzigen van bewijzen loopt via UC-UIT-01 t/m 04; verwijderen via
  UC-RAP-03.

---

## UC-RAP-03 — Uitgiftbewijs corrigeren en verwijderen

**Actor:** Beheerder · **Rollen:** ROLE_ADMIN · **Doel:** een fout bewijs ongedaan maken
zonder geschiedenis te vernietigen. Vervangt de Access-correctiefunctie op bewijzen; dit is
de enige plek in de applicatie waar een bewijs definitief verdwijnt.

### Scherm — Bewijs verwijderen (bevestiging vanuit UC-RAP-01)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Bewijs 123 verwijderen                                                   │
├──────────────────────────────────────────────────────────────────────────┤
│ Dit bewijs en al zijn regels worden definitief verwijderd.                │
│ Bewijs [ 123 ]  Datum [ 07-04-2026 ]  Ontvanger [ J. de Vries ]           │
│ Apparaat [ 891 — status Gift uitgegeven ]                                 │
│ Typ het uitgiftenummer ter bevestiging: [ 123 ] *                         │
│ [✓] Zet de status van apparaat 891 terug naar de status vóór de uitgifte  │
│ [ Verwijderen ]                              [ Annuleren ]                │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Uitgiftbewijs corrigeren en verwijderen

  Scenario: Verwijderen niet-ongetekend bewijs
    Gegeven ingelogd als ROLE_ADMIN, bewijs zonder ondertekening en geen
      gerelateerd CLOSED-ticket
    Wanneer ik het bewijs verwijder en het uitgiftenummer ter bevestiging typ
    Dan wordt de proof_of_issues-rij en zijn regels fysiek verwijderd
    En wordt apparaatstatus teruggezet naar de status vóór de uitgifte
      (alleen mogelijk als equipment.status = GIFT_ISSUED en er één bewijs was)

  Scenario: Bevestiging verplicht
    Wanneer ik Verwijderen klik zonder (of met verkeerd) getal in het bevestigingsveld
    Dan verschijnt "Typ het uitgiftenummer ter bevestiging." en wordt niets verwijderd

  Scenario: Getekend bewijs
    Gegeven signed_image gevuld
    Wanneer ik probeer te verwijderen
    Dan verschijnt "Een getekend bewijs mag niet verwijderd worden. Neem contact
      op met de beheerder voor correctie."

  Scenario: Gerelateerd afgerond ticket
    Gegeven het gerelateerde ticket 4530 via details.proof_of_issue_id is CLOSED
    Wanneer ik het bewijs verwijder
    Dan verschijnt de waarschuwing "Ticket 4530 is afgerond en verwijst naar dit
      bewijs. Het ticket behoudt zijn geschiedenis; het koppelingveld wordt geleegd."
    En kan ik pas na bevestigen doorgaan

  Scenario: Apparaatstatus terugzetten
    Gegeven apparaat 891 met status GIFT_ISSUED en exact dit bewijs als enige bewijs
    Wanneer ik verwijder met vinkje aan
    Dan wordt equipment.status SUITABLE_FOR_GIFT
    En wordt equipment.owner_id null

  Scenario: Apparaat niet terug te zetten
    Gegeven apparaat 891 met status GIFT_ISSUED maar een tweede bewijs in de periode
    Wanneer ik verwijder met vinkje aan
    Dan blijft equipment.status GIFT_ISSUED
    En verschijnt "Het apparaat heeft meer dan één bewijs; de status is niet
      teruggezet. Controleer het apparaat handmatig (UC-EQP-04)."

  Scenario: Balie-mag-niet
    Gegeven ROLE_BALIE
    Dan is de actie "Verwijderen" niet beschikbaar
    En geeft een directe aanroep van het verwijderpad HTTP 403 met
      "U hebt geen rechten voor dit scherm." (conventies §1)

  Scenario: Rapportage-mag-niet
    Gegeven ROLE_RAPPORTAGE
    Dan is de actie "Verwijderen" niet beschikbaar

  Scenario: Naslag
    Wanneer de verwijdering is voltooid
    Dan keert het scherm terug naar UC-RAP-01
    En staat er een applicatielogregel "[VERWIJDERD] proof_of_issue 123 door <user>"
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Bevestigingsveld | verplicht, numeriek, gelijk aan het bewijs-id | `Typ het uitgiftenummer ter bevestiging.` |
| Ondertekening | `signed_image` moet null zijn | `Een getekend bewijs mag niet verwijderd worden. Neem contact op met de beheerder voor correctie.` |
| Rol | uitsluitend ROLE_ADMIN (conventies §1) | `U hebt geen rechten voor dit scherm.` (HTTP 403) |

### Technische notities

- Dit is de enige plek waar fysieke DELETE op `proof_of_issues`/`proof_of_issue_lines` is
  toegestaan; alles elders is soft delete (conventies §2: archiveren =
  `unregistered = NOW()` — bewijzen kennen geen `unregistered`-kolom).
- `proof_of_issue_lines` heeft FK `ON DELETE CASCADE` op `proof_of_issue_id`; de
  regels verdwijnen mee. Verwijder in dezelfde transactie.
- Rollencheck server-side in de service-laag (niet alleen verbergen van de knop);
  optimistic locking is niet in het schema aanwezig — toon bij laden "laatst gewijzigd"
  en waarschuw bij afwijkend tijdstip vóór verwijderen (conventies §5).
- Status terugzetten: alleen indien `equipment.status = GIFT_ISSUED` en dit bewijs het
  enige `proof_of_issues`-rijtje op dat apparaat is; terugzet naar de status vóór de
  uitgifte (in de praktijk SUITABLE_FOR_GIFT) en `equipment.owner_id` op null. Andere
  statussen (RESERVED, DEMOLITION, SOLD) blijven ongewijzigd.
- Koppeling naar tickets: leeg `tickets.details.proof_of_issue_id` (JSON-sleutel uit
  conventies §4.4) van elk ticket dat naar dit bewijs verwijst; laat ticketstatus en
  `[STATUS]`-logregels ongemoeid.
- Log de verwijdering met gebruiker en tijd in de applicatielogs; export- en
  verwijderacties worden gelogd (AVG, idem UC-RAP-08).
