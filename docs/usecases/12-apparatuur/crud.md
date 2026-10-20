# 12 — Apparatuur: beheer (crud)

Onderdeel: apparatuur (giften, klanteigen apparaten, voorraad). Tabellen: `equipment`,
`persons` (eigenaar), `donors` (schenker/belanghebbende); `tickets` (leen-only voor de
open-ticket-waarschuwingen). Schermen: het apparaatformulier (`equipmentCrudBean`,
registreren én wijzigen in één formulier); het bijbehorende overzicht (het
apparatuuroverzicht en het voorraadoverzicht) staat in `list.md` en het dossier (het
apparaatdossier) staat in `geschiedenis.md`. Kernwoorden uit de specificatie: categorie-enum,
statusstroom, uniek serienummer, `specification`-JSON (conventies §4.3).

**Webapp-variant:** het apparaatbeheerformulier uit het prototype — voor dit onderdeel
hertekend tot drie domein-gebaseerde use-cases (registreren, wijzigen, statuswijziging) plus
archiveren en serienummerbeheer; overzicht en voorraad staan in `list.md`, geschiedenis in
`geschiedenis.md`.

---

## Statusstroom apparaten (leidend voor UC-EQP-05)

| Enum-waarde | Nederlands label | Betekenis |
|---|---|---|
| `CUSTOMER_OWNED` | Klanteigen | Apparaat van een klant (reparatiedomein) |
| `INCOMING_GIFT` | Binnengekomen gift | Geschonken, nog niet beoordeeld |
| `SUITABLE_FOR_GIFT` | Geschikt voor uitgifte | Schoongemaakt, basissoftware, prijs bepaald |
| `RESERVED` | Gereserveerd | Gereserveerd voor project/klant |
| `GIFT_ISSUED` | Uitgegeven | Uitgegeven (bewijs opgemaakt) |
| `SOLD` | Verkocht | Verkocht (bewijs/factuur afgerond) |
| `DEMOLITION` | Sloop / onderdelen | Afgeschreven; bruikbare onderdelen |

Toegestane overgangen (alles wat hier niet staat is verboden):

```
(null) ──► INCOMING_GIFT | CUSTOMER_OWNED
CUSTOMER_OWNED ──► INCOMING_GIFT | DEMOLITION | SOLD
INCOMING_GIFT ──► SUITABLE_FOR_GIFT | DEMOLITION
SUITABLE_FOR_GIFT ──► RESERVED | GIFT_ISSUED | SOLD | DEMOLITION
RESERVED ──► SUITABLE_FOR_GIFT | GIFT_ISSUED | SOLD | DEMOLITION
GIFT_ISSUED ──► (einde)
SOLD ──► (einde)
DEMOLITION ──► (einde)
```

**Regel:** statuswijzigingen mogen **uitsluitend** via de toegestane overgangen hierboven
gaan — UC-EQP-05 is de enige route; registreren (UC-EQP-03) kiest alleen een niet-terminale
startstatus en volgt daarmee de overgang `(null) ──► …`. De transitiematrix leeft in de
service-laag (UC-EQP-05); de UI filtert de keuzelijsten maar mag de matrix nooit zelf
dupliceren. Het schema (`equipment.status`-ENUM) en de rest van de Liquibase-changelog zijn
leidend en worden niet gewijzigd.

---

## UC-EQP-03 — Apparaat registreren

**Actor:** Balie · **Rollen:** ROLE_BALIE, ROLE_ADMIN · **Doel:** een nieuw apparaat
vastleggen (handleiding: minimaal type kast, fabrikant en modelnummer) — inclusief de
apparaatstap van de intakewizard (UC-INT-02), die dezelfde veldmeldingen en validaties
gebruikt.

### Scherm — Apparaat registreren (nieuw record)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Apparaat registreren                                        [ Annuleren ] │
├──────────────────────────────────────────────────────────────────────────┤
│ Eigenaar/schenker                                                        │
│ Eigenaar (persoon) [ zoek klant ▾ (autoComplete) ]  [ nieuw ]             │
│ Geschonken door (schenker) [ zoek schenker ▾ (autoComplete) ]             │
│ [✓] Uitgifte namens schenker (behalf_of)                                  │
├──────────────────────────────────────────────────────────────────────────┤
│ Categorie en status                                                      │
│ Categorie* [ DESKTOP ▾ ]  ( DESKTOP | LAPTOP | TABLET | MOBILE | SIM |    │
│                              USB_STICK )                                  │
│ Startstatus* [ INCOMING_GIFT ▾ ]  ( CUSTOMER_OWNED | INCOMING_GIFT |      │
│                                     SUITABLE_FOR_GIFT | RESERVED )        │
├──────────────────────────────────────────────────────────────────────────┤
│ Technische gegevens                                                      │
│ Fabrikant* [________________ ]   Model* [____________________ ]           │
│ Serienummer [_______________________ ]                                   │
│ Type kast [__________ ]  Processor [____________ ]  Geheugen [________ ] │
│ HDD/SSD [____________ ]  Videokaart [__________ ]                        │
│ Optische apparaten [__________ ]  Losse geluidskaart [______ ]           │
│ Cardreader [______ ]  Overige ingebouwde apparaten [___________ ]        │
├──────────────────────────────────────────────────────────────────────────┤
│ Software en bijzonderheden                                               │
│ Software [                                                          ]     │
│ Bijzonderheden [                                                    ]    │
│ Licentie [____________ ]  Prijs [________ ]                              │
├──────────────────────────────────────────────────────────────────────────┤
│ Datum gift [ __-__-____ ]                                                  │
│ Opmerkingen [                                                       ]     │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Opslaan ]  [ Opslaan en reparatie/werkopdracht openen ]                 │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Apparaat registreren

  Scenario: Gift van bedrijf
    Gegeven ingelogd als ROLE_BALIE
    En schenker "Gemeente 's-Hertogenbosch" (nr 31) bestaat
    Wanneer ik registreer: Eigenaar leeg, Geschonken door "Gemeente…",
      Categorie DESKTOP, Startstatus INCOMING_GIFT, Fabrikant "HP",
      Model "Compaq dc7800", Datum gift 01-04-2026
    En ik klik Opslaan
    Dan wordt het apparaat opgeslagen met donor_id 31, owner_id null,
      registered nu, status INCOMING_GIFT
    En bevat specification.legacy_computernummer niets
      en "datum_gift": "2026-04-01"

  Scenario: Gift van klant met reparatieachtergrond
    Gegeven klant "Johanna de Vries" (nr 42) is gevonden via de klantzoeker
    Wanneer ik Categorie LAPTOP, Fabrikant "Dell", Model "Latitude" invoer,
      Startstatus CUSTOMER_OWNED kies en Opslaan en reparatie/werkopdracht
      openen klik
    Dan wordt het apparaat opgeslagen met owner_id 42
    En opent UC-TIC-01 (nieuw ticket type REPAIR) met dit apparaat voorgevuld

  Scenario: Uitgifte namens schenker
    Gegeven ik heb schenker "Stichting Leergeld" (nr 44) gekozen
    Wanneer ik "Uitgifte namens schenker" aanvink en opsla
    Dan is equipment.behalf_of_id 44
    Wanneer ik het vinkje uitzet
    Dan is behalf_of_id null

  Scenario: Verplichte velden ontbreken
    Wanneer ik Opslaan klik zonder Fabrikant en zonder Model
    Dan verschijnen "Vul een fabrikant in." en "Vul een model in."
    En wordt niets opgeslagen

  Scenario: Categorie en startstatus verplicht
    Wanneer ik Opslaan klik zonder Categorie of zonder Startstatus
    Dan verschijnen "Kies een categorie." respectievelijk "Kies een startstatus."

  Scenario: Terminale startstatus
    Wanneer ik in de Startstatus-lijst kijk
    Dan bevat die uitsluitend CUSTOMER_OWNED en INCOMING_GIFT
      (en desgewenst SUITABLE_FOR_GIFT/RESERVED voor geïmporteerde voorraad);
      terminal statuses GIFT_ISSUED, SOLD en DEMOLITION staan er niet in
    En wanneer een terminal status langs directe HTTP-aanroep wordt aangeboden
      Dan verschijnt "Een nieuw apparaat kan niet direct de status
        "Uitgegeven" hebben." en wordt niets opgeslagen

  Scenario: Leeg apparaat zonder eigenaar én schenker
    Wanneer ik Opslaan klik zonder eigenaar en zonder schenker
    Dan verschijnt "Vul een eigenaar of een schenker in."

  Scenario: Leeg serienummer
    Wanneer ik Serienummer leeg laat
    Dan slaat het apparaat op met serial_number null
    En kan ik een tweede apparaat zonder serienummer opslaan

  Scenario: Dubbele waarschuwing (geen blokkade)
    Gegeven er bestaat al een apparaat "HP Compaq dc7800" met dezelfde eigenaar
    Wanneer ik een identiek apparaat registreer zonder serienummer
    Dan verschijnt "Mogelijk bestaat dit apparaat al: HP Compaq dc7800
      (nr 890). Controleer voor u doorgaat."
    En kan ik alsnog opslaan (conventies §2: fuzzy-dubbele is waarschuwing,
      geen blokkade; uitzondering: serienummer, zie UC-EQP-07)

  Scenario: Eigenaar gearchiveerd
    Gegeven de gekozen klant is gearchiveerd
    Wanneer ik Opslaan klik
    Dan verschijnt "Deze klant is gearchiveerd. Heropen het record eerst
      (beheerder)."

  Scenario: Relatie intake (UC-INT-02)
    Gegeven de intakewizard staat in stap 2 "Apparaat"
    Wanneer ik daar "nieuw apparaat" kies en Type kast, Fabrikant en Model invul
    Dan gebruikt de wizard dezelfde validaties en meldingen als dit scherm
    En wordt bij opslaan (UC-INT-04) het apparaat opgeslagen met owner = de
      klant en status CUSTOMER_OWNED (statusstroom UC-EQP-05)
    En geldt: kiezen van een apparaat dat al een OPEN-reparatie heeft blokkeert
      met de melding uit UC-INT-04

  Scenario: Annuleren
    Wanneer ik op Annuleren klik
    Dan wordt niets opgeslagen en keer ik terug naar het overzicht (UC-EQP-01)
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Categorie* | verplicht; enum-waarde uit `equipment.category` (DESKTOP, LAPTOP, TABLET, MOBILE, SIM, USB_STICK) | `Kies een categorie.` |
| Startstatus* | verplicht; enum-waarde; mag geen terminal zijn (GIFT_ISSUED/SOLD/DEMOLITION) | `Een nieuw apparaat kan niet direct de status "<label>" hebben.` |
| Fabrikant*, Model* | verplicht bij registratie | `Vul een fabrikant in.` / `Vul een model in.` |
| Eigenaar of schenker | minstens één van `owner_id` / `donor_id` | `Vul een eigenaar of een schenker in.` |
| Eigenaar | bestaande `persons.id`, niet gearchiveerd | `Deze klant is gearchiveerd. Heropen het record eerst (beheerder).` |
| Schenker | bestaande `donors.id` | `Kies een bestaande schenker.` |
| Serienummer | uniek indien gevuld | `Dit serienummer is al vastgelegd bij apparaat nr <id>.` (UC-EQP-07) |
| Prijs | positief getal of leeg | `Prijs moet een positief bedrag zijn.` |
| Datum gift | niet in de toekomst | `Datum gift kan niet in de toekomst liggen.` |

### Technische notities

- Insert `equipment`: `category`, `status`, `owner_id`, `donor_id`, `behalf_of_id` (alleen
  als "Uitgifte namens" aangevinkt én schenker gekozen; anders null), `serial_number`,
  `manufacturer`, `model`, `specification` (JSON met alle technische velden uit conventies
  §4.3: `legacy_computernummer`, `type_kast`, `type_computer`, `processor`, `geheugen`,
  `hdd_ssd`, `videokaart`, `optische_apparaten`, `losse_geluidskaart`, `cardreader`,
  `overige_apparaten`, `software`, `bijzonderheden`, `licentie`, `prijs`, `verkocht_door`,
  `datum_gift`, `leergeld`), `registered = NOW()` server-side (conventies §8). Het
  wireframe-venster "Opmerkingen" schrijft naar `specification.bijzonderheden`.
- Categorie-mapping uit Access "Type kast": desktop/kast/tower → DESKTOP; laptop/notebook →
  LAPTOP; tablet → TABLET; smartphone → MOBILE; simkaart → SIM; usb-stick → USB_STICK
  (identiek voor de import, UC-IMP-01/02).
- Startstatus-keuzelijst: alle niet-terminale waarden minus `CUSTOMER_OWNED`-varianten die
  via intake vastliggen; in de praktijk CUSTOMER_OWNED (klant brengt reparatie) en
  INCOMING_GIFT (schenking).
- Componenten: `p:autoComplete` (personen en donors; completeMethod met zoekconventies §3),
  `p:selectOneMenu` (categorie en startstatus met NL-labels, volledige enum-lijst,
  conventies §8), `p:datePicker` (Datum gift), `p:inputTextarea` (Software, Bijzonderheden,
  Opmerkingen), `p:inputNumber`/`p:inputText` (Prijs), `p:commandButton` met
  `p:confirmDialog` bij Annuleren met ingevulde velden.
- Opslaan en reparatie/werkopdracht openen: na succesvolle insert navigeert naar
  UC-TIC-01 met `equipment_id` voorgevuld; beide handelingen in één transactie, bij falen
  rolt ook de equipment-insert terug.
- Fuzzy-dubbele controle (conventies §2): vergelijk category + manufacturer + model +
  eventuele eigenaar; waarschuwing via `p:growl`, doorgaan toegestaan.

---

## UC-EQP-04 — Apparaat wijzigen

**Actor:** Balie, Werkplaats · **Rollen:** ROLE_BALIE, ROLE_WERKPLAATS, ROLE_ADMIN ·
**Doel:** specificatie, koppelingen en details bijwerken (Access: "gegevens aanvullen en
status aanpassen" in Gekregen Computers). Statuswijziging gebeurt niet inline maar via
"wijzig status" → UC-EQP-05; archiveren via UC-EQP-06.

### Scherm — Apparaat wijzigen (bestaand record)

Identiek aan registreren (zelfde veldvolgorde), met de volgende wijzigingen:

```
├──────────────────────────────────────────────────────────────────────────┤
│ Identificatie nr [ 890 (disabled) ]  Inschrijf datum [ 01-04-2026 (disabled) ]│
├──────────────────────────────────────────────────────────────────────────┤
│ Donateur [ zoek schenker ▾ (select uit donors) ]                          │
│ Eigenaar (persoon) [ zoek klant ▾ (autoComplete) ]  [ nieuw ]  [ dossier ]│
├──────────────────────────────────────────────────────────────────────────┤
│ Details — specificatie (key-value, één rij per sleutel van                │
│ equipment.specification):                                                 │
│ Sleutel                │ Waarde                          │ acties        │
│ processor              │ Core i5                          │ ✎ ✗           │
│ geheugen               │ 8 GB                             │ ✎ ✗           │
│ [ Nieuwe detail toevoegen ]                                               │
├──────────────────────────────────────────────────────────────────────────┤
│ Status [ Geschikt voor uitgifte ▾ ]  [ Wijzig status ]                    │
├──────────────────────────────────────────────────────────────────────────┤
│ Uitgeschreven datum [ __-__-____ (alleen lezen; = unregistered) ]         │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Opslaan ] [ Geschiedenis ] [ Archiveren ]   Registered 01-04-2026       │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Apparaat wijzigen

  Scenario: Specificatie aanvullen
    Wanneer ik Processor "Core i5", Geheugen "8 GB" en HDD/SSD "240 GB SSD"
      invul en opsla
    Dan bevat equipment.specification die sleutels met die waarden

  Scenario: Details bewerken (key-value)
    Gegeven specification bevat {"processor": "Core 2 Duo", "bijzonderheden": "…"}
    Wanneer ik de rij "processor" bewerk naar "Core i5" en opsla
    Dan is specification["processor"] gewijzigd en de overige sleutels ongewijzigd

  Scenario: Detail verwijderen
    Wanneer ik het prullenbak-pictogram van een rij klik en bevestig
    Dan wordt die sleutel uit de JSON verwijderd
    En blijven alle andere sleutels behouden

  Scenario: Nieuwe detail toevoegen
    Wanneer ik "Nieuwe detail toevoegen" klik, sleutel "cardreader" en waarde
      "22-in-1" invul en opsla
    Dan is specification["cardreader"] "22-in-1" toegevoegd zonder andere
      sleutels te wijzigen

  Scenario: Onbekende sleutels behouden
    Gegeven specification bevat importsleutel "_overig" met {"oude_kolom": "x"}
    Wanneer ik een andere sleutel bewerk en opsla
    Dan blijft "_overig" ongewijzigd in de JSON staan (conventies §4)

  Scenario: Donateur toewijzen
    Wanneer ik een donateur kies en opsla
    Dan is equipment.donor_id gewijzigd
    Wanneer ik de donateur-leeg maak
    Dan is donor_id null

  Scenario: Eigenaar wisselen
    Wanneer ik een andere eigenaar kies en opsla
    Dan wijzigt owner_id
    En toont het scherm "Waarschuwing: dit apparaat heeft 2 tickets.
      Controleer of de eigenaarwisseling klopt."

  Scenario: Statuswijziging via wijzigscherm
    Wanneer ik de status wil wijzigen
    Dan gebeurt dat via "Wijzig status" en UC-EQP-05 (zelfde validatie op
      de overgangsmatrix)

  Scenario: Identificatie en inschrijfdatum
    Dan zijn "Identificatie nr" (equipment.id) en "Inschrijf datum"
      (equipment.registered) niet bewerkbaar
    En blijft registered na opslaan ongewijzigd

  Scenario: Uitschrijven
    Gegeven "Uitgeschreven datum" weergeeft equipment.unregistered
    Wanneer ik Archiveren bevestig (UC-EQP-06)
    Dan wordt unregistered = NOW() en verdwijnt het apparaat uit de
      standaardoverzichten
    En een handmatige datuminvul is niet toegestaan: archiveren volgt
      conventies §2 (unregistered = NOW())

  Scenario: Geschiedenis
    Wanneer ik op "Geschiedenis" klik
    Dan opent het apparaatdossier voor het betreffende record (UC-EQP-08)

  Scenario: Laatst gewijzigd
    Gegeven het scherm is geladen om 10:00
    En een collega wijzigt het apparaat om 10:05
    Wanneer ik om 10:10 opsla
    Dan waarschuwt het scherm "Dit apparaat is na het openen gewijzigd
      (laatst gewijzigd 10:05). Herlaad het scherm en probeer opnieuw."
    En wordt niet opgeslagen (conventies §5: geen optimistic locking in
      het schema; vergelijk het bij laden vastgelegde tijdstip)

  Scenario: Niet-bestaand apparaat
    Wanneer ik het apparaatformulier voor apparaat 999999 (bestaat niet) open
    Dan verschijnt "Apparaat nr 999999 bestaat niet."
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Fabrikant*, Model* | verplicht | `Vul een fabrikant in.` / `Vul een model in.` |
| Categorie | enum-waarde | `Kies een categorie.` |
| Serienummer | uniek indien gevuld (UC-EQP-07) | `Dit serienummer is al vastgelegd bij apparaat nr <id>.` |
| Details-sleutel | niet leeg, geen dubbele sleutel in het formulier | `Vul een sleutel in.` / `De sleutel "<x>" komt al voor.` |
| Details-waarde | mag leeg (sleutel blijft met null-waarde) | — |
| Prijs | positief getal of leeg | `Prijs moet een positief bedrag zijn.` |
| Datum gift | niet in de toekomst | `Datum gift kan niet in de toekomst liggen.` |
| Eigenaar, Donateur | bestaande, niet-gearchiveerde records | `Deze klant is gearchiveerd. Heropen het record eerst (beheerder).` |

### Technische notities

- Alleen velden mogen wijzigen; `registered` nooit; `unregistered` uitsluitend via
  Archiveren (UC-EQP-06, bevestiging conform conventies §2/§5).
- `specification`-JSON upsert per sleutel; onbekende sleutels behouden
  (`_overig`-conventie, conventies §4). De key-value-tabel leest en schrijft
  `equipment.specification` direct; het bewerkpatroon (rij bewerken + verwijderen +
  "Nieuwe detail toevoegen") is hetzelfde als de details-velden in UC-INT-03.
- Kolomnamen voor de update: `category`, `status` (alleen via UC-EQP-05),
  `owner_id`, `donor_id`, `behalf_of_id`, `serial_number`, `manufacturer`, `model`,
  `specification`; `registered` en `unregistered` nooit in de UPDATE-set.
- "Uitgeschreven datum" is weergave van `unregistered` (read-only); vastleggen =
  Archiveren (UC-EQP-06) met `unregistered = NOW()` conform conventies §2.
- Componenten: `p:inplace` of `p:dataTable` met `p:rowEditor` + `p:ajax` voor de
  key-value-tabel, `p:commandButton` "Nieuwe detail toevoegen", `p:confirmDialog` voor
  rij-verwijderen en Archiveren, `p:autoComplete` (personen/donors), `p:selectOneMenu`
  (categorie, status met alleen de toegestane doelen uit UC-EQP-05), `p:commandButton`
  "Wijzig status" (opent het statusdialoog UC-EQP-05), `p:button`/link "Geschiedenis".
- "Laatst gewijzigd": `equipment` heeft geen wijzigingskolom; de bean legt bij laden
  `registered` + een interne hash/tijdstip vast en waarschuwt bij afwijking vóór opslaan
  (conventies §5).
- Eigenaar-waarschuwing: tel `tickets` met `equipment_id = <id>`; bij meer dan nul
  waarschuwingstekst met het aantal.

---

## UC-EQP-05 — Statuswijziging (statusstroom)

**Actor:** Balie, Werkplaats · **Rollen:** ROLE_BALIE, ROLE_WERKPLAATS (beperkt, zie
notities), ROLE_ADMIN · **Doel:** de voorraadstatus bijwerken conform de toegestane
overgangen (Statusstroom-tabel bovenaan dit bestand). Ook de neveneffect-route van
UC-UIT-01 (uitgifte → GIFT_ISSUED) loopt via deze overgangsvalidatie.

### Scherm — Status wijzigen (dialoog vanuit het apparaatformulier)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Status wijzigen — apparaat nr 890 (HP Compaq dc7800)                      │
├──────────────────────────────────────────────────────────────────────────┤
│ Huidige status: Binnengekomen gift (INCOMING_GIFT)                        │
│ Nieuwe status* [ Geschikt voor uitgifte ▾ ]  (alleen toegestane doelen)   │
│ Toelichting [                                            ]                │
│ [ Wijzigen ]                              [ Annuleren ]                   │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Status van apparaat

  Scenario Outline: Toegestane overgangen
    Gegeven een apparaat met status <van>
    Wanneer ik de status wijzig naar <naar>
    Dan wordt de status <naar>
    En registreert de applicatie de wijziging in het applicatielogboek
      (niet in de database; equipment kent geen historietabel — zie
      technische notities)

    Voorbeelden:
      | van                 | naar                |
      | INCOMING_GIFT       | SUITABLE_FOR_GIFT   |
      | INCOMING_GIFT       | DEMOLITION          |
      | SUITABLE_FOR_GIFT   | RESERVED            |
      | SUITABLE_FOR_GIFT   | GIFT_ISSUED         |
      | SUITABLE_FOR_GIFT   | SOLD                |
      | SUITABLE_FOR_GIFT   | DEMOLITION          |
      | RESERVED            | SUITABLE_FOR_GIFT   |
      | RESERVED            | GIFT_ISSUED         |
      | RESERVED            | SOLD                |
      | RESERVED            | DEMOLITION          |
      | CUSTOMER_OWNED      | INCOMING_GIFT       |
      | CUSTOMER_OWNED      | DEMOLITION          |
      | CUSTOMER_OWNED      | SOLD                |

  Scenario Outline: Verboden overgangen
    Gegeven een apparaat met status <van>
    Wanneer ik de status wil wijzigen naar <naar>
    Dan verschijnt "Status <naar> is niet toegestaan vanuit <van>."
    En wordt de status niet gewijzigd

    Voorbeelden:
      | van               | naar               |
      | GIFT_ISSUED       | RESERVED           |
      | GIFT_ISSUED       | SUITABLE_FOR_GIFT  |
      | SOLD              | INCOMING_GIFT      |
      | SOLD              | SUITABLE_FOR_GIFT  |
      | DEMOLITION        | SUITABLE_FOR_GIFT  |
      | DEMOLITION        | INCOMING_GIFT      |
      | INCOMING_GIFT     | GIFT_ISSUED        |
      | INCOMING_GIFT     | CUSTOMER_OWNED     |
      | RESERVED          | INCOMING_GIFT      |
      | CUSTOMER_OWNED    | GIFT_ISSUED        |

  Scenario: Sloop met onderdelen
    Gegeven status INCOMING_GIFT
    Wanneer ik naar DEMOLITION wijzig en "Bruikbare onderdelen verwijderd" invul
    Dan bevat specification.bijzonderheden "Bruikbare onderdelen verwijderd"

  Scenario: Uitgifte zet status via bewijs
    Gegeven status SUITABLE_FOR_GIFT
    Wanneer via UC-UIT-01 een bewijs van uitgifte wordt opgemaakt
    Dan wordt de status automatisch GIFT_ISSUED
    En is handmatig terugzetten naar SUITABLE_FOR_GIFT niet meer mogelijk
      (tenzij ROLE_ADMIN en het bewijs wordt verwijderd)

  Scenario: Reserveren voor project
    Gegeven status SUITABLE_FOR_GIFT
    Wanneer ik naar RESERVED (Gereserveerd) wijzig met toelichting
      "Project Iedereen surft mee"
    Dan bevat specification.bijzonderheden de toelichting
      "Project Iedereen surft mee"
    En biedt UC-UIT-05 (werkopdracht ISSUE) dit apparaat aan zodra het
      gereserveerd is

  Scenario: Toelichting niet verplicht
    Wanneer ik een overgang zonder toelichting uitvoer
    Dan slaat de statuswijziging op zonder specification.bijzonderheden
      te wijzigen

  Scenario: Keuzelijst filtert op doelen
    Gegeven status INCOMING_GIFT
    Dan bevat "Nieuwe status" uitsluitend SUITABLE_FOR_GIFT en DEMOLITION
    En wanneer een niet-toegestaan doel langs directe HTTP-aanroep wordt
      aangeboden verschijnt alsnog de validatiemelding en verandert niets

  > **Meldingentekst:** in foutmeldingen worden altijd de Nederlandse labels uit
  > de statusstroom gebruikt (bv. "Status Gereserveerd is niet toegestaan vanuit
  > Binnengekomen gift."), niet de enum-waarden.

  Scenario: Apparaat gearchiveerd
    Gegeven het apparaat is gearchiveerd (unregistered gevuld)
    Wanneer ik een statuswijziging probeer
    Dan blokkeert het scherm: "Dit apparaat is gearchiveerd. Heropen het
      record eerst."

  Scenario: Autorisatie werkplaats
    Gegeven ingelogd als ROLE_WERKPLAATS
    Wanneer ik de status wijzig naar SUITABLE_FOR_GIFT, DEMOLITION of
      RESERVED
    Dan slaat de wijziging op
    En wanneer ik naar GIFT_ISSUED of SOLD wil wijzigen
    Dan verschijnt "U mag de status niet naar Uitgegeven of Verkocht wijzigen;
      die volgen uit de uitgifte (UC-UIT-01)."
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Nieuwe status* | verplicht; enum-waarde; overgang moet in de transitiematrix staan | `Status <label> is niet toegestaan vanuit <label huidige status>.` |
| Nieuwe status | niet gelijk aan huidige status | `De status is al <label>.` |
| Apparaat | niet gearchiveerd | `Dit apparaat is gearchiveerd. Heropen het record eerst.` |
| Toelichting | optioneel; maximaal 1000 tekens | `De toelichting is te lang (maximaal 1000 tekens).` |
| Autorisatie | ROLE_WERKPLAATS niet naar GIFT_ISSUED/SOLD | `U mag de status niet naar Uitgegeven of Verkocht wijzigen; die volgen uit de uitgifte (UC-UIT-01).` |

### Technische notities

- Validatie gebeurt in de service-laag met een statische transitiematrix (exact de regels
  uit de Statusstroom-tabel bovenaan); de UI filtert de `p:selectOneMenu` op toegestane
  doelen en toont desondanks de validatiemelding bij directe HTTP-aanroep. De matrix is
  implementatie-conform dit bestand en wordt nergens anders gedupliceerd.
- Update: `equipment.status` alleen; de toelichting gaat naar
  `specification.bijzonderheden` (append met datum indien gevuld, bv.
  "07-04-2026 — Project Iedereen surft mee").
- **Logboek:** `equipment` heeft géén status-historietabel in het schema (Liquibase kent geen
  `equipment_status`-tabel; schema is leidend en wordt niet gewijzigd). De wijziging wordt
  vastgelegd in het applicatielogboek (logback, niveau INFO, met apparaat-id, oude en nieuwe
  enum-waarde en gebruiker). Ticket-geschiedenis daarentegen is wél in de database
  (ticket_status + view `equipment_history`, zie `geschiedenis.md`).
- Neveneffect UC-UIT-01: de uitgifte-transactie wijzigt `equipment.status` naar GIFT_ISSUED
  via dezelfde service-methode; handmatig terugzetten is daarna verboden (terminal state),
  tenzij ROLE_ADMIN het bewijs verwijdert en de matrix opnieuw passeert.
- Autorisatie: ROLE_WERKPLAATS mag alleen wijzigen naar SUITABLE_FOR_GIFT, DEMOLITION en
  RESERVED (niet naar GIFT_ISSUED/SOLD — die volgen uit uitgifte, UC-UIT-01); zie ook
  conventies §1. Implementeer als expliciete allowlist naast de matrix, niet in plaats daarvan.

---

## UC-EQP-06 — Apparaat archiveren

**Actor:** Balie · **Rollen:** ROLE_BALIE, ROLE_ADMIN (heropenen) · **Doel:** soft delete
(`unregistered = NOW()`); heropenen is voorbehouden aan ROLE_ADMIN (conventies §1).

### Scherm — Archiveren (bevestiging vanuit het apparaatformulier)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Apparaat archiveren — nr 890 (HP Compaq dc7800)                           │
├──────────────────────────────────────────────────────────────────────────┤
│ [✓] Archiveren (uitschrijfdatum wordt nu)                                 │
│ Waarschuwing: archiveren verbergt het record in overzichten; bestaande    │
│ tickets en bewijzen blijven zichtbaar.                                    │
│ [ Bevestigen ]                            [ Annuleren ]                   │
├──────────────────────────────────────────────────────────────────────────┤
│ (alleen ROLE_ADMIN, gearchiveerd apparaat:)                                │
│ [ Heropenen ]                                                             │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Apparaat archiveren

  Scenario: Archiveren met open ticket
    Gegeven het apparaat heeft ticket 4521 met status OPEN
    Wanneer ik het apparaat archiveer
    Dan krijg ik de waarschuwing "Dit apparaat heeft nog een open ticket
      (4521)."
    En kan ik alsnog bevestigen
    En wordt unregistered gezet (= NOW(), conventies §2)

  Scenario: Archiveren met bewijs
    Gegeven het apparaat heeft bewijs 123 (uitgifte)
    Wanneer ik het apparaat archiveer
    Dan verschijnt "Archiveren verbergt het record in overzichten; bestaande
      tickets en bewijzen blijven zichtbaar." (conventies §5)
    En blijven het bewijs en de bijbehorende regels zichtbaar in UC-RAP-01

  Scenario: Verdwijnt uit overzichten
    Gegeven apparaat 890 is gearchiveerd
    Dan bevat UC-EQP-01 en UC-EQP-02 het apparaat niet meer zonder
      "Toon gearchiveerd"
    En toont de kolom Uitgeschreven datum een gevulde datum

  Scenario: Heropenen
    Gegeven ROLE_ADMIN en apparaat 900 is gearchiveerd
    Wanneer ik "Heropenen" bevestig
    Dan wordt unregistered null
    En verschijnt het apparaat weer in de standaardoverzichten

  Scenario: Heropenen zonder rechten
    Gegeven ingelogd als ROLE_BALIE
    Dan is de knop Heropenen niet zichtbaar
    En blokkeert een directe aanroep met "U hebt geen rechten voor deze
      actie." (conventies §1)

  Scenario: Herhaald archiveren
    Gegeven apparaat 890 is al gearchiveerd
    Wanneer ik Archiveren opnieuw uitvoer
    Dan verschijnt "Dit apparaat is al gearchiveerd op <datum>."
    En verandert unregistered niet

  Scenario: Dubbel klikken
    Wanneer ik Bevestigen twee keer klik
    Dan wordt de tweede klik genegeerd (idempotent op basis van unregistered)
```

### Technische notities

- Fysieke DELETE verboden (cascades op `tickets`, `proof_of_issues` via de
  equipment-FK-referenties; ook `ON UPDATE RESTRICT`/`ON DELETE CASCADE` op de
  equipment-tabellen zelf maakt wissen onomkeerbaar).
- Update: `UPDATE equipment SET unregistered = NOW() WHERE id = ? AND unregistered IS NULL`;
  heropenen: `SET unregistered = NULL`. `status` blijft staan zodat rapportages het laatste
  bekende statusbeeld behouden.
- "Uitgeschreven datum" in UC-EQP-04 en de kolom in UC-EQP-01/02 tonen dit veld; handmatige
  datuminvul is niet toegestaan (conventies §2).
- Open-ticket-telling: `SELECT t.id FROM tickets t WHERE t.equipment_id = ?` met de
  nieuwste `ticket_status`-regel (OPEN/IN_PROGRESS); waarschuwing toont de ticket-id's.
- `p:confirmDialog` (conventies §8); na bevestigen `p:growl` "Apparaat nr 890 gearchiveerd."
  en navigatie terug naar UC-EQP-01.

---

## UC-EQP-07 — Serienummerbeheer (uniekheid)

**Actor:** Balie · **Rollen:** ROLE_BALIE, ROLE_ADMIN · **Doel:** afdwingen van
`equipment_i_3` (unieke index op `serial_number`) — zowel bij registreren (UC-EQP-03) als
bij wijzigen (UC-EQP-04) en import (UC-IMP-01/02).

### Acceptatiecriteria

```gherkin
Feature: Uniek serienummer

  Scenario: Dubbel serienummer bij invoer
    Gegeven apparaat nr 888 heeft serienummer "SN-123"
    Wanneer ik bij een nieuw apparaat serienummer "SN-123" invoer en opsla
    Dan verschijnt "Dit serienummer is al vastgelegd bij apparaat nr 888."
    En wordt niets opgeslagen

  Scenario: Waar is dit serienummer al gebruikt
    Wanneer de dubbele-melding verschijnt
    Dan biedt het scherm een link "Toon apparaat nr 888"
    En opent die link het apparaatdossier voor apparaat 888 (UC-EQP-08)

  Scenario: Dubbel serienummer bij wijzigen
    Gegeven apparaat nr 890 heeft serienummer "SN-999"
    Wanneer ik bij apparaat nr 891 serienummer "SN-999" invoer en opsla
    Dan verschijnt "Dit serienummer is al vastgelegd bij apparaat nr 890."
    En blijft serienummer van 891 ongewijzigd

  Scenario: Zelfde apparaat, zelfde serienummer
    Wanneer ik bij apparaat nr 890 zijn eigen serienummer ongewijzigd opsla
    Dan slaat het scherm op (geen vals-positief)

  Scenario: Lege serienummers
    Wanneer meerdere apparaten serienummer leeg hebben
    Dan slaan die allen op (MySQL/MariaDB-unieke index telt NULL's niet als
      dubbel)

  Scenario: Serienummer met spaties
    Wanneer ik " SN-123 " invoer
    Dan slaat het apparaat op met "SN-123" (trimmen vóór opslaan)
    En blokkeert "SN-123" van een ander apparaat alsnog

  Scenario: Alleen serienummer wijzigen
    Wanneer ik uitsluitend het serienummer wijzig en opsla
    Dan bevat de logregel oude en nieuwe waarde (applicatielogboek)
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Serienummer | optioneel; uniek binnen `equipment` (index `equipment_i_3`), na trimmen | `Dit serienummer is al vastgelegd bij apparaat nr <id>.` |
| Serienummer | maximaal 512 tekens (VARCHAR(512)) | `Het serienummer is te lang (maximaal 512 tekens).` |

### Technische notities

- Controle server-side vóór insert/update (SELECT op `serial_number`, rekening houdend met
  `unregistered IS NOT NULL` — ook gearchiveerde apparaten houden het serienummer bezet,
  zolang het record bestaat) plus vang de DB-fout af (`DataIntegrityViolationException`)
  als vangnet; melding identiek. Conventies §2 maakt van serienummer de uitzondering op de
  fuzzy-dubbele-waarschuwing: hier géén waarschuwing maar een blokkade.
- Unieke index exact zoals in Liquibase: `CREATE UNIQUE INDEX equipment_i_3 ON equipment
  (serial_number);` — het schema wordt niet gewijzigd.
- Trimmen en hoofdletternormalisatie gebeuren in de UI/service vóór opslaan; de index is
  case-insensitief door de tabelcollatie (`utf8_unicode_ci`), dus "sn-123" botst ook met
  "SN-123".
