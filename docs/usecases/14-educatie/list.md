# 14 — Educatie: overzicht

Onderdeel: educatie (de activiteiten waarmee het Computerhuis zijn aanbod labelt: Klik & Tik,
Vrije Inloop, Werkplaats …). Tabel: `activities` (`name`, `reporting`, `registered` = Vanaf,
`unregistered` = Tot); daarnaast alleen-lezen telling op `timesheets` (open bezoeken per
activiteit). Scherm: het activiteitoverzicht (`activityListBean`); aanmaken en
wijzigen gebeurt in het activiteitformulier (`activityCrudBean`, UC-EDU-02).
Rapportage-lezers zien uitsluitend dit scherm.

**Webapp-variant:** het activiteitoverzicht (lijst) · het activiteitformulier.

---

## UC-EDU-01 — Overzicht activiteiten

**Actor:** Beheerder (mutaties), Balie/Werkplaats/Rapportage (alleen lezen) · **Rollen:**
ROLE_ADMIN voor toevoegen/wijzigen/archiveren/heropenen; ROLE_BALIE, ROLE_WERKPLAATS en
ROLE_RAPPORTAGE uitsluitend lezen · **Doel:** het volledige activiteitenregister in beeld met
filters en de rapportagevlag zichtbaar, zodat duidelijk is welke activiteiten meetellen in de
kerncijfers (UC-RAP-04).

### Scherm — Activiteiten

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Activiteiten                                               [ + Nieuwe ]   │
├──────────────────────────────────────────────────────────────────────────┤
│ Naam [____________________ ]  Rapportage [ alle ▾ ]  [✓] Toon gearchiveerd│
│ [ Zoeken ]                                                               │
├──────────────────────────────────────────────────────────────────────────┤
│ #  │ Naam                │ Rapportage │ Vanaf            │ Tot           │
│ 1  │ Klik & Tik           │ [✓]        │ 01-09-2023 10:00 │ —             │
│ 2  │ Vrije Inloop         │ [✓]        │ 01-09-2023 10:00 │ —             │
│ 4  │ Gast                 │ [ ]        │ 03-01-2024 09:00 │ —             │
├──────────────────────────────────────────────────────────────────────────┤
│ #  │ … │ Acties                                                     │
│ 1  │ … │ [ bewerk ] [ archiveer ]                                    │
├──────────────────────────────────────────────────────────────────────────┤
│ 12 rijen · Rapportagevlag aan: 9 · Open bezoeken: 3                      │
└──────────────────────────────────────────────────────────────────────────┘
```

*(De kolom "Acties" sluit rechts op de tabel aan; bij niet-administratieve rollen is die leeg.)*

### Acceptatiecriteria

```gherkin
Feature: Educatie — overzicht activiteiten

  Background:
    Gegeven de activiteiten "Klik & Tik" (id 1, reporting true), "Vrije Inloop" (id 2,
      reporting true), "Gast" (id 4, reporting false) en "Workshops" (id 7,
      reporting false, unregistered gevuld) bestaan

  Scenario: Tonen
    Gegeven ingelogd als ROLE_ADMIN
    Wanneer ik het scherm open
    Dan toont de tabel alle activiteiten zonder unregistered, oplopend op #,
      10 rijen per pagina
    En "Workshops" staat er niet bij

  Scenario: Rapportagevlag zichtbaar
    Dan toont de kolom Rapportage per activiteit een vinkje (alleen lezen)
    En staat "Gast" op leeg en "Klik & Tik" op gevuld

  Scenario: Sorteren
    Wanneer ik op de kolomkop Naam klik
    Dan sorteert de lijst alfabetisch op name
    En een tweede klik keert de volgorde om

  Scenario: Filter op naam
    Wanneer ik "inloop" invul in Naam en Zoeken klik
    Dan bevat de lijst uitsluitend activiteiten waarvan name deze deelterm bevat
    En is dit een gewone LIKE-filter (conventies §3 fonetisch zoeken geldt hier niet;
      dat is alleen voor klant- en schenkerzoekopdrachten)

  Scenario: Filter op rapportagevlag
    Wanneer ik bij Rapportage "ja" kies
    Dan bevat de lijst uitsluitend activiteiten met reporting true
    En toont de voetregel "Rapportagevlag aan: 2" (op de gefilterde selectie)

  Scenario: Toon gearchiveerd
    Wanneer ik "Toon gearchiveerd" aanzet
    Dan verschijnt "Workshops" wel, met een indicator "beëindigd" en de datum in kolom Tot
    En verdwijnt het vinkje weer uit de standaardlijst als ik het uitzet (conventies §2)

  Scenario: Rij openen
    Wanneer ik op het # van "Gast" klik
    Dan opent het activiteitformulier van die activiteit (UC-EDU-02)

  Scenario: Nieuwe activiteit
    Wanneer ik op "+ Nieuwe" klik
    Dan opent het activiteitformulier in de modus Nieuw (UC-EDU-02)

  Scenario: Archiveren met open bezoeken
    Gegeven "Klik & Tik" heeft 3 timesheets-rijen met unregistered null
    Wanneer ik bij die activiteit op Archiveer klik
    Dan verschijnt "Deze activiteit heeft nog 3 open bezoeken. Sluit die eerst of
      archiveer alsnog."
    En opent een p:confirmDialog met [ Archiveer alsnog ] en [ Annuleren ]

  Scenario: Archiveren bevestigen
    Wanneer ik "Archiveer alsnog" bevestig
    Dan wordt unregistered = NOW() en verdwijnt de rij uit de standaardlijst
    En verschijnt "Archiveren verbergt het record in overzichten; geregistreerde bezoeken
      en rapportages blijven de activiteitsnaam tonen." (conventies §5, op activiteiten
      toegepast)

  Scenario: Heropenen
    Gegeven ingelogd als ROLE_ADMIN en "Workshops" is gearchiveerd
    Wanneer ik met "Toon gearchiveerd" open en op Heropenen klik
    Dan wordt unregistered = NULL en verschijnt de activiteit weer in de standaardlijst

  Scenario: Leesrecht zonder admin
    Gegeven ingelogd als ROLE_BALIE
    Dan zijn "+ Nieuwe" en de acties [ archiveer ] / [ heropenen ] niet aanwezig
    En blijven # (link), Naam, Rapportage, Vanaf en Tot zichtbaar (alleen lezen)

  Scenario: Geen rechten
    Gegeven ingelogd als ROLE_RAPPORTAGE met directe toegang tot het activiteitformulier
      (buiten het menu)
    Dan krijg ik HTTP 403 met "U hebt geen rechten voor dit scherm." (conventies §1)

  Scenario: Lege tabel
    Gegeven er zijn geen activiteiten
    Dan verschijnt "Nog geen activiteiten aangemaakt." met de knop "+ Nieuwe"
      (alleen ROLE_ADMIN)
```

### Validaties

| Situatie | Melding / gedrag |
|---|---|
| Naam-filter leeg | Alle (niet-gearchiveerde) activiteiten tonen |
| Rapportage-filter `alle` | Geen filtering op `reporting` |
| Archiveer met open bezoeken | `Deze activiteit heeft nog 3 open bezoeken. Sluit die eerst of archiveer alsnog.` + confirmatie |
| Heropenen door niet-admin | Niet beschikbaar; directe actie → `U hebt geen rechten voor dit scherm.` |
| Van > Tot bij een zichtbare activiteit | Rood veld-icoon in kolom Tot; melding `De einddatum ligt voor de begindatum.` (detail in UC-EDU-02) |

### Technische notities

- Basisquery: `SELECT id, name, reporting, registered, unregistered FROM activities WHERE
  unregistered IS NULL` (of zonder die voorwaarde bij "Toon gearchiveerd"); default
  `ORDER BY id ASC`; paginering 10 rijen per pagina (conventies §3 plafond 50).
- Kolomnamen exact zoals het schema: `id`, `name`, `reporting`, `registered` (UI-label "Vanaf"),
  `unregistered` (UI-label "Tot"). `registered`/`unregistered` worden hier als datum+tijd
  getoond (`p:datePicker` is op dit scherm read-only; UI-formaat dd-MM-yyyy HH:mm).
- Rapportagevlag: `p:selectBooleanCheckbox` met `disabled=true` in de kolom — in dit scherm
  alleen zichtbaarheid, wijzigen gebeurt in UC-EDU-02. Activiteiten met `reporting` aan tellen
  mee in UC-RAP-04 (kerncijfers per periode).
- Archiveren = `unregistered = NOW()`, heropenen = `unregistered = NULL`; nooit fysiek
  verwijderen (conventies §2). `activities` bevat geen aparte `archived`-kolom.
- Open-bezoeken-telling per rij (of éénmaal voor de voetregel):
  `SELECT COUNT(*) FROM timesheets WHERE activity_id = :id AND unregistered IS NULL`.
- Semantiek van het veld `unregistered` op activities: tegelijk looptijd-einde ("Tot") en
  archiefgrens; een activiteit met `unregistered <= NOW()` staat buiten de standaardlijst.
  Zie de technische notities van UC-EDU-02.
- Rij-acties zijn pictogramknoppen (conventies §6). De snel-toevoegregel uit de bron
  (Naam + vinkje + [Toevoegen] inline in dit scherm) is bewust naar UC-EDU-02 verplaatst:
  aanmaak kent zo één validatielogica, één plek.
- Geen exportknop: export is niet expliciet onderdeel van deze use-case (conventies §6).
- Sortering op de kolomkoppen gebeurt client-side over de huidige pagina tenzij de
  implementatie server-side sortering kiest; de defaultvolgorde is in beide gevallen `id ASC`.
