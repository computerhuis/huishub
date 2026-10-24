package com.github.computerhuis.huishub.cdm.repository;

import com.github.computerhuis.huishub.cdm.model.ProofOfIssueLine;
import com.github.computerhuis.huishub.cdm.model.ProofOfIssueLinePrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProofOfIssueLineRepository extends JpaRepository<ProofOfIssueLine, ProofOfIssueLinePrimaryKey> {
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    List<ProofOfIssueLine> findByProofOfIssueIdOrderByLineIdAsc(Long proofOfIssueId);
}
