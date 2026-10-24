package com.github.computerhuis.huishub.cdm.repository;

import com.github.computerhuis.huishub.cdm.model.TicketStatus;
import com.github.computerhuis.huishub.cdm.model.TicketStatusPrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketStatusRepository extends JpaRepository<TicketStatus, TicketStatusPrimaryKey> {
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    List<TicketStatus> findByTicketId(Long ticketId);
}
