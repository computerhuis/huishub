# 15 — Tijdsregistratie: registreren en corrigeren (crud)

Onderdeel: tijdsregistratie en aanwezigheid. Tabel: `timesheets` (de enige tabel die dit
scherm schrijft), daarnaast `activities` en `persons` (alleen lezen); view:
`timesheet_overview` (alleen lezen, voor namen en overzichten). Schermen:
het tijdregistratieformulier (`timesheetCrudBean`: check-in, check-out en correctie
in één formulier met drie modi) en het tijdregistratieoverzicht
(`timesheetListBean`: overzichten en startpunt, UC-TID-01/02). Vervangt Access
"Aanwezigheidsregistratie" (Tijd Binnengekomen / Tijd weggegaan).

**Semantiek (onveranderd):** een bezoek = één `timesheets`-rij: `registered` = binnenkomst
(verplicht, deel van de composite PK), `unregistered` = vertrek (`null` zolang de persoon er
nog is). Er is geen automatisch sluiten: openstaande bezoeken worden uitsluitend handmatig
gesloten (UC-TID-04) of gecorrigeerd (UC-TID-05).

**Webapp-variant:** het tijdregistratieformulier (registratie en correctie) ·
het tijdregistratieoverzicht (overzichten).

---

## UC-TID-03 — Bezoek registreren (check-in)

**Actor:** Balie, Werkplaats · **Rollen:** ROLE_BALIE, ROLE_WERKPLAATS, ROLE_ADMIN (geen
autorisatie → HTTP 403, conventies §1) · **Doel:** binnenkomst vastleggen als nieuwe
`timesheets`-rij (Access: "Aanwezigheidsregistratie").

### Scherm — Bezoek registreren

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Bezoek registreren                                                        │
├──────────────────────────────────────────────────────────────────────────┤
│ Bezoeker [ zoek klant ▾ (p:autoComplete) ]  [ nieuw ]                     │
│ Activiteit [ Klik & Tik ▾ ]  (alleen activiteiten zonder Tot ≤ nu)        │
│ Tijd binnen [ ■■■■■■■■  HH:mm ]  (default: nu)                            │
├──────────────────────────────────────────────────────────────────────────┤
│ Reeds aanwezig (vandaag, nog niet vertrokken):                            │
│ 10:12  M. Jansen      — Vrije Inloop           [ uitchecken ]             │
│ 10:40  J. de Vries    — Klik & Tik             [ uitchecken ]             │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Registreren ]  [ Annuleren ]                                            │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Bezoek registreren (check-in)

  Background:
    Gegeven ingelogd als ROLE_BALIE
    En bestaan "M. Jansen" (nr 77) en "J. de Vries" (nr 42)
    En bestaan de activiteiten "Vrije Inloop" (id 2, reporting true) en
      "Klik & Tik" (id 1, reporting true)
    En is het 03-05-2026 10:55

  Scenario: Check-in
    Gegeven persoon "M. Jansen" (nr 77), activiteit "Vrije Inloop" (id 2)
    Wanneer ik bezoeker en activiteit kies en Registreren klik
    Dan bestaat een timesheets-rij (person_id 77, activity_id 2, registered nu,
      unregistered null)
    En verschijnt "Bezoek geregistreerd: M. Jansen — Vrije Inloop vanaf 10:55."

  Scenario: Tijd handmatig invullen
    Wanneer ik Tijd binnen 09:30 invoer terwijl het nu 10:55 is
    Dan wordt registered vandaag 09:30
    En verschijnt de waarschuwing "Tijd in het verleden ingevuld."

  Scenario: Tijd in de toekomst
    Wanneer ik 23:59 invoer terwijl het nu 10:55 is
    Dan verschijnt "Tijd binnen kan niet in de toekomst liggen."
    En wordt er niets opgeslagen

  Scenario: Dubbele check-in
    Gegeven M. Jansen heeft vandaag al een onafgerond bezoek
    Wanneer ik hem opnieuw check-in
    Dan verschijnt "M. Jansen is al aanwezig sinds 10:12 (Vrije Inloop)."
    En kan ik alsnog een tweede activiteit registreren (aparte rij) na bevestiging

  Scenario: Bezoeker verplicht
    Wanneer ik Registreren klik zonder bezoeker
    Dan blokkeert required=true de verzending en verschijnt "Kies een bezoeker."

  Scenario: Activiteit verplicht
    Wanneer ik Registreren klik zonder activiteit
    Dan verschijnt "Kies een activiteit."

  Scenario: Gearchiveerde bezoeker
    Wanneer ik een gearchiveerde persoon selecteer
    Dan verschijnt "Deze persoon is gearchiveerd; registratie niet mogelijk."

  Scenario: Beëindigde activiteit
    Gegeven de activiteit "Workshops" heeft unregistered in het verleden
    Dan staat die niet in de keuzelijst (alleen lopende activiteiten uit UC-EDU-01)
    En wanneer ik via een directe link toch "Workshops" (id 7) meestuur
    Dan verschijnt "Deze activiteit is beëindigd; registratie niet mogelijk."

  Scenario: Directe tijdregistratie vanuit klantdossier
    Wanneer ik vanuit "Persoon weergeven" (UC-PER-03) tab Aanwezigheid op
      "Bezoek registreren" klik
    Dan is de bezoeker voorgevuld en staat het scherm op de laatste activiteit
      van die persoon als voorslag

  Scenario: Directe registratie vanuit het overzicht
    Wanneer ik vanuit UC-TID-01 op "Bezoek registreren" klik
    Dan is er geen bezoeker voorgevuld en staat de laatst gebruikte activiteit als voorslag

  Scenario: Reeds aanwezig-paneel
    Dan toont het paneel alle personen met een timesheets-rij van vandaag met
      unregistered null, gesorteerd op registered, met een [uitchecken]-knop per rij
      (UC-TID-04)

  Scenario: Nieuwe bezoeker
    Wanneer ik op [nieuw] klik
    Dan opent het persoonformulier in de modus Nieuw (UC-PER-02) en keer ik na opslaan
      terug met de nieuwe persoon als bezoeker gekozen

  Scenario: Annuleren
    Wanneer ik op Annuleren klik
    Dan wordt er niets opgeslagen en keer ik terug naar het overzicht (UC-TID-01)
```

### Validaties

| Situatie | Melding / gedrag |
|---|---|
| Bezoeker ontbreekt | `Kies een bezoeker.` (required) |
| Activiteit ontbreekt | `Kies een activiteit.` (required) |
| Tijd binnen in de toekomst | `Tijd binnen kan niet in de toekomst liggen.` (blokkerend) |
| Tijd binnen in het verleden | Opslaan mag wel; waarschuwing `Tijd in het verleden ingevuld.` (growl, niet blokkerend) |
| Persoon al aanwezig (vandaag, `unregistered IS NULL`) | `M. Jansen is al aanwezig sinds 10:12 (Vrije Inloop).` + alsnog toestaan na bevestiging |
| Persoon gearchiveerd | `Deze persoon is gearchiveerd; registratie niet mogelijk.` |
| Activiteit beëindigd (`unregistered` gevuld) | `Deze activiteit is beëindigd; registratie niet mogelijk.` |
| PK-collision (zelfde persoon, activiteit en exact dezelfde seconde) | Automatisch `registered` + 1 seconde, maximaal 5 pogingen; daarna `De registratie botst met een bestaande rij; probeer het opnieuw.` |

### Technische notities

- Insert `timesheets` met `person_id`, `activity_id`, `registered` = gekozen tijd (default
  server-side `NOW()`, conventies §8 — nooit de browserklok), `unregistered` = null.
- **PK-collision:** de PK is `(person_id, registered, activity_id)`. Als er al een rij bestaat
  met exact dezelfde seconden, persoon en activiteit, verhoog `registered` met 1 seconde en
  probeer opnieuw (max 5 pogingen).
- "Al aanwezig"-controle: `WHERE person_id = ? AND unregistered IS NULL AND
  DATE(registered) = CURDATE()`.
- Bezoeker zoeken volgt de zoekconventies §3 (fonetisch, max 50 resultaten) via
  `p:autoComplete`; nooit een dropdown met alle personen (conventies §8).
- Keuzelijst activiteiten: `activities` waar `unregistered IS NULL` (lopende activiteiten,
  UC-EDU-01); een activiteit met `unregistered <= NOW()` is beëindigd en wordt geweerd.
- PrimeFaces semantisch relevant: `p:datePicker` met `timeInput=true` op Tijd binnen (of
  `p:calendar` in implementaties die dat component al gebruikten); overige invoer via
  `p:inputText`, `p:autoComplete`, `p:selectOneMenu`; `p:confirmDialog` bij de dubbele
  check-in; `p:messages` meldingen via conventies §5.
- Geen dubbele-fuzzy-waarschuwing zoals bij personen/apparaten (conventies §2): een dubbele
  check-in is een geldig domeinverschijnsel en krijgt hier een expliciete waarschuwing.
- Na registratie: terug naar UC-TID-01 (of het klantdossier bij binnenkomst via UC-PER-03).

---

## UC-TID-04 — Vertrek registreren (check-out)

**Actor:** Balie, Werkplaats (dezelfde rollen als check-in: ROLE_BALIE, ROLE_WERKPLAATS,
ROLE_ADMIN), of incheckkiosk-gebruiker · **Doel:** vertrektijd vastleggen in
`timesheets.unregistered` en de duur van het bezoek berekenen.

### Scherm — Uitchecken (paneel van UC-TID-03 of rij-actie in UC-TID-01)

```
├──────────────────────────────────────────────────────────────────────────┤
│ Aanwezig nu                                                              │
│ Bezoeker        │ Activiteit  │ Binnen │ Tijd uit [ ■■■ ] │ [uitchecken]  │
│ J. de Vries     │ Klik & Tik  │ 10:40  │ [ 11:35 ]        │ [ uit ]       │
│ M. Jansen       │ Vrije Inl.  │ 10:12  │ [ nu ]           │ [ uit ]       │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Vertrek registreren (check-out)

  Background:
    Gegeven ingelogd als ROLE_BALIE
    En is J. de Vries vandaag 10:40 ingecheckt op "Klik & Tik"

  Scenario: Uitchecken
    Wanneer ik bij J. de Vries op Uitchecken klik met tijd 11:35
    Dan wordt unregistered vandaag 11:35
    En verdwijnt de rij uit "Aanwezig nu"
    En verschijnt "Uitgecheckt: J. de Vries — Klik & Tik, duur 0:55."

  Scenario: Duur berekenen
    Gegeven binnenkomst 10:40 en opgegeven vertrek 11:35
    Dan berekent de applicatie duur = unregistered - registered
    En toont de bevestiging die duur als "H:mm" ("0:55")

  Scenario: Vertrek vóór aankomst
    Wanneer ik 10:00 invoer terwijl binnenkomst 10:40 was
    Dan verschijnt "Vertrek kan niet vóór binnenkomst liggen."
    En wordt unregistered niet gewijzigd

  Scenario: Vertrek in de toekomst
    Wanneer ik 23:59 invoer terwijl het nu 10:55 is
    Dan verschijnt "Vertrektijd kan niet in de toekomst liggen."

  Scenario: Default-tijd
    Gegeven het Tijd uit-veld staat op "nu"
    Wanneer ik Uitchecken klik zonder tijd te wijzigen
    Dan wordt unregistered = NOW() (server-side, conventies §8)

  Scenario: Vergeten uit te checken
    Gegeven een bezoek van gisteren met unregistered null
    Wanneer ik het vandaag uitcheck
    Dan verschijnt "Dit bezoek is van gisteren. Vul de vertrektijd van gisteren in."
    En staat de datum van vertrek gelijk aan de datum van aankomst (tijd kan later zijn)

  Scenario: Bezoek is al gesloten
    Gegeven de rij heeft al unregistered 11:35
    Wanneer ik opnieuw uitcheck probeer
    Dan verschijnt "Dit bezoek is al afgesloten (uit 11:35). Gebruik de correctie (UC-TID-05)."

  Scenario: Meerdere open bezoeken
    Gegeven M. Jansen heeft twee open bezoeken (Vrije Inloop 10:12 en Werkplaats 10:30)
    Wanneer ik bij de rij van 10:12 uitcheck
    Dan wordt uitsluitend die rij gesloten (update op person_id, registered, activity_id)

  Scenario: Rollen
    Gegeven ingelogd als ROLE_RAPPORTAGE
    Dan is "Aanwezig nu" alleen-lezen en is [uit] niet beschikbaar

  Scenario: Automatisch sluiten niet toegestaan
    Dan sluit de applicatie openstaande bezoeken niet automatisch; dit gebeurt
      uitsluitend handmatig of via UC-TID-05 (correctie)
```

### Validaties

| Situatie | Melding / gedrag |
|---|---|
| Vertrek vóór binnenkomst | `Vertrek kan niet vóór binnenkomst liggen.` (blokkerend) |
| Vertrektijd in de toekomst | `Vertrektijd kan niet in de toekomst liggen.` (blokkerend) |
| Bezoek van een eerdere dag | `Dit bezoek is van gisteren. Vul de vertrektijd van gisteren in.` + vertrekdatum = aankomstdatum |
| Bezoek al gesloten | `Dit bezoek is al afgesloten (uit 11:35). Gebruik de correctie (UC-TID-05).` |
| Rij niet meer open na herladen | `Deze registratie is inmiddels gewijzigd; vernieuw het overzicht.` |

### Technische notities

- UPDATE `timesheets.unregistered`; de PK bevat `registered`, dus update op
  `(person_id, registered, activity_id)` — nooit op een auto-increment id (bestaat niet).
- "Aanwezig nu": `WHERE unregistered IS NULL ORDER BY registered`; in dit paneel
  uitsluitend rijen van vandaag, tenzij het scherm expliciet "alle open bezoeken" toont
  (correctie van gisteren gebeurt via UC-TID-05).
- Duur wordt in de applicatielaag berekend en nergens opgeslagen (`timesheets` heeft geen
  duurkolom en geen `updated`); de view `timesheet_overview` bevat evenmin een duurkolom.
- Geen `NOW()`-gebaseerde "stilzwijgende sluiting" bij het laden van schermen of bij
  nachtelijke jobs: openstaande bezoeken zijn feitelijk aanwezig en mogen niet verdwijnen.
- PrimeFaces: `p:datePicker` met `timeInput=true` op Tijd uit, `p:commandButton` Uitchecken,
  `p:confirmDialog` niet nodig (geen destructieve actie).

---

## UC-TID-05 — Aanwezigheid corrigeren

**Actor:** Beheerder · **Rollen:** ROLE_ADMIN uitsluitend; ROLE_BALIE, ROLE_WERKPLAATS en
ROLE_RAPPORTAGE krijgen geen correctie (HTTP 403 bij directe URL, conventies §1) · **Doel:**
foutieve tijden herstellen (vergeten uitchecken, dubbele registratie, verkeerde activiteit)
met verplichte reden en logging.

### Scherm — Bezoek corrigeren

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Bezoek corrigeren — id via overzicht (UC-TID-01) of per persoon (UC-TID-02)│
├──────────────────────────────────────────────────────────────────────────┤
│ Bezoeker  Johanna de Vries      Activiteit [ Klik & Tik ▾ ]                │
│ Binnen  [ ■■■■■■■■  HH:mm ]     Uit [ ■■■■■■■■  HH:mm ]                    │
│ Reden van correctie [________________________________ ] *                 │
│ [ Opslaan ]  [ Bezoek verwijderen ]                                        │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Aanwezigheid corrigeren

  Background:
    Gegeven ingelogd als ROLE_ADMIN
    En bestaat een bezoek van Johanna de Vries op "Klik & Tik" van 03-05-2026 10:40
      met vertrek 11:35

  Scenario: Vertrektijd aanvullen
    Gegeven een bezoek met unregistered null
    Wanneer ik de uit-tijd 14:05 invul, een reden opgeef en opsla
    Dan wordt unregistered gewijzigd
    En bevat de applicatielogs de correctie met gebruiker, reden en oude waarde

  Scenario: Binnenkomst aanpassen
    Wanneer ik Binnen van 10:40 naar 10:30 wijzig en opsla
    Dan wordt registered gewijzigd
    En waarschuwt de applicatie als daardoor de composite PK (person_id, registered,
      activity_id) bots met een bestaande rij: "Deze combinatie van bezoeker, activiteit
      en binnenkomst bestaat al. Kies een andere binnenkomsttijd."

  Scenario: Activiteit wijzigen
    Wanneer ik Activiteit van "Klik & Tik" naar "Vrije Inloop" wijzig en opsla
    Dan wordt activity_id gewijzigd
    En bevat de logregel de oude en nieuwe activiteit

  Scenario: Reden verplicht
    Wanneer ik Opslaan klik zonder reden
    Dan blokkeert required=true de verzending met "Vul een reden voor de correctie in."

  Scenario: Uit vóór binnen
    Wanneer ik Uit 10:00 invul bij binnenkomst 10:40
    Dan verschijnt "Vertrek kan niet vóór binnenkomst liggen."
    En wordt er niets opgeslagen

  Scenario: Verwijderen
    Wanneer ik Bezoek verwijder en de reden bevestig
    Dan wordt de timesheets-rij fysiek verwijderd (toegestaan: correctie van
      een niet-rapporteerde meting), met logging

  Scenario: Verwijderen bevestigen
    Wanneer ik in de p:confirmDialog "Bezoek definitief verwijderen" bevestig
    Dan verdwijnt de rij uit UC-TID-01 en UC-TID-02
    En bevat de logregel persoon, activiteit, registered, reden en gebruiker

  Scenario: Na rapportageperiode
    Gegeven het bezoek valt in een afgesloten rapportageperiode (eerdere kalendermaand)
    Wanneer ik probeer te corrigeren
    Dan verschijnt "Dit bezoek valt in een afgesloten rapportageperiode.
      Correctie is alleen mogelijk met ROLE_ADMIN en wordt gemarkeerd."
    En volstaat een applicatielogs-regel als markering (schema ongewijzigd)

  Scenario: Balie
    Gegeven ingelogd als ROLE_BALIE
    Dan is correctie niet beschikbaar (geen knop; directe URL → HTTP 403 met
      "U hebt geen rechten voor dit scherm.")

  Scenario: Open bezoek corrigeren
    Gegeven een bezoek met unregistered null
    Dan blijft het veld Uit leeg toegestaan; opslaan met leeg Uit sluit het bezoek niet
      (alleen de velden Binnen/Activiteit worden gewijzigd)

  Scenario: Niet-bestaand bezoek
    Wanneer ik een onbekende combinatie van person_id, registered en activity_id open
    Dan verschijnt "Dit bezoek bestaat niet (meer). Vernieuw het overzicht."
```

### Validaties

| Veld / situatie | Regel | Melding |
|---|---|---|
| Reden van correctie | verplicht (`@NotNull`) | `Vul een reden voor de correctie in.` |
| Uit (vertrek) | ≥ Binnen (als beide gevuld) | `Vertrek kan niet vóór binnenkomst liggen.` |
| Binnen | niet in de toekomst | `De binnenkomsttijd kan niet in de toekomst liggen.` |
| Uit (vertrek) | niet in de toekomst | `De vertrektijd kan niet in de toekomst liggen.` |
| Binnen (gewijzigd) | `(person_id, registered, activity_id)` uniek | `Deze combinatie van bezoeker, activiteit en binnenkomst bestaat al. Kies een andere binnenkomsttijd.` |
| Periode | `registered` in eerdere kalendermaand (afgesloten rapportageperiode) | `Dit bezoek valt in een afgesloten rapportageperiode. Correctie is alleen mogelijk met ROLE_ADMIN en wordt gemarkeerd.` |
| Bezoek verwijderen | fysieke verwijdering alleen met reden + bevestiging | `Vul een reden voor de correctie in.` / confirmatie verplicht |
| Rol | alleen ROLE_ADMIN | HTTP 403 + `U hebt geen rechten voor dit scherm.` |
| Record verdwenen | rij niet meer aanwezig | `Dit bezoek bestaat niet (meer). Vernieuw het overzicht.` |

### Technische notities

- Update gebeurt altijd op de composite PK `(person_id, registered, activity_id)`; er is geen
  `id`-kolom op `timesheets`. Wijzigt `registered`, dan is dat de PK-wijziging zelf —
  implementeer als delete+insert binnen één transactie alleen als de PK niet conflictueus
  bijgewerkt kan worden, en log in beide gevallen de oude en nieuwe waarden.
- Correcties zijn administratief: schrijf per opslaan of verwijderen één regel naar de
  applicatielog (SLF4J, niveau WARN, met persoon, activiteit, oude en nieuwe waarden,
  reden en gebruiker). `timesheets` heeft geen `updated`-kolom; de log is de enige
  audittrail — dit is bewust (schema ongewijzigd, `v1/schema/timesheets.sql`).
- Afgesloten rapportageperiode = `registered` ligt vóór de eerste dag van de huidige
  kalendermaand; de markering is eveneens een WARN-logregel ("gemarkeerde correctie na
  rapportageperiode") — geen extra kolom, geen extra tabel (schema niet wijzigen).
- Verwijderen: `DELETE FROM timesheets WHERE person_id = ? AND registered = ? AND
  activity_id = ?` — fysiek, uitsluitend in dit scherm (conventies §2 kent zachte
  verwijdering, maar `timesheets` heeft geen betekenisvolle archiefstatus: een gewiste
  meting zou anders blijven doorwegen in UC-RAP-04; de bron-use-case stelt
  fysieke verwijdering expliciet toe).
- View-gebruik: alleen voor het tonen van namen (`timesheet_overview` met de
  `dco.persons`-prefix, zie de technische notities bij UC-TID-01); de correctie zelf werkt
  rechtstreeks op `timesheets`.
- PrimeFaces: `p:datePicker` met `timeInput=true` op Binnen en Uit (of `p:calendar` in
  implementaties die dat component al gebruikten), `p:selectOneMenu` op Activiteit
  (lopende activiteiten, UC-EDU-01), `p:inputText` op Reden, `p:commandButton` Opslaan,
  `p:commandButton` met `styleClass="ui-button-danger"` op Bezoek verwijderen,
  `p:confirmDialog` ter bevestiging,
  `p:messages` + `p:message` per veld (conventies §5).
- Optimistische locking (conventies §5) is hier niet beschikbaar zonder
  `updated`-kolom; de applicatie waarschuwt in plaats daarvan bij het niet-bestaan van de
  PK bij laden en bij PK-conflict bij opslaan (zie validaties).
