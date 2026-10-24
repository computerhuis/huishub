package com.github.computerhuis.huishub.web.inout.helper;

import lombok.RequiredArgsConstructor;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentCategoryType;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentStatus;
import com.github.computerhuis.huishub.cdm.model.Equipment;
import com.github.computerhuis.huishub.cdm.repository.EquipmentRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WriterDigidromenLeergeld {

    private static final long STICHTING_LEERGELD_ID = 61;

    private final EquipmentRepository equipmentRepository;

    public boolean notExist(final String serialNumber) {
        return !equipmentRepository.existsBySerialNumberAndDonorId(serialNumber, STICHTING_LEERGELD_ID);
    }

    public void write(final String serialNumber) {
        equipmentRepository.save(Equipment.builder()
            .donorId(STICHTING_LEERGELD_ID)
            .category(EquipmentCategoryType.LAPTOP)
            .serialNumber(serialNumber)
            .status(EquipmentStatus.INCOMING_GIFT)
            .build());
    }
}
