package com.github.computerhuis.huishub.cdm.repository;

import com.github.computerhuis.huishub.cdm.view.TicketLogOverview;
import com.github.computerhuis.huishub.cdm.view.TicketLogOverviewPrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface TicketLogOverviewRepository extends JpaRepository<TicketLogOverview, TicketLogOverviewPrimaryKey> {
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    List<TicketLogOverview> findByTicketIdOrderByDateDesc(Long ticketId);
}
