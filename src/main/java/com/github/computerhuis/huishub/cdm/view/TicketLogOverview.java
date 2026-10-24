package com.github.computerhuis.huishub.cdm.view;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import com.github.computerhuis.huishub.cdm.model.TicketLogPrimaryKey;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Setter
@Getter
@Entity
@Immutable
@Table(name = "ticket_log_overview")
@IdClass(TicketLogPrimaryKey.class)
public class TicketLogOverview {

    @Id
    private Long ticketId;
    @Id
    private LocalDateTime date;

    private Long volunteerId;
    private String firstName;
    private String infix;
    private String lastName;

    private String log;

    public String getFullname() {
        val value = new StringBuilder();
        value.append(firstName);
        if (isNotBlank(infix)) {
            value.append(" ");
            value.append(infix);
        }
        if (isNotBlank(lastName)) {
            value.append(" ");
            value.append(lastName);
        }
        return value.toString();
    }
}
