# 13 — Tickets: beheer (crud)

Onderdeel: tickets-domein (reparaties en werkopdrachten). Tabellen: `tickets`,
`ticket_status`, `ticket_log`, gekoppeld `equipment` en `persons`. Views: `ticket_overview`,
`ticket_status_overview`, `ticket_log_overview`. Scherm: het ticketformulier
(`ticketCrudBean`) — het enige scherm waar tickets, hun status, hun details-JSON en hun
logboek worden gewijzigd; de overzichten staan in `13-tickets/list.md`. Het ticketoverzicht
en de werkplaatslijst openen dit scherm per ticket; ook de balie opent het vanuit
UC-BAL-04 en UC-BAL-05. Archivering is soft delete (`tickets.unregistered`, conventies §2);
het schema wordt niet gewijzigd.

**Webapp-variant:** het ticketformulier uit het prototype had vier losse
onderdelen (ticketvelden, details-keyvalue-tabel, logs-tabel met teksteditor en
status-/logdialogen); deze zijn hier samengevoegd tot UC-TIC-03 t/m UC-TIC-06, aangevuld met de
domeinregels uit oud "Tickets": statusstroom, klant bellen, logconventie en archivering.

---

## Ticketstatusstroom (leidend voor UC-TIC-04)

| Enum-waarde | Nederlands label | Kleur (indicatief) | Betekenis |
|---|---|---|---|
| `OPEN` | Binnengekomen | rood | Aangenomen, nog niet behandeld |
| `IN_PROGRESS` | In behandeling | groen | Behandelaar is bezig, óf wacht op klant (logregel) |
| `READY` | Gereed | oranje | Werk af, nog niet gemeld |
| `CUSTOMER_INFORMED` | Klant geïnformeerd | zwart | Klant gebeld/ingespreken; computer kan opgehaald worden |
| `CLOSED` | Afgerond | grijs | Opgehaald/vervalt; niet meer te wijzigen |

Toegestane overgangen:

```
OPEN ──► IN_PROGRESS | CLOSED
IN_PROGRESS ──► READY | OPEN  (terug, bv. heropening na herbeoordeling)
READY ──► CUSTOMER_INFORMED | IN_PROGRESS
CUSTOMER_INFORMED ──► CLOSED | IN_PROGRESS  (klant komt alsnog met nieuwe wens)
CLOSED ──► (einde; heropening verboden, ook niet voor ADMIN)
```

**Vertaling Access-status "Klant gebeld, wachtende" (blauw):** status blijft `IN_PROGRESS`;
het belmoment gaat als logregel met prefix `[BELLEN WACHTEN]` naar `ticket_log`. Er is geen
aparte enum-waarde (schema is leidend).

## Logconventie `ticket_log.log`

Elke regel begint met een code tussen blokhaken, gevolgd door vrije tekst:

| Code | Gebruik |
|---|---|
| `[BELLEN]` | Klant gebeld; afspraak/voicemail |
| `[BELLEN WACHTEN]` | Klant gebeld, wacht op klant (blauw) |
| `[SAMENVATTING]` | Samenvatting reparatie door behandelaar |
| `[STATUS]` | Automatische regel bij elke statuswijziging |
| `[OPMERKING]` | Vrije opmerking |

Datum/tijd en vrijwilliger komen uit de kolommen `ticket_log.date` en `volunteer_id` zelf;
herhaal ze niet in de logtekst. Logregels zijn append-only: nooit wijzigen of verwijderen.
De prefix-codes zijn verplicht bij handmatige regels (UC-TIC-06); `[STATUS]`-regels worden
uitsluitend automatisch geschreven (UC-TIC-04).

---

## UC-TIC-03 — Ticket weergeven en wijzigen

**Actor:** Balie, Werkplaats, Rapportage (alleen lezen) · **Rollen:** ROLE_BALIE,
ROLE_WERKPLAATS, ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:** één ticket volledig in beeld
(dossier) én de basisvelden wijzigen: type, onderwerp en omschrijving. Dit is het
openingsscherm vanuit het klantdossier (UC-BAL-04 en UC-BAL-05), vanuit het statusbord
(UC-TIC-01) en vanuit het werkplaatsoverzicht (UC-TIC-02).

### Scherm — Ticketdossier

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Ticket 4521 (REPAIR) — Binnengekomen (OPEN)                               │
│ [ Status ] [ Klant bellen ] [ Logboek ] [ Details ] [ Archiveren ]        │
├──────────────────────────────────────────────────────────────────────────┤
│ Apparaat (alleen lezen)                                                   │
│ Nr (link) [ 890 ]  Categorie [ DESKTOP ]  Fabrikant [ HP ]                │
│ Model [ dc7800 ]  Status [ Klanteigen ▾ (alleen lezen) ]                  │
├──────────────────────────────────────────────────────────────────────────┤
│ Ticket                                                                    │
│ Identificatienummer [ 4521        ] (disabled)                            │
│ Type [ REPAIR ▾ ]   Inschrijf datum [ 01-04-2026 09:12 ] (disabled)      │
│ Onderwerp [ start niet op_________________________________ ]              │
│ Omschrijving [                                                       ]    │
│ Factuuradres: M. Jansen (nr 77)   (alleen als details.klantnr_verkoop)   │
│ Uitgeschreven datum [ __-__-____ ] (alleen lezen; vult UC-TIC-07)        │
├──────────────────────────────────────────────────────────────────────────┤
│ Samenvatting                                                              │
│ Klant     Johanna de Vries (nr 42)  0612345678                            │
│ Ingenomen 01-04-2026 09:12  door Klaas                                    │
│ Backup    GEEN   Accessoires: voedingsadapter                             │
│ Details   tafel 3 · accountwachtwoord: ingevuld · e-mailgegevens: nee     │
├──────────────────────────────────────────────────────────────────────────┤
│ Tabblad [ Statushistorie ] [ Logboek ] [ Bewijzen ] [ Apparaathistorie ] │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Annuleren ]                                          [ Opslaan ]        │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Ticketdossier en wijzigen

  Scenario: Alles in één beeld
    Dan toont het scherm het apparatuurblok (alleen lezen), de ticketvelden,
      een samenvatting uit tickets.details en vier tabbladen

  Scenario: Tabbladen
    Dan toont Statushistorie de statusregels (ticket_status_overview),
      Logboek de logregels (UC-TIC-06), Bewijzen de gekoppelde bewijzen
      (proof_of_issues via tickets.details.proof_of_issue_id) en
      Apparaathistorie de historie van het gekoppelde apparaat (equipment_history)

  Scenario: Onderwerp en omschrijving wijzigen
    Wanneer ik Onderwerp en Omschrijving aanpas en Opslaan klik
    Dan zijn tickets.subject en tickets.description opgeslagen
    En verschijnt een growl-bevestiging "Ticket 4521 opgeslagen."

  Scenario: Type wijzigen
    Wanneer ik Type op ISSUE zet en opsla
    Dan is tickets.ticket_type gewijzigd
    En bevestigt het scherm "Type gewijzigd naar ISSUE."

  Scenario: Rijacties in de kop
    Wanneer ik op Status klik opent UC-TIC-04, op Klant bellen UC-TIC-05,
      op Logboek het tabblad Logboek (UC-TIC-06) en op Details het
      details-formulier (UC-TIC-06) en op Archiveren UC-TIC-07

  Scenario: Wijzigen bij CLOSED
    Gegeven status CLOSED
    Dan zijn Status, Klant bellen en Archiveren uitgeschakeld
    En is het formulier alleen-lezen ("Dit ticket is afgesloten en niet meer
      te wijzigen."); alleen het Logboek-tabblad is nog leesbaar

  Scenario: Gearchiveerd ticket
    Gegeven tickets.unregistered is gevuld
    Dan verschijnt bij laden "Dit ticket is gearchiveerd."
    En is Opslaan geblokkeerd tot heropening (UC-TIC-07, ADMIN)

  Scenario: Wijzigen door een ander (laatst gewijzigd)
    Gegeven het scherm is geladen om 10:00 en een collega sloeg om 10:05 op
    Wanneer ik Opslaan klik
    Dan verschijnt "Dit ticket is gewijzigd nadat u het opende (laatst
      gewijzigd 10:05). Vernieuw het scherm en probeer opnieuw."
      (conventies §5, geen optimistic locking in het schema)

  Scenario: Uitgeschreven datum
    Dan toont het scherm tickets.unregistered alleen-lezen (NL-formaat);
      vulling volgt uit UC-TIC-07 (archiveren) of de uitgiftestroom
      (UC-BAL-04 + UC-UIT-01) — handmatig invullen is niet mogelijk

  Scenario: Factuuradres (klantnr_verkoop)
    Gegeven tickets.details.klantnr_verkoop wijst naar persoon 77
    Dan toont het scherm "Factuuradres: M. Jansen (nr 77)" naast de klantgegevens

  Scenario: Rapportage
    Gegeven ik ben ingelogd als ROLE_RAPPORTAGE
    Dan opent het scherm alleen-lezen: geen Opslaan, geen rijacties
      (conventies §1)
```

### Validaties

| Situatie | Melding |
|---|---|
| Type niet gekozen | `Kies een type.` |
| Onderwerp langer dan 255 tekens | `Onderwerp is maximaal 255 tekens.` |
| Ticket met status CLOSED, Opslaan aangeklikt | `Dit ticket is afgesloten en niet meer te wijzigen.` |
| Ticket gearchiveerd | `Dit ticket is gearchiveerd. Heropen het scherm eerst (beheerder).` |
| Apparaat gearchiveerd (apparatuurblok) | Geen blokkade; het blok toont `gearchiveerd` bij de apparaatstatus (conventies §2) |

### Technische notities

- Wijzigbare kolommen: `tickets.ticket_type` (enum `REPAIR` | `ISSUE`), `tickets.subject`,
  `tickets.description`. `tickets.id`, `tickets.registered` en `tickets.unregistered` zijn
  niet-wijzigbaar in de UI (conventies §2); `tickets.equipment_id` komt uit de intake
  (UC-INT-01..04) of de werkopdracht (UC-UIT-05) en wordt hier niet herkozen.
- Apparatuurblok uit `ticket_overview` (`equipment_id`, `category`, `manufacturer`, `model`)
  plus de apparaatstatus uit `equipment.status` (alleen-lezen); apparaatlink naar
  het apparaatformulier (UC-EQP-04).
- Klantgegevens via `equipment.owner_id → persons`; "Ingenomen door" uit de eerste
  `ticket_status`-regel (OPEN) met naam via `ticket_status_overview`.
- Samenvatting-blok leest `tickets.details` (JSON) volgens de registry uit
  00-conventies.md §4.4: `backup` (enum GEEN | GEHELE | DEEL | ONBEKEND),
  `bijgeleverde_accessoires`, `tafel`, `account_wachtwoord` (nooit de waarde tonen, alleen
  "ingevuld ja/nee"), `email_gegevens`, `klantnr_verkoop`, `derde_betaalt`,
  `proof_of_issue_id`, `legacy_reparatienummer`, `_overig`.
- Bewijzen-tabblad: koppeling tickets ↔ bewijzen loopt via
  `tickets.details.proof_of_issue_id` en via `proof_of_issues` op apparaat/klant
  (schema leidend: `proof_of_issues` heeft geen ticketkolom). Uitgifte van een gereed
  ticket loopt via UC-BAL-04 + UC-UIT-01; het bewijs verschijnt daarna in UC-RAP-01.
- Opslaan is transactieel over de ticketvelden; details-JSON en logregels hebben hun eigen
  acties (UC-TIC-06) maar worden in dezelfde `@Transactional`-grens meegenomen als ze in
  dezelfde request zitten.
- `p:selectOneMenu` voor Type met de volledige enum-lijst (`REPAIR`, `ISSUE`) en Nederlandse
  labels (Reparatie / Werkopdracht); `p:datePicker` uitsluitend voor weergave (disabled).

---

## UC-TIC-04 — Ticketstatus bijwerken

**Actor:** Werkplaats, Balie · **Rollen:** ROLE_WERKPLAATS, ROLE_BALIE, ROLE_ADMIN · **Doel:**
de statusstroom bijwerken; iedere wijziging legt een `ticket_status`-regel én een
`[STATUS]`-logregel vast (historie!). De toegestane overgangen staan in de
Ticketstatusstroom bovenaan dit bestand.

### Scherm — Status bijwerken

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Ticket 4521 — status                                                      │
├──────────────────────────────────────────────────────────────────────────┤
│ Huidige status: Binnengekomen (OPEN)         Vastgelegd door: Klaas      │
│ Nieuwe status [ In behandeling ▾ ]  (alleen toegestane doelstatussen)    │
│ Toelichting [________________________________________ ]                   │
│ (alleen bij OPEN → IN_PROGRESS) Aan welke tafel gaat u werken? [__ ]     │
│ [ Wijzigen ]                                    [ Annuleren ]             │
├──────────────────────────────────────────────────────────────────────────┤
│ Laatste statusregels (ticket_status_overview):                           │
│ 01-04 09:12 OPEN        Klaas                                            │
│ 01-04 13:40 IN_PROGRESS Klaas                                            │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Ticketstatus bijwerken

  Scenario Outline: Toegestane overgangen
    Gegeven ticket met status <van>
    Wanneer ik de status wijzig naar <naar> met een verplichte toelichting
    Dan bestaat een nieuwe ticket_status-regel met <naar>, date nu en de huidige gebruiker
    En bestaat een nieuwe ticket_log-regel "[STATUS] <van> → <naar>: <toelichting>"
      met dezelfde datum en vrijwilliger

    Voorbeelden:
      | van               | naar                  |
      | OPEN              | IN_PROGRESS           |
      | OPEN              | CLOSED                |
      | IN_PROGRESS       | READY                 |
      | IN_PROGRESS       | OPEN                  |
      | READY             | CUSTOMER_INFORMED     |
      | READY             | IN_PROGRESS          |
      | CUSTOMER_INFORMED | CLOSED                |
      | CUSTOMER_INFORMED | IN_PROGRESS           |

  Scenario Outline: Verboden overgangen
    Gegeven ticket met status <van>
    Wanneer ik de status wil wijzigen naar <naar>
    Dan verschijnt "Status <naar> is niet toegestaan vanuit <van>."
    (In de melding staan de Nederlandse labels, niet de enum-waarden.)

    Voorbeelden:
      | van               | naar               |
      | CLOSED            | IN_PROGRESS        |
      | CLOSED            | OPEN               |
      | CLOSED            | READY              |
      | CLOSED            | CUSTOMER_INFORMED  |
      | OPEN              | READY              |
      | OPEN              | CUSTOMER_INFORMED  |
      | IN_PROGRESS       | CUSTOMER_INFORMED  |

  Scenario: Alleen doelstatussen in de selectie
    Gegeven ticket met status READY
    Dan bevat de selectie uitsluitend CUSTOMER_INFORMED en IN_PROGRESS

  Scenario: In behandeling met tafel
    Wanneer ik van OPEN naar IN_PROGRESS wijzig
    Dan vraagt het scherm "Aan welke tafel gaat u werken? (tafelnummer)"
    En bevat details.tafel het ingevoerde getal

  Scenario: Gereed meldt verplichte samenvatting
    Wanneer ik van IN_PROGRESS naar READY wil wijzigen zonder dat er een
      [SAMENVATTING]-logregel bestaat
    Dan verschijnt "Vul eerst de samenvatting van de reparatie aan."
    En opent het logboek-invulvenster (UC-TIC-06)

  Scenario: Heropening na herbeoordeling
    Gegeven status IN_PROGRESS en "verkeerd beoordeeld" als toelichting
    Wanneer ik terug naar OPEN wijzig
    Dan bestaat de statusregel OPEN met die toelichting in de [STATUS]-logregel

  Scenario: Sluiten automatisme bij uitgifte
    Gegeven UC-UIT-01 rondt een bewijs van uitgifte af voor dit ticket
      (gekoppeld via tickets.details.proof_of_issue_id)
    Dan wordt de status automatisch CLOSED
    En wordt een [STATUS]-logregel geschreven door dat proces
    En is handmatige heropening onmogelijk

  Scenario: Apparaatstatus volgt niet mee
    Gegeven het ticket is REPAIR op apparaat 890 (status Klanteigen)
    Wanneer ik de ticketstatus naar READY wijzig
    Dan verandert equipment.status niet
      (ticket- en apparaatstatus zijn onafhankelijke statusstromen)
```

### Validaties

| Situatie | Melding |
|---|---|
| Verboden overgang | `Status <Nederlands label> is niet toegestaan vanuit <Nederlands label>.` |
| Toelichting leeg | `Vul een toelichting in.` |
| OPEN → IN_PROGRESS zonder tafelnummer | `Vul het tafelnummer in.` |
| Tafelnummer niet numeriek | `Vul een getal in voor het tafelnummer.` |
| IN_PROGRESS → READY zonder [SAMENVATTING]-logregel | `Vul eerst de samenvatting van de reparatie aan.` |
| Status CLOSED (scherm geopend) | `Dit ticket is afgesloten; de status is niet meer te wijzigen.` |
| Ticket gearchiveerd | `Dit ticket is gearchiveerd; de status is niet te wijzigen.` |

### Technische notities

- `ticket_status.date` heeft seconden-nauwkeurigheid; de composite PK is
  (`ticket_id`, `date`). Twee wijzigingen in dezelfde seconde door dezelfde persoon op
  hetzelfde ticket zijn daardoor onmogelijk — verhoog bij PK-collision `date` met één seconde
  en probeer opnieuw.
- Altijd transactueel: `ticket_status`-regel + `ticket_log`-regel in één `@Transactional`;
  bij falen rolt alles terug.
- De `p:selectOneMenu` toont alleen de doelstatussen uit de matrix hierboven; de
  service-laag herhaalt dezelfde validatie (conventies §5: bij conflict wint de
  service-laag).
- Afhankelijkheden: apparaatstatus heeft een eigen stroom (UC-EQP-03, labels/kleuren in
  UC-EQP-05); een ticketstatuswijziging raakt `equipment.status` nooit. UC-EQP-05 toont de
  tickethistorie in het apparaatdossier en leest daar dezelfde nieuwste-statuslogica
  (`MAX(date)`). De sluiting door uitgifte (UC-UIT-01) schrijft dezelfde
  status+`[STATUS]`-logregel server-side; UC-BAL-04 is het baliepunt waar die uitgifte
  start.
- Rollen: ROLE_WERKPLAATS, ROLE_BALIE en ROLE_ADMIN mogen alle overgangen; er zijn geen
  verdere per-overgang rechten (conventies §1). Sluiten bij intake zonder reparatie
  (OPEN → CLOSED) is een balie-taak ("klant haalt apparaat op zonder reparatie").

---

## UC-TIC-05 — Klant bellen (afspraken vastleggen)

**Actor:** Balie · **Rollen:** ROLE_BALIE, ROLE_ADMIN · **Doel:** het Access-knoppenwerk
"Klant bellen: over reparatie" en "Klant bellen: computer gereed" vervangen: elk belmoment
wordt een logregel; de maximaal drie "gebelde" velden uit Access zijn hiermee vervangen door
een onbeperkte historie. Het vinkje "wacht op klant" vertaalt de Access-status "Klant gebeld,
wachtende" (blauw) naar de logpreflight `[BELLEN WACHTEN]` — de status blijft
**IN_PROGRESS** (Ticketstatusstroom hierboven; schema is leidend).

### Scherm — Klant bellen

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Klant bellen — ticket 4521 — Johanna de Vries (0612345678)                │
├──────────────────────────────────────────────────────────────────────────┤
│ Soort gesprek [ Over reparatie ▾ ]                                        │
│   ( Over reparatie | Computer gereed )                                    │
│ Resultaat [ Afspraak ▾ ]                                                  │
│   ( Afspraak | Voicemail | Geen reactie | Opgehaald )                     │
│ Afspraak/bericht [____________________________________________ ]          │
│ Beschikbaar op [ __-__-____ ] van [____] tot [____]                       │
├──────────────────────────────────────────────────────────────────────────┤
│ [✓] Zet ticket op "wacht op klant" (status blijft In behandeling)         │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Registreren ]                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Klant bellen

  Scenario: Afspraak over reparatie
    Gegeven ticket 4521 met status IN_PROGRESS
    Wanneer ik Soort "Over reparatie", Resultaat "Afspraak" en
      "onderdelen arriveeren donderdag, ophalen vrijdag" invul en registreer
    Dan bestaat een ticket_log-regel "[BELLEN] Over reparatie; afspraak:
      onderdelen arriveeren donderdag, ophalen vrijdag"
    En verandert de status niet

  Scenario: Wacht op klant
    Gegeven ik vink "Zet ticket op wacht op klant"
    Wanneer ik registreer
    Dan bestaat de logregel "[BELLEN WACHTEN] Over reparatie; …"
    En blijft de status IN_PROGRESS (conform vertaling Access "blauw")

  Scenario: Computer gereed
    Gegeven ticket met status READY
    Wanneer ik Soort "Computer gereed" en Resultaat "Afspraak" registreer
    Dan bestaat de logregel "[BELLEN] Computer gereed; afspraak: …"
    En stelt het scherm voor de status te wijzigen naar CUSTOMER_INFORMED
      (UC-TIC-04)

  Scenario: Voicemail
    Wanneer ik Resultaat "Voicemail" kies en registreer
    Dan bevat de logregel "[BELLEN] Over reparatie; voicemail ingesproken"

  Scenario: Geen reactie
    Wanneer ik Resultaat "Geen reactie" kies en registreer
    Dan bevat de logregel "[BELLEN] Over reparatie; geen reactie"

  Scenario: Opgehaald
    Gegeven ticket met status CUSTOMER_INFORMED
    Wanneer ik Soort "Computer gereed" en Resultaat "Opgehaald" registreer
    Dan bevat de logregel "[BELLEN] Computer gereed; opgehaald"
    En stelt het scherm voor de status naar CLOSED te wijzigen (UC-TIC-04);
      de uitgifte zelf loopt via UC-BAL-04 + UC-UIT-01

  Scenario: Beschikbaarheid vastleggen
    Wanneer ik Beschikbaar op "10-04-2026" van "13:00" tot "17:00" invul
    Dan bevat de logregel "beschikbaar 10-04-2026 13:00-17:00"

  Scenario: Verplicht bericht bij afspraak
    Wanneer ik Resultaat "Afspraak" kies zonder Afspraak/bericht
    Dan verschijnt "Vul een afspraak of bericht in."

  Scenario: Klant zonder telefoonnummer
    Gegeven de klant heeft zowel persons.telephone als persons.mobile leeg
    Dan verschijnt de waarschuwing "Deze klant heeft geen telefoonnummer."
    En kan ik alsnog registreren (het gesprek vond immers plaats)
```

### Validaties

| Situatie | Melding |
|---|---|
| Soort gesprek niet gekozen | `Kies een soort gesprek.` |
| Resultaat niet gekozen | `Kies een resultaat.` |
| Resultaat "Afspraak" zonder bericht | `Vul een afspraak of bericht in.` |
| Beschikbaar op ingevuld zonder van/tot | `Vul de beschikbare tijdspanne volledig in.` |
| Status CLOSED | `Dit ticket is afgesloten; registreren van een belmoment is niet meer mogelijk.` (alleen-lezen logboek, UC-TIC-06) |
| Ticket gearchiveerd | `Dit ticket is gearchiveerd.` |

### Technische notities

- Klanttelefoon in het scherm komt uit `persons.telephone`/`persons.mobile`; als beide leeg:
  waarschuwing "Deze klant heeft geen telefoonnummer."
- Datum/tijd van bellen = `ticket_log.date = NOW()` server-side (conventies §8); vrijwilliger
  = huidige gebruiker in `volunteer_id`; vrije tekst in `log` volgens de Logconventie
  bovenaan dit bestand.
- De logtekst wordt opgebouwd als `<prefix> <Soort gesprek lowercase>; <resultaat-lowercase:
  afspraak: … | voicemail ingesproken | geen reactie | opgehaald>[; beschikbaar <datum>
  <van>-<tot>]`, waarbij de prefix `[BELLEN]` is, of `[BELLEN WACHTEN]` als het vinkje aanstaat.
- Max drie belmomenten uit Access wordt bewust **niet** overgenomen: historie is oneindig
  (logboek UC-TIC-06 toont ze chronologisch).
- Statuswijziging gebeurt nooit in dit scherm: het scherm stuurt alleen een voorstel aan
  UC-TIC-04 (knop "Status wijzigen naar Klant geïnformeerd").
- Het vinkje "wacht op klant" is een **logpreflight**: het beïnvloedt uitsluitend de prefix
  van de te schrijven logregel; er komt geen extra `ticket_status`-regel en
  `equipment.status` verandert niet.

---

## UC-TIC-06 — Details (JSON) bewerken en logboek

**Actor:** Werkplaats, Balie · **Rollen:** ROLE_WERKPLAATS, ROLE_BALIE, ROLE_ADMIN · **Doel:**
de ticketdetails (`tickets.details`, JSON) en het logboek (`ticket_log`) beheren. Vervangt
oud "Logboek per ticket" (Samenvatting reparatie, Reactie gebeld) plus de
details-keyvalue-tabel en de log-tabel/dialogen van het webapp-prototype. Twee secties van
het crudscherm: tabblad **Details** en tabblad **Logboek**.

### Scherm — Details (JSON) bewerken

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Ticket 4521 — Details (JSON)                                              │
├──────────────────────────────────────────────────────────────────────────┤
│ Naam                        │ Waarde                                     │
│ legacy_reparatienummer      │ 4521                    [ bewerk ] [ 🗑 ]  │
│ backup                      │ GEEN ▾                 [ bewerk ] [ 🗑 ]  │
│ tafel                       │ 3                       [ bewerk ] [ 🗑 ]  │
│ bijgeleverde_accessoires    │ voedingsadapter         [ bewerk ] [ 🗑 ]  │
│ account_wachtwoord          │ ingevuld (waarde verborgen) [ bewerk ] [ 🗑 ]│
│ proof_of_issue_id           │ 123                     [ bewerk ] [ 🗑 ]  │
│ _overig                     │ {} (leeg)               [ bewerk ] [ 🗑 ]  │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Nieuwe detail toevoegen ]                                              │
└──────────────────────────────────────────────────────────────────────────┘
```

### Scherm — Logboek

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Logboek — ticket 4521                                     [ vernieuwen ]  │
├──────────────────────────────────────────────────────────────────────────┤
│ Datum/tijd      │ Code             │ Door (link)   │ Inhoud (HTML)       │
│ 01-04 09:12     │ [STATUS]         │ Klaas         │ → OPEN              │
│ 01-04 13:42     │ [SAMENVATTING]   │ Klaas         │ OS opnieuw geïnst…  │
│ 02-04 10:05     │ [BELLEN]         │ Truus         │ Afspraak vrijdag    │
├──────────────────────────────────────────────────────────────────────────┤
│ Nieuwe regel: Code [ OPMERKING ▾ ]                                       │
│   ( OPMERKING | SAMENVATTING | BELLEN | BELLEN WACHTEN )                 │
│ Tekst [                                                             ]     │
│ [ Toevoegen ]                                                            │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Ticketdetails (JSON)

  Scenario: Bestaande details
    Dan toont de keyvalue-tabel alle sleutels uit tickets.details met hun waarden
      (beginset uit conventies §4.4: legacy_reparatienummer, backup,
      bijgeleverde_accessoires, tafel, account_wachtwoord, email_gegevens,
      klantnr_verkoop, derde_betaalt, proof_of_issue_id, _overig)

  Scenario: Waarde wijzigen
    Wanneer ik een rij bewerk, de waarde aanpas en opslaan klik
    Dan is details[sleutel] gewijzigd bij het ticket opgeslagen
    En verschijnt "Detail backup bijgewerkt."

  Scenario: Backup-enum als sleutel
    Wanneer ik sleutel backup bewerk
    Dan biedt het veld de enum-waarden GEEN | GEHELE | DEEL | ONBEKEND
      (conventies §4.4)

  Scenario: Detail verwijderen
    Wanneer ik de prullenbak-knop van een rij klik en bevestig
    Dan verdwijnt die sleutel uit de JSON
    En verschijnt "Sleutel tafel verwijderd."
    En blijven alle overige sleutels behouden

  Scenario: Detail toevoegen
    Wanneer ik "Nieuwe detail toevoegen" klik, een sleutelnaam invoer en bevestig
    Dan wordt de sleutel toegevoegd met lege waarde
    En weigert het scherm een naam die al bestaat: "Deze naam bestaat al."

  Scenario: Accountwachtwoord afschermen
    Gegeven account_wachtwoord een plaintext-veld van de legacy is
    Dan toont de tabel bij die sleutel uitsluitend "ingevuld ja/nee"
      en nooit de waarde (conventies §4.4)
    En schrijft een wijziging de nieuwe waarde wel weg maar toont die niet terug

  Scenario: Onbekende sleutels uit imports
    Gegeven details een onbekende sleutel "oude_kolom" bevat
    Dan toont de tabel die sleutel gewoon (behouden, niet weggooien)
    En is verplaatsen naar _overig mogelijk via bewerken

Feature: Logboek

  Scenario: Regels tonen
    Gegeven ticket met drie logregels
    Wanneer ik het logboek open
    Dan toont de tabel de regels oplopend op datum, met code, vrijwilliger
      (link naar persoon) en de HTML-inhoud (escape=false), 10 per pagina

  Scenario: Vrije opmerking
    Wanneer ik code OPMERKING, tekst "reserveonderdelen besteld" invoer en toevoeg
    Dan bestaat een ticket_log-regel "[OPMERKING] reserveonderdelen besteld"
      met date nu en de huidige gebruiker

  Scenario: Samenvatting
    Wanneer ik code SAMENVATTING kies en de samenvatting invoer
    Dan bevat de logregel "[SAMENVATTING] …"

  Scenario: Log met opmaak
    Wanneer ik in de editor vetgedrukte tekst invoer
    Dan bewaart de regel de HTML en toont de Inhoud-kolom die HTML (escape=false)

  Scenario: Alle logconventie-codes beschikbaar
    Dan bevat de code-selectie de vier handmatige codes uit de Logconventie
      (OPMERKING, SAMENVATTING, BELLEN, BELLEN WACHTEN)
    En is STATUS niet handmatig kiesbaar (uitsluitend automatisch, UC-TIC-04)

  Scenario: Log verwijderen
    Dan is verwijderen niet mogelijk (append-only)

  Scenario: Ticket gesloten
    Gegeven status CLOSED
    Dan is de toevoegregel en "Nieuwe detail toevoegen" geblokkeerd:
      "Dit ticket is afgesloten; toevoegen is niet meer mogelijk."
    En blijven details en logboek leesbaar
```

### Validaties

| Situatie | Melding |
|---|---|
| Sleutelnaam bestaat al | `Deze naam bestaat al.` |
| Sleutelnaam leeg | `Vul een sleutelnaam in.` |
| Logtekst leeg | `Vul de logtekst in.` |
| Code niet gekozen | `Kies een code.` |
| Poging tot wijzigen/verwijderen van een logregel | Niet mogelijk in de UI; service weigert `UPDATE`/`DELETE` op `ticket_log` |
| Ticket CLOSED | `Dit ticket is afgesloten; toevoegen is niet meer mogelijk.` |
| Ticket gearchiveerd | `Dit ticket is gearchiveerd.` |

### Technische notities

- Details: lees en schrijf `tickets.details` (JSON-kolom) via de registry uit
  00-conventies.md §4.4. De UI werkt key-value; server-side wordt de JSON
  merged (sleutel voor sleutel), zodat onbekende sleutels en `_overig` behouden blijven.
  Enum-sleutel `backup`: `GEEN`, `GEHELE`, `DEEL`, `ONBEKEND` (conventies §4.4).
  `derde_betaalt` is een object `{ "donor_id": …, "email_bewijs": "…" }`; bij bewerken toont
  het scherm donor-zoekveld (p:autoComplete, UC-DON-01) en e-mail.
  `klantnr_verkoop`, `proof_of_issue_id` en `legacy_reparatienummer` zijn numeriek.
- Logboek-regels: `ticket_log` is append-only; logregels zijn nooit te wijzigen of te
  verwijderen — implementeer geen UPDATE/DELETE-mogelijkheid (ook niet voor ADMIN).
  De composite PK is (`ticket_id`, `date`) — pas dezelfde collision-strategie toe als
  UC-TIC-04 (seconde ophogen, opnieuw proberen).
- Log-inhoud is HTML (`p:textEditor`); de kolom toont die HTML met `escape=false`. De
  prefix-codes uit de Logconventie zijn in de HTML-inhoud optioneel, maar dit scherm schrijft
  ze altijd server-side voor de handmatige codes; in tekstuele exports zijn ze verplicht.
- Gebruik de view `ticket_log_overview` in plaats van een eigen join (levert `ticket_id`,
  `date`, `volunteer_id`, `log`, `first_name`, `infix`, `last_name`).
- Na opslaan: altijd `update` op de logs-tabel én de details-tabel plus een
  growl-bevestiging; autorisatie server-side herhalen (ROLE_RAPPORTAGE krijgt geen
  bewerkcomponenten, conventies §1).
- De sleutelnamen zijn bindend; bij het wegschrijven van een enum-waarde of numerieke waarde
  serializeert de service naar JSON (`Jackson` of MariaDB `JSON_SET`) — nooit de hele
  JSON-kolom blind overschrijven vanuit de browser.

---

## UC-TIC-07 — Ticket archiveren (vervalt)

**Actor:** Balie · **Rollen:** ROLE_BALIE, ROLE_ADMIN (heropenen) · **Doel:** soft delete van
een ticket dat niet doorgaat (bv. klant haalt apparaat op zonder reparatie). Archiveren =
`tickets.unregistered = NOW()`; fysieke wis bestaat niet (conventies §2).

### Scherm — Ticket archiveren

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Ticket 4521 — archiveren                                                  │
├──────────────────────────────────────────────────────────────────────────┤
│ Reden (optioneel) [________________________________ ]                     │
│ [ Archiveren ]                                     [ Annuleren ]          │
├──────────────────────────────────────────────────────────────────────────┤
│ Heropening (alleen ADMIN, alleen gearchiveerde tickets):                  │
│ [ Heropenen ]                                                             │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Ticket archiveren

  Scenario: Archiveren
    Wanneer ik op "Archiveren" klik en in de confirmDialog bevestig
    Dan wordt tickets.unregistered nu
    En verdwijnt het ticket uit statusbord en werkplaatsoverzicht
      (UC-TIC-01, UC-TIC-02; conventies §2)
    En blijven ticket_status en ticket_log behouden en leesbaar in het dossier

  Scenario: Reden vastleggen
    Wanneer ik een reden invul
    Dan bestaat een [OPMERKING]-logregel "Ticket gearchiveerd: <reden>"
      met date nu en de huidige gebruiker

  Scenario: Alleen niet-CLOSED
    Gegeven status CLOSED
    Dan is Archiveren niet beschikbaar ("Afgesloten tickets blijven bewaard.")

  Scenario: Waarschuwing bij kindrecords
    Gegeven het ticket hoort bij apparaat 890 met een bewijs
    Dan verschijnt vooraf "Archiveren verbergt het record in overzichten;
      bestaande tickets en bewijzen blijven zichtbaar." (conventies §5)

  Scenario: Heropenen door admin
    Gegeven ik ben ingelogd als ROLE_ADMIN en het ticket is gearchiveerd
    Wanneer ik op "Heropenen" klik en bevestig
    Dan wordt tickets.unregistered NULL
    En geldt verder dezelfde regel als UC-PER-05 (heropening door ADMIN)

  Scenario: Heropenen door balie
    Gegeven ik ben ingelogd als ROLE_BALIE zonder ADMIN
    Dan is "Heropenen" niet beschikbaar

  Scenario: Heropening van CLOSED is verboden
    Gegeven status CLOSED (niet gearchiveerd)
    Dan is er geen heropeningsoptie; heropening is verboden, ook niet voor ADMIN
      (Ticketstatusstroom hierboven)
```

### Validaties

| Situatie | Melding |
|---|---|
| Status CLOSED | `Afgesloten tickets blijven bewaard.` (knop uitgeschakeld) |
| Heropenen zonder ADMIN | `U hebt geen rechten voor deze handeling.` (conventies §1) |
| Ticket al gearchiveerd | `Dit ticket is al gearchiveerd.` |

### Technische notities

- Update `tickets.unregistered = NOW()` (server-side, conventies §8); nooit `DELETE`.
  `ticket_status` en `ticket_log` worden niet gewist; overzichten en het dossier filteren op
  het ticket (conventies §2).
- De optionele reden wordt als `[OPMERKING]`-logregel vastgelegd; datum/tijd uit
  `ticket_log.date`, vrijwilliger uit `volunteer_id` — niet in de tekst herhalen.
- Archiveren blokkeert alle andere acties van UC-TIC-03 t/m UC-TIC-06 (formulier,
  status, bellen, details en logtoevoeging worden alleen-lezen); het dossier blijft
  inzichtelijk.
- De confirmDialog is verplicht (destructieve actie, conventies §8);
  `p:confirmDialog` met bevestiging "Ticket 4521 archiveren?".
