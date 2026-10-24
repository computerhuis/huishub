package com.github.computerhuis.huishub.cdm.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TicketStatusPrimaryKey implements Serializable {

    @Serial
    private static final long serialVersionUID = -8133452964785904525L;

    private Long ticketId;
    private LocalDateTime date;
}
