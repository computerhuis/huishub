package com.github.computerhuis.huishub.web.dataset.equipment;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentStatus;
import com.github.computerhuis.huishub.cdm.model.Equipment;
import com.github.computerhuis.huishub.cdm.repository.EquipmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class EquipmentStockBean {
    private final EquipmentRepository equipmentRepository;

    private List<Equipment> list;

    public void fetch() {
        list = equipmentRepository.findByStatus(EquipmentStatus.SUITABLE_FOR_GIFT);
    }
}
