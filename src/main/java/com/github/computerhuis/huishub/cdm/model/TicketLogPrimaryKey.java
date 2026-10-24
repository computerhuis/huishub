package com.github.computerhuis.huishub.cdm.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TicketLogPrimaryKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 8813358491304643702L;

    private Long ticketId;
    private LocalDateTime date;
}
