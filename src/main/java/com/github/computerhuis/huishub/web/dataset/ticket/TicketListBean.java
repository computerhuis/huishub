package com.github.computerhuis.huishub.web.dataset.ticket;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.repository.TicketOverviewRepository;
import com.github.computerhuis.huishub.cdm.view.TicketOverview;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class TicketListBean {

    private final TicketOverviewRepository ticketOverviewRepository;

    private List<TicketOverview> list;

    public void fetch() {
        list = ticketOverviewRepository.findAll();
    }
}
