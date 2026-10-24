package com.github.computerhuis.huishub.web.inout;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentStatus;
import com.github.computerhuis.huishub.cdm.model.Equipment;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.repository.EquipmentRepository;
import com.github.computerhuis.huishub.cdm.repository.PersonRepository;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
@Scope("view")
public class OutputBean {

    private static final String SEARCH_STRING = "%%%s%%";

    private final EquipmentRepository equipmentRepository;
    private final PersonRepository personRepository;

    private List<Equipment> stock;

    private String voornaam;
    private String achternaam;
    private List<Person> resultaten;

    private Person person;
    private Equipment equipment;

    public void fetch() {
        stock = equipmentRepository.findByStatus(EquipmentStatus.SUITABLE_FOR_GIFT);
    }

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

    public void selecteerEquipment(final Equipment selected) {
        equipment = selected;
    }

    @Transactional
    public void uitgeven() {
        if (person == null || equipment == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Uitgave", "Kies eerst een klant en een apparaat"));
            return;
        }

        equipment.setOwnerId(person.getId());
        equipment.setStatus(EquipmentStatus.GIFT_ISSUED);
        if (equipment.getUnregistered() == null) {
            equipment.setUnregistered(LocalDateTime.now());
        }
        equipmentRepository.saveAndFlush(equipment);

        log.info("Uitgave: equipment={}, person={}", equipment.getId(), person.getId());
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Uitgave",
                "Apparaat %d is uitgegeven aan %s".formatted(equipment.getId(), person.getFullname())));

        equipment = null;
        person = null;
        resultaten = null;
        fetch();
    }
}
