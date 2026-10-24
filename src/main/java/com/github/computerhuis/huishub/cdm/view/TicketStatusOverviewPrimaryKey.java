package com.github.computerhuis.huishub.cdm.view;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TicketStatusOverviewPrimaryKey implements Serializable {

    private static final long serialVersionUID = -4163550932920094923L;

    private Long ticketId;
    private LocalDateTime date;
}
