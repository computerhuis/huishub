# 12 — Apparatuur: geschiedenis

Onderdeel: apparatuur. Tabellen: `equipment`, `tickets`, `ticket_status`, `persons`
(behandelaar), `proof_of_issues` (bewijzen-tabblad). View: `equipment_history` (één rij per
ticket, de nieuwste statusregel). Schermen: het apparaatdossier (`equipmentHistoryBean`,
het dossier met historie); het bijbehorende overzicht (het apparatuuroverzicht en het
voorraadoverzicht) staat in `list.md` en het beheerformulier (het apparaatformulier) in
`crud.md`.

**Webapp-variant:** het dossier uit het prototype — voor dit onderdeel hertekend tot het
apparaatdossier: historietabel met ticketknop, bewijzen en koppelingen naar wijzigen en
status.

---

## UC-EQP-08 — Apparaatdossier met historie

**Actor:** Balie, Werkplaats · **Rollen:** ROLE_BALIE, ROLE_WERKPLAATS, ROLE_ADMIN;
ROLE_RAPPORTAGE alleen lezen · **Doel:** dossier van één apparaat inclusief ticket- en
statushistorie (view `equipment_history`), bewijzen en snelle doorstap naar wijzigen
(UC-EQP-04), status (UC-EQP-05) en een nieuw ticket.

### Scherm — Apparaatdossier

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Apparaat nr 890 — HP Compaq dc7800    [ Wijzigen ] [ Status ] [ Nieuw ticket ]│
├───────────────────────────┬──────────────────────────────────────────────┤
│ Kenmerken                 │ Historie (equipment_history)                 │
│ Categorie    DESKTOP      │ # (ticket) │ Geopend  │ Type   │ Status │    │
│ Status       Geschikt…    │ 4521       │ 01-04-2026│ REPAIR│ CLOSED │    │
│ Eigenaar     —            │ 4530       │ 07-04-2026│ ISSUE │ CLOSED │    │
│ Schenker     Gemeente …   │ Onderwerp (laatste rij): klaar voor uitgifte │
│ Serienummer  —            ├──────────────────────────────────────────────┤
│ Fabrikant    HP           │ Bewijzen                                     │
│ Model        dc7800       │ 07-04-2026  Bewijs 123 (uitgifte, niet getek.)│
│ Prijs        € 80,00      │                                              │
│ Inschrijfdatum 01-04-2026 ├──────────────────────────────────────────────┤
│ Uitgeschreven datum  —    │ Specificatie (JSON, key-value weergave)      │
├───────────────────────────┴──────────────────────────────────────────────┤
│ Tabbladen: [ Historie ] [ Bewijzen ] [ Specificatie ]                     │
└──────────────────────────────────────────────────────────────────────────┘
```

Rij-acties: bij een historierij een ticket-pictogram/knop (# als knop, tekst als label) die
het ticket opent; bij een bewijs een pictogram naar UC-RAP-02 (afdrukken). "Wijzigen" opent
het apparaatformulier voor het betreffende record (UC-EQP-04); "Status" opent het dialoog
uit UC-EQP-05; "Nieuw ticket" opent UC-TIC-01 met dit apparaat voorgevuld.

Kolommen van de historietabel: `# (ticket)` · `Geopend` · `Type` · `Status` · `Behandelaar`
· `Omschrijving` (subject).

### Acceptatiecriteria

```gherkin
Feature: Apparaatdossier

  Scenario: Historie toont laatste status per ticket
    Gegeven ticket 4521 (registered 01-04-2026) heeft statusregels OPEN,
      READY en CLOSED (ticket_status)
    Wanneer ik het dossier open
    Dan toont de historie uitsluitend één rij voor ticket 4521:
      Geopend 01-04-2026, Type REPAIR, Status CLOSED, Behandelaar Truus
      (view levert MAX(date) per ticket; de datum van díé laatste status zit
       niet in de view — gebruik ticket_status/ticket_status_overview als die
       nodig is; zie technische notities)

  Scenario: Meer tickets
    Gegeven tickets 4521 (REPAIR, CLOSED) en 4530 (ISSUE, CLOSED) horen bij
      dit apparaat
    Dan toont de historie twee rijen, elk met de nieuwste status van dat ticket
    En toont de kolom Type het Nederlandse tickettype-label
      (reparatie/werkopdracht; labels uit het ticketdeel, UC-TIC-01 t/m 07)

  Scenario: Apparaat zonder tickets
    Gegeven apparaat 900 heeft geen tickets
    Dan toont de historie geen regels
    En biedt het scherm "Nieuw ticket" aan

  Scenario: Ticket openen
    Wanneer ik op het ticket-pictogram bij een rij klik
    Dan opent het ticketformulier voor het betreffende record

  Scenario: Nieuw ticket
    Wanneer ik op "Nieuw ticket" klik
    Dan opent UC-TIC-01 met apparaat 890 voorgevuld

  Scenario: Bewijzen
    Gegeven bewijs 123 heeft equipment_id 890 en issue_date 07-04-2026
    Dan toont het tabblad Bewijzen alle proof_of_issues met
      equipment_id = dit apparaat, met datum, id en soort-afleiding
      (uitgifte herkenbaar aan status GIFT_ISSUED met bewijs in de periode,
       zie UC-UIT-01; inname aan het gerelateerde REPAIR-ticket)
    En "niet getekend" als signed_image null is (UC-UIT-04)

  Scenario: Specificatie
    Dan toont het tabblad Specificatie alle key-value-paren uit
      equipment.specification (conventies §4.3), inclusief onbekende sleutels
      en _overig

  Scenario: Wijzigen en status
    Wanneer ik op "Wijzigen" klik
    Dan opent het apparaatformulier voor het betreffende record (UC-EQP-04)
    Wanneer ik op "Status" klik
    Dan opent het statusdialoog (UC-EQP-05) met de huidige status getoond

  Scenario: Gearchiveerd apparaat
    Gegeven apparaat 900 is gearchiveerd
    Dan opent het dossier wel (historie en bewijzen blijven zichtbaar,
      conventies §5)
    En zijn Wijzigen en Status vervangen door "Heropenen" (alleen ROLE_ADMIN,
      UC-EQP-06)

  Scenario: Niet-bestaand apparaat
    Wanneer ik het apparaatdossier voor apparaat 999999 (bestaat niet) open
    Dan verschijnt "Apparaat nr 999999 bestaat niet."

  Scenario: Alleen lezen
    Gegeven ingelogd als ROLE_RAPPORTAGE
    Dan werkt het dossier als leesweergave
    En zijn Wijzigen, Status en Nieuw ticket niet zichtbaar (conventies §1)
```

### Technische notities

- Gebruik view `equipment_history` exact zoals die is gedefinieerd in Liquibase
  (`v1/view/equipment_history.sql`), inclusief de joins op `persons` voor de naam van de
  behandelaar van de laatste statusregel (`first_name`, `infix`, `last_name` via
  `ticket_status.volunteer_id`); de view niet nabouwen of wijzigen — het schema is leidend.
  Kolommen van de view: `ticket_id`, `status`, `equipment_id`, `ticket_type`, `registered`,
  `subject`, `first_name`, `infix`, `last_name`.
- **Valkuil (expliciet):** de view levert per ticket ÉÉN rij (de nieuwste statusregel via
  `WHERE date = (SELECT MAX(date) FROM ticket_status WHERE ticket_id = ts.ticket_id)`), maar
  de enige datumkolom die terugkomt is `t.registered` — het openstelmoment van het
  **ticket**, niet de datum van de laatste statusregel zelf (`ticket_status.date`).
  Implementaties die de statusdatum nodig hebben (bv. doorlooptijd, "laatst gewijzigd")
  halen die uit `ticket_status` of de view `ticket_status_overview`; de kolom "Geopend" in
  dit scherm toont dus bewust `tickets.registered`.
- De view-filtert op `equipment_id`; parameter van dit scherm is `equipment.id`.
- `ticket_type`-afhandeling en statuslabels: het ticketdeel (UC-TIC-01 t/m 07); de enum
  `ticket_status.status` is OPEN, IN_PROGRESS, READY, CUSTOMER_INFORMED, CLOSED — toon
  Nederlandse labels, niet de enum-waarden (conventies §8).
- Bewijzen: `SELECT * FROM proof_of_issues WHERE equipment_id = ?` gesorteerd op
  `issue_date`; handtekening-status via `signed_image IS NULL` (UC-UIT-04).
- Specificatie-tabblad: toon `equipment.specification` als key-value, met de beginset uit
  conventies §4.3 in schema-volgorde en daarna alfabetisch; sleutels onder `_overig` tonen
  onder een aparte groep (behouden, niet weggooien, conventies §4).
- PrimeFaces: `p:dataTable` (historie, bewijzen) met `p:button`/`p:commandButton` per rij,
  `p:tabView` voor de drie tabbladen, `p:graphicImage` niet nodig in dit scherm (handtekening
  toont UC-RAP-02).
- Autorisatie per knop conform conventies §1; ROLE_RAPPORTAGE en niet-geautoriseerden
  (HTTP 403 "U hebt geen rechten voor dit scherm.") zoals algemeen bepaald.
