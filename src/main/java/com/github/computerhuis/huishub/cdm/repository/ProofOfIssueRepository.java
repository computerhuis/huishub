package com.github.computerhuis.huishub.cdm.repository;

import com.github.computerhuis.huishub.cdm.model.ProofOfIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProofOfIssueRepository extends JpaRepository<ProofOfIssue, Long> {
    // https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

    List<ProofOfIssue> findByRecipientIdInOrderByIssueDateDesc(Collection<Long> recipientIds);
}
