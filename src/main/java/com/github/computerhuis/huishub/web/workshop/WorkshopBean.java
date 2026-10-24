package com.github.computerhuis.huishub.web.workshop;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.model.TicketLog;
import com.github.computerhuis.huishub.cdm.repository.PersonRepository;
import com.github.computerhuis.huishub.cdm.repository.TicketLogRepository;
import com.github.computerhuis.huishub.cdm.repository.TicketOverviewRepository;
import com.github.computerhuis.huishub.cdm.view.TicketOverview;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class WorkshopBean {

    private final TicketOverviewRepository ticketOverviewRepository;
    private final TicketLogRepository ticketLogRepository;
    private final PersonRepository personRepository;

    private List<TicketOverview> list;
    private Map<Long, List<TicketLog>> logMap;
    private Map<Long, Person> ticketVolunteers;

    public void fetch() {
        list = ticketOverviewRepository.getActiveTickets();
        logMap = new HashMap<>();
        ticketVolunteers = new HashMap<>();
        for (val ticketOverview : list) {
            val ticketLogs = ticketLogRepository.findByTicketId(ticketOverview.getId());
            ticketLogs.sort(Comparator.comparing(TicketLog::getDate).reversed());
            logMap.put(ticketOverview.getId(), ticketLogs);
            for (val ticketLog : ticketLogs) {
                ticketVolunteers.put(ticketLog.getVolunteerId(), personRepository.findById(ticketLog.getVolunteerId()).orElse(null));
            }
        }
    }

    public List<TicketLog> getTicketLogs(final Long ticketId) {
        return logMap.get(ticketId);
    }

    public Person getVolunteer(final Long volunteerId) {
        return ticketVolunteers.get(volunteerId);
    }
}
