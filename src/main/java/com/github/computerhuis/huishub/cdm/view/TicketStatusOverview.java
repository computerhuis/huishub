package com.github.computerhuis.huishub.cdm.view;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import com.github.computerhuis.huishub.cdm.enumeration.TicketStatusType;
import com.github.computerhuis.huishub.cdm.model.TicketStatusPrimaryKey;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Setter
@Getter
@Entity
@Immutable
@Table(name = "ticket_status_overview")
@IdClass(TicketStatusPrimaryKey.class)
public class TicketStatusOverview {

    @Id
    private Long ticketId;
    @Id
    private LocalDateTime date;

    private String subject;

    private Long equipmentId;
    private Long volunteerId;
    private String firstName;
    private String infix;
    private String lastName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatusType status;

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
