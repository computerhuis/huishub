package com.github.computerhuis.huishub.web.balie;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.github.computerhuis.huishub.cdm.model.Equipment;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.repository.EquipmentRepository;
import com.github.computerhuis.huishub.cdm.repository.PersonRepository;
import com.github.computerhuis.huishub.cdm.repository.TicketOverviewRepository;
import com.github.computerhuis.huishub.cdm.view.TicketOverview;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
@Scope("view")
public class BalieBean {

    private static final String SEARCH_STRING = "%%%s%%";

    private final List<String> steps = Arrays.asList(
            "Klant zoeken",
            "Klantgegevens",
            "Apparatuur",
            "Tickets");

    private final PersonRepository personRepository;
    private final EquipmentRepository equipmentRepository;
    private final TicketOverviewRepository ticketOverviewRepository;

    private int currentStep = 1;

    private String voornaam;
    private String achternaam;
    private List<Person> resultaten;

    private Person person;
    private List<Equipment> equipment;
    private List<TicketOverview> tickets;

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
        equipment = equipmentRepository.findByOwnerId(person.getId());
        val equipmentIds = equipment.stream().map(Equipment::getId).toList();
        tickets = equipmentIds.isEmpty()
            ? Collections.emptyList()
            : ticketOverviewRepository.findByEquipmentIdIn(equipmentIds);
    }

    // --[ navigatie ]--------------------------------------------------------------------------------------------------
    public void volgende() {
        currentStep = Math.min(steps.size(), currentStep + 1);
    }

    public void vorige() {
        currentStep = Math.max(1, currentStep - 1);
    }

    public String afronden() {
        return "/balie?faces-redirect=true";
    }
}
