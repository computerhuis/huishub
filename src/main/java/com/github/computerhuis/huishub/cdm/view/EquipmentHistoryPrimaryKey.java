package com.github.computerhuis.huishub.cdm.view;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class EquipmentHistoryPrimaryKey implements Serializable {

    @Serial
    private static final long serialVersionUID = -3642444426900240024L;

    private Long equipmentId;
    private Long ticketId;
}
