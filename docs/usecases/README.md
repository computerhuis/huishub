# BDD-usecases — Huishub (Werkgroep Computerhuis Oost)

Usecases voor implementatie van het portaal, afgeleid uit `docs/SPECIFICATIE.md`. Het schema in
`src/main/resources/db/changelog` is leidend en wordt **niet** gewijzigd. De UI is 100% PrimeFaces
(JSF via JoinFaces); de wireframes hieronder zijn **uitsluitend** bedoeld om de velden en knoppen
van een scherm kenbaar te maken — geen layout-, thema- of componentadvies.

De set is één geheel: de **flowbestanden** (01–05) beschrijven de doorlopende werkproces­sen, de
**onderdeelbestanden** (10–17) zijn per onderdeel gesplitst in `list.md` (overzichten, zoeken,
export) en `crud.md` (aanmaken/wijzigen/archiveren). Schermvelden en knoppen komen overeen met de
prototypen in `access/webapp`; waar domeinregels en prototype verschillen wint het domein
(afwijkingsmatrix hieronder).

## Bestanden

### Algemeen

| Bestand | Onderwerp |
|---|---|
| `00-conventies.md` | Rollen, JSON-registry (§4), validatieregels, foutafhandeling, wireframe-legenda |

### Flowbestanden (doorlopende werkproces­sen)

| Bestand | Onderwerp | Use-cases |
|---|---|---|
| `01-login.md` | Inloggen (vrijwilligersdropdown), sessie, uitloggen | UC-AUT-01 t/m 02 |
| `02-balie.md` | Baliewizard: zoeken → gegevens → apparatuur → tickets | UC-BAL-01 t/m 05 |
| `03-intake-werkplaats.md` | Reparatie-intake (3-steps wizard), één transactie | UC-INT-01 t/m 04 |
| `04-uitgave.md` | Uitgifte met bewijs: regels, derde betaalt, ondertekening, ISSUE | UC-UIT-01 t/m 05 |
| `05-import-leergeld.md` | Leergeld-import: Digidromen en Gezinnen | UC-IMP-01/02 |

### Onderdeelbestanden (list + crud per onderdeel)

| Map | Bestand | Onderwerp | Use-cases |
|---|---|---|---|
| `10-personen/` | `list.md` | Klanten zoeken (fonetisch), overzicht | UC-PER-01 |
| | `crud.md` | Registreren, weergeven, wijzigen, archiveren, rollen, wachtwoord | UC-PER-02 t/m 07 |
| `11-donateurs/` | `list.md` | Schenkers zoeken; apparatuurlijst per donateur | UC-DON-01, UC-DON-05 |
| | `crud.md` | Schenker aanmaken/wijzigen/archiveren; koppeling persoon ↔ schenker | UC-DON-02 t/m 04 |
| `12-apparatuur/` | `list.md` | Overzicht met statusfilter; voorraadoverzicht | UC-EQP-01/02 |
| | `crud.md` | Registreren, wijzigen, statusstroom, archiveren, serienummer | UC-EQP-03 t/m 07 |
| | `geschiedenis.md` | Apparaatdossier met historie | UC-EQP-08 |
| `13-tickets/` | `list.md` | Ticketoverzicht (statusbord); werkplaatsoverzicht | UC-TIC-01/02 |
| | `crud.md` | Ticket wijzigen, status, klant bellen, details/log, archiveren | UC-TIC-03 t/m 07 |
| `14-educatie/` | `list.md` | Overzicht activiteiten | UC-EDU-01 |
| | `crud.md` | Activiteit aanmaken/wijzigen (rapportagevlag, looptijd) | UC-EDU-02 |
| `15-tijdsregistratie/` | `list.md` | Tijdregistratie-overzicht; per persoon | UC-TID-01/02 |
| | `crud.md` | Check-in, check-out, correctie | UC-TID-03 t/m 05 |
| `16-rapportage/` | `uitgiftbewijzen.md` | Uitgiftbewijzen zoeken (badge "ongetekend") | UC-RAP-01 |
| | `uitgiftbewijs.md` | Bewijs bekijken/afdrukken; corrigeren/verwijderen | UC-RAP-02/03 |
| | `overzichten.md` | Zes rapportages en exports | UC-RAP-04 t/m 09 |
| `17-instellingen/` | `postcodes.md` | Postcodeoverzicht + CSV-import; read-only detail | UC-SET-01/02 |
| | `vrijwilligers.md` | Vrijwilligersoverzicht met periodefilter | UC-SET-03 |

Totaal: **64 use-cases** in 23 bestanden (plus `00-conventies.md` en deze index).

## Opbouw van elke use-case

- **Actor / Rollen / Doel** — wie mag het en waarom.
- **Scherm** — ASCII-wireframe met uitsluitend velden en knoppen; schermnamen verwijzen naar de
  prototypen in `access/webapp`.
- **Acceptatiecriteria** — Gherkin (Engelse keywords, Nederlandse inhoud), inclusief
  `Scenario Outline` met `Examples` voor alle toegestane waarden uit de enums.
- **Validaties en foutmeldingen** — veldregels met exacte meldingteksten.
- **Technische notities** — tabellen/views, afgeleiden, PrimeFaces-componenten per veld,
  autorisatie-aandachtspunten.

## Volgorde van implementatie

`00-conventies` → `01-login` → `10-personen` → `02-balie` → `11-donateurs` → `12-apparatuur` →
`03-intake-werkplaats` → `13-tickets` → `04-uitgave` → `16-rapportage` → `05-import-leergeld` →
`14-educatie` → `15-tijdsregistratie` → `17-instellingen`. De flowbestanden verwijzen door naar
de onderdeelbestanden (bv. registratie klant = UC-PER-02); de rapportage use-cases bouwen op alle
voorgaande tabellen.

## Historische mapping (eerdere sets → huidige nummering)

De vroegere domeinbestanden (`01-personen.md` … `07-rapportage.md`) en de schermset (`webapp/`,
UC-WK-01 t/m 97) zijn opgesplitst en samengevoegd in bovenstaande structuur. Oude nummers komen
in de huidige set **niet** meer voor; deze tabel is er alleen om oud materiaal te kunnen
terelezen.

| Oud | Nieuw | Opmerking |
|---|---|---|
| UC-PER-01 t/m 06 | UC-PER-01 t/m 06 | zelfde nummering, `list`/`crud` gesplitst |
| UC-PER-07 (vrijwilligers) | UC-SET-03 | verhuisd naar instellingen |
| UC-PER-08 (inloggen) | UC-AUT-01 | opgegaan in login-flow |
| UC-DON-01 t/m 04 | UC-DON-01 t/m 04 | zelfde nummering |
| UC-EQP-01 (registreren) | UC-EQP-03 | |
| UC-EQP-02 (wijzigen) | UC-EQP-04 | |
| UC-EQP-03 (statusstroom) | UC-EQP-05 | |
| UC-EQP-04 (voorraad) | UC-EQP-02 | |
| UC-EQP-05 (historie) | UC-EQP-08 | |
| UC-EQP-06/07 | UC-EQP-06/07 | ongewijzigd |
| UC-TIC-01 (intake) | UC-INT-01 t/m 04 | opgegaan in intake-flow |
| UC-TIC-02 (status) | UC-TIC-04 | |
| UC-TIC-03 (bellen) | UC-TIC-05 | |
| UC-TIC-04 (ISSUE) | UC-UIT-05 | opgegaan in uitgifteflow |
| UC-TIC-05 (statusbord) | UC-TIC-01 | |
| UC-TIC-06 (logboek) | UC-TIC-06 | met WK-52/53 samengevoegd |
| UC-TIC-07 (dossier) | UC-TIC-03 | |
| UC-TIC-08 (archiveren) | UC-TIC-07 | |
| UC-UIT-01 t/m 04 | UC-UIT-01 t/m 04 | zelfde nummering |
| UC-UIT-05 (overzicht bewijzen) | UC-RAP-01 | rapportage |
| UC-UIT-06 (bewijs corrigeren) | UC-RAP-03 | |
| UC-TID-01 (activiteiten) | UC-EDU-02 | verhuisd naar educatie |
| UC-TID-02/03 (check-in/out) | UC-TID-03/04 | |
| UC-TID-04 (aanwezigheid) | UC-TID-01 | |
| UC-TID-05 (correctie) | UC-TID-05 | |
| UC-RAP-01 t/m 06 | UC-RAP-04 t/m 09 | opgeschoven door bewijs-UC's |
| UC-WK-01/02 (login) | UC-AUT-01/02 | |
| UC-WK-10 t/m 14 (balie) | UC-BAL-01 t/m 05 | |
| UC-WK-20 (werkplaats) | UC-TIC-02 | |
| UC-WK-21 t/m 23 (intake) | UC-INT-01 t/m 04 | |
| UC-WK-30/31 (Leergeld) | UC-IMP-01/02 | |
| UC-WK-32/33 (uitgifte) | UC-UIT-01 t/m 05 | |
| UC-WK-40/41 (educatie) | UC-EDU-01/02 | |
| UC-WK-50 t/m 53 (tickets) | UC-TIC-01, UC-TIC-03, UC-TIC-06 | WK-51→TIC-03, WK-52/53→TIC-06 |
| UC-WK-60 t/m 63 (apparatuur) | UC-EQP-01, UC-EQP-04, UC-EQP-08 | WK-61→EQP-02, WK-62→EQP-04, WK-63→EQP-08 |
| UC-WK-70/71 (personen) | UC-PER-01/04 | |
| UC-WK-72/73 (tijd) | UC-TID-01/02 | |
| UC-WK-80 t/m 82 (donateurs) | UC-DON-01, UC-DON-03, UC-DON-05 | |
| UC-WK-90/91 (bewijzen) | UC-RAP-01/02 | |
| UC-WK-95/96 (instellingen) | UC-SET-01 t/m 03 | |

## Controle: domeinregels versus webapp-prototype

| Onderwerp | Domein-use-cases | Webapp-prototype | Beoordeling |
|---|---|---|---|
| Inloggen | E-mailadres + wachtwoord (oud UC-PER-08) | Vrijwilliger **dropdown** (person-id) + wachtwoord | ⚠ webapp is leidend voor het prototype (UC-AUT-01); e-mail-login is wens voor later |
| Rollen | `person_roles` met 4 authorities | Booleans `volunteer`/`admin` op person (vinkjes, disabled) | ⚠ schema kent `person_roles`; vinkjes zijn afgeleid (vrijwilliger = true, admin = ROLE_ADMIN) |
| Klant zoeken | Fonetisch (SOUNDEX), 2 tekens-minimum, archieffilter | Voornaam + achternaam, geen fonetisch, geen archieffilter | ⚠ fonetisch zoeken (UC-PER-01) is leidend; nog niet in prototype |
| Archiveren | Knop "Archiveren"/"Heropenen" zet `unregistered` | Datumkiezer "Uitgeschreven datum" op elk CRUD-scherm | ⚠ zelfde semantiek, andere bediening — datumkiezer blijft |
| Balie | Persoonsdossier met tabbladen (UC-PER-03) | 4-staps wizard | ✔ UC-BAL-01 t/m 05 |
| Reparatie-intake | Oud UC-TIC-01 met "Bewijs van inname afdrukken" | 3-steps wizard `werkplaats/new`, geen inname-bewijs | ⚠ bewijs van inname ontbreekt in prototype (UC-INT-03) |
| Ticketstatus | Statemachine, statusregels | **Geen** statusbeheer in UI; alleen `unregistered` ("Teruggave datum") | ✖ open punt — UC-TIC-04 bestaat nog niet in prototype |
| Klant bellen | Codes `[BELLEN]` e.d. | Niet aanwezig; alleen "Voeg log toe" (rich text HTML) | ✖ open punt (UC-TIC-05) |
| Ticket-log | Append-only tekst met prefix-codes | `p:textEditor` (HTML), escape=false | ⚠ logs zijn HTML in prototype; prefix-codes blijven zoekconventie |
| Details/specification | Vaste JSON-registry (conventies §4) | Beweegbare key-value-tabellen (`ui:repeat`) | ✔ registry levert de beginset sleutels; UI is dynamisch |
| Uitgifte | UC-UIT-01 t/m 04 met regels, derde betaalt, ondertekening | Eén knop "Uitgeven" op voorraad-apparaat | ⚠ prototype is kleiner; UC-UIT-01 t/m 05 leidend |
| Bewijsview | Afdrukoverzicht met vaste voetteksten | Printbaar document met `documentNumber`, `purpose`, handtekening data-URI | ⚠ kolommen bestaan niet in `proof_of_issues` — zie open punten |
| Leergeld-import | Buiten scope verklaard | Volledige 4-staps wizard | ✔ nieuw: UC-IMP-01/02 |
| Educatie/activiteiten | Reporting-vlag, archiveren | CRUD met "Vanaf"/"Tot" = `registered`/`unregistered` (looptijd) | ⚠ `unregistered` = einde looptijd, niet alleen archivering |
| Tijdregistratie | Check-in/check-out met klok | Alleen overzichten; geen in-/uitklok-knoppen | ✖ open punt (UC-TID-03/04) |
| Postcode | Auto-invul plaats bij personen | Alleen beheer (CSV-import + read-only detail) | ⚠ auto-invul nog niet in prototype |
| Rapportage | Zes uitdraaien | Alleen Uitgiftbewijzen | ✖ overige rapportages toekomstig (UC-RAP-04 t/m 09) |
| Vrijwilligers | Overzicht met start/einddatum (msaccess) | Overzicht personen met `volunteer`-vlag | ⚠ start/einddatum niet in UI (UC-SET-03) |

## Open punten (prototype versus vast schema)

1. Het uitgiftbewijsdocument leest `documentNumber` en `purpose` — kolommen die niet in
   `proof_of_issues` bestaan en niet toegevoegd mogen worden (schema vast). **Afspraak voor
   implementatie:** documentnummer = afgeleid `"U-" + id`; doel van uitgifte gaat naar
   `tickets.details` van het bijbehorende ISSUE-ticket, of — bij geen ticket — naar de eerste
   `proof_of_issue_lines.description` met prefix "Doel:".
2. `equipment`-status komt niet terug in de uitgiveflow: het prototype geeft elk voorraad-apparaat
   direct uit; de statemachine UC-EQP-05 (SUITABLE_FOR_GIFT → GIFT_ISSUED) moet de
   voorraadselectie filteren.
3. De login toont alle personen in een dropdown; zonder filter op vrijwilliger/admin kan iedere
   klant zichzelf kiezen. Implementeer: alleen personen met rol (UC-AUT-01).
4. `config/liquibase.properties` bevat hardgecodeerde database-inloggegevens (zie
   `docs/SPECIFICATIE.md` §8.6); persoonlijke data uit `access/` is in het repo aanwezig en hoort
   daar niet thuis.

## Bekende buggetjes in de prototypen (niet overnemen)

- Het tijdregistratieformulier gebruikt `#{timesheetListBean.list}` en `#{personCrudBean.save}`
  i.p.v. de eigen bean.
- Het apparaatformulier gebruikt `#{owner}` (onbeschermd bereik) en navigeert naar het
  persoonformulier zonder de dataset-prefix.
- Het ticketoverzicht: kolomkop "Binnnekomst datum" (drie n's).
- Het apparatuuroverzicht en het voorraadoverzicht: kolomkop "Desktop" i.p.v. "Categorie"; de
  voorraadlijst toont geen filter op SUITABLE_FOR_GIFT.
- De importwizard: alleen Digidromen heeft een voorvertoning; "Gezinnen" heeft geen
  stap-3-blok.
- `text-succes-color` (verkeerde klassenaam) in het importresultaat.
- De uitlogpagina is leeg; het menu logt uit via een aparte uitlogactie.
- Label "Mobile" verwijst met `for` naar `telephone`.

## Paginastructuur (geldt voor alle use-cases)

Elke pagina gebruikt de gezamenlijke paginasjabloon: menubalk (Werkplaats, Balie,
Educatie, In/Uit, Data Set, Rapportage, Instellingen, uitlog-knop), broodkruimelpad, growl,
inhoud, voettekst. Formulierpagina's laden via `f:viewParam name="id"` + `preRenderView` en hebben
standaard knoppen **Annuleren** (reset) en **Opslaan** (submit + update growl).
