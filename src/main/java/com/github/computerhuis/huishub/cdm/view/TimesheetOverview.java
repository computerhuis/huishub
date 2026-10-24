package com.github.computerhuis.huishub.cdm.view;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "timesheet_overview")
@IdClass(TimesheetOverviewPrimaryKey.class)
public class TimesheetOverview {

    @Id
    private Long personId;
    @Id
    private Long activityId;
    @Id
    private LocalDateTime registered;
    private LocalDateTime unregistered;

    private String activityName;
    private String firstName;
    private String infix;
    private String lastName;

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
