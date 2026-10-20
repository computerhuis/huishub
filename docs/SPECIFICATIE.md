# Specificatie — legacy Access-database Computerhuis en het Huishub-schema

Deze documentie beschrijft:

1. de vier bronbestanden in `access/` (Microsoft Access-front-ends, handleiding, screenshots);
2. het legacy-datamodel en de bedrijfsprocessen die daarin zijn vastgelegd;
3. het doelschema in `src/main/resources/db/changelog` — **leidend en ongewijzigd**;
4. de mapping van het legacy-datamodel op dat doelschema, inclusief wat geen plaats heeft in het huidige schema.

Het schema in `src/main/resources/db/changelog` mag niet worden aangepast. Waar het legacy-model niet in het huidige schema past, is dat hier expliciet benoemd (JSON-velden of buiten scope), niet opgelost met schemawijzigingen.

---

## 1. Bronbestanden in `access/`

| Bestand | Type | Datum (uit naam) | Inhoud |
|---|---|---|---|
| `Balie_werkplaats_10-4-2025.accdb` | Access 2007+ front-end | 10 april 2025 | Front-end voor de balie/werkplaats: klantregistratie, computer- en reparatie-invoer, verkoop, facturatie, giften, licenties, Leergeld-import |
| `klant_registraties_9-1-2025.accdb` | Access 2007+ front-end | 9 januari 2025 | Front-end voor klant-/bezoekersregistratie: aanwezigheid, tijdsregistratie, cursussen, mailinglijst, rapportages ("uitdraai cijfers") |
| `Handleiding Database Humanitas Computerhuis (003).docx` | Word | — | Gebruikershandleiding werkplaatsdatabase, 9 hoofdstukken, met workflow en kleurcodes |
| `cumputerhuis_screenshots.docx` | Word | — | Uitsluitend screenshots van het Access-programma (geen extracteerbare tekst) |

### 1.1 Aard van de Access-bestanden (belangrijk)

Beide `.accdb`-bestanden zijn **front-ends zonder embedded data-tabellen**. De echte gegevenstabellen (`Tbl_Gebruikers_NAW`, `Tbl_computers`, `Tbl_Reparaties_main`, `Tbl_factuur`, …) zijn als *linked tables* gekoppeld aan een back-enddatabase die **niet** in `access/` aanwezig is. Consequenties:

- `mdb-tables` toont alleen de lokale (meestal tijdelijke) werktabellen; rijenaantallen in §1.3 betreffen die lokale tabellen.
- De velddefinities van de kern-tabellen zijn **afgeleid** uit VBA-code, query-definities en formulieropmaak in de front-ends. Kolomtypen en sleutels van de back-end konden niet letterlijk worden uitgelezen.
- Formulieren, query's en rapporten zijn wél volledig aanwezig en vormen de bron voor §3 en §4.

In beide bestanden staat een groot aantal `~sq_…`-objecten: dat zijn Access-interne recordset-caches van formulieren; geen queries. De daadwerkelijke queryaantallen: 97 (Balie/werkplaats) en 122 (klantregistraties).

### 1.2 Lokale tabellen per bestand

**Balie_werkplaats_10-4-2025.accdb** (7 tabellen):

| Tabel | Rijen | Kolommen | Rol |
|---|---|---|---|
| `Tbl_Import_Laptops_Leergeld` | 40 | Type kast, Gift van, Status, Datum Gift, Omschrijving, Fabrikant, Type, Optische apparaten, Processor, Geheugen, HDD SSD, Videokaart, Software | Importbuffer laptops Stichting Leergeld |
| `Tbl_Import_smartphone_simkaart_Leergeld` | 150 | Gift van, Type kast, Fabrikant, Model nummer, Processor, Geheugen, HDD SSD, Software, Bijzonderheden, Status, Datum Gift | Importbuffer smartphones/simkaarten Leergeld |
| `Tbl_Import_Klant_Leergeld` | 24 | Voorletters, Voornaam, Tussenvoegsel, Achternaam, Geslacht, Adres, Huisnummer, Postcode, Plaatsnaam, Geboortedatum, 1e Telefoon, Datum Inschrijving, E-mailadres, Opmerkingen | Importbuffer klantgegevens Leergeld (kolomstructuur = subverzameling van `Tbl_Gebruikers_NAW`) |
| `List_Prijslijst` | 15 | product, prijs, grootboeknummer | Losse onderdelen/prijzen (beeldschermen, RAM, HDD, cardreader, toetsenbord+mu is); alle op grootboek 81550 |
| `Temp_factuur_omschrijvingen` | 0 | Prijs, Produkt omschrijving, Grootboeknummer, Computernummer | Werkbuffer factuurregels |
| `Temp_Gebruikersnummer` | 1 | GebruikersNummer | Onthoudt laatst gezocht/ingevoerd gebruikersnummer |
| `Temp_Soundex_Search_Output` | 1 | Soundex_Search_Output, Search_string | Werkbuffer fonetische klantzoekopdracht |

**klant_registraties_9-1-2025.accdb** (13 tabellen):

| Tabel | Rijen | Kolommen | Rol |
|---|---|---|---|
| `Tbl_mailinglijst_actieve_gebruikers` | 309 | Gebruikersnummer, E-Mailadres, Voornaam, Tussenvoegsels, Achternaam, LaatsteVanActiviteit | Mailinglijst actieve gebruikers (laatste activiteit) |
| `Tbl_mailinglijst_actieve_gebruikers1` | 231 | idem, met EersteVanLaatsteVanActiviteit | Variant/oudere versie van bovenstaande |
| `temp_gebruikers_buiten_den_bosch` | 105 | Export van `Tbl_Gebruikers_NAW` inclusief Vrije-veldnamen; 29 kolommen | Werk-/exporttabel gebruikers buiten 's-Hertogenbosch |
| `Tbl_temp_gebruikers_rapporten_periode` | 495 | Gebruikersnummer, Datum, Postcode, Geboortedatum, Begin datum, Eind datum | Werkbuffer rapportageperiode |
| `Tbl_temp_postcodes` | 441 | postcodes | Werkbuffer postcodecontrole/-opzoeklijst |
| `Tbl_Uitdraai_Cijfers_temp` | 16 | id, Activiteit, Aantal, Periode | Resultaatwerkbuffer periodieke cijferuitdraai |
| `Temp_Tbl_aantal_gebruikers_per_buurt` | 14 | Aantal, Plaats, Wijk, Begin datum, Eind datum | Werkbuffer rapportage per buurt |
| `Tbl_temp` | 4 | Temp | Algemene teller/werkvariabele |
| `Tbl_Temp1` | 1 | Vrienden, Totaal, Aantal Reparaties, Aantal Verkopen, ABC-Leermee, Cursussen, Gast, Huiswerkklas, Klik & Tik, Klik & Tik Bieb, LesOpMaat, Medewerker, Medewerker Werkplaats, Vrije Inloop, Werkplaats, Workshops, WWW, Cursus, Digisterker, Klik&tik, Workshop, Vrije_Inloop, Eind datum | Stapelresultaat tellingen per activiteit (illustratief voor de rapportagetaal) |
| `Tbl_temp_gebruikers_rapporten_periode_datums` | 1 | Begin_datum, Eind_datum | Actieve rapportageperiode |
| `Temp_periode` | 1 | Begin datum, Eind datum | Actieve rapportageperiode (variant) |
| `Temp_Gebruikersnummer` | 1 | GebruikersNummer | Zelfde rol als in Balie/werkplaats |
| `Temp_Soundex_Search_Output` | 1 | Soundex_Search_Output, Search_string | Zelfde rol als in Balie/werkplaats |

### 1.3 Overige objecten (aanwezig in de front-end)

- **Balie/werkplaats**: ±97 query's (`Qwy_*`, `Qry_*`, o.a. klantzoeken met Soundex, openstaande reparaties, factuur-aanmaakquery's per scenario, prijslijst-invulquery's per betaalvorm, verkooporders, innamebewijs), 2 rapporten (`Rpt_innameBewijs` = Bewijs van Inname, `Rpt_VerkoopOpdracht` = werkopdracht/verkoopopdracht), ±40+ formulieren, 3 standaardlijsten (`List_ja_nee`, `List_Reparatie_status`, `List_Computer_status` — gekoppeld, dus niet uitgelezen), `Tbl_Licenties`, `Tbl_kas`, `grootboeknummers`, `Tbl_Vestigingen`, `Tbl_Cursussen`, `Tbl_Cursus_klant_gegevens` (gekoppeld).
- **Klantregistratie**: ±122 query's (uitdraai-cijfers per categorie, unieke bezoekers per activiteit, verloop reparaties, gebruikers per buurt/wijk/leeftijd, mailinglijstselecties, donor-rapporten), 12 rapporten (`Rpt_Donateurs`, `Rpt_Aantal_Gebruikers_per_Leeftijds_categorie`, `Rpt_Verloop_Reparaties_werkplaats`, `Rpt_Gebruikers_per_wijk`, `Rpt_Gebruikers_per_Buurt`, `Rpt_Wkrpl_bezoeken`, `Rpt_gratis_pcs`, `Rpt_gratis_smartphone`, `Rpt_gratis_simkaart`, `Rpt_Uitdraai_cijfers`, `Rpt_stichting_leergeld_Laptop`, `Rpt_stichting_leergeld_Smartphone`), formulieren rond aanwezigheid (`Frm_Aanwezigheids_registratie*`), cursussen (`Frm_Cursus_Zoeken`, `Frm_Aanwezigheids_registratie_Overzicht_cursisten`), tijdsregistratie (`Frm_Tijdsregistratie`, `subFrm_tijdsregistratie`), factuurformulieren en mailinglijst (`Frm_Mailing_lijst`).

---

## 2. Doelplatform en doelschema

### 2.1 Platform

| Onderdeel | Waarde |
|---|---|
| Build | Maven (`pom.xml`), packaging **WAR** |
| Runtime | Java 25, Spring Boot 4.1.1 |
| Web/UI | JSF via JoinFaces 6.1.1 (PrimeFaces, thema `saga`, Font Awesome, CSP aan), locale `nl` |
| Data | Spring Data JPA/Hibernate (`hbm2ddl.auto: validate`), MariaDB-driver, JDBC-timezone `Europe/Amsterdam` |
| Migratie | Liquibase 5.0.4 (`db.changelog-master.yaml` → `v1/changelog.yaml`), ook via `config/liquibase.properties` extern uitvoerbaar |
| Overig | Actuator, Security, virtual threads, multipart 10 MB, sessie-timeout 12 uur, HTTPS op poort 8443 (PKCS12-keystore), OWASP HTML-sanitizer, okapibarcode, Apache POI (Excel-export) |

Er is (nog) geen Java-code in `src/main/java`; de huidige projectomvang is pom, configuratie en het Liquibase-schema.

### 2.2 Het schema (leidend, ongewijzigd)

Volgorde van aanmaak (Liquibase `v1/changelog.yaml`): `postal_codes`, `activities`, `donors`, `persons`, `person_roles`, `donor_persons`, `equipment`, `timesheets`, `tickets`, `ticket_status`, `ticket_log`, `proof_of_issues`, `proof_of_issue_lines`, daarna de vijf views. Alle tabellen InnoDB/utf8/utf8_unicode_ci, tenzij vermeld.

**`postal_codes`** — referentietabel Nederlandse postcodes; PK `code CHAR(6)`; verplicht `url`, `city`, `municipality`, `province`; optioneel `neighbourhood`, `district`, `street`, `house_number_min`, `house_number_max`, `pobox BOOLEAN`.

**`activities`** —activiteiten (workshops, inloop, …); `id` AI, `name VARCHAR(50)` NOT NULL, `reporting BOOLEAN DEFAULT FALSE` (bepaalt meedoen in rapportages), `registered`/`unregistered` (soft delete).

**`donors`** — schenkers (personen of bedrijven); `id` AI, `name` NOT NULL, `reporting`, `email`, `mobile CHAR(10)`, `telephone CHAR(10)`, adresvelden (`postal_code`, `street`, `house_number`, `house_number_addition`, `city`), `comments`, `registered`/`unregistered`, `msaccess JSON` (overschot uit Access).

**`persons`** — klanten, vrijwilligers, medewerkers; `id` AI, `initials`, `first_name` NOT NULL, `infix`, `last_name` NOT NULL, `date_of_birth`, `email`, `mobile`, `telephone` (beide `CHAR(10)`), adres (`postal_code` FK → `postal_codes.code` **met ON DELETE CASCADE**), `comments`, `registered`/`unregistered`, `msaccess JSON`, `passhash VARCHAR(4096)` (weblogin).

**`person_roles`** — rollen per persoon; PK `(person_id, authority)`, FK person_id → persons (cascade). `authority VARCHAR(225)` (Spring Security-authoriteiten). Let op: deze tabel is als enige gedefinieerd zonder ENGINE/charset-optie.

**`donor_persons`** — koppeltabel persoon ↔ donor (bv. werknemer ↔ bedrijf); PK en dubbele FK met cascade.

**`equipment`** — computers en andere apparatuur; `id` AI; `category ENUM('DESKTOP','LAPTOP','TABLET','MOBILE','SIM','USB_STICK')`; `status ENUM('CUSTOMER_OWNED','INCOMING_GIFT','SUITABLE_FOR_GIFT','RESERVED','GIFT_ISSUED','DEMOLITION','SOLD')`; eigenaar-koppelingen `owner_id` → persons, `donor_id` → donors, `behalf_of_id` → donors (namens wie uitgegeven); `serial_number VARCHAR(512)` met **unieke index**, `manufacturer`, `model`; `specification JSON` (hardware-details); `registered`/`unregistered`.

**`timesheets`** — aanwezigheid/tijdregistratie; PK `(person_id, registered, activity_id)`; `registered DEFAULT NOW()`, `unregistered` = vertrektijd; FK's naar persons en activities met cascade.

**`tickets`** — werkopdrachten (reparatie én uitgifte/verkoop); `id` AI, `equipment_id` FK (optioneel — een ticket kan zonder apparaat), `ticket_type ENUM('REPAIR','ISSUE')`, `subject VARCHAR(255)`, `description TEXT`, `registered`/`unregistered`, `details JSON` (uitbreidingsvelden).

**`ticket_status`** — statushistorie; PK `(ticket_id, date)`; `status ENUM('OPEN','IN_PROGRESS','READY','CUSTOMER_INFORMED','CLOSED')`; `volunteer_id` → persons.

**`ticket_log`** — vrije logregels bij tickets; PK `(ticket_id, date)`, `volunteer_id`, `log TEXT`.

**`proof_of_issues`** — bewijs van uitgifte/inname; `issue_date`, `issued_by` → persons, `recipient_id` → persons, `behalf_of_id` → donors, `equipment_id` → equipment (allen FK met cascade), `signed_image LONGBLOB` (getekend bewijs als afbeelding).

**`proof_of_issue_lines`** — regels van een bewijs; PK `(proof_of_issue_id, line_id)`, `description TEXT` NOT NULL, `quantity DECIMAL(10,2)` default 1, `unit VARCHAR(50)` default `'pcs'`.

**Views** (met bedoeling en één bekende eigenaardigheid):

| View | Inhoud |
|---|---|
| `ticket_overview` | tickets + laatste status + vrijwilliger (naam) + apparaatgegevens (category/manufacturer/model) |
| `ticket_status_overview` | `ticket_status` + naam van de vastleggende vrijwilliger |
| `ticket_log_overview` | `ticket_log` + naam van de loggende vrijwilliger |
| `timesheet_overview` | timesheets + activiteitsnaam + naam van de persoon (query gebruikt `dco.persons`, dus schema-prefix `dco` is in productie verondersteld) |
| `equipment_history` | tickets + laatste status + betrokken vrijwilliger per apparaat (historieoverzicht) |

Bij de views `ticket_overview` en `equipment_history` geldt: als een ticket nog geen statusregel heeft, verdwijnt het uit de view (INNER-achtige werking van de `WHERE date = (SELECT MAX…)`); dat is een gegeven eigenschap van het onveranderlijke schema.

---

## 3. Het legacy-datamodel (afgeleid uit de front-ends)

De kern van het Access-datamodel bestaat uit de onderstaande gekoppelde tabellen. Veldlijsten zijn reconstrueerd uit VBA (`INSERT`/`UPDATE`/recordsets), query's en formulieren; niet-volledig zeker is als zodanig gemarkeerd.

### 3.1 `Tbl_Gebruikers_NAW` — klanten/vrijwilligers (kern)

Gebruikersnummer (sleutel, ook als "klantnummer" gebruikt), Voorletters, Voornaam, Tussenvoegsels, Achternaam, Geboortedatum, Geslacht, Adres, Huisnummer, Postcode, Plaatsnaam, 1e/2e/3e Telefoon, E-Mailadres, persoonlijke email, Datum Inschrijving, Opmerkingen, Organisatie, Contactpersoon, Bedrijfsnummer (FK → `Tbl_Bedrijven_NAW`), Vestigingsnummer, Vrienden Computerhuis Oost (ja/nee-vlag), Startdatum vrijwilligerswerk, einddatum vrijwilligerswerk, Afgemeld voor nieuwsbrief, Website, Postbus, Postcode postbus, Gekoppeld Gebruikersnummer (dubbele registratie koppelen), Bedrijf, soundexVoornaam/soundexAchternaam (fonetische zoekindex). De tabel is zowel klant-, vrijwilligers- als beheerderstabel in één; rolonderscheid liep via losse lijsten/rapportselecties, niet via een rollentabel.

### 3.2 `Tbl_Bedrijven_NAW` — bedrijven/instanties

BedrijfsNummer (sleutel), Bedrijfsnaam, Vestiging, Straat, Huisnummer, Postbus, Postcode, Postcode postbus, Plaats, Telefoon, E-mail, Contactpersoon(s), KvK nummer, BTW nummer, Website.

### 3.3 `Tbl_computers` — apparatuur

Computernummer (sleutel), gebruikersnummer (FK → klant/eigenaar), Type kast (lijst `Tbl_type_kast`), Type computer, Fabrikant, Model nummer, Processor, Geheugen, HDD/SSD, Videokaart, Optische apparaten, Losse geluidskaart, Cardreader, overige ingebouwde apparaten, Software, Bijzonderheden, Licentie (FK → `Tbl_Licenties`), Prijs, Status (lijst `List_Computer_status`), Datum ingeschreven, Datum Gift, Gift van (schenker), Verkocht op, Verkocht door. In verkoopmodus werd "PROJECT" als fabrikantwaarde gebruikt voor projectcomputers.

### 3.4 `Tbl_Reparaties_main` — reparaties/werkopdrachten

Reparatienummer (sleutel), Computernummer (FK → computers), Gebruikersnummer (klant), klantnr_verkoop (afwijkend factuuradres bij verkoop), datum Inname, Probleem, Samenvatting Reparatie, Medewerker, Aangenomen door, Backup (keuzelijst), Bijgeleverde accessoires, Account wachtwoord, Email gegevens, Tafel (werktafel), Status (lijst `List_Reparatie_status`), Datum Gebeld1/2/3 met Reactie Gebeld1/2/3 (klantbelhistorie), Datum Gebeld Handeling/Reactie Gebeld Handeling, Gebeld Counter, Datum opgelost, Verkoop_order.

### 3.5 Overige kern-tabellen

| Tabel | Velden (afgeleid) | Rol |
|---|---|---|
| `Tbl_Reparaties_uitgediept` | reparatienummer, computernummer, datum | Log "reparatie verdiept/opgevolgd"; elke statusactie op het statusbord voegt hier een regel toe |
| `Tbl_factuur` | Factuurnummer, datum, klantnr, kas_of_bank, Datum Betaald, bataald (ja/nee), Bedrijfsnummer, afschift nummer | Factuurkop |
| `Tbl_factuur_omschrijvingen` | Factuurnummer, Product omschrijving, Grootboeknummer, Prijs, Computernummer | Factuurregels |
| `grootboeknummers` | grootboeknummer, uitgave post | Grootboekregister; werkplaatsfacturen gebruiken 81500–81599, 81050 en 44918 |
| `Tbl_Prijslijst_werkplaats` | product omschijving, product prijs, Grootboeknummers | Verkoopprijzen apparatuur en onderdelen |
| `Tbl_Licenties` | Omschrijving, Licentie prijs (bv. "Windows 7 licentie", grootboek 81550) | Windows-licenties; veld "Licentie code" bewust leeg |
| `Tbl_kas` | kas/bank-mutaties (kas_of_bank in factuurformulier) | Kasadministratie |
| `Tbl_Tijdsregistratie` | Gebruikersnummer, Datum, Tijd Binnengekomen, Tijd weggegaan/Vertrokken, Activiteit | Aanwezigheidsregistratie bezoekers/vrijwilligers |
| `Tbl_Activiteiten` | Activiteiten | Activiteitenlijst; waarden o.a. ABC-Leermee, Cursussen, Digisterker, Gast, Huiswerkklas, Klik & Tik, Klik & Tik Bieb, LesOpMaat, Medewerker, Medewerker Werkplaats, Vrije Inloop, Werkplaats, Workshops, WWW |
| `Tbl_activiteiten_van_gebruikers` | gebruikersnummer, activiteit, datums | Deelname per gebruiker per activiteit |
| `Tbl_Cursussen` | Cursus nummer, Naam cursus, Cursus/Workshop, 1e datum, Tijdstip cursus, Cursusleider, Cursusassistent(en) | Cursusadministratie |
| `Tbl_Cursus_klant_gegevens` | klantnr, cursus | Inschrijvingen |
| `Tbl_Klant_tevredenheidsonderzoek` | Onderzoeksnr, Gebruikersnummer, Computernummer, Reparatienummer, Leverings_Datum, Soort_levering, Mening_klant, Datum_gebeld, Gebeld | Tevredenheidsonderzoek (handleiding: "wordt voorlopig niet meer gebruikt") |
| `Tbl_Postcodes` | Postcode, Plaats, Gemeente, Provincie, Regio, Wijk, Buurt | Opzoektabel adresgegevens → rapportage wijk/buurt |
| `Tbl_Wijken`, `Tbl_Buurten`, `Tbl_Vestigingen` | wijk-/buurt-/vestigingslijsten | Referentielijsten |
| `Tbl_Geslacht`, `Tbl_ja_nee`, `Tbl_type_kast` | Geslacht; ja/nee; Type kast | Korte keuzelijsten |
| `List_Reparatie_status`, `List_Computer_status` | statuswaarden reparaties resp. apparatuur | Zie §5 |

---

## 4. Bedrijfsprocessen (uit de handleiding en VBA)

### 4.1 Reparaties ("Ingekomen klant-PC ter reparatie")

1. Klant zoeken (op achternaam/voornaam, met Soundex-fonetic fallback); controleer telefoonnummer. Niet gevonden → nieuwe gebruiker inschrijven op `Tbl_Gebruikers_NAW`, waarna direct de computerinvoer opent.
2. Controleer via "Computers weergeven" of de computer al bekend is; bestaande computer hergebruiken, anders nieuw invoeren (minimaal Type kast, Fabrikant, Model nummer; altijd Probleem, Backup en Bijgeleverde accessoires invullen).
3. "Reparatie invoeren": er wordt een **Bewijs van Inname** in 2-voud geprint; klant én baliemedewerker tekenen beide exemplaren. Klantexemplaar ("Bewaar dit bewijs goed…") gaat mee; kopie blijft bij de computer. Op computer/adapter/randapparatuur komen stickers met klant- en computernummer.
4. Statusstroom (met kleurcodes, zie §5): binnengekomen (rood) → in behandeling (groen, met naam behandelaar en tafelnummer) → gereed (oranje) → klant gebeld, gereed (zwart) → opgehaald.
5. Klantcontact: "Klant bellen: over reparatie" legt afspraak/voicemail vast in Datum/Reactie Gebeld1–3 (maximaal drie belmomenten, bij een nieuw belmoment schuiven de eerdere door en wordt de verste gewist). Status gaat naar "Klant gebeld, wachtende" (blauw) zolang actie van de klant verwacht wordt; zodra de klant levert, weer "In behandeling".
6. Bij factuur maken wordt de reparatie automatisch op "Opgehaald" gezet en afgesloten (VBA-waarschuwing: daarna niet meer te wijzigen); `Datum opgelost` wordt gevuld.

### 4.2 Schenking van computers aan het computerhuis

- **2A**: klant wil dure reparatie niet meer → vanuit status "Klant gebeld, wachtende" via knop "De klant hoeft de computer niet meer te hebben"; computer gaat naar "Gekregen Computers".
- **2B**: reeds geregistreerde computer zonder reparatie → knop "De klant geeft deze computer aan het computerhuis"; geen reparatie ingevoerd.
- **2C**: bedrijf/persoon doneert computer(s) — procedure onvolledig gedocumenteerd (zie §8).
- **Invoer giften** (hoofdstuk 3): bulk-invoer van giften door bedrijf of gemeente: bedrijf invoeren (`Tbl_Bedrijven_NAW`), daarna computers invoeren met "Datum Gift" en "Gift van". Handleiding meldt hier storingen.

### 4.3 Voorraad en status "Gekregen Computers"

Statussen apparatuur: Geschikt voor verkoop → schoonmaken, basissoftware/image, prijskaart printen, in rek "Aanbiedingen" (nog geen activatie/licentie); Gereserveerd → geselecteerd voor een project, niet verkopen of slopen; Klaar voor verkoop → alles inclusief updates én geactiveerde Windows-licentie; Onderdelen computer → niet verkoopbaar maar bruikbare onderdelen; Sloop → niet meer te repareren/te oud, eigendom van het Computerhuis; bruikbare onderdelen mogen verwijderd worden.

### 4.4 Verkoop en facturatie

- Verkooplijst met kleurcodes (groen = klaar voor verkoop, blauw = geschikt voor verkoop, rood = gereserveerd).
- "Verkoop factuur maken" opent `Frm_Factuur`; regels worden via query's (`Qwy_Computer_verkoop_input_prijslijst`, `Qwy_Backup_verkoop_input_prijslijst`, `Qwy_windows_installatie_verkoop_input_prijslijst`, `Qwy_ubuntu_installatie_verkoop_input_prijslijst`, `Qwy_New_Factuur_Losse_onderdelen`) vanuit prijslijst en grootboek gegenereerd. Factuurnummer volgt uit `Qwy_Highest_Factuurnummer` (DMax op `Tbl_Factuur`).
- Factuur wordt in twee richtingen gebruikt: per klant (`klantnr`) of per bedrijf (`Bedrijfsnummer`, met aparte bedrijvenformulieren en PDF-afdruk, ook met postbusadres).
- Reparatiesoorten in de factuurlogica: grote reparatie, kleine reparatie, onderzoekskosten — elk met eigen aanmaakquery's en tijdsregistratie-invulquery's.
- Licentiebeheer: lijst `Tbl_Licenties` met Windows-versies en licentiekosten; licentiecode wordt niet in de database bewaard.

### 4.5 Klantbelangstelling en onderzoek

Teledenvredenheidsonderzoek per levering/reparatie: mening klant, gebeld ja/nee, datum gebeld; rapport "Reparatie/verkoop rapport inzien". Handleiding: wordt voorlopig niet meer gebruikt.

### 4.6 Afwijkende procedures

- **9.1 Derde betaalt voor cliënt**: instelling stuurt garantie-e-mail; factuur op naam van cliënt met kopie naar kantoor; kantoor stuurt factuur naar de derde; na betaling melding aan werkplaats; bij ophalen krijgt cliënt de factuur mee.
- **9.2 Project "Iedereen surft mee op www en heeft een werkende PC"**: projectcomputer (bv. HP Compaq dc7800) uit voorraad (rood/gereserveerd, "PROJECT" als fabrikant) → verkoop aan betrokken gebruiker → werkopdracht "Klaar maken voor verkoop" plus accessoires ("Eizo-monitor, toetsenbord en muis") → controle → uitgifte. Factuurregels: PC € 80,00 incl. licentie, monitor/toetsenbord/muis € 15,00 (grootboek 81550), subsidiepost project −€ 95,00, en "Getekend voor ontvangst" als paraafregel.

### 4.7 Aanwezigheid, cursussen en rapportage (klantregistratie)

- Aanwezigheidsregistratie: klant zoeken → activiteit kiezen → binnenkomsttijd vastleggen; vertrek registreren met "TuidUit/Vertrokken". Cursisten worden via cursusselectie in batch aangemeld (`Qwy_Aanwezigheids_Registratie_cursisten_aanmelden`).
- Periode-uitdraai "cijfers" (`Frm_Uitdraai_Cijfers`, `Qwy_Uitdraai_cijfers_*`) levert o.a.: totaal bezoeken, unieke bezoekers (per activiteit), aantal medewerkers, aantal vrienden Computerhuis Oost, gratis pc's/smartphones/simkaarten, Leergeld-laptops en -smartphones, werkplaatsbezoeken, reparaties/verkopen, cursisten en workshops, gebruikers per buurt/wijk en leeftijdscategorie, verloop van reparaties (binnen 1 week / 2 weken / 3 weken / langer).
- Uitdraairesultaten worden in `Tbl_Uitdraai_Cijfers_temp` neergezet en via rapporten uitgedraaid; periodestapeling via `Temp_periode`/`Tbl_temp*`.

---

## 5. Statuswaarden en kleurcodes

### Reparaties (`List_Reparatie_status`, `Tbl_Reparaties_main.Status`)

| Status | Kleur | Betekenis |
|---|---|---|
| Binnengekomen | rood | Computer aangenomen, nog niet behandeld |
| In behandeling | groen | Behandelaar is bezig (naam + tafel vastgelegd) |
| Gereed | oranje | Reparatie klaar gemeld door behandelaar |
| Klant gebeld, wachtende | blauw | Wachten op klant (onderdelen/afspraak) |
| Klant gebeld, gereed | zwart | Klant geïnformeerd dat computer klaar is |
| Opgehaald | — | Computer opgehaald; vastgelegd bij factuur maken; afgesloten |

Daarnaast komen in code voor: "Klant gebeld" (tussentijdse belstatus), "Verkocht", "Handeling klant", "Installatie Windows 7".

### Apparatuur (`List_Computer_status`, `Tbl_computers.Status`)

Geschikt voor verkoop (blauw in verkooplijst) · Gereserveerd (rood) · Klaar voor verkoop (groen) · Onderdelen computer · Sloop · Binnengekomen gift · Verkocht.

---

## 6. Mapping legacy → doelschema

### 6.1 Personen, bedrijven en rollen

| Access | Huishub | Toelichting |
|---|---|---|
| `Tbl_Gebruikers_NAW`.Voorletters | `persons.initials` | |
| Voornaam / Tussenvoegsels / Achternaam | `first_name` / `infix` / `last_name` | first_name en last_name NOT NULL: lege namen opschonen vóór migratie |
| Geboortedatum | `date_of_birth` | |
| E-Mailadres | `email` | |
| 1e Telefoon / 2e Telefoon | `telephone` / `mobile` | Beide `CHAR(10)`: 3e telefoon past niet → `msaccess` |
| Adres + Huisnummer | `street` + `house_number` (+ `house_number_addition`) | Huisnummer-a/B splitsen naar toevoeging |
| Postcode + Plaatsnaam | `postal_code` (FK → `postal_codes`) + `city` | Postcode moet in `postal_codes` bestaan (anders FK-fout); niet-bestaande postcodes eerst toevoegen |
| Datum Inschrijving | `registered` | |
| Opmerkingen | `comments` | |
| Geslacht, Vrienden Computerhuis Oost, 3e telefoon, persoonlijke email, Organisatie, Contactpersoon, Start-/einddatum vrijwilligerswerk, Afgemeld voor nieuwsbrief, Website, Postbus, Postcode postbus, Gekoppeld Gebruikersnummer | `persons.msaccess` (JSON) | Overschot uit Access; expliciet buiten het relationele model |
| Rollen (Medewerker, Medewerker Werkplaats, Cursusleider, proefperiode, …) | `person_roles.authority` | Legacy had geen rollentabel; rollen afleiden uit rapportselecties/activiteiten |
| — | `persons.passhash` | Nieuw: weblogin; leeg bij migratie |
| `Tbl_Bedrijven_NAW` | `donors` | Bedrijfsnaam → `name`; adres/vestiging → donorvelden; KvK/BTW/website/contact → `donors.msaccess` |
| `Tbl_Gebruikers_NAW`.Bedrijfsnummer | `donor_persons` | Koppeling persoon ↔ bedrijf; Vestigingsnummer → `donors.msaccess` |
| `Tbl_Geslacht` | — | Keuzelijst; geen entiteit in het doelschema |

### 6.2 Apparatuur

| Access | Huishub | Toelichting |
|---|---|---|
| Computernummer | — (niet als kolom) | Legacy-nummer bewaren in `equipment.specification` (JSON) of koppeling via `serial_number`; `id` wordt de nieuwe sleutel |
| gebruikersnummer (eigenaar) | `owner_id` → persons | |
| Gift van (bedrijf/persoon) | `donor_id` → donors | |
| Type kast | `category` | Mapping: desktop/kast → DESKTOP, laptop → LAPTOP, tablet → TABLET, smartphone → MOBILE, simkaart → SIM, USB-stick → USB_STICK |
| Status | `status` | Binnengekomen gift → INCOMING_GIFT; Geschikt voor verkoop → SUITABLE_FOR_GIFT; Gereserveerd → RESERVED; Klaar voor verkoop/Verkocht → SOLD of GIFT_ISSUED (keuze per migratiebeleid: GIFT_ISSUED voor uitgifte tegen betaling); Onderdelen computer/Sloop → DEMOLITION; klanteigen zonder schenking → CUSTOMER_OWNED |
| Fabrikant / Model nummer | `manufacturer` / `model` | |
| — | `serial_number` | Access had geen serienummer; unieke index betekent: alleen invullen als werkelijk bekend, anders NULL (MySQL staat meerdere NULL toe) |
| Processor, Geheugen, HDD/SSD, Videokaart, Optische apparaten, Losse geluidskaart, Cardreader, overige ingebouwde apparaten, Software, Bijzonderheden, Licentie, Prijs, Type computer | `specification` (JSON) | Alle hardware-/optiegegevens |
| Datum ingeschreven / Datum Gift | `registered` | |
| Verkocht op | `unregistered` | Datum van uitgifte/verkoop |
| Verkocht door | `specification` of `ticket_log` | Wie uitgaf; zie ook `proof_of_issues.issued_by` |

### 6.3 Reparaties en werkopdrachten

| Access | Huishub | Toelichting |
|---|---|---|
| `Tbl_Reparaties_main` (één rij per reparatie) | `tickets` + `ticket_status` + `ticket_log` | Normalisatie: het Access-model bewaarde alleen de **huidige** status; Huishub legt status- en loghistorie evenementsgewijs vast |
| Reparatienummer | — (niet als kolom) | Legacy-nummer in `tickets.details` (JSON) bewaren voor traceerbaarheid |
| Computernummer | `equipment_id` | |
| Gebruikersnummer (klant) | via `equipment.owner_id`; bij losse tickets → `ticket_log`/`details` | `tickets` kent geen klant-FK; de klant hangt via de apparatuur of via `details` |
| ticket_type | `ticket_type` | Reparatie → REPAIR; verkoop-/werkopdracht → ISSUE |
| Probleem | `subject` + `description` | |
| Samenvatting Reparatie | `ticket_log.log` (laatste regel) of `details` | Verplicht vóór facturatie in Access |
| datum Inname | `registered` | |
| Status (huidige waarde) | nieuwste rij in `ticket_status` | OPEN (binnengekomen) → IN_PROGRESS (in behandeling) → READY (gereed) → CUSTOMER_INFORMED (klant gebeld, gereed) → CLOSED (opgehaald); "Klant gebeld, wachtende" heeft geen enum-equivalent → IN_PROGRESS met `ticket_log`-regel, óf CUSTOMER_INFORMED (keuze moet worden vastgelegd; schema is leidend) |
| Medewerker / Aangenomen door | `ticket_status.volunteer_id` / `ticket_log.volunteer_id` | |
| Datum/Reactie Gebeld1–3, Gebeld Counter | `ticket_status` + `ticket_log` | Elk belmoment = status-/logregel met datum; tekst in `log` |
| Datum opgelost | statusovergang CLOSED | |
| Backup, Bijgeleverde accessoires, Tafel, Account wachtwoord, Email gegevens | `tickets.details` (JSON) | |
| `Tbl_Reparaties_uitgediept` | `ticket_log` | Elke opvolgactie = logregel met datum |
| Status kleurcodes | — (weergave) | Kleur is presentatielogica; status komt uit `ticket_status` |

### 6.4 Uitgifte, bewijzen en financiering

| Access | Huishub | Toelichting |
|---|---|---|
| Ontvangstbewijs/Bewijs van Inname (print) | `proof_of_issues` + `proof_of_issue_lines` | `issue_date` = datum inname/uitgifte; `issued_by` = baliemedewerker (persons); `recipient_id` = klant (persons); `equipment_id` = apparaat |
| Derde die betaalt (§4.6 9.1) | `behalf_of_id` → donors | Instantie namens wie wordt uitgegeven |
| Ondertekend papieren bewijs | `signed_image` (LONGBLOB) | Scanning van het getekende formulier; alternatief: digitaal tekenen |
| Prijslijstregels op het bewijs/factuur | `proof_of_issue_lines` | description/quantity/unit per regel |
| `Tbl_Licenties` | `equipment.specification` of regel op `proof_of_issue_lines` | Licenties zijn geen entiteit in het doelschema |

### 6.5 Aanwezigheid en activiteiten

| Access | Huishub | Toelichting |
|---|---|---|
| `Tbl_Tijdsregistratie` (Tijd Binnengekomen / Tijd weggegaan) | `timesheets` | `registered` = binnenkomst (PK-deel), `unregistered` = vertrek |
| Activiteit | `activity_id` → `activities` | Activiteitenlijst importeren; `reporting` = TRUE voor activiteiten die in de uitdraaicijfers meetellen |
| `Tbl_Activiteiten` | `activities.name` | |
| `Tbl_activiteiten_van_gebruikers` | `activities`-koppeling via `timesheets` | Deelnamehistorie volgt uit timesheets |
| `Tbl_Cursussen`, `Tbl_Cursus_klant_gegevens` | — (buiten scope) | Cursusadministratie heeft geen plaats in het huidige schema |
| `Tbl_Postcodes` | `postal_codes` | Postcode, Plaats→city, Gemeente→municipality, Provincie→province, Buurt→neighbourhood, Wijk→district; Regio/street/huisnummerranges→JSON-vrije velden of `url` verplicht (minimaal opvullen) |

### 6.6 Zonder tegenhanger in het huidige schema

Het huidige schema kent **geen** financiële module. De volgende legacy-functionaliteit valt buiten het schema en is als zodanig bepaald (schema mag niet wijzigen):

- `Tbl_factuur`, `Tbl_factuur_omschrijvingen`, `grootboeknummers`, `Tbl_kas`, `Tbl_Prijslijst_werkplaats` — facturatie, grootboek en kas; hoogstens te benaderen via `proof_of_issues`/`proof_of_issue_lines` (bewijs met regels en bedragen), niet als factuuradministratie.
- Cursus- en workshopadministratie (`Tbl_Cursussen`, `Tbl_Cursus_klant_gegevens`).
- Tevredenheidsonderzoek (`Tbl_Klant_tevredenheidsonderzoek`) — handleiding meldt al dat het niet meer wordt gebruikt.
- Mailinglijsten (`Tbl_mailinglijst_actieve_gebruikers`) — af te leiden uit persons + timesheets.
- Rapportage "uitdraai cijfers" — herbouw op views/queries bovenop `timesheets`, `tickets`, `equipment`, `activities`; de Access-werktabellen hebben geen tegenhanger nodig.
- Leergeld-importbuffers — eenmalige import; kolommen landen in `equipment` (+ `specification`) en `persons` (+ `msaccess`).

### 6.7 Migratie-aandachtspunten (geen wijzigingen aan het schema)

1. `persons.postal_code` heeft FK naar `postal_codes.code` met **ON DELETE CASCADE**: een onbekende of onjuiste postcode blokkeert de insert; een verwijdering in `postal_codes` zou personen verwijderen. Eerst `postal_codes` volledig vullen (bijv. uit `Tbl_Postcodes`).
2. `equipment.serial_number` is uniek: Leergeld-import bevat waarschijnlijk geen serienummers; meervoudige lege waarden zijn toegestaan, dubbele gevulde waarden niet.
3. `timesheets` heeft `(person_id, registered, activity_id)` als sleutel: twee activiteiten van dezelfde persoon mogen niet op exact hetzelfde seconden tijdstip vallen; leg tijdstippen met seconden vast.
4. `ticket_status`-enum kent geen "wacht op klant": leg vast hoe "Klant gebeld, wachtende" wordt vertaald (zie §6.3).
5. Alle `unregistered`-kolommen zijn soft-delete: Access kende dit niet; vul `unregistered` alleen bij afsluiten/verhuizen.
6. `msaccess JSON`-velden zijn de aangewezen plek voor legacy-overgangsvelden; leg de sleutelnamen van die JSON eenmaal vast en houd ze consistent tussen persons, donors en equipment.
7. Views verwachten schema `dco` (`timesheet_overview`): database moet `dco` heten of de view moet bij een eventuele herimplementatie worden aangepast (schema zelf niet wijzigen).

---

## 7. Rapportagespecificatie (klantregistratie, als functionele eis)

De periodieke uitdraai levert per gekozen periode (begin/eind-datum):

- Totaal aantal bezoeken en aantal unieke bezoekers, ook per activiteit.
- Aantal medewerkers/vrijwilligers (op basis van start-/einddatum vrijwilligerswerk).
- Aantal "vrienden Computerhuis Oost".
- Aantal gratis pc's, gratis smartphones, gratis simkaarten.
- Aantal Leergeld-laptops en Leergeld-smartphones.
- Aantal werkplaatsbezoeken en werkplaats-ondersteuning.
- Aantal reparaties en verkopen, verloop van reparaties (binnen 1 week / 2 weken / 3 weken / langer dan 3 weken).
- Cursisten en workshops per activiteit (Klik & Tik, LesOpMaat, Digisterker, Vrije Inloop, etc.).
- Gebruikers per buurt en per wijk (minimaal 5 per buurt in de weergave), gebruikers buiten 's-Hertogenbosch, leeftijdscategorieën (<40, 40–60, >60).
- Donateurs per jaar.

In Huishub is dit te realiseren op `timesheets` + `activities` (bezoeken, activiteiten, unieke bezoekers), `persons` (vriendenvlag via `msaccess`, geboortedatum, postcode→buurt/wijk via `postal_codes`), `tickets` + `ticket_status` (reparaties/verloop) en `equipment` (uitgiften). Er is geen schema-uitbreiding nodig; rapportage hoort in de applicatielaag.

---

## 8. Openstaande punten uit de bronnen

Uit de handleiding en de front-ends zelf:

1. Hoofdstuk 2A: "VERDERE INVULLING MET MARC OVERLEGGEN"; 2C: "VOOR VERVOLG MARC VRAGEN" — procedures 2A/2C zijn nooit definitief vastgelegd.
2. Hoofdstuk 3 (Invoer giften): "Werking is niet duidelijk"; opslaan van bedrijfsgegevens en contactpersonen werkt niet betrouwbaar; "Computers invoeren" geeft een foutmelding.
3. Hoofdstuk 8 (Facturen inzien): wens om computernummer als extra kolom toe te voegen.
4. Handleiding verwijst naar Windows 7-imageprocedure en HP Compaq dc7800 — procedures en hardware uit 2013–2015; actualiseren bij herimplementatie.
5. De twee Access-bestanden bevatten **echte persoonsgegevens** (namen, adressen, e-mail, telefoon). Bewaren en verwerken valt onder de AVG; de bestanden horen niet onbeveiligd in een git-repository en het is de vraag of ze daar überhaupt thuishoren.

Technische bevindingen bij het project zelf (buiten `access/`, wel relevant):

6. `config/liquibase.properties` bevat een **hardgecodeerd wachtwoord** voor de database op 192.168.99.11; dit hoort in een secret/omgevingsvariabele en het huidige wachtwoord moet als gecompromitteerd worden beschouwd.
7. `README.md` noemt `org.codeberg.computerhuis:portaal` (Codeberg) terwijl `pom.xml` naar GitHub (`computerhuis/huishub`) verwijst; ook groepsid/naam (huishub vs. portaal) lopen uiteen.
8. `equipment`, `tickets`, `donors` en `persons` hebben allen `msaccess JSON`; leg vast welke sleutels daarin worden gebruikt vóór de eerste migratierun.

---

## 9. Bijlagen

### 9.1 Objecttellingen

| Object | Balie_werkplaats | klant_registraties |
|---|---|---|
| Lokale tabellen | 7 | 13 |
| Echte query's | 97 | 122 |
| Rapporten | 2 | 12 |
| Formulieren (benaderd via `~sq_`-caches en VBA) | ±45 | ±35 |
| Gekoppelde (back-end) tabellen | ≥15 | ≥12 |

### 9.2 Verwijzingen in dit document

- Doelschema: `src/main/resources/db/changelog/db.changelog-master.yaml`, `v1/changelog.yaml`, `v1/schema/*.sql`, `v1/view/*.sql`.
- Platform: `pom.xml`, `src/main/resources/application.yml`, `application-development.yml`, `config/liquibase.properties`.
- Bronmateriaal: `access/Balie_werkplaats_10-4-2025.accdb`, `access/klant_registraties_9-1-2025.accdb`, `access/Handleiding Database Humanitas Computerhuis (003).docx`, `access/cumputerhuis_screenshots.docx`.
- Analysetechniek: veldlijsten en statuswaarden zijn afgeleid uit VBA- en queryteksten in de front-ends (back-end niet beschikbaar); formuleer bij twijfel de exacte definitie in Access na voordat migratiecode erop wordt gebouwd.
