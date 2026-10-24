package com.github.computerhuis.huishub.cdm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;

@DynamicUpdate
@DynamicInsert
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proof_of_issue_lines")
@IdClass(ProofOfIssueLinePrimaryKey.class)
public class ProofOfIssueLine {

    @Id
    private Long proofOfIssueId;

    @Id
    private Long lineId;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private String unit;
}
