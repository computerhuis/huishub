package com.github.computerhuis.huishub.cdm.view;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentCategoryType;
import com.github.computerhuis.huishub.cdm.enumeration.TicketStatusType;
import com.github.computerhuis.huishub.cdm.enumeration.TicketType;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Getter
@Setter
@Entity
@Immutable
@Table(name = "ticket_overview")
public class TicketOverview {

    @Id
    private Long id;
    @Enumerated(EnumType.STRING)
    private TicketStatusType status;
    private Long equipmentId;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketType ticketType;
    private LocalDateTime registered;
    private LocalDateTime unregistered;
    private String subject;
    private String description;
    private String firstName;
    private String infix;
    private String lastName;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EquipmentCategoryType category;
    private String manufacturer;
    private String model;

    public String getPractitioner() {
        val result = new StringBuilder();
        result.append(firstName);
        if (isNotBlank(infix)) {
            result.append(" ");
            result.append(infix);
        }
        result.append(" ");
        result.append(lastName);

        return result.toString().trim();
    }

    public String getTicketTypeName() {
        if (ticketType != null) {
            return ticketType.getLabel();
        }
        return null;
    }

    public String getStatusName() {
        if (status != null) {
            return status.getLabel();
        }
        return null;
    }

    public String getStatusSeverity() {
        if (status != null) {
            switch (status) {
                case OPEN -> {
                    return "success";
                }
                case IN_PROGRESS -> {
                    return "danger";
                }
                case READY -> {
                    return "";
                }
                case CUSTOMER_INFORMED -> {
                    return "warning";
                }
                case CLOSED -> {
                    return "info";
                }
            }
        }
        return null;
    }

    public String getCategoryName() {
        if (category != null) {
            return category.getLabel();
        }
        return null;
    }
}
