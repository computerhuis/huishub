package com.github.computerhuis.huishub.cdm.model;

import jakarta.persistence.*;
import lombok.*;
import com.github.computerhuis.huishub.cdm.enumeration.PersonRoleType;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static com.github.computerhuis.huishub.cdm.enumeration.PersonRoleType.ROLE_ADMIN;
import static com.github.computerhuis.huishub.cdm.enumeration.PersonRoleType.ROLE_VOLUNTEER;

@DynamicUpdate
@DynamicInsert
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "persons")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String initials;
    private String firstName;
    private String infix;
    private String lastName;
    private LocalDate dateOfBirth;
    private String email;
    private String mobile;
    private String telephone;
    private String postalCode;
    private String street;
    private Integer houseNumber;
    private String houseNumberAddition;
    private String city;
    private String comments;
    private LocalDateTime registered;
    private LocalDateTime unregistered;
    private String msaccess;
    private String passhash;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(name = "person_roles", joinColumns = @JoinColumn(name = "person_id"))
    @Column(name = "authority")
    @Enumerated(STRING)
    private Set<PersonRoleType> roles;

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

    public Long getAge() {
        return ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now());
    }

    public String getDateOfBirthAsISO() {
        if (dateOfBirth != null) {
            return dateOfBirth.toString();
        }
        return null;
    }

    public boolean isVolunteer() {
        return roles != null && roles.contains(ROLE_VOLUNTEER);
    }

    public boolean isAdmin() {
        return roles != null && roles.contains(ROLE_ADMIN);
    }
}
