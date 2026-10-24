package com.github.computerhuis.huishub.web.dataset.ticket;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.github.computerhuis.huishub.cdm.enumeration.TicketType;
import com.github.computerhuis.huishub.cdm.model.Equipment;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.model.Ticket;
import com.github.computerhuis.huishub.cdm.model.TicketLog;
import com.github.computerhuis.huishub.cdm.repository.EquipmentRepository;
import com.github.computerhuis.huishub.cdm.repository.TicketLogOverviewRepository;
import com.github.computerhuis.huishub.cdm.repository.TicketLogRepository;
import com.github.computerhuis.huishub.cdm.repository.TicketRepository;
import com.github.computerhuis.huishub.cdm.view.TicketLogOverview;
import org.primefaces.event.RowEditEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

import static com.github.computerhuis.huishub.web.UtilBean.getRedirectUrl;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class TicketCrudBean {

    private static final List<TicketType> TICKET_TYPES = Arrays.asList(TicketType.values());

    private final TicketRepository ticketRepository;
    private final TicketLogOverviewRepository ticketLogOverviewRepository;
    private final EquipmentRepository equipmentRepository;
    private final TicketLogRepository ticketLogRepository;

    private Person owner;
    private Equipment equipment;
    private Ticket ticket;
    private Long id;
    private List<TicketLogOverview> ticketLogs;
    private String newLog;

    @PostConstruct
    public void init() {
        ticket = new Ticket();
        ticket.setDetails(new HashMap<>());
        ticketLogs = new ArrayList<>();
    }

    public List<TicketType> getTicketTypes() {
        return TICKET_TYPES;
    }

    public void fetch() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (id != null) {
                ticket = ticketRepository.findById(id).orElse(new Ticket());
                ticketLogs = ticketLogOverviewRepository.findByTicketIdOrderByDateDesc(id);
                if (ticket.getEquipmentId() != null) {
                    equipment = equipmentRepository.findById(ticket.getEquipmentId()).orElse(null);
                }
                if (ticket.getDetails() == null) {
                    ticket.setDetails(new HashMap<>());
                }
            } else {
                init();
            }
        }
    }

    public String save() {
        ticketRepository.saveAndFlush(ticket);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Save", "Wijzigingen opgeslagen"));
        return getRedirectUrl(ticket.getId());
    }

    // --[ details ]----------------------------------------------------------------------------------------------------
    public Set<Map.Entry<String, String>> getDetails() {
        return ticket.getDetails().entrySet();
    }

    public void createDetail() {
        ticket.getDetails().put("nieuw-%s".formatted(ticket.getDetails().size()), "");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Create", "Een nieuw rij is toegevoegd, maar NIET opgeslagen"));
    }

    public void deleteDetail(final String key) {
        ticket.getDetails().remove(key);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Delete", "Detail '%s' is verwijderd, maar NIET opgeslagen".formatted(key)));
    }

    public void updateDetail(final RowEditEvent<Map.Entry<String, String>> event) {
        Map.Entry<String, String> entry = event.getObject();
        ticket.getDetails().put(entry.getKey(), entry.getValue());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Save", "Detail '%s' is aangepast, maar NIET opgeslagen".formatted(entry.getKey())));
    }

    // --[ logs ]-------------------------------------------------------------------------------------------------------
    public void createLog() {
        val username = Long.valueOf(getContext().getAuthentication().getName());
        val ticketLog = new TicketLog();
        ticketLog.setDate(LocalDateTime.now());
        ticketLog.setVolunteerId(username);
        ticketLog.setTicketId(id);
        ticketLog.setLog(newLog);
        ticketLogRepository.saveAndFlush(ticketLog);

        ticketLogs = ticketLogOverviewRepository.findByTicketIdOrderByDateDesc(id);
        newLog = "";
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Save", "Log is toegevoegd"));
    }
}
