# 11 — Donateurs: beheer (crud)

Onderdeel: schenkers/donateurs — aanmaken, wijzigen, archiveren/heropenen en de koppeling
persoon ↔ schenker. Dit bestand is de mutatieslag; het lees-/zoekscherm en de apparatuurlijst
staan in `list.md` (UC-DON-01 en UC-DON-05). Tabellen: `donors` (met `msaccess` JSON volgens
conventies §4.2), koppeltabel `donor_persons`, voor postcodevalidatie `postal_codes`;
lezend/verwijzend `equipment` (`donor_id`, `behalf_of_id`), `proof_of_issues.behalf_of_id` en
`tickets.details.derde_betaalt` (`donor_id`). Views: **geen** — er bestaat geen donor-view in
`src/main/resources/db/changelog`; alle overzichten en tellen query'en direct op `donors` en
`donor_persons`. Schermen: het schenkerformulier (`donorCrudBean`, formulier voor
aanmaken én wijzigen), het schenkeroverzicht (`donorListBean`, vertrekpunt en
terugkeerpad) en de apparatuurlijst van de schenker (`donorEquipmentListBean`, alleen-lezen
lijst vanuit de CRUD).

**Webapp-variant:** het schenkerformulier uit het prototype (veldopbouw met
identificatienummer en inschrijfdatum disabled, rapportagevinkje, uitgeschreven datum,
opmerkingen en de knop "Apparatuurlijst"), samengevoegd met de domeinregels van
contactpersonen, archiveren/heropenen en de koppeling `donor_persons`.

---

## UC-DON-02 — Schenker aanmaken

**Actor:** Balie · **Rollen:** ROLE_BALIE, ROLE_ADMIN · **Doel:** bedrijf, organisatie of
particuliere schenker vastleggen (Access: "Bedrijf invoeren" bij Invoer giften), inclusief
administratiegegevens en contactpersonen.

### Scherm — Schenker registreren

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Schenker registreren                                       [ Annuleren ] │
├──────────────────────────────────────────────────────────────────────────┤
│ Identificatienummer [ — nieuw —                     ] (id, disabled)     │
│ Inschrijfdatum [ — bij opslaan —             ] (registered, disabled)    │
│ Naam/Bedrijfsnaam [__________________________________________ ] * (name)  │
│ Vestiging [________________________________ ] (msaccess.vestiging)        │
│ E-mail [__________________________________ ] (email)                     │
│ Telefoon [__________ ]   Mobiel [__________ ] (telephone / mobile)       │
├──────────────────────────────────────────────────────────────────────────┤
│ Adres                                                                     │
│ Straat [___________________________________ ] (street)                   │
│ Huisnummer [_____ ]  Toevoeging [____ ]  Postcode [______ ]              │
│ Woonplaats [______________________________ ] (city)                      │
│ [ postcode ophalen ]  Postbus [________ ]  Postcode postbus [______ ]     │
├──────────────────────────────────────────────────────────────────────────┤
│ administratie (msaccess)                                                  │
│ KvK-nummer [____________ ]  BTW-nummer [______________________ ]         │
│ Website [_________________________________ ]                              │
├──────────────────────────────────────────────────────────────────────────┤
│ [✓] Rapporteert mee in donateursrapportage (reporting)                    │
│ Opmerking [                                                       ]       │
│                                                       (comments)          │
├──────────────────────────────────────────────────────────────────────────┤
│ Contactpersonen (msaccess.contactpersonen)                                │
│ Naam │ E-mail │ Telefoon │                                  [ verwijder ]│
│ — geen contactpersonen —        [ Contactpersoon toevoegen ]             │
├──────────────────────────────────────────────────────────────────────────┤
│ [ Opslaan ]                                                              │
└──────────────────────────────────────────────────────────────────────────┘
```

> De blokken "Contactpersonen" worden na het eerste opslaan actief (het record heeft dan een
> identificatienummer); "Uitgeschreven datum" is **niet** op dit scherm aanwezig — een nieuwe
> schenker wordt direct actief opgeslagen (zie UC-DON-03).

### Acceptatiecriteria

```gherkin
Feature: Schenker aanmaken

  Background:
    Gegeven ik ben ingelogd als ROLE_BALIE
    En ik sta op het schenkerformulier in de modus Nieuw

  Scenario: Bedrijf met alleen naam en e-mail
    Gegeven het Access-meldpunt: "Bedrijf Ros-Kabelkrant, alleen e-mailadres en
      contactpersoon" moest betrouwbaar opslaan
    Wanneer ik Naam "Ros-Kabelkrant" en E-mail "info@ros-kabelkrant.example.nl" invul
      en op Opslaan klik
    Dan wordt de schenker opgeslagen met de velden die gevuld zijn
    En bevat donors.msaccess._overig een leeg object (onbekende invoer bewaren we daar)
    En is donors.id een nieuw auto-incrementnummer
    En is donors.registered NOW() (server-side, conventies §8)
    En is donors.unregistered null
    En is donors.reporting false (default)
    En verschijnt "Schenker opgeslagen."

  Scenario: Identificatienummer en inschrijfdatum zijn disabled
    Dan staan Identificatienummer en Inschrijfdatum niet-editable op het formulier
    En toont Identificatienummer "— nieuw —" tot na het opslaan
    Wanneer het opslaan gelukt is
    Dan tonen beide velden respectievelijk het nieuwe id en de opslagdatum

  Scenario: Volledig bedrijf vastleggen
    Wanneer ik Naam "Digitaland Recycling", Vestiging "Vestiging 's-Hertogenbosch",
      KvK-nummer "87654321", BTW-nummer "NL009876543B01",
      Website "https://digitaland-recycling.example.nl", Telefoon "0731234567",
      Straat "Vughterweg", Huisnummer 145, Postcode "5213AA", Woonplaats "'s-Hertogenbosch"
      invoer en opsla
    Dan bevat donors de velden name, email, mobile, telephone, postal_code, street,
      house_number, house_number_addition, city, comments
    En bevat donors.msaccess de sleutels legacy_bedrijfsnummer (null), vestiging,
      kvk_nummer, btw_nummer, website, postbus (null), postcode_postbus (null),
      contactpersonen ([]) en _overig ({})
    En verschijnt "Schenker opgeslagen."

  Scenario: Contactpersoon vastleggen
    Wanneer ik na het opslaan op "Contactpersoon toevoegen" klik en naam, e-mail en
      telefoon invul en bevestig
    Dan bevat donors.msaccess.contactpersonen een extra element met die waarden
      ( {"naam": …, "email": …, "telefoon": …} )
    En blijven de eerder toegevoegde elementen staan (append, niet vervangen)

  Scenario: Contactpersoon verwijderen
    Wanneer ik bij een contactpersoon op verwijder klik en bevestig
    Dan verdwijnt dat element uit donors.msaccess.contactpersonen
    En blijven de overige elementen ongewijzigd

  Scenario: Postcode onbekend
    Wanneer ik een postcode invoer die niet in postal_codes staat
    Dan blokkeert de opslag met de postcode-melding (donors heeft geen FK, maar de
      applicatie valideert wel tegen postal_codes voor consistentie)

  Scenario: Adresloze schenker
    Wanneer ik uitsluitend naam invul
    Dan slaat het scherm op (adres is niet verplicht)
    En zijn postal_code, street, house_number, house_number_addition, city null

  Scenario: Naam verplicht
    Gegeven Naam is in het prototype niet required
    Dan moet de implementatie "Vul een naam in." afdwingen
      (kolom donors.name is NOT NULL, max 50 tekens)

  Scenario: Te lange naam
    Wanneer ik een bedrijfsnaam van 63 tekens invoer
    Dan slaat het scherm op met donors.name afgekort tot 50 tekens
    En verschijnt "De naam is ingekort tot 50 tekens."
    En blijft de ongewijzigde naam bewaard in donors.msaccess._overig

  Scenario: Rapportagevlag direct bij aanmaak
    Wanneer ik "Rapporteert mee in donateursrapportage" aanvink en opsla
    Dan is donors.reporting true
    En telt de schenker mee in het schenkersrapport (UC-RAP-06)

  Scenario: Na opslaan
    Wanneer het opslaan gelukt is
    Dan blijft het scherm open in weergavemodus met het nieuwe id
    En biedt het de knoppen Apparatuurlijst (UC-DON-05), Opslaan, Archiveren
    En staat de knop "Contactpersoon toevoegen" vanaf nu beschikbaar

  Scenario: Annuleren
    Wanneer ik op Annuleren klik zonder opgeslagen wijzigingen
    Dan keert het scherm terug naar het schenkeroverzicht (UC-DON-01)
    En bij ingevulde wijzigingen vraagt het "Niet-opgeslagen wijzigingen gaan verloren.
      Doorgaan?"
```

### Validaties

| Veld | Regel | Melding |
|---|---|---|
| Naam | verplicht, max 50 tekens (`donors.name` NOT NULL VARCHAR(50)) | `Vul een naam in.` |
| Naam | langer dan 50 tekens | `De naam is ingekort tot 50 tekens.` (bij succes) |
| E-mail | geldig of leeg | `Vul een geldig e-mailadres in.` |
| Telefoon / Mobiel | exact 10 cijfers of leeg, formaat `0612345678` / `0731234567` | `Telefoonnummer moet 10 cijfers bevatten.` |
| Postcode | formaat `1234AB` (4 cijfers + 2 letters, uppercase, geen spatie), moet bestaan in `postal_codes.code` | `Postcode 1234AB komt niet voor in de postcodeverwijzing. Controleer de invoer of laat de beheerder de postcode toevoegen.` |
| Huisnummer | numeriek, positief, of leeg | `Vul een geldig huisnummer in.` |
| Huisnummer-toevoeging | max 4 tekens, bijv. `A`, `B`, `12`, `bis` | `De toevoeging is maximaal 4 tekens.` |
| Postcode postbus | formaat `1234AB` of leeg (geen FK-check: postbusbestemmingen staan niet in `postal_codes`) | `Postcode moet 4 cijfers en 2 letters bevatten.` |
| Website / KvK / BTW / Vestiging | vrij, leeg toegestaan | — |
| Rapportage (reporting) | boolean, default false | — |
| Opmerking | vrij (`comments` TEXT) | — |

### Technische notities

- Insert `donors`: `name`, `reporting`, `email`, `mobile`, `telephone`, `postal_code`,
  `street`, `house_number`, `house_number_addition`, `city`, `comments`,
  `registered = NOW()` (server-side, nooit browserklok — conventies §8), `unregistered = NULL`,
  `msaccess`; `id` auto-increment.
- Vestiging, KvK-nummer, BTW-nummer, website, postbus en postcode postbus gaan naar
  `donors.msaccess` volgens conventies §4.2; `legacy_bedrijfsnummer` blijft null bij nieuwe
  schenkers (alleen gevuld door imports, waarbij onbekende sleutels in `_overig` belanden).
  Voorbeeld:

  ```json
  {
    "legacy_bedrijfsnummer": null,
    "vestiging": "Vestiging 's-Hertogenbosch",
    "kvk_nummer": "87654321",
    "btw_nummer": "NL009876543B01",
    "website": "https://digitaland-recycling.example.nl",
    "postbus": null,
    "postcode_postbus": null,
    "contactpersonen": [
      { "naam": "Ans Vermeulen", "email": "ans@digitaland-recycling.example.nl", "telefoon": "0731234567" }
    ],
    "_overig": {}
  }
  ```

- `donors.name` is max VARCHAR(50): langere bedrijfsnamen worden afgekort met melding; de
  ongewijzigde naam blijft bewaard in `donors.msaccess._overig` (vrije sleutel binnen het
  `_overig`-object; conventies §4 — onbekende/vrije sleutels behouden).
- **Geen** fuzzy-dubbelecheck-blokkade: conventies §2 noemen personen, apparaten en tickets;
  donors vallen daar niet onder, dus een gelijke bedrijfsnaam geeft geen blokkade en geen
  verplichte waarschuwing.
- Postcode: `donors.postal_code` heeft **geen** FK naar `postal_codes` (in tegenstelling tot
  `persons.postal_code`); de applicatie valideert wel tegen `postal_codes.code` voor
  consistentie en blokkeert de opslag met de melding uit conventies §5. Invoer wordt
  vóór opslaan genormaliseerd naar uppercase zonder spatie.
- Telefoonvelden zijn `CHAR(10)`: UI-formatteerder stript spaties, puntjes en haakjes naar
  10 cijfers vóór opslaan (conventies §2).
- PrimeFaces: `p:form` met `p:inputText` (naam, adres, msaccess-velden),
  `p:selectBooleanCheckbox` (reporting), `p:commandButton` (Opslaan/Annuleren),
  `p:messages` + samenvattende melding bovenaan bij blokkerende validatie (conventies §5),
  `p:autoComplete` voor "postcode ophalen" tegen `postal_codes`, `p:dataTable`-rij of
  `p:repeat` voor `contactpersonen` met `p:commandButton` per rij. Geen `p:datePicker` nodig
  (Inschrijfdatum is disabled en server-side).
- Rollen: ROLE_BALIE en ROLE_ADMIN mogen aanmaken; ROLE_RAPPORTAGE en ROLE_WERKPLAATS
  krijgen 403 (conventies §1).
- Na opslaan: terug naar of verversen van het schenkeroverzicht (UC-DON-01); de
  apparatuurknop verwijst naar de apparatuurlijst van de schenker (UC-DON-05).

---

## UC-DON-03 — Schenker wijzigen en archiveren

**Actor:** Balie (wijzigen/archiveren), Beheerder (heropenen) · **Rollen:** ROLE_BALIE,
ROLE_ADMIN · **Doel:** gegevens van een bestaande schenker bijwerken — inclusief
rapportagevlag, uitgeschreven datum en contactpersonen — en archiveren/heropenen zoals
UC-PER-05.

### Scherm — Schenker wijzigen

Identiek aan "Schenker registreren" (UC-DON-02), met het record geladen, plus:

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Schenker wijzigen — Ros-Kabelkrant (nr 12)                  [ Annuleren ] │
├──────────────────────────────────────────────────────────────────────────┤
│ Identificatienummer [ 12 ] (disabled)                                    │
│ Inschrijfdatum [ 04-01-2023 ] (disabled)                                 │
│ Uitgeschreven datum [■■■■■■■■  ] (unregistered)                          │
│ Status: actief  |  of bij gearchiveerd: GEARCHIVEERD (14-02-2026)        │
│ …(overige velden als op het aanmaakscherm, inclusief blok                 │
│  Contactpersonen met toevoegen/wijzigen/verwijderen)…                     │
├──────────────────────────────────────────────────────────────────────────┤
│ Registered 04-01-2023  ·  Laatst gewijzigd 18-06-2026 14:12             │
│ [ Apparatuurlijst ]  [ Opslaan ]  [ Archiveren ]                         │
└──────────────────────────────────────────────────────────────────────────┘
```

Bij een gearchiveerd record vervalt de knop "Archiveren" en verschijnt
`[ Heropenen ]` (uitsluitend ROLE_ADMIN); de velden blijven leesbaar.

### Acceptatiecriteria

```gherkin
Feature: Schenker wijzigen en archiveren

  Background:
    Gegeven schenker "Ros-Kabelkrant" (id 12) met 3 donor_persons-koppelingen en
      10 apparaten (equipment.donor_id = 12)

  Scenario: Naam wijzigen
    Gegeven ik ben ingelogd als ROLE_BALIE
    Wanneer ik de bedrijfsnaam wijzig en op Opslaan klik
    Dan is donors.name gewijzigd
    En is donors.unregistered ongewijzigd
    En is donors.registered ongewijzigd (nooit wijzigbaar, conventies §2)

  Scenario: Rapportagevlag
    Wanneer ik Rapportage aanvink en opsla
    Dan is donors.reporting true (telt mee in schenkersrapportage, UC-RAP-06)
    Wanneer ik het vinkje weer uitzet en opsla
    Dan is donors.reporting false

  Scenario: Uitschrijven via datum
    Wanneer ik Uitgeschreven datum vul en opsla
    Dan is donors.unregistered gevuld met die datum
    En verdwijnt de schenker uit het standaardoverzicht (UC-DON-01, conventies §2)

  Scenario: Uitschrijven ongedaan maken
    Wanneer ik Uitgeschreven datum leeg maak en opsla
    Dan is donors.unregistered null
    En staat de schenker weer in het standaardoverzicht
    (alleen ROLE_ADMIN; zie validaties)

  Scenario: Archiveren via knop
    Wanneer ik op Archiveren klik en de bevestiging bevestig
    Dan wordt donors.unregistered gezet op NOW() (server-side, conventies §2)
    En verschijnt "Schenker gearchiveerd."

  Scenario: Archiveren met actieve koppelingen
    Gegeven de schenker heeft 3 donor_persons-koppelingen en 10 apparaten (donor_id)
    Wanneer ik de schenker archiveer
    Dan wordt unregistered gezet
    En blijven koppelingen en apparaten zichtbaar bij hun eigen dossiers
    En toont het scherm de waarschuwing uit conventies §5: "Archiveren verbergt het
      record in overzichten; bestaande tickets en bewijzen blijven zichtbaar."

  Scenario: Heropenen
    Gegeven schenker 44 is gearchiveerd
    En ik ben ingelogd als ROLE_ADMIN
    Wanneer ik op Heropenen klik en bevestig
    Dan is donors.unregistered null en verschijnt "Record heropend."
    (procedure UC-PER-05)

  Scenario: Heropenen zonder rechten
    Gegeven ik ben ingelogd als ROLE_BALIE op een gearchiveerd record
    Dan staat de knop Heropenen niet op het scherm
    En verschijnt bij laden "Deze schenker is gearchiveerd. Het record kan alleen door
      een beheerder heropend worden."

  Scenario: Naam verplicht bij wijzigen
    Wanneer ik Naam leeg laat en opsla
    Dan blokkeert de opslag met "Vul een naam in." en wordt niets gewijzigd
      (kolom donors.name is NOT NULL, max 50 tekens)

  Scenario: Laatst gewijzigd afwijkend
    Gegeven het scherm toont "Laatst gewijzigd 18-06-2026 14:12"
    En een ander scherm heeft intussen dezelfde schenker gewijzigd
    Wanneer ik opsla
    Dan verschijnt "De schenker is inmiddels elders gewijzigd. Ververs het scherm en
      probeer het opnieuw." en wordt niet opgeslagen (conventies §5)

  Scenario: Apparatuurlijst
    Wanneer ik op Apparatuurlijst klik
    Dan opent de apparatuurlijst van de schenker voor het betreffende record (UC-DON-05)

  Scenario: Contactpersonen beheren
    Wanneer ik een bestaand contactpersonen-element wijzig en opsla
    Dan bevat donors.msaccess.contactpersonen het bijgewerkte element
      met de overige elementen ongewijzigd

  Scenario: Wijzigen van een gearchiveerde schenker
    Gegeven het record is gearchiveerd
    Wanneer ik een veld wijzig en opsla
    Dan blokkeert de opslag: "Dit record is gearchiveerd. Heropen het record eerst."
```

### Validaties

| Veld / actie | Regel | Melding |
|---|---|---|
| Naam | verplicht, max 50 tekens | `Vul een naam in.` |
| Uitgeschreven datum | mag niet voor de inschrijfdatum (`registered`) liggen | `De uitschrijfdatum mag niet voor de inschrijfdatum liggen.` |
| Uitgeschreven datum leegmaken (unregister) | alleen ROLE_ADMIN | `Alleen een beheerder mag een schenker opnieuw inschrijven.` |
| Archiveren | bevestiging verplicht; melding bij kindrecords (conventies §5) | `Archiveren verbergt het record in overzichten; bestaande tickets en bewijzen blijven zichtbaar.` |
| Heropenen | alleen ROLE_ADMIN, alleen bij `unregistered IS NOT NULL` | `Alleen een beheerder mag een gearchiveerd record heropenen.` |
| Postcode / e-mail / telefoon | ongewijzigd geldigheid als UC-DON-02 | zie UC-DON-02 |
| Gearchiveerd record | geen mutaties vóór heropenen | `Dit record is gearchiveerd. Heropen het record eerst.` |
| Laatst gewijzigd afwijkend | blokkerend vóór opslaan (conventies §5) | `De schenker is inmiddels elders gewijzigd. Ververs het scherm en probeer het opnieuw.` |

### Technische notities

- UPDATE op `donors`: alleen de gewijzigde kolommen (`name`, `reporting`, `email`, `mobile`,
  `telephone`, `postal_code`, `street`, `house_number`, `house_number_addition`, `city`,
  `comments`, `msaccess`, `unregistered`); `registered` en `id` nooit.
- Archiveren = `unregistered = NOW()`; heropenen = `unregistered = NULL` (conventies §2).
  Het schenkerformulier in het prototype bood "Uitgeschreven datum" als vrij
  datepaderveld dat direct naar `unregistered` schreef; **afspraak**: de knop Archiveren vult
  `NOW()` server-side (conventies §2 zijn leidend), het datepaderveld toont de waarde en mag
  alleen door ROLE_ADMIN worden overschreven (bijv. om een bekende uitschrijfdatum uit
  Access terug te plaatsen). Bij altijd-niet-lege-waarde is de schenker "uitgeschreven",
  ongeacht het tijdstip.
- `donors` kent geen fysieke verwijdering. De FK's die naar `donors` wijzen
  (`equipment.donor_id`, `equipment.behalf_of_id`, `proof_of_issues.behalf_of_id`) zijn
  `ON UPDATE RESTRICT ON DELETE CASCADE` — nooit `DELETE` gebruiken (conventies §2); een
  verwijderde donor zou immers apparaten en bewijzen meeslepen. Soft delete cascadert niet:
  apparaten en `donor_persons`-rijen blijven staan.
- Optimistic locking: `donors` heeft geen `updated_at`-kolom. Conform conventies §5 toont het
  scherm bij laden een "laatst gewijzigd"-tijdstip (vastgelegd in de applicatielaag bij het
  openen, bv. als snapshot in de bean) en waarschuwt het bij een afwijkend tijdstip vóór
  opslaan. `registered` is nadrukkelijk **niet** dat tijdstip.
- `msaccess`-updates mergen: alleen gewijzigde sleutels vervangen, `contactpersonen` als
  geheel array-element bijwerken, `_overig` en overige onbekende sleutels onaangeroerd laten
  (conventies §4).
- Contactpersonen: `donors.msaccess.contactpersonen[]` met bindende sleutels
  `naam`, `email`, `telefoon` — dit is **iets anders** dan de relationele koppeling
  `donor_persons` (UC-DON-04); verwarring tussen beide vermijden in UI en documentatie.
- PrimeFaces: zelfde componentset als UC-DON-02, aangevuld met `p:datePicker`
  (Uitgeschreven datum, NL-formaat `dd-MM-yyyy`), `p:confirmDialog` voor Archiveren en
  Heropenen, en `p:commandButton` Apparatuurlijst (verwijzing naar de
  apparatuurlijst van de schenker).
- Rollen: wijzigen/archiveren = ROLE_BALIE en ROLE_ADMIN; heropenen en datumoverschrijving =
  uitsluitend ROLE_ADMIN; ROLE_RAPPORTAGE is lezend en krijgt 403 op dit scherm
  (conventies §1).

---

## UC-DON-04 — Koppeling persoon ↔ schenker

**Actor:** Balie · **Rollen:** ROLE_BALIE, ROLE_ADMIN · **Doel:** `donor_persons` beheren —
bv. de medewerker van een bedrijf dat doneert, of een particuliere schenker die ook als
persoon bekend is. De koppeling is de grondslag voor "Cliënten (uitgifte)" en voor de
UI-hint bij "derde betaalt" (UC-UIT-03).

### Scherm — Koppelingen (vanaf Persoon weergeven (UC-PER-03) of Schenker weergeven (UC-DON-03))

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Schenkerkoppelingen — Johanna de Vries (persoon nr 42)   [ Sluiten ]      │
├──────────────────────────────────────────────────────────────────────────┤
│ gekoppeld: Ros-Kabelkrant (donateur nr 12)                 [ verwijderen ]│
│ gekoppeld: Stichting Digimaat (donateur nr 44, gearchiveerd)
│                                                [ verwijderen ]           │
├──────────────────────────────────────────────────────────────────────────┤
│ Schenker toevoegen: [ zoek schenker ▾ (autoComplete) ]  [ koppelen ]      │
│ [✓] Koppelingen tonen voor schenker "Ros-Kabelkrant" (nr 12)             │
├──────────────────────────────────────────────────────────────────────────┤
│ voor gekoppelde personen:  [ open persoon (UC-PER-03) ]                  │
└──────────────────────────────────────────────────────────────────────────┘
```

Weergave vanuit de schenkerkant (donateur nr 12) draait de lijst om: per rij een persoon met
achternaam, woonplaats en de knoppen `Open persoon` (UC-PER-03) en `verwijderen`; de
toevoegregel is dan `Persoon toevoegen: [ zoek klant ▾ (autoComplete) ] [ koppelen ]`
(conventies §8: nooit een volledige tabel in een dropdown).

### Acceptatiecriteria

```gherkin
Feature: Koppeling persoon en schenker

  Background:
    Gegeven ik ben ingelogd als ROLE_BALIE
    En persoon "Johanna de Vries" (nr 42) en schenker "Ros-Kabelkrant" (nr 12) bestaan
    En donor 12 heeft nog geen koppeling met persoon 42

  Scenario: Koppelen vanaf de persoon
    Wanneer ik vanaf Persoon weergeven (UC-PER-03) de schenkerkoppelingen open,
      een schenker kies en op Koppelen klik
    Dan bestaat er een rij donor_persons (donor_id 12, person_id 42)
    En verschijnt de schenker in de lijst "gekoppeld"

  Scenario: Koppelen vanaf de schenker
    Wanneer ik vanaf Schenker weergeven (UC-DON-03) "Personen/Cliënten" open,
      een persoon kies en op Koppelen klik
    Dan bestaat er een rij donor_persons met dezelfde waarden
    En toont de lijst alle personen gekoppeld via donor_persons

  Scenario: Dubbele koppeling
    Wanneer ik dezelfde schenker nog eens koppel
    Dan verschijnt "Deze schenker is al gekoppeld."
    En wordt geen tweede rij toegevoegd (combinatie donor_id + person_id is uniek)

  Scenario: Verwijderen
    Wanneer ik bij een koppeling op Verwijderen klik en bevestig in het
      p:confirmDialog
    Dan is de rij donor_persons verwijderd (fysiek; koppeltabel kent geen soft delete)
    En verdwijnt de rij uit beide weergaven

  Scenario: Zoeken naar een schenker om te koppelen
    Wanneer ik "ros" in de autocomplete typ
    Dan verschijnt "Ros-Kabelkrant" (naamzoekregels uit UC-DON-01)
    En komen gearchiveerde schenkers niet voor in het voorstel (conventies §3)

  Scenario: Zoeken naar een persoon om te koppelen (vanaf schenker)
    Wanneer ik "vries" in de autocomplete typ
    Dan verschijnt "Johanna de Vries" (fonetisch zoeken, conventies §3)
    En komen gearchiveerde personen niet voor in het voorstel

  Scenario: Koppeling uit schenkerzicht
    Wanneer ik vanaf Schenker weergeven "Cliënten/beheerders" open
    Dan toont de lijst alle personen gekoppeld via donor_persons
    En opent een persoon in het persoonformulier (UC-PER-03)

  Scenario: Lege lijst
    Gegeven er zijn geen koppelingen
    Dan verschijnt "Nog geen personen gekoppeld aan deze schenker."

  Scenario: Zonder keuze koppelen
    Wanneer ik op Koppelen klik zonder schenker of persoon te kiezen
    Dan verschijnt "Kies een schenker om te koppelen." respectievelijk
      "Kies een persoon om te koppelen." en wordt niets opgeslagen

  Scenario: Gekoppelde persoon gearchiveerd
    Gegeven persoon 42 is gearchiveerd (unregistered gevuld)
    Dan blijft de koppeling zichtbaar met label "persoon gearchiveerd"
    En kan de koppeling alsnog verwijderd worden

  Scenario: UI-hint bij derde betaalt
    Gegeven ik sta in de uitgifte (UC-UIT-03) met "derde betaalt" en kies schenker 12
    En de te kiezen ontvanger (persoon) is via donor_persons gekoppeld aan 12
    Dan verschijnt de hint "Johanna de Vries is gekoppeld aan deze schenker."
    Wanneer er geen koppeling bestaat
    Dan verschijnt geen foutmelding: de koppeling is een hint, geen dwingende voorwaarde
```

### Validaties

| Veld / actie | Regel | Melding |
|---|---|---|
| Schenkerkeuze | verplicht bij koppelen, bestaand en niet-gearchiveerd record | `Kies een schenker om te koppelen.` |
| Persoonkeuze | verplicht bij koppelen, bestaand en niet-gearchiveerd record | `Kies een persoon om te koppelen.` |
| Combinatie | uniek (`donor_persons_pk` = donor_id + person_id) | `Deze schenker is al gekoppeld.` |
| Verwijderen | bevestiging verplicht (`p:confirmDialog`) | `Koppeling verwijderen? Dit kan niet ongedaan worden gemaakt.` |
| Geen autorisatie | rollen conform conventies §1 | `U hebt geen rechten voor dit scherm.` |

### Technische notities

- Tabel `donor_persons`: kolommen `donor_id`, `person_id`; composite PK
  (`donor_persons_pk`), FK's `ON UPDATE RESTRICT ON DELETE CASCADE` op beide zijden —
  daarom **uitsluitend INSERT en DELETE, nooit UPDATE** (een "herkoppelen" = verwijderen +
  opnieuw invoegen).
- De koppeltabel heeft **geen** `registered`/`unregistered`: geen soft delete; fysiek
  verwijderen van een rij mag en is de enige vorm van opheffen (conventies §2 geldt alleen
  voor tabellen met `unregistered`).
- Koppelingen overleven soft delete van beide kanten (cascade geldt alleen bij fysieke
  `DELETE`): een gearchiveerde schenker houdt zijn personen en een gearchiveerde persoon
  houdt zijn schenkers — tonen met label, niet wegfilteren binnen dit scherm.
- Bij gebruik in de uitgifte: `proof_of_issues.behalf_of_id` → FK `donors`, en
  `tickets.details.derde_betaalt` = `{ "donor_id": 12, "email_bewijs": "…" }`
  (conventies §4.4). De koppeling bevestigt dat de persoon bij die donor hoort: **UI-hint,
  geen dwingende FK** — dezelfde hint wordt in UC-UIT-03 gebruikt.
- `equipment.donor_id` (geschonken apparaat) en `equipment.behalf_of_id` (apparaat namens
  deze partij uitgegeven) staan los van `donor_persons`; de koppeling zegt iets over
  *relatie*, niet over *eigendom van apparaten*.
- De webapp-prototypes van het schenkeroverzicht, het schenkerformulier en de
  apparatuurlijst van de schenker kenden deze koppeling nog niet (alleen `donors` +
  `donor_persons` in het schema); dit scherm is nieuw t.o.v. dat prototype en leunt op de
  personenset (koppelingen vanaf UC-PER-03).
- PrimeFaces: `p:autoComplete` (schenkers en personen, zelfde zoekregels als UC-DON-01 en
  UC-PER-01), `p:commandButton` voor Koppelen/verwijderen per rij,
  `p:confirmDialog` voor verwijderen, `p:growl` voor de meldingen.
- De koppelingen zijn leidend voor "Cliënten (uitgifte)" in het donateursoverzicht
  (UC-DON-01) en voor de schenkersrapportage (UC-RAP-06) waar die personen aan schenkers
  worden toegerekend.
