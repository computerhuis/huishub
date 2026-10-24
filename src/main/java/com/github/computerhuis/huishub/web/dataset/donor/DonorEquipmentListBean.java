package com.github.computerhuis.huishub.web.dataset.donor;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.model.Donor;
import com.github.computerhuis.huishub.cdm.model.Equipment;
import com.github.computerhuis.huishub.cdm.repository.DonorRepository;
import com.github.computerhuis.huishub.cdm.repository.EquipmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class DonorEquipmentListBean {

    private final EquipmentRepository equipmentRepository;
    private final DonorRepository donorRepository;

    private Long id;
    private Donor donor;
    private List<Equipment> list;

    public void fetch() {
        donor = donorRepository.findById(id).orElse(new Donor());
        list = equipmentRepository.findByDonorId(id);
    }
}
