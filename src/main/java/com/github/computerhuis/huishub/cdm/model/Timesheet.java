package com.github.computerhuis.huishub.cdm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
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
@Table(name = "timesheets")
@IdClass(TimesheetPrimaryKey.class)
public class Timesheet {

    @Id
    private Long personId;
    @Id
    private Long activityId;
    private LocalDateTime registered;
    private LocalDateTime unregistered;
}
