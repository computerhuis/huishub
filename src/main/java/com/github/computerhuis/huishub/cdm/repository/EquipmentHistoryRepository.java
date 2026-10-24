package com.github.computerhuis.huishub.cdm.repository;

import com.github.computerhuis.huishub.cdm.view.EquipmentHistory;
import com.github.computerhuis.huishub.cdm.view.EquipmentHistoryPrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface EquipmentHistoryRepository extends JpaRepository<EquipmentHistory, EquipmentHistoryPrimaryKey> {
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    List<EquipmentHistory> findByEquipmentId(Long equipmentId);
}
