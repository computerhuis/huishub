package com.github.computerhuis.huishub.cdm.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TimesheetPrimaryKey implements Serializable {

    @Serial
    private static final long serialVersionUID = -8714404286147373620L;

    private Long personId;
    private Long activityId;
}
