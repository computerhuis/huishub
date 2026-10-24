package com.github.computerhuis.huishub.cdm.repository;

import com.github.computerhuis.huishub.cdm.view.TicketStatusOverview;
import com.github.computerhuis.huishub.cdm.view.TicketStatusOverviewPrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketStatusOverviewRepository extends JpaRepository<TicketStatusOverview, TicketStatusOverviewPrimaryKey> {
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    List<TicketStatusOverview> findByTicketId(Long ticketId);

    List<TicketStatusOverview> findByEquipmentId(Long equipmentId);
}
