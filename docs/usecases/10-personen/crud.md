# 10 — Personen: beheer (crud)

Onderdeel: personenbeheer (aanmaken, weergeven, wijzigen, uitschrijven/archiveren, rollen en
wachtwoord). Tabellen: `persons` (incl. `msaccess`-JSON, `passhash`, `registered`/`unregistered`),
`person_roles` (composite PK `(person_id, authority)`), `postal_codes` (FK-doel
`persons.postal_code`, `ON UPDATE RESTRICT ON DELETE CASCADE`), koppeltabel `donor_persons`;
geen eigen view — leesacties query'en direct op `persons` en gebruiken voor de dossier-tabbladen
`ticket_overview` en `timesheet_overview`. Schermen (webapp-prototype): het persoonformulier
(`personCrudBean`) — één formulier in drie modi: **nieuw**, **weergeven** (leesmodus,
UC-PER-03) en **wijzigen** (invulmodus, UC-PER-04) — plus de dialoogvensters voor
archiveren (UC-PER-05), rollen
(UC-PER-06) en wachtwoordreset (UC-PER-07). Het zoek-/overzichtsscherm staat in
`10-personen/list.md` (UC-PER-01); rollen en autoriteit: conventies §1; soft delete:
conventies §2; JSON-keys: conventies §4.1; validatie en foutafhandeling: conventies §5.

**Webapp-variant:** het persoonformulier uit het prototype (veldlijst inclusief
"Identificatie nummer", "Inschrijf datum", "Uitgeschreven datum" als uitschrijfmechanisme,
disabled rollenvinkjes, knoppen Annuleren/Opslaan) hier samengevoegd met de domeinregels uit
de eerdere personenuse-cases: msaccess-velden, dubbelcontrole, postcode-FK, vrijwilligerschap,
archief/heropening, rollen en wachtwoordreset.

---

## UC-PER-02 — Nieuwe klant registreren

**Actor:** Balie · **Rollen:** ROLE_BALIE, ROLE_ADMIN · **Doel:** een nieuwe klant opnemen in
`persons`, zoals in de handleiding ("Gebruiker inschrijven en computer invoer openen") —
inclusief de extra velden van het webapp-formulier.

### Scherm — Klant registreren (nieuw record)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Klant registreren                                                        │
├──────────────────────────────────────────────────────────────────────────┤
│ Identificatie nummer [ nieuw ] (id, disabled)                            │
│ Inschrijf datum [ — ] (registered, disabled; na opslaan NOW())           │
├──────────────────────────────────────────────────────────────────────────┤
│ Persoonlijke gegevens                                                    │
│ Voorletters [____ ]  Roepnaam/Voornaam [_______________ ] *              │
│ Tussenvoegsels [___________ ]  Achternaam [___________________ ] *       │
│ Geslacht [ kies ▾ ] ( M | V | X )  Geboortedatum [ __-__-____ ]          │
├──────────────────────────────────────────────────────────────────────────┤
│ Contact                                                                   │
│ E-mail [__________________________________ ]                             │
│ Telefoon [__________ ]  Mobiel [__________ ]  Derde telefoon [__________ ]│
├──────────────────────────────────────────────────────────────────────────┤
│ Adres                                                                     │
│ Postcode [______ ]  [ postcode ophalen ]  Woonplaats [_____________ ]    │
│ Straat [__________________________________ ]                              │
│ Huisnummer [_____ ] *  Huisnummer toevoeging [____ ]                     │
├──────────────────────────────────────────────────────────────────────────┤
│ Overig                                                                    │
│ Opmerkingen [ lang tekst ] (comments)                                    │
│ [✓] Vrienden Computerhuis Oost (msaccess.vrienden_computerhuis_oost)     │
│ [ ] Vrijwilliger (disabled, leeg — geen rollen)  [ ] Admin (disabled)    │
│ Uitgeschreven datum [ __-__-____ ] (disabled bij aanmaak; zie UC-PER-04) │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Opslaan ]  [ Opslaan en computerinvoer openen ]  [ Annuleren ]          │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Nieuwe klant registreren

  Background:
    Gegeven ik ben ingelogd als ROLE_BALIE
    En ik ben op het scherm "Klant registreren"

  Scenario: Geldige registratie
    Wanneer ik vul: Voorletters "J.", Voornaam "Johanna", Tussenvoegsels "de",
      Achternaam "Vries", Geslacht "V", Geboortedatum 14-03-1958,
      E-mail "jvries@example.nl", Telefoon "073 123 45 67", Mobiel "06 1234 5678",
      Postcode "5211 AB", Woonplaats "Den Bosch", Straat "Molenstraat", Huisnummer 12
    En ik klik Opslaan
    Dan wordt een persoon aangemaakt met registered = nu en unregistered = null
    En is Identificatie nummer daarna gevuld met het nieuwe id
    En is postal_code opgeslagen als "5211AB" en city als "Den Bosch"
      (opgehaald uit postal_codes)
    En is telefoon opgeslagen als "0731234567" en mobile als "0612345678" (CHAR(10))
    En verschijnt de melding "Persoon opgeslagen."
    En opent het scherm "Persoon weergeven" (UC-PER-03) met dit id

  Scenario: Opslaan en direct computerinvoer openen
    Wanneer ik Opslaan en computerinvoer openen klik
    Dan wordt de persoon opgeslagen zoals hierboven
    En opent het apparaatformulier in de modus Nieuw (apparaat registreren)
      met owner_id op deze persoon voorgevuld
    (knoplogica uit de Access-knop "Gebruiker inschrijven en computer invoer openen")

  Scenario: Postcodeauto-invul
    Wanneer ik "5211AB" invoer en Tab verlaat het veld
    Dan vult Woonplaats automatisch in met de city uit postal_codes
    En blijft Woonplaats handmatig overschrijfbaar (de zichtbare waarde wordt bewaard)

  Scenario: Postcode bestaat niet
    Wanneer ik "9999ZZ" invoer en op Opslaan klik
    Dan verschijnt bij Postcode: "Postcode 9999ZZ komt niet voor in de postcodeverwijzing.
      Controleer de invoer of laat de beheerder de postcode toevoegen."
    En wordt niets opgeslagen (JSF-validatie wint; ook de FK vangt dit op, conventies §5)

  Scenario: Huisnummer buiten het bekende bereik
    Gegeven postal_codes 5211AB heeft house_number_min 1 en house_number_max 150
    Wanneer ik Huisnummer 999 invul en opsla
    Dan verschijnt de waarschuwing "Huisnummer 999 valt buiten het bereik 1-150 voor
      postcode 5211AB. Controleer de invoer."
    En wordt de persoon wél opgeslagen (waarschuwing, geen blokkade)

  Scenario: Verplichte velden ontbreken
    Wanneer ik Opslaan klik zonder Voornaam en zonder Achternaam
    Dan verschijnen veldmeldingen "Vul een voornaam in." en "Vul een achternaam in."
    En wordt niets opgeslagen

  Scenario: Telefoon met opmaak
    Wanneer ik Telefoon "073 123 45 67" invoer en opsla
    Dan staat er "0731234567" in persons.telephone (normalisatie vóór opslaan, conventies §2)
    Wanneer ik Telefoon "073-1234" invul
    Dan verschijnt "Telefoonnummer moet 10 cijfers bevatten, bijvoorbeeld 0731234567."

  Scenario: Mogelijke dubbele registratie
    Gegeven er bestaat al "Johanna de Vries" geboren 14-03-1958
    Wanneer ik een nieuwe persoon invoer met dezelfde naam en geboortedatum
    Dan verschijnt de waarschuwing "Mogelijk bestaat deze persoon al: Johanna de Vries
      (gebruikersnr onbekend). Controleer voor u doorgaat."
    En kan ik alsnog Opslaan klikken om door te gaan (conventies §2: waarschuwing, geen
      blokkade)

  Scenario: Derde telefoon, geslacht en Vrienden CHO landen in msaccess
    Wanneer ik Derde telefoon "0739876543", Geslacht "X" en Vrienden Computerhuis Oost
      aanvink en opsla
    Dan bevat persons.msaccess de sleutels "telefoon_3": "0739876543",
      "geslacht": "X" en "vrienden_computerhuis_oost": true (conventies §4.1)

  Scenario: Disabled velden bij een nieuw record
    Dan zijn Identificatie nummer, Inschrijf datum, Uitgeschreven datum, Vrijwilliger en
      Admin niet invulbaar
    En zijn id en registered leeg tot na het opslaan
    En heeft de nieuwe persoon geen rollen en passhash null (dus nog niet inlogbaar,
      conventies §1)

  Scenario: Annuleren
    Wanneer ik op Annuleren klik
    Dan worden alle ingevulde waarden teruggezet (type=reset) en niets opgeslagen

  Scenario: Geen rechten
    Gegeven ik ben ingelogd als ROLE_RAPPORTAGE
    Wanneer ik het formulier via de URL open
    Dan verschijnt "U hebt geen rechten voor dit scherm." (HTTP 403, conventies §1)
```

### Validaties

| Veld | Regel | Melding bij overtreding |
|---|---|---|
| Voornaam | verplicht, max 255 | `Vul een voornaam in.` |
| Achternaam | verplicht, max 255 | `Vul een achternaam in.` |
| Voorletters / Tussenvoegsels | optioneel, max 255 | — |
| Postcode | formaat `1234AB` (vier cijfers, twee letters, uppercase), moet bestaan in `postal_codes.code` | `Postcode 9999ZZ komt niet voor in de postcodeverwijzing. Controleer de invoer of laat de beheerder de postcode toevoegen.` |
| Huisnummer | positief getal (`INT`) | `Vul een geldig huisnummer in.` |
| Huisnummer toevoeging | max 4 tekens (`A`, `B`, `12`, `bis`), leeg toegestaan | `Vul maximaal 4 tekens in bij de huisnummertoevoeging.` |
| Woonplaats | suggestie uit `postal_codes.city`, overschrijfbaar | — |
| Telefoon / Mobiel / Derde telefoon | na normalisatie exact 10 cijfers of leeg | `Telefoonnummer moet 10 cijfers bevatten, bijvoorbeeld 0731234567.` |
| E-mail | RFC-ish, of leeg | `Vul een geldig e-mailadres in.` |
| Geboortedatum | niet in de toekomst, niet vóór 1900 | `Vul een geldige geboortedatum in.` |
| Geslacht | enum `M`, `V`, `X` (optioneel) | `Kies een geldig geslacht.` |
| Opmerkingen | geen expliciete limiet (`TEXT`) | — |

### Technische notities

- Insert in `persons`: `initials, first_name, infix, last_name, date_of_birth, email, mobile,
  telephone, postal_code, street, house_number, house_number_addition, city, comments,
  registered = NOW(), unregistered = NULL, msaccess, passhash = NULL`. `registered` altijd
  server-side (conventies §8); `id` auto-increment via `persons_pk`.
- `msaccess` wordt bij aanmaak opgebouwd volgens conventies §4.1 met minimaal
  `legacy_gebruikersnummer: null`, `geslacht`, `vrienden_computerhuis_oost`, `telefoon_3`,
  `afgemeld_nieuwsbrief: false` en `_overig: {}`; onbekende importsleutels blijven onder
  `_overig` (conventies §4). Derde telefoon en geslacht hebben geen eigen kolom en gaan
  uitsluitend naar `msaccess`.
- Postcode: de UI haalt `city` op bij Tab in `postal_code` (`SELECT city FROM postal_codes
  WHERE code = :pc`); bij opslaan wordt de zichtbare waarde van Woonplaats in `city`
  bewaard (default `postal_codes.city`). De FK `persons_fk_1` blokkeert onbekende postcodes;
  vang de `DataIntegrityViolationException` en meld alsnog de tekst uit de validatietabel
  (conventies §5).
- `postal_codes.house_number_min`/`house_number_max` leveren alleen een **waarschuwing**
  (soft check in de service-laag), omdat bereiken indicatief zijn; `pobox = true` op een
  postcode is eveneens een waarschuwing: "Dit is een postbuspostcode; vul geen huisnummer in."
- Fuzzy-dubbelcontrole (conventies §2): vergelijk `lower(first_name)`, `lower(infix)`,
  `lower(last_name)` en `date_of_birth` (bij gelijke postcode: extra signaal); toon de
  waarschuwing met "Doorgaan"-knop, blokkeer niet.
- "Opslaan en computerinvoer openen" navigeert naar het apparaatformulier met `owner_id`
  voorgevuld en slaat daarbij eerst de persoon op (één transactie voor de persoon; het
  apparaat is een aparte transactie).
- Pas `p:watermark`/hints niet toe als enige label; labels zijn expliciet (conventies §8).
- Componenten: `p:inputText`, `p:datePicker` (dateOnly) voor Geboortedatum en
  Uitgeschreven datum, `p:selectOneMenu` voor Geslacht (M/V/X, Nederlandse labels
  Man/Vrouw/X), `p:selectBooleanCheckbox` voor Vrienden CHO en de disabled
  Vrijwilliger/Admin-vinkjes, `p:commandButton` + `update`, `p:messages` bovenaan het
  formulier met `p:message` per veld.

---

## UC-PER-03 — Persoon weergeven

**Actor:** Balie, Werkplaats, Rapportage (alleen lezen) · **Rollen:** ROLE_BALIE,
ROLE_WERKPLAATS, ROLE_RAPPORTAGE, ROLE_ADMIN · **Doel:** alle gegevens van één persoon in
beeld: dossier met tabs, samengevatte activiteit, rollen, schenkerkoppelingen en
archiefstatus. Dit is de "Gebruikersgegevens"-weergave van Access, nu op één plek.

### Scherm — Persoon weergeven (persoonsdossier in leesmodus)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Persoon: Johanna de Vries (nr 42)                                        │
│ [ Wijzigen ] [ Archiveren | Heropenen ]  Laatst gewijzigd: 03-05-2026 …  │
├───────────────────────────┬──────────────────────────────────────────────┤
│ Identificatie nummer  42  │ Laatste activiteit                           │
│ Inschrijf datum  01-02-2024│ Laatste reparatie  : 12-05-2026 OPEN        │
│ Voorletters  J.           │ Laatste bezoek     : 03-05-2026 Klik & Tik   │
│ Voornaam     Johanna      │ Laatste uitgifte   : geen                   │
│ Tussenvoegsels de         ├──────────────────────────────────────────────┤
│ Achternaam   Vries        │ Rollen (alleen ADMIN wijzigbaar, UC-PER-06)  │
│ Geboortedatum 14-03-1958  │ [✓] ROLE_BALIE  [ toevoegen | verwijderen ] │
│ Geslacht     V            ├──────────────────────────────────────────────┤
│ E-mail       jvries@…     │ Schenkerkoppelingen (donor_persons)          │
│ Derde telef. 0739876543   │ Bedrijf "Ros-Kabelkrant"   [ verwijderen ]  │
│ Telefoon     0731234567   │ [ koppeling toevoegen ] (UC-DON-04)          │
│ Mobiel       0612345678   ├──────────────────────────────────────────────┤
│ Straat       Molenstraat  │ Tabbladen:                                   │
│ Huisnummer   12           │ [ Apparaten ] [ Tickets ]                    │
│ Huisnr. toev. —           │ [ Bewijzen ] [ Aanwezigheid ]                │
│ Postcode     5211AB       │                                              │
│ Woonplaats   Den Bosch    │                                              │
│ Opmerkingen  —            │                                              │
│ Vrienden CHO [✓]          │                                              │
│ Vrijwilliger [✓] (lees)   │                                              │
│ Admin        [ ] (lees)   │                                              │
│ Uitgeschreven datum —     │                                              │
│ Gearchiveerd  nee         │                                              │
└───────────────────────────┴──────────────────────────────────────────────┘
```

Tabbladen (veld-/knoppenlijst):

```
│ Apparatuur │ Nr (link) │ Categorie │ Fabrikant │ Model │ Serienummer │ Status │
│ Tickets    │ Nr (link) │ Apparaat (link) │ Type │ Inname │ Status │ Onderwerp │
│ Bewijzen   │ Nr (link) │ Datum │ Apparaat (link) │ Ontvanger │ Status ondertekening │
│ Aanwezigheid │ Activiteit │ Datum/tijd (registered) │ Status │
```

### Acceptatiecriteria

```gherkin
Feature: Persoon weergeven

  Background:
    Gegeven persoon "Johanna de Vries" (nr 42) heeft rollen ["ROLE_BALIE"],
      één schenkerkoppeling, twee apparaten, drie tickets, één bewijs en
      vijf aanwezigheidsregels

  Scenario: Alle secties gevuld
    Wanneer ik het scherm open
    Dan tonen alle velden de waarden uit persons (+ msaccess voor geslacht,
      derde telefoon, Vrienden CHO en vrijwilligersdata)
    En tonen de vier tabbladen de gekoppelde records via hun respectieve queries
    En toont "Laatste activiteit" de meest recente datum per categorie
    En tonen lege velden "—"

  Scenario: Leesmodus
    Gegeven het scherm is geopend voor het betreffende record zonder wijzigmaanduiding
    Dan zijn alle velden disabled en staan de knoppen Wijzigen/Archiveren bovenaan
    En verdwijnt de knop Opslaan

  Scenario: Gearchiveerde persoon
    Gegeven de persoon is gearchiveerd (unregistered 30-06-2024)
    Wanneer ik het scherm open
    Dan staat bij Uitgeschreven datum 30-06-2024 en bij Gearchiveerd "ja (30-06-2024)"
    En is de knop Archiveren vervangen door "Heropenen" (alleen ROLE_ADMIN)
    En verschijnt voor BALIE/WERKPLAATS "Deze persoon is gearchiveerd. Heropen het
      record eerst (beheerder)."

  Scenario: Wijzigen
    Wanneer ik op Wijzigen klik
    Dan opent hetzelfde formulier in invulmodus met alle velden voorgevuld (UC-PER-04)

  Scenario: Openen vanuit de balie
    Wanneer ik in UC-BAL-02 op de naam-link of "open in CRUD" klik
    Dan opent het persoonsdossier van dit record
    En blijft de balie-wizardkeuze behouden (Terug keert terug naar de wizard)
    Wanneer ik in UC-BAL-04 bij een ticket van deze klant "Persoon weergeven" kies
    Dan opent hetzelfde dossier en toont tabblad Tickets actief

  Scenario: Tabblad Apparaten
    Dan toont het tabblad alle apparaten waarvan equipment.owner_id deze persoon is
      (zonder gearchiveerde records, conventies §2)
    En "Geen apparatuur bekend" bij leeg
    Wanneer ik op een apparaatnummer klik
    Dan opent het apparaatformulier van dat apparaat

  Scenario: Tabblad Tickets
    Dan toont het tabblad alle tickets van de apparaten van de persoon via ticket_overview
      (nieuwste status, zelfde opbouw als UC-BAL-04)
    Wanneer ik op een ticketnummer of apparaatnummer klik
    Dan openen respectievelijk het ticketformulier van dat ticket en het apparaatformulier
      van dat apparaat

  Scenario: Tabblad Bewijzen
    Dan toont het tabblad alle proof_of_issues-rijen waarvan recipient_id deze persoon is,
      met datum, apparaat en of signed_image gevuld is ("getekend" / "ongetekend")
    Wanneer ik op een bewijsnummer klik
    Dan opent het uitgiftbewijsdocument van dat bewijs

  Scenario: Tabblad Aanwezigheid
    Dan toont het tabblad alle timesheets-rijen van deze persoon met activity_name
      uit timesheet_overview
    Wanneer ik op "Bezoek toevoegen" klikt (alleen BALIE/ADMIN)
    Dan opent het tijdregistratieformulier voor dit record (tijdsregistratie, UC-TID-01 t/m 05)

  Scenario: Rollen wijzigen
    Gegeven ingelogd als ROLE_ADMIN
    Wanneer ik bij een rol op "verwijderen" of "toevoegen" klik
    Dan opent het rolbeheer (UC-PER-06) zonder dit scherm te verlaten
    Gegeven ingelogd als ROLE_BALIE
    Dan staan toevoegen/verwijderen er niet (alleen ADMIN, conventies §1)

  Scenario: Schenkerkoppeling
    Wanneer ik op "koppeling toevoegen" klik
    Dan opent het koppelscherm persoon ↔ schenker (UC-DON-04)

  Scenario: Alleen-lezen rol
    Gegeven ik ben ingelogd als ROLE_RAPPORTAGE
    Dan toont het scherm alle gegevens en tabs (lezen)
    En ontbreken de knoppen Wijzigen, Archiveren en Bezoek toevoegen
```

### Validaties

| Situatie | Melding |
|---|---|
| Persoon gearchiveerd, geen ADMIN | `Deze persoon is gearchiveerd. Heropen het record eerst (beheerder).` |
| Geen rollen | `Geen rollen toegekend.` |
| Geen schenkerkoppelingen | `Geen koppelingen met schenkers.` |
| Tab leeg | `Geen apparaten bekend.` / `Geen tickets bekend.` / `Geen bewijzen bekend.` / `Geen bezoeken bekend.` |
| Onbekend record-id | `Persoon niet gevonden.` + terug naar UC-PER-01 |
| Geen autorisatie | `U hebt geen rechten voor dit scherm.` (HTTP 403) |

### Technische notities

- Bovenste regel: naamopbouw `initials` + `first_name` + `infix` + `last_name`
  (bijv. "J. de Vries" in de lijst, "Johanna de Vries" in de kop); `nr` = `persons.id`.
- "Laatste reparatie": laatste rij uit `ticket_overview` (view levert de nieuwste
  `ticket_status` per ticket) waar `equipment.owner_id = :person`, met `ts.status` en
  `t.registered`. "Laatste bezoek": laatste `timesheets`-rij (`registered` DESC). "Laatste
  uitgifte": laatste `proof_of_issues` waar `recipient_id = :person` (anders "geen").
- Tab Apparaten: `SELECT id, category, manufacturer, model, serial_number, status FROM
  equipment WHERE owner_id = :person AND unregistered IS NULL ORDER BY id`; enum-labels
  Nederlands (conventies §8). Tab Tickets: zoals UC-BAL-04 (`ticket_overview` via
  `equipment.owner_id`). Tab Bewijzen: `SELECT id, issue_date, equipment_id, signed_image
  FROM proof_of_issues WHERE recipient_id = :person ORDER BY issue_date DESC`. Tab
  Aanwezigheid: `SELECT * FROM timesheet_overview WHERE person_id = :person AND
  unregistered IS NULL ORDER BY registered DESC`.
- Rollen: `person_roles WHERE person_id = :person` (toevoegen/verwijderen alleen ROLE_ADMIN,
  UC-PER-06). Schenkerkoppelingen: `donor_persons WHERE person_id = :person` (beheer:
  UC-DON-04; PK is `(donor_id, person_id)`, dupliceren is dus impossible).
- `msaccess`-weergave: `geslacht`, `telefoon_3` (toon als "Derde telefoon"),
  `vrienden_computerhuis_oost`, `vrijwilligerswerk_start`/`_eind`,
  `afgemeld_nieuwsbrief`, `persoonlijke_email` (toon als tweede e-maillink).
  Sleutels die ontbreken tonen "—" (conventies §4).
- `passhash` wordt **nooit** in de view gezet, ook niet als ADMIN; "Wachtwoord resetten" is
  een actie, geen weergave (UC-PER-07).
- Vinkjes "Vrijwilliger" en "Admin" zijn disabled en afgeleid (conform het prototype):
  Vrijwilliger = er bestaat ≥1 rij in `person_roles`; Admin = `ROLE_ADMIN` aanwezig.
  Wijzigen gebeurt in UC-PER-06; het vrijwilligersoverzicht (start/einddatum) hoort bij
  UC-SET-03 (`17-instellingen/vrijwilligers.md`).
- "Laatst gewijzigd" komt uit `msaccess._laatst_gewijzigd` (ISO-datumtijd, zie UC-PER-04);
  het schema heeft geen `updated`-kolom (conventies §5).

---

## UC-PER-04 — Persoon wijzigen

**Actor:** Balie (alleen velden), Werkplaats (uitsluitend opmerkingen), ROLE_ADMIN (alles,
inclusief uitschrijving en vrijwilligersdata) · **Rollen:** ROLE_BALIE, ROLE_WERKPLAATS,
ROLE_ADMIN · **Doel:** gegevens actueel houden en uitschrijven — in de handleiding heette dit
"Gebruikers gegevens weergeven en telefoonnummer controleren".

### Scherm — Persoon wijzigen (persoonformulier in invulmodus)

Identiek aan "Klant registreren" (UC-PER-02), met gevulde en disabled
Identificatie nummer + Inschrijf datum, en daaronder:

```
├──────────────────────────────────────────────────────────────────────────┤
│ Status                                                                    │
│ Uitgeschreven datum [ __-__-____ ]  (unregistered; leeg = actief)         │
├──────────────────────────────────────────────────────────────────────────┤
│ Vrijwilligerschap (alleen ROLE_ADMIN, of zichtbaar bij rol WERKPLAATS)    │
│ Startdatum vrijwilligerswerk [ __-__-____ ]                               │
│ Einddatum vrijwilligerswerk  [ __-__-____ ]                               │
├──────────────────────────────────────────────────────────────────────────┤
│ Beheer (alleen ROLE_ADMIN)                                                │
│ [ Rollen ]  [ Wachtwoord resetten ]                                       │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Opslaan ]  [ Annuleren ]   Laatst gewijzigd: 03-05-2026 14:22           │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Persoon wijzigen

  Background:
    Gegeven de persoon heeft telefoon "0731234567", postcode 5211AB en
      registered 01-02-2024

  Scenario: Telefoon actualiseren
    Gegeven ik ben ingelogd als ROLE_BALIE
    Wanneer ik telefoon wijzig in "0737654321" en opsla
    Dan is de nieuwe waarde "0737654321"
    En is registered ongewijzigd (conventies §2: registered niet wijzigbaar)
    En verschijnt "Persoon opgeslagen."

  Scenario: Naam wijzigen
    Wanneer ik Achternaam aanpas en Opslaan klik
    Dan is de naam gewijzigd en verschijnt de bevestiging (growl)
    En werkt het zoekscherm (UC-PER-01) de nieuwe naam meteen fonetisch mee

  Scenario: Wijziging op verouderde versie
    Gegeven het scherm is geopend om 14:00 (laatst gewijzigd 03-05-2026 14:00)
    En een collega wijzigde het record om 14:05
    Wanneer ik om 14:10 opsla
    Dan verschijnt "Dit record is intussen gewijzigd door een ander
      (03-05-2026 14:05). Vernieuw de gegevens en probeer opnieuw."
    En wordt niets opgeslagen

  Scenario: Postcode wijzigt naar onbekende postcode
    Wanneer ik postcode wijzig naar "1111AA" (bestaat niet in postal_codes)
    Dan blokkeert de opslag met de postcode-melding uit UC-PER-02
    En blijft de oude waarde staan

  Scenario: Uitschrijven via datum
    Gegeven ingelogd als ROLE_BALIE
    Wanneer ik Uitgeschreven datum 30-06-2026 invul en opsla
    Dan is persons.unregistered gevuld met 30-06-2026 (00:00, conventies §2)
    En verdwijnt de persoon uit standaardkeuzes en zoekopdrachten (UC-PER-01)
    En toont het dossier "Gearchiveerd: ja (30-06-2026)"
    En blijven bestaande tickets, bewijzen en timesheets zichtbaar (conventies §5)

  Scenario: Uitschrevingsdatum wissen (heropenen)
    Gegeven ingelogd als ROLE_ADMIN en de persoon is gearchiveerd
    Wanneer ik Uitgeschreven datum leeg maak en opsla
    Dan wordt persons.unregistered null en is de persoon weer actief
    Gegeven ingelogd als ROLE_BALIE
    Dan is het veld niet leeg te maken: "Alleen een beheerder kan een persoon heropenen."

  Scenario: Uitschrevingsdatum in de toekomst
    Wanneer ik Uitgeschreven datum morgen invul
    Dan verschijnt "Uitgeschreven datum mag niet in de toekomst liggen."
    En wordt niets opgeslagen

  Scenario: Vrijwilligersdata
    Gegeven ingelogd als ROLE_ADMIN
    Wanneer ik Startdatum 01-09-2020 en Einddatum 31-12-2025 invul en opsla
    Dan bevat msaccess "vrijwilligerswerk_start": "2020-09-01"
      en "vrijwilligerswerk_eind": "2025-12-31" (conventies §4.1)
    En bestaan die velden niet als kolom in persons (schema ongewijzigd)

  Scenario: Werkplaats beperkt wijzigen
    Gegeven ingelogd als ROLE_WERKPLAATS
    Dan zijn alle velden disabled behalve Opmerkingen
    Wanneer ik een ander veld toch wijzig en opsla
    Dan verschijnt "U kunt alleen opmerkingen bijwerken." en wordt niets opgeslagen
    En slaat Opslaan met een gewijzigde opmerking wél op

  Scenario: Rollenvinkjes blijven disabled
    Gegeven Vrijwilliger en Admin zijn disabled
    Dan zijn die alleen leesbaar (afgeleid uit person_roles)
    En wijzig ik rollen via [ Rollen ] (UC-PER-06, alleen ADMIN)

  Scenario: Derde telefoon en geslacht bijwerken zonder dataverlies
    Wanneer ik Derde telefoon en Geslacht wijzig en opsla
    Dan bevat persons.msaccess nog steeds alle bestaande sleutels
      (vrienden_computerhuis_oost, vrijwilligerswerk_start, _overig …)
    En is alleen het gewijzige sleutel-veld vervangen (merge, geen hele JSON vervangen)

  Scenario: Annuleren
    Wanneer ik Annuleren klik
    Dan worden wijzigingen niet verwerkt (type=reset) en blijft de lijst ongewijzigd

  Scenario: Beheeracties
    Gegeven ingelogd als ROLE_ADMIN
    Wanneer ik op Wachtwoord resetten klik
    Dan opent het dialoog van UC-PER-07
    Wanneer ik op Archiveren zou klikken
    Dan loopt dat via het bevestigingsdialoog van UC-PER-05
```

### Validaties

| Veld | Regel | Melding bij overtreding |
|---|---|---|
| Alle velden van UC-PER-02 | zelfde regels als bij aanmaak | zie UC-PER-02 |
| Inschrijf datum / Identificatie nummer | disabled, nooit gewijzigd | — (veld disabled) |
| Uitgeschreven datum | datum, niet in de toekomst, niet vóór `registered`; leegmaken alleen ADMIN | `Uitgeschreven datum mag niet in de toekomst liggen.` / `Uitgeschreven datum mag niet vóór de inschrijfdatum liggen.` / `Alleen een beheerder kan een persoon heropenen.` |
| Start-/Einddatum vrijwilligerswerk | eind ≥ start, ISO in `msaccess` | `De einddatum mag niet vóór de startdatum liggen.` |
| Opmerkingen (ROLE_WERKPLAATS) | enige veld dat mag veranderen | `U kunt alleen opmerkingen bijwerken.` |
| Verouderde versie | `msaccess._laatst_gewijzigd` wijkt af van de geladen waarde | `Dit record is intussen gewijzigd door een ander (<tijdstip>). Vernieuw de gegevens en probeer opnieuw.` |

### Technische notities

- Wijzigbare velden = alle velden uit UC-PER-02 + `Uitgeschreven datum` (`unregistered`) +
  vrijwilligersdata in `msaccess` + (ADMIN) `passhash` via UC-PER-07 en rollen via UC-PER-06.
  `registered` en `id` zijn nooit wijzigbaar (conventies §2).
- Transactie: één `UPDATE persons SET … WHERE id = :id` waarbij `msaccess` als
  `JSON_SET`/herbouwde JSON wordt meegegeschreven (merge op sleutelniveau, onbekende
  sleutels en `_overig` behouden, conventies §4). Bij falen rolt alles terug; niets deels
  opslaan.
- Uitschrijven = `unregistered = :datum` (`DATETIME`, om middernacht); heropenen =
  `unregistered = NULL`. Beide zijn hetzelfde mechanisme als UC-PER-05 — het
  "Uitgeschreven datum"-veld in dit formulier is de webapp-route ernaartoe; dezelfde
  autorisatieregels gelden (uitschrijven BALIE/ADMIN, heropenen alleen ADMIN).
- Optimistic locking: het schema heeft geen versiekolom (conventies §5). Implementatie: bij
  laden `msaccess._laatst_gewijzigd` (ISO-datumtijd) lezen en meesturen; bij opslaan eerst
  `SELECT msaccess._laatst_gewijzigd FROM persons WHERE id = :id` vergelijken, dan pas
  updaten en de nieuwe waarde als `NOW()` zetten. De sleutel is niet-gebruikersdata en valt
  onder de `_overig`-discipline: nooit tonen in de UI, nooit wissen bij merge.
- `postal_code`-wijziging is een gewone UPDATE op `persons`; onthoud dat `postal_codes` én
  `persons` cascades hebben (`persons_fk_1` = `ON DELETE CASCADE` op `code`): het verwijderen
  van een postcode in UC-SET-01 verwijdert daarmee personen — beheerfunctie, nooit dit
  scherm; waarschuw daar conform de FK-regels (conventies §5).
- `person_roles`-effect: gewijzigde rollen gelden bij de volgende request (Spring Security
  leest per request); de eigen sessie houdt zijn authorities tot de volgende request.
- Componenten: zelfde als UC-PER-02, aangevuld met `p:datePicker` voor Uitgeschreven datum,
  `p:confirmDialog` voor het uitschrijven ("Persoon uitschrijven op <datum>?"), en
  `p:commandButton` Annuleren met `type="reset"`.

---

## UC-PER-05 — Persoon archiveren en heropenen

**Actor:** Balie (archiveren), ROLE_ADMIN (heropenen) · **Rollen:** ROLE_BALIE, ROLE_ADMIN ·
**Doel:** soft delete conform `unregistered` — geen enkel persoonsrecord wordt fysiek gewist.

### Scherm — bevestigingsdialoog (`p:confirmDialog`)

```
┌──────────────────────────────────────────────────────────────────┐
│ Persoon archiveren?                                              │
│ "Johanna de Vries" krijgt de status gearchiveerd en verdwijnt    │
│ uit standaard zoekopdrachten.                                    │
│ Archiveren verbergt het record in overzichten; bestaande tickets  │
│ en bewijzen blijven zichtbaar.                                   │
│ Datum [ __-__-____ ] (default: vandaag)                          │
│ [ Archiveren ]                            [ Annuleren ]          │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│ Persoon heropenen? (alleen ROLE_ADMIN)                            │
│ "Johanna de Vries" komt weer terug in de standaard zoekopdrachten.│
│ [ Heropenen ]                            [ Annuleren ]            │
└──────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Persoon archiveren en heropenen

  Scenario: Archiveren
    Gegeven ingelogd als ROLE_BALIE
    En de persoon heeft twee tickets, één bewijs en vijf timesheets-rijen
    Wanneer ik "Archiveren" bevestig
    Dan wordt unregistered van de persoon op nu (of de gekozen datum) gezet
    En verdwijnt de persoon uit alle standaardzoekopdrachten (UC-PER-01)
    En blijven bestaande tickets, bewijzen en timesheets zichtbaar
    En blijven de records van kindtabbladen bereikbaar via het dossier met
      "gearchiveerd"-indicator

  Scenario: Waarschuwing bij kindrecords
    Wanneer het bevestigingsdialoog opent
    Dan toont het de melding "Archiveren verbergt het record in overzichten;
      bestaande tickets en bewijzen blijven zichtbaar." (conventies §5)

  Scenario: Heropenen door admin
    Gegeven ingelogd als ROLE_ADMIN en de persoon is gearchiveerd
    Wanneer ik "Heropenen" bevestig
    Dan wordt unregistered weer null
    En verschijnt "Persoon heropend."
    En staat de persoon weer in de standaardzoekresultaten

  Scenario: Balie mag niet heropenen
    Gegeven ingelogd als ROLE_BALIE en de persoon is gearchiveerd
    Dan is de knop Heropenen niet zichtbaar
    En meldt een directe URL-aanroep "Alleen een beheerder kan een persoon heropenen."

  Scenario: Archiveren vanuit het formulier
    Gegeven ik ben op UC-PER-04 met Uitgeschreven datum leeg
    Wanneer ik de datum vul en opsla
    Dan is dat exact hetzelfde mechanisme als Archiveren in dit dialoog
      (unregistered gevuld; conventies §2)

  Scenario: Geen DELETE
    Gegeven een implementatie die zou willen wissen
    Dan bestaat op dit scherm geen enkele "Verwijderen"-actie
    En voert de applicatie nooit DELETE op persons uit

  Scenario: Rapportage
    Gegeven ingelogd als ROLE_RAPPORTAGE
    Dan is het dialoog niet bereikbaar (alleen lezen)
```

### Validaties

| Situatie | Melding |
|---|---|
| Datum in de toekomst | `Archiefdatum mag niet in de toekomst liggen.` |
| Archiveren zonder rechten | `U hebt geen rechten voor dit scherm.` (HTTP 403) |
| Heropenen zonder ROLE_ADMIN | `Alleen een beheerder kan een persoon heropenen.` |

### Technische notities

- Archiveren: `UPDATE persons SET unregistered = :datum WHERE id = :id`; heropenen:
  `UPDATE persons SET unregistered = NULL WHERE id = :id`. Nooit `DELETE` op `persons`
  (cascades op `equipment`, `ticket_status`, `ticket_log`, `timesheets`, `proof_of_issues`,
  `donor_persons`, `person_roles` zouden meerdere domeinen raken).
- Archiveren cascadert **niet** naar kindrecords; die houden hun eigen `unregistered`
  (conventies §2). `person_roles`-rijen blijven staan — logins blokkeren al doordat
  "actief ingelogd" `unregistered IS NULL` vereist (conventies §1).
- Uitschrijfdatum uit UC-PER-04 en Archiveren uit dit dialoog schrijven dezelfde kolom;
  valideer beide paden in dezelfde service-methodode (`archiveer(id, datum)`,
  `heropen(id)`), zodat regels en meldingen niet uiteenlopen.
- Beheer van `postal_codes`-verwijdering (cascadestop) staat in UC-SET-01; hier alleen
  bewustzijn: een cascade zou personen fysiek uit `persons` verwijderen.

---

## UC-PER-06 — Rollen beheren

**Actor:** ROLE_ADMIN · **Rollen:** ROLE_ADMIN · **Doel:** `person_roles` vullen — de enige
plek waar autoriteit ontstaat (conventies §1).

### Scherm — Rollen (dialoog vanuit UC-PER-03/04)

```
┌──────────────────────────────────────────────────────────────────┐
│ Rollen — Johanna de Vries (nr 42)                                │
├──────────────────────────────────────────────────────────────────┤
│ Huidige rollen:                                                   │
│ [✓] ROLE_BALIE       Balie              [ verwijderen ]         │
│ [ ] ROLE_WERKPLAATS  Werkplaats (niet toegekend)                 │
│ Toevoegen: [ kies rol ▾ ] ( ROLE_ADMIN | ROLE_BALIE |            │
│            ROLE_WERKPLAATS | ROLE_RAPPORTAGE )                   │
│ [ toevoegen ]                                                    │
├──────────────────────────────────────────────────────────────────┤
│ Let op: gewijzigde rollen gelden bij de volgende request.         │
│ [ Sluiten ]                                                      │
└──────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Rollen beheren

  Background:
    Gegeven persoon "Johanna de Vries" (nr 42) heeft de rol ROLE_BALIE
    En ik ben ingelogd als ROLE_ADMIN

  Scenario: Rol toevoegen
    Wanneer ik "ROLE_WERKPLAATS" kies en toevoeg
    Dan bestaat er een rij in person_roles voor deze persoon met
      authority "ROLE_WERKPLAATS"
    En verschijnt "Rol toegevoegd."

  Scenario: Dubbele rol
    Wanneer ik "ROLE_BALIE" toevoeg terwijl die al bestaat
    Dan verschijnt "Deze rol is al toegekend."
    En wordt geen tweede rij aangemaakt (PK (person_id, authority))

  Scenario: Rol verwijderen
    Wanneer ik bij ROLE_WERKPLAATS op verwijderen klik
    Dan verdwijnt de rij uit person_roles
    En verdwijnt de bijbehorende autoriteit bij de volgende request uit mijn sessie
      als het mijn eigen rol is

  Scenario: Laatste rol van jezelf verwijderen
    Gegeven ik ben ingelogd als ADMIN en verwijder mijn eigen laatste rol
    Dan blokkeert het scherm: "U kunt uw eigen laatste rol niet verwijderen."

  Scenario: Laatste ADMIN van een andere persoon
    Wanneer ik de enige ROLE_ADMIN van een andere persoon verwijder
    Dan verschijnt de waarschuwing "Dit is de laatste beheerder. Weet u zeker dat u
      deze rol verwijderd?" en bevestig ik met Ja

  Scenario: Alleen ADMIN
    Gegeven ik ben ingelogd als ROLE_BALIE
    Wanneer ik het dialoog via de URL open
    Dan verschijnt "U hebt geen rechten voor dit scherm." (HTTP 403, conventies §1)
    En staan in UC-PER-03 de knoppen toevoegen/verwijderen niet

  Scenario: Geen enkele rol
    Gegeven een persoon zonder rijen in person_roles
    Dan toont het dialoog "Geen rollen toegekend."
    En is die persoon niet inlogbaar op een scherm (conventies §1, HTTP 403)
```

### Validaties

| Situatie | Melding |
|---|---|
| Rol al toegekend | `Deze rol is al toegekend.` |
| Eigen laatste rol verwijderen | `U kunt uw eigen laatste rol niet verwijderen.` |
| Laatste beheerder weghalen | `Dit is de laatste beheerder. Weet u zeker dat u deze rol verwijderd?` |
| Onbekende authority | `Kies een geldige rol.` |
| Geen rechten | `U hebt geen rechten voor dit scherm.` (HTTP 403) |

### Technische notities

- Authority-waarden (UI-enum, `VARCHAR(225)` in schema, volledige lijst conform
  conventies §8): `ROLE_ADMIN` → label "Beheerder", `ROLE_BALIE` → "Balie",
  `ROLE_WERKPLAATS` → "Werkplaats", `ROLE_RAPPORTAGE` → "Rapportage". Nooit een waarde
  buiten deze vier opslaan.
- PK is `(person_id, authority)` met FK `person_roles_fk_1` op `persons(id)`
  (`ON DELETE CASCADE` — fysieke persoonsverwijdering zou rollen meenemen; daarom
  uitsluitend soft delete, UC-PER-05). Insert via `INSERT INTO person_roles
  (person_id, authority) VALUES (:p, :a)`; vang `DuplicateKeyException` en meld
  "Deze rol is al toegekend."
- Er bestaat géén rol "vrijwilliger": "Vrijwilliger" in het formulier is een afgeleid
  vinkje (≥1 rij in `person_roles`) en het vrijwilligersoverzicht / de status actief-gestopt
  staat in UC-SET-03 (`17-instellingen/vrijwilligers.md`).
- Einde sessie-impact: gewijzigde rollen gelden bij volgende request (Spring Security leest
  per request); toon daarom de tekst "gewijzigde rollen gelden bij de volgende request".
- Autorisatie: alleen ROLE_ADMIN bereikt dit dialoog; UI-verbergen is geen beveiliging —
  de service controleert ook (conventies §1/§5).

---

## UC-PER-07 — Wachtwoord resetten (alleen ADMIN)

**Actor:** ROLE_ADMIN · **Rollen:** ROLE_ADMIN · **Doel:** `persons.passhash` vervangen voor
een bekende persoon — beheeractie náást het inloggen (de loginflow zelf hoort bij
UC-AUT-01/02; hier is geen sprake van een e-mailverificatiestroom).

### Scherm — Wachtwoord resetten (dialoog vanuit UC-PER-04)

```
┌──────────────────────────────────────────────────────────────────┐
│ Wachtwoord resetten — Johanna de Vries (nr 42)                    │
├──────────────────────────────────────────────────────────────────┤
│ Nieuw wachtwoord [__________________ ] *                          │
│ Bevestig wachtwoord [__________________ ] *                       │
│ [✓] Persoon moet bij volgende inlog opnieuw inloggen              │
├──────────────────────────────────────────────────────────────────┤
│ [ Opslaan ]                              [ Annuleren ]            │
└──────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Wachtwoord resetten

  Scenario: Geldige reset
    Gegeven ingelogd als ROLE_ADMIN
    En persoon "Johanna de Vries" heeft e-mail jvries@example.nl en een passhash
    Wanneer ik een nieuw wachtwoord en bevestiging invul en op Opslaan klik
    Dan wordt persons.passhash vervangen door de hash van het nieuwe wachtwoord
    En verschijnt "Wachtwoord gereset."
    En kan de persoon inloggen met dat wachtwoord (UC-AUT-01)
    En wordt het ingevulde wachtwoord nergens in de UI getoond (alleen "ingevuld ja")

  Scenario: Wachtwoorden komen niet overeen
    Wanneer ik twee verschillende waarden invoer
    Dan verschijnt "De wachtwoorden komen niet overeen." en wordt niets opgeslagen

  Scenario: Te kort
    Wanneer ik "geheim" (7 tekens) invoer
    Dan verschijnt "Het wachtwoord moet minimaal 8 tekens bevatten."

  Scenario: Leeg formulier
    Wanneer ik Opslaan klik zonder invoer
    Dan verschijnen veldmeldingen "Vul een nieuw wachtwoord in." en
      "Bevestig het nieuwe wachtwoord."

  Scenario: Leeg laten en toch bevestigen
    Gegeven ik wil alleen een bevestiging wissen
    Wanneer ik Annuleren klik
    Dan wordt passhash niet gewijzigd (type=reset)

  Scenario: Resetten van iemand die niet inlogt
    Gegeven de persoon heeft passhash null (nog nooit ingelogd)
    Wanneer ik een wachtwoord instel
    Dan is passhash gevuld en unregistered null, dus "actief ingelogd" mogelijk
      (conventies §1)

  Scenario: Gearchiveerde persoon
    Gegeven de persoon is gearchiveerd (unregistered gevuld)
    Wanneer ik de reset bevestig
    Dan verschijnt "Deze persoon is gearchiveerd. Heropen het record eerst."
    En wordt passhash niet gewijzigd

  Scenario: Alleen ADMIN
    Gegeven ingelogd als ROLE_BALIE
    Wanneer ik het dialoog via de URL open
    Dan verschijnt "U hebt geen rechten voor dit scherm." (HTTP 403)
    En is de knop "Wachtwoord resetten" in UC-PER-04 niet zichtbaar

  Scenario: Bestaande sessie van de persoon
    Gegeven de persoon is op dit moment ingelogd
    Wanneer ik het wachtwoord reset
    Dan blijft die sessie actief tot uitloggen of verloop
    En kan de persoon niet opnieuw inloggen zonder het nieuwe wachtwoord
```

### Validaties

| Veld | Regel | Melding bij overtreding |
|---|---|---|
| Nieuw wachtwoord | verplicht, minimaal 8 tekens | `Vul een nieuw wachtwoord in.` / `Het wachtwoord moet minimaal 8 tekens bevatten.` |
| Bevestiging | verplicht, gelijk aan nieuw wachtwoord | `Bevestig het nieuwe wachtwoord.` / `De wachtwoorden komen niet overeen.` |
| Doelpersoon | bestaand, niet gearchiveerd | `Deze persoon is gearchiveerd. Heropen het record eerst.` |
| Rechten | alleen ROLE_ADMIN | `U hebt geen rechten voor dit scherm.` (HTTP 403) |

### Technische notities

- Opslaan: `UPDATE persons SET passhash = :hash, msaccess = JSON_SET(msaccess,
  '$._laatst_gewijzigd', NOW()) WHERE id = :id` — dezelfde "laatst gewijzigd"-discipline
  als UC-PER-04 (conventies §5).
- Hashing in de service-laag (nooit in de bean, nooit in de view): BCrypt via Spring
  Security (`BCryptPasswordEncoder`) of SHA-256-formaat; `passhash` is `VARCHAR(4096)`.
  Plaintext wordt nooit opgeslagen, gelogd of getoond (vergelijk de regel voor
  `tickets.details.account_wachtwoord` in conventies §4.4).
- Geen e-mail- of tokenflow: de ADMIN kent de persoon en typt het wachtwoord; een
  zelfaanvraag door de gebruiker valt onder UC-AUT-01 (login) en bestaat niet in dit
  domein.
- Reset blokkeert niet op `unregistered IS NULL` in de database (er is geen constraint);
  de service controleert dat vóór de update en meldt de tekst uit de validatietabel.
- Dialoog is `p:dialog`/`p:confirmDialog` met `p:password` (met `redisplay="false"`,
  zodat waarden na submit niet terugkomen in de HTML), `p:commandButton` + `update` op
  `p:messages`, en een `autocomplete="new-password"`-hint tegen browser-opslag.
- Logging: log alleen "Wachtwoord gereset voor person_id <id> door <acteur>", nooit de
  waarde of de hash.
