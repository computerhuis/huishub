# 13 — Tickets: overzichten

Onderdeel: tickets-domein (reparaties en werkopdrachten). Tabellen: `tickets`,
`ticket_status`, `ticket_log`, gekoppeld `equipment` en `persons`. Views: `ticket_overview`
(laatste status per ticket), `ticket_status_overview` en `ticket_log_overview` (beide met
naam-vrijwilliger). Schermen: het werkplaatsoverzicht (`workshopBean`) en
het ticketoverzicht (`ticketListBean`); beide zijn uitsluitend lees-schermen —
mutaties lopen via het ticketformulier (UC-TIC-03 t/m 07, zie `13-tickets/crud.md`).

Elke reparatie-intake én elke werkopdracht voor verkoop/uitgifte is één `tickets`-rij met
`ticket_type` REPAIR resp. ISSUE. De intake zelf staat in `03-intake-werkplaats.md`
(UC-INT-01 t/m 04); de ISSUE-werkopdracht staat in `04-uitgave.md` (UC-UIT-05).

**Webapp-variant:** het prototype leverde twee overzichtschermen: het werkplaatsoverzicht
(tickets in behandeling, met logs-dialog) en het ticketoverzicht (ruwe ticketlijst);
deze zijn hier samengevoegd tot UC-TIC-01 (statusbord) en UC-TIC-02 (werkplaatsoverzicht).

---

## Statuslabels en kleuren (leidend voor UC-TIC-01 en UC-TIC-02)

De status komt altijd uit de nieuwste `ticket_status`-regel (view `ticket_overview` levert
precies die regel). De labels en kleuren zijn dezelfde als in `13-tickets/crud.md`
(Ticketstatusstroom); overzichten tonen nooit enum-waarden, altijd de Nederlandse labels.

| Enum-waarde | Nederlands label | Kleur (indicatief) | Tag-severity (PrimeFaces) |
|---|---|---|---|
| `OPEN` | Binnengekomen | rood | `danger` |
| `IN_PROGRESS` | In behandeling | groen | `success` |
| `READY` | Gereed | oranje | `warning` |
| `CUSTOMER_INFORMED` | Klant geïnformeerd | zwart | `contrast` |
| `CLOSED` | Afgerond | grijs | `secondary` |

---

## UC-TIC-01 — Ticketoverzicht (statusbord)

**Actor:** Balie, Werkplaats, Rapportage (alleen lezen) · **Rollen:** ROLE_BALIE,
ROLE_WERKPLAATS, ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:** het Access "Status Reparaties"-scherm:
alle openstaande tickets met status, kleur en groepering per status — het centrale volgscherm
van de balie. Vervangt oud "Statusbord" en de ruwe ticketlijst van het webapp-prototype.

### Scherm — Ticketoverzicht (statusbord)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Ticketoverzicht                                        [ Vernieuwen ]      │
├──────────────────────────────────────────────────────────────────────────┤
│ Type [ alle ▾ ]  Status [ alle ▾ ]  Medewerker [ alle ▾ ]                 │
│ [✓] Toon alleen openstaande (niet Afgerond)  [✓] Toon gearchiveerd       │
├──────────────────────────────────────────────────────────────────────────┤
│ ▸ Binnengekomen (1)                                                       │
│ │ Ticket │ Type   │ Apparaat           │ Klant        │ Medewerker │ Datum│
│ │ 4521   │ REPAIR │ HP dc7800          │ J. de Vries  │ Klaas      │ 01-04│
│ ├────────────────────────────────────────────────────────────────────────┤
│ ▸ Gereed (1)                                                              │
│ │ 4530   │ ISSUE  │ Dell Latitude      │ M. Jansen    │ Truus      │ 05-04│
├──────────────────────────────────────────────────────────────────────────┤
│ Rijacties: [ Openen ] [ Status ] [ Klant bellen ] [ Logboek ] [ Archiveren ]│
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Ticketoverzicht (statusbord)

  Scenario: Standaardweergave
    Wanneer ik het scherm open
    Dan toont de lijst alle niet-gearchiveerde tickets (unregistered IS NULL)
      waarvan de nieuwste status niet CLOSED is
    En staat de nieuwste ticket bovenaan (# aflopend op tickets.id)

  Scenario: Groepering op status
    Dan zijn de rijen gegroepeerd per status met een groepskop
      "<Nederlands label> (<aantal>)" in de volgorde van de statusstroom
      (Binnengekomen, In behandeling, Gereed, Klant geïnformeerd)
    En kleurt elke rij volgens de statuskleur uit de tabel bovenaan dit bestand

  Scenario: Typefilter
    Wanneer ik Type "REPAIR" kies
    Dan bevat de lijst uitsluitend reparaties (tickets.ticket_type)

  Scenario: Statusfilter
    Wanneer ik Status "Gereed" kies
    Dan bevat de lijst uitsluitend tickets waarvan de nieuwste statusregel READY is

  Scenario: Medewerkerfilter
    Wanneer ik Medewerker "Klaas" kies
    Dan bevat de lijst uitsluitend tickets waarvan de nieuwste ticket_status-regel
      volunteer_id = Klaas

  Scenario: Toon alleen openstaande
    Gegeven het vinkje staat standaard aan
    Dan bevat de lijst geen tickets met nieuwste status CLOSED
    Wanneer ik het vinkje uitzet
    Dan verschijnen ook de afgeronde tickets, gegroepeerd onder "Afgerond (grijs)"

  Scenario: Toon gearchiveerd
    Gegeven ticket 4599 is gearchiveerd (unregistered gevuld)
    Wanneer ik met "Toon gearchiveerd" zoek
    Dan bevat de lijst ticket 4599 wel, met indicator "gearchiveerd"
      (conventies §2)

  Scenario: Rij openen
    Wanneer ik een rij dubbelklik of op "Openen" klik
    Dan opent het ticketformulier (UC-TIC-03)

  Scenario: Rijacties
    Wanneer ik op Status klik opent UC-TIC-04, op Klant bellen opent UC-TIC-05,
      op Logboek opent UC-TIC-06 (tabblad Logboek) en op Archiveren opent UC-TIC-07
    En zijn Klant bellen, Status en Archiveren uitgeschakeld als de status CLOSED is

  Scenario: Vernieuwen
    Wanneer ik op Vernieuwen klik
    Dan wordt de query opnieuw uitgevoerd (geen client-side cache)

  Scenario: Export
    Wanneer ik op Export (CSV) klik
    Dan bevat de export de zichtbare kolommen inclusief statuslabel,
      gesorteerd zoals op het scherm
```

### Validaties

| Situatie | Melding |
|---|---|
| Geen resultaten onder de filters | `Geen tickets gevonden voor de huidige filters.` |
| Rapportage-gebruiker klikt een rijactie | `U hebt geen rechten voor deze handeling.` (conventies §1: ROLE_RAPPORTAGE is alleen-lezen) |
| Ticket zonder enige `ticket_status`-regel | Geen melding in de UI; log een warning (programmeerfout, zie technische notities) |

### Technische notities

- Lees de view `ticket_overview`; die levert exact: `id`, `status`, `equipment_id`,
  `ticket_type`, `registered`, `unregistered`, `subject`, `description`, `first_name`,
  `infix`, `last_name` (vrijwilliger), `category`, `manufacturer`, `model`. Geen eigen joins.
- De view levert per ticket één rij (nieuwste `ticket_status` via `MAX(date)`); tickets zonder
  statusregel komen er niet in voor — behandel dat als programmeerfout en log een warning.
- Groepering op status gebeurt client-side (of een `p:selectOneMenu`-filter plus
  `p:accordionPanel`-achtige groepskoppen); de paginering is client-side, maximaal 50 rijen
  per pagina (conventies §3, max-resultaatregel).
- Statuslabels/kleuren via `p:tag` met de severity-waarden uit de tabel bovenaan; enum-waarden
  nooit tonen, alleen Nederlandse labels (conventies §8).
- De knoppen [Status] en [Archiveren] zijn server-side autorisatiefilterend: ROLE_RAPPORTAGE
  krijgt ze niet (conventies §1); bij status CLOSED zijn ze client-side disabled.
- Koppeling naar de balie: UC-BAL-04 (stap 4 van het klantdossier) toont dezelfde
  `ticket_overview`-gegevens gefilterd op `equipment.owner_id = :person` en opent het
  ticketformulier van dat ticket (UC-TIC-03).
- Optellingen/wijzigingen: dit scherm schrijft nooit; alle mutaties lopen via
  het ticketformulier.

---

## UC-TIC-02 — Werkplaatsoverzicht

**Actor:** Werkplaats, Balie · **Rollen:** ROLE_WERKPLAATS, ROLE_BALIE, ROLE_ADMIN · **Doel:**
het werkplaatsoverzicht: de lijst tickets in behandeling met behandelaar
en logboek-inzage — het startpunt van elke intake (UC-INT-01 opent vanaf dit scherm) en van
de dagelijkse werkplaatsrondgang.

### Scherm — Werkplaatsoverzicht

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Werkplaats — tickets in behandeling                     [ + Nieuwe intake ]│
├──────────────────────────────────────────────────────────────────────────┤
│ Status [ alle ▾ ]  Behandelaar [ alle ▾ ]  [✓] Toon alleen openstaande    │
├──────────────────────────────────────────────────────────────────────────┤
│ # (link) │ Apparaat │ Inname    │ Type   │ Behandelaar │ Status │ Taak  │ Logs │
│ 4521     │ HP dc78… │ 01-04-2026│ REPAIR │ Klaas       │ OPEN   │ start…│ [bon] │
│ 4530     │ Dell Lat…│ 05-04-2026│ ISSUE  │ Truus       │ READY  │ klaar…│ [bon] │
├──────────────────────────────────────────────────────────────────────────┤
│ 15 rijen per pagina · # aflopend (nieuwste ticket bovenaan)               │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Werkplaatsoverzicht

  Scenario: Overzicht
    Gegeven de ticket-overzichtview levert alle tickets met hun nieuwste status
    Wanneer ik het scherm open
    Dan toont de tabel de kolommen # (link), Apparaat (link), Inname
      (= tickets.registered), Type, Behandelaar, Status (tag met severity),
      Taak (= tickets.subject) en Logs (pictogramknop)
    En draait # aflopend (nieuwste ticket bovenaan)
    En bevat de lijst standaard uitsluitend niet-gearchiveerde tickets met
      status ≠ CLOSED (conventies §2)

  Scenario: Paginering
    Dan toont de tabel 15 rijen per pagina

  Scenario: Logs bekijken
    Wanneer ik op het bon-pictogram in de kolom Logs klik
    Dan opent een dialog "Logs van ticket <id>" met per logregel
      "<dd-MM HH:mm> - <volledige naam vrijwilliger>" en de HTML-inhoud van
      ticket_log.log (escape=false)

  Scenario: Ticket openen
    Wanneer ik op het ticketnummer (#) of het apparaatnummer klik
    Dan openen respectievelijk het ticketformulier (UC-TIC-03) en
      het apparaatformulier (UC-EQP-04)

  Scenario: Filters
    Wanneer ik Status "In behandeling" kies
    Dan bevat de lijst uitsluitend tickets met nieuwste status IN_PROGRESS
    Wanneer ik Behandelaar "Klaas" kies
    Dan bevat de lijst uitsluitend tickets waarvan de nieuwste ticket_status-regel
      volunteer_id = Klaas

  Scenario: Nieuwe intake
    Wanneer ik op de +-knop ("Nieuwe intake") rechtsboven klik
    Dan opent de intakewizard voor een nieuwe reparatie (UC-INT-01)

  Scenario: Gesloten tickets
    Gegeven het vinkje "Toon alleen openstaande" staat uit
    Dan verschijnen ook CLOSED-tickets, zonder intake-knop
```

### Validaties

| Situatie | Melding |
|---|---|
| Geen resultaten | `Geen tickets gevonden voor de huidige filters.` |
| Logs-dialog bij ticket zonder logregels | `Dit ticket heeft nog geen logregels.` (leegstaat in de dialog) |

### Technische notities

- Gebruik bij voorkeur de views `ticket_overview` en `ticket_status_overview` voor
  status en behandelaar; geen eigen joins (kolommen `first_name`, `infix`, `last_name` zitten
  al in de view).
- Behandelaar = `volunteer_id` van de nieuwste `ticket_status`-regel; de kolom toont de
  samengestelde naam uit de view.
- De logs-dialog leest `ticket_log_overview` (levert `ticket_id`, `date`, `volunteer_id`,
  `log` plus naam-vrijwilliger), gefilterd op het ticket, oplopend op `date`.
- Kolom "Inname" toont `tickets.registered` in NL-formaat `dd-MM-yyyy` (conventies §2,
  datumafronding); de ticketlink opent het ticketformulier van dat ticket.
- Dit scherm is startpunt van de intake: UC-INT-04 keert na opslaan terug naar
  het werkplaatsoverzicht en selecteert het nieuwe ticket.
- Rollen: ROLE_WERKPLAATS en ROLE_BALIE mogen alles; ROLE_RAPPORTAGE ziet het scherm
  alleen-lezen (conventies §1); ROLE_ADMIN zonder balie/werkplaats-authoriteit krijgt
  HTTP 403 (conventies §1).
- Uitgifte van een gereed (READY) ticket loopt via UC-BAL-04 en UC-UIT-01; dit overzicht
  toont na die uitgifte de automatische overgang naar CLOSED (UC-TIC-04).
