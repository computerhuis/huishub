# 05 — Import Leergeld

Onderdeel: gegevensimport. Schermen: de importwizard (`inputWizardBean`,
`p:wizard` met tabs "Kies bestandindeling · Laad bestand · Te importeren · Resultaat").
Tabellen: `equipment` (match op `serial_number`), `equipment.specification.leergeld`
(conventies §4.3).

**Webapp-variant:** de importwizard uit het prototype (enige importflow; niet eerder in de
domein-use-cases).

---

## UC-IMP-01 — Importeren: Digidromen (Stichting Leergeld)

**Actor:** Balie · **Rollen:** ROLE_BALIE, ROLE_ADMIN · **Doel:** aangeleverde serienummers
van Leergeld-apparaten koppelen aan de voorraad.

### Scherm — Import (4 stappen)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Invoeren                                                                  │
├──────────────────────────────────────────────────────────────────────────┤
│ Stap 1 Kies bestandindeling                                              │
│ Bestand indeling* [ Digidromen - Stichting Leergeld ▾ ]                   │
│   ( Digidromen - Stichting Leergeld | Gezinnen - Stichting Leergeld )     │
├──────────────────────────────────────────────────────────────────────────┤
│ Stap 2 Laad bestand                                                      │
│ [ Kies bestand … (meerdere toegestaan, auto-upload) ]                     │
├──────────────────────────────────────────────────────────────────────────┤
│ Stap 3 Te importeren                                                     │
│ Serie nummer                                                              │
│ SN-00123                                                                  │
├──────────────────────────────────────────────────────────────────────────┤
│ Stap 4 Resultaat                            [ Volgende ]                  │
│ Serie nummer │ Status                                                    │
│ SN-00123     │ [✓] geïmporteerd (groen)                                  │
│ SN-00999     │ [⚠] bestaat al / overgeslagen (geel)                      │
│ SN-01000     │ [✗] niet gevonden (rood)                                  │
└──────────────────────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Import Digidromen

  Scenario: Bestandindeling kiezen
    Wanneer ik "Digidromen - Stichting Leergeld" kies
    Dan gaat de wizard naar stap "Laad bestand"

  Scenario: Bestanden laden
    Wanneer ik minimaal een bestand kies (meerdere toegestaan, auto-upload)
    Dan toont stap "Te importeren" een lijst met de gevonden serienummers
    En gaat de wizard automatisch naar "Resultaat" na het uploaden

  Scenario: Resultaat per regel
    Dan toont de tabel (Serie nummer, Status) per serienummer een vinkje (groen,
      geïmporteerd), waarschuwing (geel, overgeslagen/bestaat al) of kruis (rood,
      serienummer niet gevonden in de database)

  Scenario: Import uitvoeren
    Wanneer ik op "Volgende" klik bij "Te importeren"
    Dan worden de gevonden serienummers gekoppeld: equipment.specification.leergeld
      wordt true voor het gevonden apparaat
    En bevat de applicatielogs per nummer de importstatus

  Scenario: Leeg bestand
    Wanneer het bestand geen serienummers bevat
    Dan verschijnt "Het bevat geen serienummers. Controleer het bestand."
```

### Technische notities

- Importtype-waarden in de bean: `DIGIDROMEN_LEERGELD`, `GEZINNEN_LEERGELD`.
- Match op `equipment.serial_number` (exact, trims); apparaten zonder serienummer worden
  niet aangeraakt.
- rowStyleClass onderscheidt success/warn/error; `null`-waarde = niet gevonden (rood kruis).
- De klassen-naam `text-succes-color` uit het prototype is een typo; gebruik
  `text-success-color`.
- Geen transactie over meerdere bestanden: per regel afhandelen, zodat een deelfout de rest
  niet blokkeert.

---

## UC-IMP-02 — Importeren: Gezinnen (Stichting Leergeld)

```gherkin
Feature: Import Gezinnen

  Scenario: Bestandindeling kiezen
    Wanneer ik "Gezinnen - Stichting Leergeld" kies
    Dan doorloopt de wizard dezelfde stappen en gelden dezelfde regels als UC-IMP-01

  Scenario: Geen voorvertoning in het prototype
    Dan toont stap "Te importeren" momenteel geen lijst
    En implementeert de eindversie een voorvertoning gelijk aan Digidromen,
      of meldt "Geen voorvertoning beschikbaar" vóór de importbevestiging
```

### Technische notities

- Het verschil tussen beide indelingen zit in het bronbestand (kolomindeling); de
  verwerking (serienummer → apparaat) is identiek.
- Toekomstige koppeling naar `specification.leergeld.laptop/.smartphone/.simkaart` hangt af
  van de kolommen in het brondocument (buiten scope zolang het schema vast is).
