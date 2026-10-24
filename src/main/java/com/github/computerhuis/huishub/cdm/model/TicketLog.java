package com.github.computerhuis.huishub.cdm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@DynamicUpdate
@DynamicInsert
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ticket_log")
@IdClass(TicketLogPrimaryKey.class)
public class TicketLog {

    @Id
    private Long ticketId;
    @Id
    private LocalDateTime date;

    private Long volunteerId;

    @Lob
    private String log;

}
