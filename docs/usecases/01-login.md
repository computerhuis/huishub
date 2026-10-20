# 01 — Login en sessie

Onderdeel: authenticatie. Tabellen: `persons`, `person_roles`. Schermen: het inlogscherm en
de uitlogactie in de menubalk (webapp-prototype); geen aparte CRUD.

**Webapp-variant:** inloggen gebeurt met een dropdown van vrijwilligers + wachtwoord
(`loginBean.fetch()` vult de lijst). De e-mail-login uit de eerdere domein-use-case is
teruggelegd als toekomstige uitbreiding (§ Notities).

---

## UC-AUT-01 — Inloggen

**Actor:** elke gebruiker · **Doel:** sessie starten; autoriteiten uit `person_roles` laden.

### Scherm — Login

```
┌──────────────────────────────────────────────────────────┐
│            [ logo ]  Welkom bij Database                 │
│            Computerhuis Oost                             │
│         Login om verder te gaan                          │
│                                                          │
│ Vrijwilliger [ J. de Vries (id 42) ▾ ]   *               │
│ Wachtwoord   [**********]                   *           │
│ [ Login ]                                                │
│ Ongeldige gebruikersnaam of wachtwoord (rood)            │
└──────────────────────────────────────────────────────────┘
```

### Acceptatiecriteria

```gherkin
Feature: Inloggen

  Scenario: Geldige inlog
    Gegeven vrijwilliger "Johanna de Vries" (id 42) met een passhash, unregistered null
    Wanneer ik haar kies, wachtwoord invul en op Login klik
    Dan ben ik ingelogd en open ik de homepagina
    En zijn de autoriteiten uit person_roles in de security-context geladen

  Scenario: Onjuist wachtwoord
    Wanneer ik een onjuist wachtwoord invoer
    Dan verschijnt "Ongeldige gebruikersnaam of wachtwoord"
    En wordt bevestigd niet of de vrijwilliger bestaat

  Scenario: Verplichte velden
    Wanneer ik Login klik zonder vrijwilliger of wachtwoord
    Dan blokkeert JSF de verzending (required=true) met veldmeldingen

  Scenario: Gearchiveerde vrijwilliger
    Gegeven de persoon is gearchiveerd (unregistered gevuld)
    Dan staat die persoon niet in de dropdown
    En kan die ook niet direct inloggen via een handmatige URL

  Scenario: Onthoud mij (toekomstig)
    Gegeven "Onthoud mij" aangevinkt
    Wanneer ik inlog en de sessie verloopt
    Dan blijf ik ingelogd via een remember-me-cookie
    En bij uitloggen wordt de cookie verwijderd
```

### Technische notities

- Dropdown toont alleen `unregistered IS NULL` met (implementatie-afspraak) een
  vrijwilligersrol of `msaccess.vrijwilligerswerk_start`; label = `fullname`, waarde =
  `person.id`. Foutpad: het inlogscherm toont een foutmelding zonder te verraden of de
  gebruikersnaam of het wachtwoord onjuist is.
- Spring Security (joinfaces security-starter) met remember-me via
  `SECURITY_REMEMBER_ME_KEY`-omgevingsvariabele.
- `passhash` VARCHAR(4096): accepteer BCrypt/SHA-256; nooit plaintext opslaan of terugtonen.
- Zonder autorisatie op een scherm: HTTP 403 + "U hebt geen rechten voor dit scherm."
  (conventies §1).
- **Toekomstig:** e-mailadres als gebruikersnaam (exacte match op `persons.email`) naast de
  dropdown; dezelfde regels uit conventies §3.

---

## UC-AUT-02 — Uitloggen

**Actor:** ingelogde gebruiker · **Doel:** sessie beëindigen.

```gherkin
Feature: Uitloggen

  Scenario: Uitloggen via menubalk
    Gegeven ik ben ingelogd
    Wanneer ik op het uitlog-pictogram klik
    Dan wordt de sessie beëindigd en kom ik op het login-scherm
    En is remember-me (indien later toegevoegd) gewist
```
