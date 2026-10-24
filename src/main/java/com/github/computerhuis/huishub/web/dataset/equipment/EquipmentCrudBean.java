package com.github.computerhuis.huishub.web.dataset.equipment;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentCategoryType;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentStatus;
import com.github.computerhuis.huishub.cdm.model.Donor;
import com.github.computerhuis.huishub.cdm.model.Equipment;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.repository.DonorRepository;
import com.github.computerhuis.huishub.cdm.repository.EquipmentRepository;
import com.github.computerhuis.huishub.cdm.repository.PersonRepository;
import org.primefaces.event.RowEditEvent;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.github.computerhuis.huishub.web.UtilBean.getRedirectUrl;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class EquipmentCrudBean {

    private static final List<EquipmentCategoryType> EQUIPMENT_CATEGORIES = Arrays.asList(EquipmentCategoryType.values());
    private static final List<EquipmentStatus> EQUIPMENT_STATUSES = Arrays.asList(EquipmentStatus.values());

    private final EquipmentRepository equipmentRepository;
    private final PersonRepository personRepository;
    private final DonorRepository donorRepository;

    private Person owner;
    private Equipment equipment;
    private Long id;
    private List<Donor> donors;

    @PostConstruct
    public void init() {
        equipment = new Equipment();
        equipment.setSpecification(new HashMap<>());
        donors = donorRepository.findAll();
    }

    public void fetch() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (id != null) {
                equipment = equipmentRepository.findById(id).orElse(new Equipment());
                if (equipment.getOwnerId() != null) {
                    owner = personRepository.findById(equipment.getOwnerId()).orElse(null);
                }
                if (equipment.getSpecification() == null) {
                    equipment.setSpecification(new HashMap<>());
                }
            } else {
                init();
            }
        }
    }

    public List<EquipmentCategoryType> getEquipmentCategories() {
        return EQUIPMENT_CATEGORIES;
    }

    public List<EquipmentStatus> getEquipmentStatuses() {
        return EQUIPMENT_STATUSES;
    }

    public String save() {
        equipmentRepository.saveAndFlush(equipment);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Save", "Wijzigingen opgeslagen"));
        return getRedirectUrl(equipment.getId());
    }

    // --[ specifications ]---------------------------------------------------------------------------------------------
    public Set<Map.Entry<String, String>> getSpecifications() {
        return equipment.getSpecification().entrySet();
    }

    public void createSpecification() {
        equipment.getSpecification().put("nieuw-%s".formatted(equipment.getSpecification().size()), "");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Create", "Een nieuw rij is toegevoegd, maar NIET opgeslagen"));
    }

    public void deleteSpecification(final String key) {
        equipment.getSpecification().remove(key);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Delete", "Specification '%s' is verwijderd, maar NIET opgeslagen".formatted(key)));
    }

    public void updateSpecification(final RowEditEvent<Map.Entry<String, String>> event) {
        Map.Entry<String, String> entry = event.getObject();
        equipment.getSpecification().put(entry.getKey(), entry.getValue());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Save", "Specification '%s' is aangepast, maar NIET opgeslagen".formatted(entry.getKey())));
    }
}
