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
@Table(name = "donors")
public class Donor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private boolean reporting;
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
}
