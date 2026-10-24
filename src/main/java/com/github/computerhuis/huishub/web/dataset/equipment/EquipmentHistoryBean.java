package com.github.computerhuis.huishub.web.dataset.equipment;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.repository.EquipmentHistoryRepository;
import com.github.computerhuis.huishub.cdm.view.EquipmentHistory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class EquipmentHistoryBean {

    private final EquipmentHistoryRepository equipmentHistoryRepository;

    private Long id;
    private List<EquipmentHistory> list;

    public void fetch() {
        list = equipmentHistoryRepository.findByEquipmentId(id);
    }
}
