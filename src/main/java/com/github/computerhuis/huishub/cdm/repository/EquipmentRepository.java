package com.github.computerhuis.huishub.cdm.repository;

import com.github.computerhuis.huishub.cdm.enumeration.EquipmentStatus;
import com.github.computerhuis.huishub.cdm.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    List<Equipment> findByDonorId(Long donorId);

    List<Equipment> findByOwnerId(Long ownerId);

    List<Equipment> findByStatus(EquipmentStatus status);

    Boolean existsBySerialNumberAndDonorId(String serialNumber, Long donorId);

}
