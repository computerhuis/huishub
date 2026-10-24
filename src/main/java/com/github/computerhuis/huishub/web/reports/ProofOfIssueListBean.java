package com.github.computerhuis.huishub.web.reports;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.repository.PersonRepository;
import com.github.computerhuis.huishub.cdm.repository.ProofOfIssueRepository;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Backing bean for the uitgiftbewijzen overview ({@code /reports/proofofissue/list}).
 */
@Slf4j
@RequiredArgsConstructor
@Data
@Component
@Scope("view")
public class ProofOfIssueListBean {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final ProofOfIssueRepository proofOfIssueRepository;
    private final PersonRepository personRepository;

    private String query;
    private List<Row> list = List.of();

    public void fetch() {
        val issues = proofOfIssueRepository.findAll(Sort.by(Sort.Direction.DESC, "issueDate"));

        val personIds = issues.stream()
                .flatMap(issue -> Stream.of(issue.getRecipientId(), issue.getIssuedBy()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        val persons = personRepository.findAllById(personIds).stream()
                .collect(Collectors.toMap(Person::getId, Function.identity()));

        val rows = issues.stream()
                .map(issue -> Row.builder()
                        .id(issue.getId())
                        .number(issue.getDocumentNumber())
                        .issueDate(issue.getIssueDate())
                        .recipient(name(persons.get(issue.getRecipientId())))
                        .issuedBy(name(persons.get(issue.getIssuedBy())))
                        .build())
                .toList();

        list = filter(rows, query);
    }

    public void clear() {
        query = null;
        fetch();
    }

    private static List<Row> filter(final List<Row> rows, final String query) {
        if (isBlank(query)) {
            return rows;
        }

        val needle = query.trim().toLowerCase();
        return rows.stream()
                .filter(row -> matches(row, needle))
                .toList();
    }

    private static boolean matches(final Row row, final String needle) {
        return contains(row.getNumber(), needle)
                || contains(row.getRecipient(), needle)
                || contains(row.getIssuedBy(), needle)
                || (row.getIssueDate() != null && row.getIssueDate().format(DATE_FORMAT).contains(needle));
    }

    private static boolean contains(final String value, final String needle) {
        return value != null && value.toLowerCase().contains(needle);
    }

    private static String name(final Person person) {
        return person != null ? person.getFullname() : null;
    }

    @Data
    @Builder
    public static class Row {
        private Long id;
        private String number;
        private LocalDateTime issueDate;
        private String recipient;
        private String issuedBy;
    }
}
