package com.github.computerhuis.huishub.cdm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@DynamicUpdate
@DynamicInsert
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proof_of_issues")
public class ProofOfIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime issueDate;

    @Column(nullable = false)
    private Long issuedBy;

    private Long behalfOfId;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private Long equipmentId;

    @Column(columnDefinition = "text")
    private String purpose;

    @JdbcTypeCode(SqlTypes.LONGVARBINARY)
    private byte[] signedImage;

    /**
     * Human readable document number, e.g. {@code UB-2026-001}.
     */
    public String getDocumentNumber() {
        if (id == null) {
            return null;
        }
        val year = issueDate != null ? issueDate.getYear() : LocalDateTime.now().getYear();
        return "UB-%d-%03d".formatted(year, id);
    }
}
