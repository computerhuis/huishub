# 16 — Rapportage: overzichten en exports

Onderdeel: rapportage. Bronnen: `timesheets`, `activities`, `tickets`, `ticket_status`,
`equipment`, `proof_of_issues`, `proof_of_issue_lines`, `persons`, `postal_codes`,
`donors`. Views waar mogelijk: `ticket_overview`, `ticket_status_overview`,
`timesheet_overview`, `equipment_history`. Alle rapportages zijn **leesactiviteiten**
(conventies §1: `ROLE_RAPPORTAGE` en `ROLE_ADMIN`); mutaties komen niet voor in dit
bestand. Schermen: het kerncijferrapport (`figuresBean`, UC-RAP-04),
het rapport verloop reparaties (`repairProgressBean`, UC-RAP-05),
het rapport uitgiften en schenkingen (`issueReportBean`, UC-RAP-06),
het rapport gebruikersverdeling (`userDistributionBean`, UC-RAP-07),
de mailinglijstexport (`mailingListBean`, UC-RAP-08),
het schenkersrapport (`donorReportBean`, UC-RAP-09). Het doorzoeken en bekijken van
uitgiftbewijzen staat in UC-RAP-01 t/m UC-RAP-03 (aparte bestanden in dit onderdeel).

**Webapp-variant:** geen pendant in het webapp-prototype; de bovenstaande schermen zijn
nieuw, met dezelfde schermopbouw als de prototypepagina's (PrimeFaces,
conventies §6 en §8).

Algemene schermopbouw voor elk rapport:

```
┌──────────────────────────────────────────────────────────────────────────┐
│ <Rapportnaam>                                                             │
├──────────────────────────────────────────────────────────────────────────┤
│ Periode: Van [ __-__-____ ]  Tot [ __-__-____ ]                           │
│ <extra filters per rapport>                                               │
│ [ Genereer ]  [ Excel-export ]  [ Print / PDF ]                           │
├──────────────────────────────────────────────────────────────────────────┤
│ <resultaatlijst>                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

Periode-algemeen: `Genereer` rekent server-side; resultaat in `p:dataTable`; periodekeuze
via `p:calendar`; `Excel-export` levert `.xlsx` via POI met identieke kolommen;
`Print / PDF` via `p:exporter`; standaardperiode = lopend kalenderjaar.

---

## UC-RAP-04 — Uitdraai cijfers (kerncijfers per periode)

**Actor:** Rapportage, Beheerder · **Rollen:** ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:**
vervangt `Frm_Uitdraai_Cijfers` en `Tbl_Uitdraai_Cijfers_temp` uit Access: de kerncijfers
over bezoeken, vrijwilligers en aanwas per periode. Scherm: het kerncijferrapport.

### Scherm — Uitdraai cijfers

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Uitdraai cijfers                                                          │
├──────────────────────────────────────────────────────────────────────────┤
│ Periode: Van [ 01-01-2026 ]  Tot [ 31-12-2026 ]                           │
│ [✓] Alleen activiteiten met "meedoen in rapportage"                        │
│ [ Genereer ]  [ Excel-export ]                                            │
├──────────────────────────────────────────────────────────────────────────┤
│ Cijfer                                    │ Aantal │ Periode              │
│ Totaal aantal bezoeken                    │ 1 204  │ 01-01-2026–31-12-2026│
│ Unieke bezoekers                          │   312  │ idem                 │
│ Bezoeken per activiteit: Klik & Tik        │   146  │ idem                 │
│ Bezoeken per activiteit: Vrije Inloop      │   512  │ idem                 │
│ Aantal medewerkers/vrijwilligers (actief)  │    14  │ idem                 │
│ Vrienden Computerhuis Oost                 │   354  │ idem                 │
│ Nieuwe personen geregistreerd              │    87  │ idem                 │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Uitdraai cijfers

  Scenario: Kerncijfers over periode
    Gegeven 1204 timesheets-rijen met registered in de periode
    Wanneer ik Genereer klik
    Dan toont Totaal aantal bezoeken 1204
    En toont Unieke bezoekers het aantal DISTINCT person_id daarvan

  Scenario: Activiteitenfilter
    Gegeven activiteit "Gast" heeft reporting false
    Wanneer ik het vinkje "Alleen activiteiten met meedoen in rapportage" aanhoud
    Dan bevat de lijst geen regel voor "Gast"
    En wanneer ik het vinkje uitzet wel

  Scenario: Rapportagevlag per rij
    Dan bevat elke activiteitsregel de telling van timesheets in de periode
      voor die activiteit (ook als reporting false, mits vinkje uit)

  Scenario: Lege activiteit
    Gegeven een timesheet-rij op een activiteit met reporting true en geen
      bezoeken in de periode
    Dan bevat de lijst die activiteit met Aantal 0

  Scenario: Medewerkers
    Dan telt "Aantal medewerkers" personen met msaccess.vrijwilligerswerk_start niet-null
      en eind null of eind ≥ periode-einde

  Scenario: Vrienden
    Dan telt "Vrienden Computerhuis Oost" personen met
      msaccess.vrienden_computerhuis_oost = true (unregistered null)

  Scenario: Nieuwe personen
    Dan telt "Nieuwe personen" personen met registered binnen de periode

  Scenario: Lege periode
    Wanneer Van > Tot
    Dan verschijnt "De begindatum ligt na de einddatum."
    En wordt niets gegenereerd

  Scenario: Excel-export
    Wanneer ik Excel-export klik zonder resultaat
    Dan verschijnt "Genereer eerst een overzicht."
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Periode Van/Tot | verplicht, Van ≤ Tot | `De begindatum ligt na de einddatum.` |
| Vinkje rapportagevlag | optioneel (default aan) | — |

### Technische notities

- Bezoeken: `SELECT COUNT(*), COUNT(DISTINCT person_id) FROM timesheets WHERE registered
  BETWEEN :van AND :tot + 1 dag`.
- Per-activiteit: gebruik `timesheet_overview` (levert `activity_name` naast
  `person_id`/`registered`); groepeer op `activity_id`.
- Rapportagevlag: `activities.reporting` (BOOLEAN, default false) — de vlag wordt beheerd
  in UC-EDU-02; dit rapport leest hem uitsluitend.
- Medewerkers: JSON-sleutels uit conventies §4.1 —
  `msaccess.vrijwilligerswerk_start` (niet-null) en `msaccess.vrijwilligerswerk_eind`
  (null of ≥ periode-einde). Vrienden: `msaccess.vrienden_computerhuis_oost = true`.
  Ontbrekende sleutels mogen null zijn (conventies §4).
- Nieuwe personen: `persons.registered` binnen de periode, `unregistered IS NULL`.
- Geen temp-tabel nodig (Access `Tbl_Uitdraai_Cijfers_temp` is hier overbodig); resultaat
  is een DTO-lijst.
- Exportkolommen: Cijfer, Aantal, Van, Tot.

---

## UC-RAP-05 — Verloop reparaties per periode

**Actor:** Rapportage, Beheerder · **Rollen:** ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:**
vervangt `Qwy_Verloop_reparaties_in_periode_*` en `Rpt_Verloop_Reparaties_werkplaats`:
hoe lang liep een reparatie? Scherm: het rapport verloop reparaties.

### Scherm — Verloop reparaties

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Verloop reparaties                                                        │
├──────────────────────────────────────────────────────────────────────────┤
│ Periode (ingevoerde tickets): Van [ 01-01-2026 ]  Tot [ 31-12-2026 ]      │
│ Type [ Reparaties ▾ ] ( Reparaties | Alle )                               │
│ [✓] Alleen afgerond (CLOSED)                                              │
│ [ Genereer ]  [ Excel-export ]                                            │
├──────────────────────────────────────────────────────────────────────────┤
│ Afhandelduur              │ Aantal │ Aandeel                              │
│ Binnen 1 week             │   64   │ 54%                                  │
│ Binnen 2 weken            │   31   │ 26%                                  │
│ Binnen 3 weken            │   14   │ 12%                                  │
│ Langer dan 3 weken        │    9   │   8%                                 │
│ Totaal                    │  118   │ 100%                                │
├──────────────────────────────────────────────────────────────────────────┤
│ Openstaande tickets op einddatum: 7                                       │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Verloop reparaties

  Scenario: Bucketing
    Gegeven ticket 4521 registered 01-04-2026 en gesloten (nieuwste status CLOSED)
      05-04-2026
    Wanneer ik Genereer klik
    Dan valt het ticket in "Binnen 1 week"

  Scenario Outline: Grenzen
    Gegeven een afgerond ticket met duur <duur>
    Dan valt het in bucket <bucket>

    Voorbeelden:
      | duur       | bucket                |
      | 6 dagen    | Binnen 1 week         |
      | 7 dagen    | Binnen 1 week         |
      | 8 dagen    | Binnen 2 weken        |
      | 14 dagen   | Binnen 2 weken        |
      | 15 dagen   | Binnen 3 weken        |
      | 21 dagen   | Binnen 3 weken        |
      | 22 dagen   | Langer dan 3 weken    |

  Scenario: Alleen afgerond uitgeschakeld
    Wanneer ik "Alleen afgerond" uitzet
    Dan bevat elke bucket uitsluitend tickets die op de einddatum al CLOSED waren;
      openstaande tickets tellen mee in "Langer dan 3 weken" tot hun sluitingsdatum
      (implementatie: sluitingsdatum ontbreekt → classificeer op einddatum periode)

  Scenario: Openstaande op einddatum
    Dan toont de regel "Openstaande tickets op einddatum" het aantal tickets met
      registered ≤ einddatum en (geen CLOSED-status of laatste status-datum > einddatum)

  Scenario: Alleen reparaties
    Gegeven het filter Type staat op "Reparaties"
    Wanneer ik Genereer klik
    Dan bevat het rapport uitsluitend ticket_type REPAIR
    En bij keuze "Alle" bevat het rapport zowel REPAIR- als ISSUE-tickets

  Scenario: Aandeel
    Dan is elk aandeel = Aantal / Totaal, afgerond op hele procenten
      En is de som van de aandelen 100%

  Scenario: Lege periode
    Wanneer Van > Tot
    Dan verschijnt "De begindatum ligt na de einddatum."
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Periode Van/Tot | verplicht, Van ≤ Tot | `De begindatum ligt na de einddatum.` |
| Type | verplicht, enum-waardenlijst | — |

### Technische notities

- Sluitingsdatum = `MAX(ticket_status.date)` waar status CLOSED; aanmaak =
  `tickets.registered`.
- Duur in dagen inclusief; bucketgrenzen: `<=7`, `<=14`, `<=21`, `>21`.
- Bron bij voorkeur `ticket_status_overview` (levert de naam van de vrijwilliger);
  geen eigen join met `persons`.
- "Statusstroom" per ticket: de volledige statusgeschiedenis staat in `ticket_status` /
  `ticket_status_overview` en is raadpleegbaar via UC-TIC-03 (ticketdetail); dit rapport
  toont uitsluitend de optellingen hierboven.
- Typefilter: `tickets.ticket_type` ENUM ('REPAIR', 'ISSUE'); de periode filtert op
  `tickets.registered`, niet op de statusdatum.

---

## UC-RAP-06 — Uitgiften en schenkingen per periode

**Actor:** Rapportage, Beheerder · **Rollen:** ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:**
vervangt `Rpt_gratis_pcs`, `Rpt_gratis_smartphone`, `Rpt_gratis_simkaart`,
Leergeld-rapporten en `Qwy_overzicht_uitgiften`: wat is er weggegeven? Scherm:
het rapport uitgiften en schenkingen.

### Scherm — Uitgiften en schenkingen

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Uitgiften en schenkingen                                                  │
├──────────────────────────────────────────────────────────────────────────┤
│ Periode: Van [ 01-01-2026 ]  Tot [ 31-12-2026 ]                           │
│ Schenker [ alle ▾ ]   Categorie [ alle ▾ ]                                │
│ [ Genereer ]  [ Excel-export ]                                            │
├──────────────────────────────────────────────────────────────────────────┤
│ Cijfer                              │ Aantal │ Waarde (som regels)        │
│ Uitgegeven apparaten totaal         │   42   │ € 2 340,00                 │
│   waarvan desktop                   │   21   │ € 1 470,00                 │
│   waarvan laptop                    │   14   │ €   700,00                 │
│   waarvan tablet                    │    4   │ €   120,00                 │
│   waarvan mobiel (smartphone)       │    2   │ €    30,00                 │
│   waarvan sim                       │    1   │ €    20,00                 │
│ Waarvan via Stichting Leergeld      │    9   │ —                          │
│ Waarvan in project "Iedereen …"     │   12   │ € 1 200,00                 │
│ Sloopgeschreven (DEMOLITION)        │    6   │ —                          │
│ Nieuwe schenkingen ontvangen        │   18   │ —                          │
├──────────────────────────────────────────────────────────────────────────┤
│ Per schenker:                                                              │
│ Gemeente 's-Hertogenbosch │ geschonken 12 │ uitgegeven 10 │ restant 2     │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Uitgiften en schenkingen

  Scenario: Uitgiften tellen
    Gegeven bewijzen met soort uitgifte en issue_date in de periode
    Wanneer ik Genereer klik
    Dan telt "Uitgegeven apparaten" elk uniek apparaat uit proof_of_issues in die periode

  Scenario: Categorieverdeling
    Dan sommeert de som van de categorieregels gelijk aan het totaal

  Scenario: Waarde
    Dan berekent Waarde de som van de bedragen uit proof_of_issue_lines
      (suffixconventie uit UC-UIT-02) per bewijs/apparaat

  Scenario: Regel zonder bedrag
    Gegeven een proof_of_issue_lines-regel zonder suffixbedrag
    Dan telt die regel wel mee in Aantal maar niet in Waarde
    En toont de Waarde-cel "—" als er geen enkel bedrag is

  Scenario: Schenkerfilter
    Wanneer ik schenker "Gemeente" kies
    Dan bevat de lijst uitsluitend bewijzen/apparaten met donor_id van die schenker

  Scenario: Categoriefilter
    Wanneer ik Categorie "Laptop" kies
    Dan bevat de lijst uitsluitend apparaten met category LAPTOP
      En is de som van de categorieregels gelijk aan het totaal binnen dat filter

  Scenario: Leergeld
    Dan telt "Waarvan via Stichting Leergeld" apparaten waar
      specification.leergeld.laptop of .smartphone of .simkaart true is
      of waar de schenker de naam "Leergeld" bevat

  Scenario: Nieuwe schenkingen
    Dan telt "Nieuwe schenkingen ontvangen" apparaten met
      status INCOMING_GIFT en registered in de periode

  Scenario: Sloop
    Dan telt "Sloopgeschreven" apparaten met status DEMOLITION waarvan de
      sloop in de periode viel (registered/statusdatum, zie notities)

  Scenario: Lege periode
    Wanneer Van > Tot
    Dan verschijnt "De begindatum ligt na de einddatum."
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Periode Van/Tot | verplicht, Van ≤ Tot | `De begindatum ligt na de einddatum.` |
| Schenker | bestaande donor (conventies §2, UC-DON-01) | `Kies een bestaande schenker.` |
| Categorie | enum-waarden uit `equipment.category` | — |

### Technische notities

- Bewijssoort: `proof_of_issues` kent geen kolom voor soort; **afspraak** (idem UC-UIT-01):
  het soort zit in de eerste regelomschrijving of — beter — in het gerelateerde
  `tickets.ticket_type` (ISSUE = uitgifte, REPAIR = inname). Implementeer "uitgifte" als:
  `equipment.status = GIFT_ISSUED` met `proof_of_issues` in de periode; "inname" als bewijs
  bij een REPAIR-ticket. Koppeling ticket ↔ bewijs via
  `tickets.details.proof_of_issue_id` (conventies §4.4) en via `equipment_id` +
  `issue_date`. Leg dit vast in codecomments.
- Waarde: `proof_of_issue_lines` heeft geen bedragkolom — parse de suffixconventie uit
  UC-UIT-02 (`<omschrijving> | <bedrag EUR formaat 0,00>`); negatieve bedragen (subsidie)
  tellen mee; bij ontbreken "—".
- Categorieën: `equipment.category` ENUM ('DESKTOP', 'LAPTOP', 'TABLET', 'MOBILE', 'SIM',
  'USB_STICK') met Nederlandse labels uit UC-EQP-05.
- Leergeld: JSON-sleutel `leergeld` in `equipment.specification` (conventies §4.3) met
  `laptop` / `smartphone` / `simkaart`; of `donors.name LIKE '%Leergeld%'` — implementeer
  beide condities met OR en documenteer de keuze.
- Project "Iedereen surft mee": filter op `equipment.specification.bijzonderheden LIKE
  '%Iedereen surft mee%'`.
- Per schenker: `equipment.donor_id` voor "geschonken";
  `proof_of_issues.behalf_of_id` of het gerelateerde ISSUE-ticket voor "uitgegeven";
  "restant" = geschonken − uitgegeven.
- Sloop: `equipment.status = DEMOLITION`; neem het moment van sloop uit de
  statushistorie (`equipment_history`, indien beschikbaar) of — bij ontbreken —
  `equipment.registered`; documenteer de keuze in de implementatie.

---

## UC-RAP-07 — Gebruikers per buurt, wijk en leeftijd

**Actor:** Rapportage, Beheerder · **Rollen:** ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:**
vervangt `Rpt_Gebruikers_per_Buurt`, `Rpt_Gebruikers_per_wijk` en
`Rpt_Aantal_Gebruikers_per_Leeftijds_categorie`. Scherm: het rapport gebruikersverdeling.

### Scherm — Verdeling gebruikers

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Verdeling gebruikers                                                      │
├──────────────────────────────────────────────────────────────────────────┤
│ Groepering [ Buurt ▾ ]  ( Buurt | Wijk | Plaats | Leeftijdscategorie )    │
│ Periode registratie: Van [ __-__-____ ]  Tot [ __-__-____ ] (optioneel)   │
│ [✓] Alleen actieve personen (niet gearchiveerd)                           │
│ Minimum aantal per groep [ 5 ]                                            │
│ [ Genereer ]  [ Excel-export ]                                            │
├──────────────────────────────────────────────────────────────────────────┤
│ Buurt                │ Aantal │ Aandeel                                   │
│ Binnenstad            │   38  │ 12%                                        │
│ Bossche Broek         │   21  │  7%                                        │
│ Onbekend              │   44  │ 14%                                        │
├──────────────────────────────────────────────────────────────────────────┤
│ Totaal inbegrepen: 268   Totaal buiten minimum: 44                         │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Verdeling gebruikers

  Scenario: Groeperen op buurt
    Wanneer ik Buurt kies en Genereer
    Dan groepeert het rapport op postal_codes.neighbourhood via persons.postal_code
    En toont "Onbekend" voor personen zonder postcode of met een postcode zonder buurt

  Scenario: Groeperen op wijk
    Wanneer ik Wijk kies
    Dan groepeert het rapport op postal_codes.district
    En toont "Onbekend" voor lege districten

  Scenario: Groeperen op plaats
    Wanneer ik Plaats kies
    Dan groepeert het rapport op postal_codes.city
      (bij ontbrekende postcode: persons.city, of "Onbekend")

  Scenario: Groeperen op leeftijd
    Wanneer ik Leeftijdscategorie kies
    Dan toont het rapport drie regels: "Jonger dan 40 jaar" (<40),
      "Tussen 40 en 60" (40 t/m 59) en "Ouder dan 60" (≥60),
      berekend op de einddatum van de periode (of vandaag zonder periode)

  Scenario: Onbekende geboortedatum
    Dan valt de persoon in een vierde regel "Onbekend"

  Scenario: Periode registratie
    Wanneer ik een registratieperiode invoer
    Dan telt de lijst uitsluitend personen met registered in die periode
    En zonder periode alle personen

  Scenario: Minimum
    Wanneer ik minimum 5 invoer
    Dan bevat de lijst groepen met minimaal 5 personen
    En vermeldt de resterende regel "Totaal buiten minimum"

  Scenario: Actieve personen
    Dan bevat de telling uitsluitend personen met unregistered null
    En wanneer ik het vinkje uitzet tellen gearchiveerde personen mee
      (conventies §2)

  Scenario: Aandeel
    Dan is Aandeel = Aantal / Totaal inbegrepen, afgerond op hele procenten

  Scenario: Lege periode
    Wanneer Van > Tot
    Dan verschijnt "De begindatum ligt na de einddatum."
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Groepering | verplicht, enum-waardenlijst | `Kies een groepering.` |
| Periode Van/Tot | optioneel; indien gevuld Van ≤ Tot | `De begindatum ligt na de einddatum.` |
| Minimum | geheel getal ≥ 0 | `Minimum moet 0 of hoger zijn.` |

### Technische notities

- Postcode-spatie: `persons.postal_code` is zonder spatie; `postal_codes.code` eveneens
  (CHAR(6)); join rechtstreeks op `persons.postal_code = postal_codes.code`.
- Buurt = `postal_codes.neighbourhood`, wijk = `postal_codes.district`, plaats =
  `postal_codes.city`; kolomnamen letterlijk uit het schema.
- Leeftijd: `TIMESTAMPDIFF(YEAR, date_of_birth, :peildatum)`; NULL-datum → Onbekend;
  peildatum = periode-einde of `CURDATE()` zonder periode.
- Aandeel wordt berekend over "Totaal inbegrepen"; groepen onder het minimum verdwijnen
  uit de lijst maar worden wel meegerekend in "Totaal buiten minimum".

---

## UC-RAP-08 — Mailinglijst export

**Actor:** Rapportage, Beheerder · **Rollen:** ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:**
vervangt `Tbl_mailinglijst_actieve_gebruikers` en `Qry_Mailinglijst*`: e-mailadressen van
actieve gebruikers (personen met bezoekactiviteit in de periode), exporteerbaar naar
CSV/PDF. Scherm: de mailinglijstexport.

### Scherm — Mailinglijst

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Mailinglijst actieve gebruikers                                          │
├──────────────────────────────────────────────────────────────────────────┤
│ Actief sinds [ __-__-____ ]  (default: 12 maanden terug)                  │
│ [✓] Alleen personen met e-mailadres                                       │
│ [✓] Sluit afgemelden nieuwsbrief uit (msaccess.afgemeld_nieuwsbrief)      │
│ [ Genereer ]  [ CSV ]  [ PDF ]                                            │
├──────────────────────────────────────────────────────────────────────────┤
│ Nr │ Naam            │ E-mail            │ Laatste activiteit             │
│ 42 │ Johanna de Vries│ jvries@example.nl │ 03-05-2026 Klik & Tik          │
├──────────────────────────────────────────────────────────────────────────┤
│ Totaal: 309 personen                                                     │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Mailinglijst

  Scenario: Actieve gebruikers
    Wanneer ik Genereer klik met default periode 12 maanden
    Dan bevat de lijst personen met een timesheets-rij na de peildatum
    En toont Laatste activiteit de meest recente timesheets-rij (datum + activiteitnaam)

  Scenario: Zonder e-mail
    Wanneer ik "Alleen met e-mailadres" aanhoud
    Dan bevat de lijst uitsluitend personen met een gevuld, niet-leeg e-mailadres

  Scenario: Afgemeld nieuwsbrief
    Wanneer ik het vinkje aanhoud
    Dan sluit de lijst personen uit met msaccess.afgemeld_nieuwsbrief = true

  Scenario: Exportkolommen
    Dan bevat de export exact: Gebruikersnummer, Naam, E-mail, Laatste activiteit

  Scenario: CSV-export
    Wanneer ik op CSV klik
    Dan download ik een .csv van de gefilterde lijst met dezelfde kolommen
      en puntkomma als scheidingsteken (NL-Excel)

  Scenario: PDF-export
    Wanneer ik op PDF klik
    Dan opent het printbare document met dezelfde kolommen en een kop
      "Mailinglijst actieve gebruikers", periode en totalen

  Scenario: Export zonder resultaat
    Wanneer ik CSV of PDF klik zonder gegenereerde lijst
    Dan verschijnt "Genereer eerst een overzicht."

  Scenario: Gearchiveerde personen
    Dan bevat de lijst uitsluitend personen met unregistered null
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Actief sinds | verplicht, datum | `Vul een peildatum in.` |
| Actief sinds | niet in de toekomst | `De peildatum ligt in de toekomst.` |

### Technische notities

- Bepaalt actiefheid via `EXISTS (SELECT 1 FROM timesheets t WHERE t.person_id = p.id AND
  t.registered >= :peildatum)`.
- "Laatste activiteit": laatste `timesheets`-rij per persoon; toon datum (`dd-MM-yyyy`)
  plus `timesheet_overview.activity_name`.
- `msaccess.afgemeld_nieuwsbrief` (BOOLEAN, conventies §4.1): JSON-extractie in de query;
  ontbrekende sleutel telt als false.
- Export: `p:exporter` op de `p:dataTable` (CSV en PDF); POI/`.xlsx`-export is hier niet
  van toepassing — CSV en PDF zijn expliciet de formaten van deze use-case.
- Privacy: export bevat geen telefoon/adres; toegang alleen via ROLE_RAPPORTAGE/ADMIN en
  exportacties worden gelogd (AVG).

---

## UC-RAP-09 — Schenkersrapport (donateurs)

**Actor:** Rapportage, Beheerder · **Rollen:** ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:**
vervangt `Rpt_Donateurs` en `Qry_Donateurs_per_jaar`: giften per schenker over een jaar.
Doorverwijzing naar de schenkerdossiers loopt via UC-DON-01. Scherm:
het schenkersrapport.

### Scherm — Schenkersrapport

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Schenkersrapport                                                          │
├──────────────────────────────────────────────────────────────────────────┤
│ Jaar [ 2026 ▾ ]   [✓] Alleen schenkers met "rapportage" aan                 │
│ [ Genereer ]  [ Excel-export ]                                            │
├──────────────────────────────────────────────────────────────────────────┤
│ Schenker                   │ Apparaten │ Uitgegeven │ Restant │ Waarde   │
│ Gemeente 's-Hertogenbosch  │    12     │    10      │    2    │ € 960,00 │
│ Stichting Leergeld         │     9     │     9      │    0    │ € 540,00 │
├──────────────────────────────────────────────────────────────────────────┤
│ Totaal: 31 apparaten, € 2 340,00                                        │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Schenkersrapport

  Scenario: Giften per schenker
    Wanneer ik een jaar kies en Genereer
    Dan telt per donor de apparaten met donor_id en registered (of datum_gift in
      specification) in dat jaar

  Scenario: Reportingfilter
    Gegeven schenker X heeft donors.reporting false
    Wanneer ik het vinkje aanhoud
    Dan bevat de lijst schenker X niet

  Scenario: Restant
    Dan berekent Restant = Apparaten − Uitgegeven (waar bewijs bestaat in dat jaar
      of later)

  Scenario: Waarde
    Dan berekent Waarde de som van de bedragen uit proof_of_issue_lines
      (suffixconventie UC-UIT-02) van de uitgiften van die schenker
    En toont "—" wanneer geen enkele regel een bedrag draagt

  Scenario: Uitgiften via derde
    Gegeven bewijs met behalf_of_id = die schenker (UC-UIT-03)
    Dan telt dat bewijs mee in de kolom Uitgegeven van die schenker

  Scenario: Lege schenkers
    Dan bevat de lijst alleen schenkers met minimaal één geschenk dat jaar

  Scenario: Schenker openen
    Wanneer ik op een schenkernaam klik
    Dan opent het scherm van UC-DON-01 (schenker zoeken) op die schenker

  Scenario: Jaar buiten bereik
    Wanneer ik een jaar kies zonder giften
    Dan toont de tabel "Geen giften gevonden voor 2027." en een leeg totaal
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Jaar | verplicht, 2000 t/m huidig jaar | `Kies een geldig jaar.` |

### Technische notities

- Datum van gift: `equipment.registered` of `specification.datum_gift`
  (JSON-sleutel uit conventies §4.3; indien gevuld die vooraan nemen — documenteer de
  keuze in de implementatie).
- Waarde komt uit `proof_of_issue_lines`-bedragen (suffixconventie UC-UIT-02); bij
  ontbreken "—".
- Uitgegeven: `proof_of_issues` waarvan `equipment.donor_id` de schenker is, of waar
  `behalf_of_id` de schenker is, met `issue_date` in het gekozen jaar of later (voor
  Restant); koppeling ticket ↔ bewijs via `tickets.details.proof_of_issue_id`
  (conventies §4.4).
- Rapportagevlag: `donors.reporting` (BOOLEAN, default false) — vinkje default uit.
- Schenkers zonder `unregistered IS NULL` tellen niet mee (conventies §2); naam en plaats
  uit `donors.name` / `donors.city`; laatst bekende contactgegevens via
  `donors.msaccess` (conventies §4.2).
