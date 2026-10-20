# 14 — Educatie: beheer (crud)

Onderdeel: educatie. Tabel: `activities` (de enige tabel die dit scherm schrijft); voor de
deelnemerslijst alleen-lezen `timesheets` (of de view `timesheet_overview`). Schermen:
het activiteitformulier (`activityCrudBean`: aanmaken en wijzigen), terugkeer naar
het activiteitoverzicht (UC-EDU-01). Toegang uitsluitend ROLE_ADMIN.

**Webapp-variant:** het activiteitformulier · het activiteitoverzicht.

---

## UC-EDU-02 — Activiteit aanmaken en wijzigen

**Actor:** Beheerder · **Rollen:** ROLE_ADMIN (alleen die rol; overige rollen krijgen HTTP 403
op dit scherm, conventies §1) · **Doel:** `activities` vullen en onderhouden — vervangt Access
`Tbl_Activiteiten` (beginset: ABC-Leermee, Cursussen, Digisterker, Gast, Huiswerkklas,
Klik & Tik, LesOpMaat, Medewerker, Medewerker Werkplaats, Vrije Inloop, Werkplaats, Workshops,
WWW) en brengt de rapportagevlag en de koppeling aan personen in beeld.

### Scherm — Activiteit (nieuw of wijzigen)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Activiteit — nieuw                                   [ Annuleren ]        │
├──────────────────────────────────────────────────────────────────────────┤
│ Identificatie nummer [ 12 ] (disabled; leeg bij nieuw record)             │
│ Naam* [__________________________________________ ]                       │
│ Meedoen in rapportage [✓]                                                 │
│ Vanaf [ ■■■■■■■■  HH:mm ]   (registered; default: nu)                     │
│ Tot   [ ■■■■■■■■  HH:mm ]   (unregistered; leeg = loopt door)             │
├──────────────────────────────────────────────────────────────────────────┤
│ Koppeling aan personen (alleen lezen; afgeleid uit timesheets)            │
│ Persoon           │ Eerste bezoek │ Laatste bezoek │ Bezoeken             │
│ Johanna de Vries  │ 03-05-2024    │ 03-05-2026     │ 14                   │
│ Mark Jansen       │ 11-11-2024    │ 02-05-2026     │ 27                   │
│ (leeg bij nieuw record) — nieuwe deelnemers ontstaan bij check-in         │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Opslaan ]  [ Archiveren ]*  [ Heropenen ]*                              │
└──────────────────────────────────────────────────────────────────────────┘
```

\* Archiveren alleen bij `unregistered` null; Heropenen alleen bij `unregistered` gevuld;
beide alleen ROLE_ADMIN.

### Acceptatiecriteria

```gherkin
Feature: Educatie — activiteit aanmaken en wijzigen

  Background:
    Gegeven ingelogd als ROLE_ADMIN
    En bestaan "Klik & Tik" (id 1, reporting true) en "Vrije Inloop" (id 2, reporting true)

  Scenario: Activiteit aanmaken
    Wanneer ik Naam "Klik & Tik Bieb" invul, "Meedoen in rapportage" aanvink en Opslaan klik
    Dan bestaat een activities-rij met name "Klik & Tik Bieb", reporting true,
      registered = de gekozen Vanaf (of NOW() als Vanaf leeg) en unregistered = null
    En keer ik terug naar het activiteitoverzicht (UC-EDU-01) waar de nieuwe rij als laatste staat

  Scenario: Naam verplicht
    Wanneer ik Opslaan klik zonder Naam
    Dan blokkeert required=true de verzending en verschijnt "Vul een naam in."

  Scenario: Naam te lang
    Wanneer ik een naam invul van 51 tekens
    Dan verschijnt "De naam mag maximaal 50 tekens bevatten."
    En bevat de activities-rij niets (geen gedeeltelijke opslag)

  Scenario: Naam dubbel
    Wanneer ik "vrije inloop" toevoeg terwijl die actief bestaat
    Dan verschijnt "Deze activiteit bestaat al."
    En wordt er geen tweede rij aangemaakt

  Scenario: Naam dubbel bij wijzigen
    Gegeven activiteit id 4 "Gast"
    Wanneer ik de naam van id 4 wijzig in "Klik & Tik"
    Dan verschijnt "Deze activiteit bestaat al." (de eigen id telt niet mee als dubbele)

  Scenario: Rapportagevlag wijzigen
    Gegeven activiteit id 2 "Vrije Inloop" met reporting true
    Wanneer ik "Meedoen in rapportage" uitzet en opsla
    Dan wordt reporting false
    En telt die activiteit niet meer mee in UC-RAP-04

  Scenario: Looptijd beëindigen
    Wanneer ik een Tot-datum invul en opsla
    Dan staat die als einddatum (unregistered)
    En verdwijnt de activiteit pas uit de standaardlijst zodra die datum bereikt is
      (unregistered is hier looptijd-einde volgens conventies §2; historische bezoeken
      en rapportages blijven de naam tonen)

  Scenario: Einddatum vóór begindatum
    Wanneer ik Tot 01-01-2025 en Vanaf 01-06-2026 invul en opsla
    Dan verschijnt "De einddatum ligt voor de begindatum."
    En wordt er niets opgeslagen

  Scenario: Vanaf default
    Gegeven nieuw record en leeg Vanaf-veld
    Wanneer ik opsla
    Dan wordt registered server-side op NOW() gezet (conventies §8; nooit de browserklok)

  Scenario: Wijzigen
    Gegeven activiteit id 4 "Gast" bestaat
    Wanneer ik vanuit het activiteitoverzicht op het # klik, Naam aanpas en opsla
    Dan bevat de rij de nieuwe naam en ongewijzigd registered/reporting

  Scenario: Annuleren
    Wanneer ik op "Annuleren" klik
    Dan keer ik terug naar het activiteitoverzicht zonder opgeslagen wijzigingen

  Scenario: Activiteit bestaat niet
    Wanneer ik het activiteitformulier open voor activiteit 9999 (bestaat niet)
    Dan verschijnt "Activiteit 9999 bestaat niet." en keer ik terug naar het overzicht

  Scenario: Koppeling aan personen tonen
    Gegeven "Klik & Tik" heeft timesheets-rijen van Johanna de Vries (14) en Mark Jansen (27)
    Wanneer ik id 1 open
    Dan toont paneel "Koppeling aan personen" per persoon eerste bezoek, laatste bezoek
      en aantal bezoeken, oplopend op naam

  Scenario: Koppeling aan personen bij nieuw record
    Gegeven het record is nog niet opgeslagen
    Dan toont het paneel "(leeg bij nieuw record)" en is het read-only

  Scenario: Nieuwe deelnemer
    Wanneer een persoon voor het eerst wordt ingecheckt bij deze activiteit (UC-TID-03)
    Dan verschijnt die persoon na het herladen van dit scherm in de deelnemerslijst
    En is er in dit scherm geen handmatige "toevoegen"-actie (zie technische notities)

  Scenario: Beëindigde activiteit openen
    Gegeven "Workshops" heeft unregistered gevuld
    Wanneer ik die open
    Dan verschijnt "Deze activiteit is beëindigd op <datum>." bovenaan het formulier
    En is het formulier bewerkbaar voor ROLE_ADMIN (heropenen zet unregistered = NULL)

  Scenario: Leesrecht
    Gegeven ingelogd als ROLE_BALIE met directe toegang tot het activiteitformulier
      (buiten het menu)
    Dan krijg ik HTTP 403 met "U hebt geen rechten voor dit scherm." (conventies §1)
```

### Validaties

| Veld / situatie | Regel | Melding |
|---|---|---|
| `name` | verplicht (`@NotNull`) | `Vul een naam in.` |
| `name` | maximaal 50 tekens (`@Size(max=50)`, `VARCHAR(50)`) | `De naam mag maximaal 50 tekens bevatten.` |
| `name` | uniek onder actieve rijen, hoofdletterongevoelig, eigen id uitgezonderd | `Deze activiteit bestaat al.` |
| `registered` | default `NOW()` bij nieuw record, niet leeg | — (server-side, conventies §8) |
| `unregistered` | leeg = loopt door; gevuld ≥ `registered` | `De einddatum ligt voor de begindatum.` |
| `reporting` | default false; boolean, geen overige waarden | — |
| `id` | read-only in de UI | `Identificatie nummer is alleen ter informatie.` |
| Bestaand record | `unregistered` gevuld → waarschuwing bovenaan | `Deze activiteit is beëindigd op <datum>.` |
| Niet-admin | geen autorisatie op dit scherm | HTTP 403 + `U hebt geen rechten voor dit scherm.` |
| Onbekend id | id niet in `activities` | `Activiteit <id> bestaat niet.` + redirect overzicht |

### Technische notities

- Schema (onveranderd, Liquibase `v1/schema/activities.sql`): `id BIGINT AUTO_INCREMENT`,
  `name VARCHAR(50) NOT NULL`, `reporting BOOLEAN DEFAULT FALSE NOT NULL`,
  `registered DATETIME DEFAULT NOW() NOT NULL`, `unregistered DATETIME`;
  PK `activities_pk (id)`. Het schema wordt niet gewijzigd.
- **Twee betekenissen van `unregistered`:** dit scherm labelt het veld "Tot" (looptijd-einde,
  zoals het webapp-prototype) terwijl conventies §2 `unregistered` als archivering definieert.
  Beide vallen in hetzelfde veld: archiveren (knop) zet `unregistered = NOW()` per direct,
  een toekomstige Tot laat de activiteit zichtbaar tot die datum. Documenteer dit zo in de
  implementatie; de filter in UC-EDU-01 is leidend voor wat "standaard zichtbaar" is.
- Dubbele-naam-controle (service-laag, wint boven de JSF-laag, conventies §5):
  `SELECT id FROM activities WHERE LOWER(name) = LOWER(:naam) AND unregistered IS NULL`
  met `AND id <> :eigenId` bij wijzigen.
- **`activities_persons` bestaat niet in de huidige changelog.** De opdracht noemt een
  koppeltabel `activities_persons`, maar `src/main/resources/db/changelog/v1/schema/` bevat
  alleen `activities`, `timesheets`, `donor_persons` e.a. — deze tabel ontbreekt volledig in
  de repo. De "Koppeling aan personen" is daarom **afgeleid, read-only**:
  `SELECT p.id, p.first_name, p.infix, p.last_name, MIN(ts.registered), MAX(ts.registered),
  COUNT(*) FROM timesheets ts JOIN persons p ON p.id = ts.person_id WHERE ts.activity_id = :id
  GROUP BY p.id ORDER BY p.last_name` (of dezelfde groepering over `timesheet_overview`).
  Een echte bewerkbare koppeling vereist een **nieuwe changeset** met die tabel; het
  bestaande schema-bestand blijft ongemoeid (README: schema is leidend en wordt niet gewijzigd).
  Nadat die changeset bestaat, wordt het paneel bewerkbaar (`p:autoComplete` op personen +
  [Toevoegen] / [Verwijderen]) zonder de veldvolgorde van het formulier te veranderen.
- PrimeFaces: `p:form`, `p:inputText` (Naam), `p:selectBooleanCheckbox` (Meedoen in rapportage),
  `p:datePicker` met `timeInput=true` op Vanaf/Tot (conventies §8; `p:calendar` waar de
  implementatie dat al gebruikte — alleen hier en op de tijdschermen semantisch relevant),
  `p:commandButton` Opslaan/Annuleren, `p:messages` + `p:message` per veld (conventies §5),
  `p:dataTable` voor de deelnemerslijst.
- Beginset als seed-data: voorbereiden als **losse nieuwe Liquibase-changeset**; het
  bestaande schema-bestand (`v1/schema/activities.sql`) blijft ongemoeid — het toestaan van
  seed-changesets is een implementatiebeslissing.
- Rapportagevlag: `reporting = true` → de activiteit telt mee in UC-RAP-04 (kerncijfers per
  periode); `reporting = false` → alleen zichtbaar als het filter daar is uitgezet.
- Geen `updated`-kolom op `activities`: de "laatst gewijzigd"-waarschuwing uit conventies §5
  kan hier niet getoond worden zonder schema-uitbreiding (toegestaan: schema ongewijzigd).
  Wijzigingen zijn verder niet auditbaar op dit record.
- Archiveren en heropenen staan ook als actie op het overzicht (UC-EDU-01); de waarschuwing
  bij open bezoeken (`timesheets` met `unregistered IS NULL`) hoort daar.
