package com.github.computerhuis.huishub.web.workshop;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentCategoryType;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentStatus;
import com.github.computerhuis.huishub.cdm.enumeration.TicketType;
import com.github.computerhuis.huishub.cdm.model.Equipment;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.model.Ticket;
import com.github.computerhuis.huishub.cdm.repository.EquipmentRepository;
import com.github.computerhuis.huishub.cdm.repository.PersonRepository;
import com.github.computerhuis.huishub.cdm.repository.TicketRepository;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
@Scope("view")
public class NewWizardBean {

    private static final String SEARCH_STRING = "%%%s%%";
    private static final List<EquipmentCategoryType> EQUIPMENT_CATEGORIES = Arrays.asList(EquipmentCategoryType.values());
    private static final List<TicketType> TICKET_TYPES = Arrays.asList(TicketType.values());
    private static final List<String> EQUIPMENT_SPECIFICATION_KEYS = Arrays.asList(
        "Processor", "Geheugen", "Hdd/Ssd", "Videokaart", "Optische apparaten", "Software");
    private static final List<String> TICKET_DETAIL_KEYS = Arrays.asList(
        "Backup", "Bijgeleverde accessoires");

    private final PersonRepository personRepository;
    private final EquipmentRepository equipmentRepository;
    private final TicketRepository ticketRepository;

    private final List<String> steps = Arrays.asList(
        "Klant gegevens",
        "Apparaat gegevens",
        "Omschrijf werkzaamheden");

    private int currentStep = 1;

    private String voornaam;
    private String achternaam;
    private List<Person> resultaten;

    private Person person;
    private Equipment equipment;
    private Ticket ticket;

    @PostConstruct
    public void init() {
        equipment = new Equipment();
        equipment.setCategory(EquipmentCategoryType.DESKTOP);
        equipment.setStatus(EquipmentStatus.CUSTOMER_OWNED);
        equipment.setSpecification(new LinkedHashMap<>());
        for (val key : EQUIPMENT_SPECIFICATION_KEYS) {
            equipment.getSpecification().put(key, null);
        }

        ticket = new Ticket();
        ticket.setTicketType(TicketType.REPAIR);
        ticket.setDetails(new LinkedHashMap<>());
        for (val key : TICKET_DETAIL_KEYS) {
            ticket.getDetails().put(key, null);
        }
    }

    public List<EquipmentCategoryType> getEquipmentCategories() {
        return EQUIPMENT_CATEGORIES;
    }

    public List<TicketType> getTicketTypes() {
        return TICKET_TYPES;
    }

    public List<String> getEquipmentSpecificationKeys() {
        return EQUIPMENT_SPECIFICATION_KEYS;
    }

    public List<String> getTicketDetailKeys() {
        return TICKET_DETAIL_KEYS;
    }

    // --[ stap 1 - klant ]---------------------------------------------------------------------------------------------
    public void zoek() {
        val searchFirst = isNotBlank(voornaam) ? SEARCH_STRING.formatted(voornaam.trim()) : null;
        val searchLast = isNotBlank(achternaam) ? SEARCH_STRING.formatted(achternaam.trim()) : null;

        if (searchFirst != null && searchLast != null) {
            resultaten = personRepository.findByFirstNameLikeOrLastNameLike(searchFirst, searchLast);
        } else if (searchLast != null) {
            resultaten = personRepository.findByLastNameLike(searchLast);
        } else if (searchFirst != null) {
            resultaten = personRepository.findByFirstNameLike(searchFirst);
        } else {
            resultaten = null;
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Zoeken", "Vul een voor- of achternaam in"));
        }
    }

    public void selecteer(final Person selected) {
        person = selected;
    }

    public void nieuweKlant() {
        person = new Person();
    }

    // --[ navigatie ]--------------------------------------------------------------------------------------------------
    public void volgende() {
        currentStep = Math.min(steps.size(), currentStep + 1);
    }

    public void vorige() {
        currentStep = Math.max(1, currentStep - 1);
    }

    public String annuleren() {
        return "/werkplaats/list?faces-redirect=true";
    }

    // --[ stap 3 - opslaan ]-------------------------------------------------------------------------------------------
    @Transactional
    public String opslaan() {
        if (person.getId() == null) {
            person.setRegistered(LocalDateTime.now());
        }
        personRepository.saveAndFlush(person);

        equipment.setOwnerId(person.getId());
        if (equipment.getRegistered() == null) {
            equipment.setRegistered(LocalDateTime.now());
        }
        equipmentRepository.saveAndFlush(equipment);

        ticket.setEquipmentId(equipment.getId());
        if (ticket.getRegistered() == null) {
            ticket.setRegistered(LocalDateTime.now());
        }
        ticketRepository.saveAndFlush(ticket);

        log.info("Intake opgeslagen: person={}, equipment={}, ticket={}", person.getId(), equipment.getId(), ticket.getId());
        return "/werkplaats/list?faces-redirect=true";
    }
}
