package com.github.computerhuis.huishub.cdm.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ProofOfIssueLinePrimaryKey implements Serializable {

    @Serial
    private static final long serialVersionUID = -6243964552748117281L;

    private Long proofOfIssueId;
    private Long lineId;
}
