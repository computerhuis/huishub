package com.github.computerhuis.huishub.cdm.repository;

import com.github.computerhuis.huishub.cdm.model.TicketLog;
import com.github.computerhuis.huishub.cdm.model.TicketLogPrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketLogRepository extends JpaRepository<TicketLog, TicketLogPrimaryKey> {
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    List<TicketLog> findByTicketId(Long ticketId);
}
