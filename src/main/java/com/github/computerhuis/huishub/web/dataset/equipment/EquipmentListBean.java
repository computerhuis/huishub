package com.github.computerhuis.huishub.web.dataset.equipment;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentStatus;
import com.github.computerhuis.huishub.cdm.model.Equipment;
import com.github.computerhuis.huishub.cdm.repository.EquipmentRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class EquipmentListBean {

    private final EquipmentRepository equipmentRepository;
    private static final List<EquipmentStatus> EQUIPMENT_STATUSES = Arrays.asList(EquipmentStatus.values());

    private List<Equipment> list;
    private EquipmentStatus selectedStatus;

    public void fetch() {
        if (selectedStatus == null) {
            list = equipmentRepository.findAll();
        } else {
            list = equipmentRepository.findByStatus(selectedStatus);
        }
    }

    public List<EquipmentStatus> getEquipmentStatuses() {
        return EQUIPMENT_STATUSES;
    }

    public void setStatus(final EquipmentStatus status) {
        selectedStatus = status;
        fetch();
    }
}
