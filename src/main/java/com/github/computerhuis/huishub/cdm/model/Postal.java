package com.github.computerhuis.huishub.cdm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@DynamicUpdate
@DynamicInsert
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "postal_codes")
public class Postal {

    @Id
    @Column(length = 6)
    private String code;
    private Integer houseNumberMin;
    private Integer houseNumberMax;
    private String street;
    private String url;
    private String neighbourhood;
    private String district;
    private String city;
    private String municipality;
    private String province;
    private Boolean pobox;

    public String getHouseNumbers() {
        val result = new StringBuilder();
        if (houseNumberMin != null) {
            result.append(houseNumberMin);
            result.append("-");
        }
        if (houseNumberMax != null) {
            result.append(houseNumberMax);
        }
        return result.toString().trim();
    }
}
