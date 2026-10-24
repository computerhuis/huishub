package com.github.computerhuis.huishub.cdm.model;

import jakarta.persistence.*;
import lombok.*;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentCategoryType;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentStatus;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@DynamicUpdate
@DynamicInsert
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EquipmentCategoryType category;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    private Long ownerId;
    private Long donorId;

    private String serialNumber;
    private String manufacturer;
    private String model;

    private LocalDateTime registered;
    private LocalDateTime unregistered;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, String> specification;

    public String getCategoryName() {
        if (category != null) {
            return category.getLabel();
        }
        return null;
    }

    public String getStatusName() {
        if (status != null) {
            return status.getLabel();
        }
        return null;
    }
}
