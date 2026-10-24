package com.github.computerhuis.huishub.cdm.view;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import com.github.computerhuis.huishub.cdm.enumeration.TicketStatusType;
import com.github.computerhuis.huishub.cdm.enumeration.TicketType;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Setter
@Getter
@Entity
@Immutable
@Table(name = "equipment_history")
@IdClass(EquipmentHistoryPrimaryKey.class)
public class EquipmentHistory {

    @Id
    private Long equipmentId;
    @Id
    private Long ticketId;

    @Enumerated(EnumType.STRING)
    private TicketStatusType status;
    @Enumerated(EnumType.STRING)
    private TicketType ticketType;
    private LocalDateTime registered;
    private String subject;
    private String firstName;
    private String infix;
    private String lastName;

    public String getFullName() {
        val result = new StringBuilder();

        if (isNotBlank(firstName)) {
            result.append(firstName);
        }

        if (isNotBlank(infix)) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            result.append(infix);
        }

        if (isNotBlank(lastName)) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            result.append(lastName);
        }

        return result.toString().trim();
    }
}
