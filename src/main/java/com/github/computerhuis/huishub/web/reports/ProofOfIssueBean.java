package com.github.computerhuis.huishub.web.reports;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.github.computerhuis.huishub.cdm.model.Donor;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.model.ProofOfIssue;
import com.github.computerhuis.huishub.cdm.model.ProofOfIssueLine;
import com.github.computerhuis.huishub.cdm.repository.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Backing bean for the uitgiftbewijs document ({@code /reports/proofofissue/view}).
 */
@Slf4j
@RequiredArgsConstructor
@Data
@Component
@Scope("view")
public class ProofOfIssueBean {

    public static final String ISSUING_ORGANISATION = "Werkgroep Computerhuis Oost";
    private static final String UNIT = "stuks";

    private final ProofOfIssueRepository proofOfIssueRepository;
    private final ProofOfIssueLineRepository proofOfIssueLineRepository;
    private final PersonRepository personRepository;
    private final DonorRepository donorRepository;
    private final EquipmentRepository equipmentRepository;

    private Long id;

    private ProofOfIssue proofOfIssue;
    private List<ProofOfIssueLine> goods = List.of();
    private Person recipient;
    private Person issuedBy;
    private Donor behalfOf;

    public void fetch() {
        if (id == null) {
            return;
        }

        proofOfIssue = proofOfIssueRepository.findById(id).orElse(null);
        if (proofOfIssue == null) {
            log.warn("Uitgiftbewijs {} niet gevonden", id);
            return;
        }

        val lines = proofOfIssueLineRepository.findByProofOfIssueIdOrderByLineIdAsc(id);
        goods = lines.isEmpty() ? equipmentAsGoods(proofOfIssue.getEquipmentId()) : lines;

        recipient = findPerson(proofOfIssue.getRecipientId());
        issuedBy = findPerson(proofOfIssue.getIssuedBy());

        if (proofOfIssue.getBehalfOfId() != null) {
            behalfOf = donorRepository.findById(proofOfIssue.getBehalfOfId()).orElse(null);
        }
    }

    public String getIssuingOrganisation() {
        return ISSUING_ORGANISATION;
    }

    public String getRecipientName() {
        return recipient != null ? recipient.getFullname() : null;
    }

    public String getIssuedByName() {
        return issuedBy != null ? issuedBy.getFullname() : null;
    }

    public String getRecipientAddress() {
        if (recipient == null) {
            return null;
        }

        val streetLine = new StringBuilder();
        if (isNotBlank(recipient.getStreet())) {
            streetLine.append(recipient.getStreet().trim());
            if (recipient.getHouseNumber() != null) {
                streetLine.append(" ").append(recipient.getHouseNumber());
            }
            if (isNotBlank(recipient.getHouseNumberAddition())) {
                streetLine.append(recipient.getHouseNumberAddition().trim());
            }
        }

        val cityLine = new StringBuilder();
        if (isNotBlank(recipient.getPostalCode())) {
            cityLine.append(recipient.getPostalCode().trim());
        }
        if (isNotBlank(recipient.getCity())) {
            if (!cityLine.isEmpty()) {
                cityLine.append(" ");
            }
            cityLine.append(recipient.getCity().trim());
        }

        return Stream.of(streetLine.toString(), cityLine.toString())
            .filter(line -> !line.isBlank())
            .reduce((first, second) -> first + "\n" + second)
            .orElse(null);
    }

    /**
     * The captured signature as a data URI, so it can be shown without an extra
     * request and is included when the page is printed.
     */
    public String getSignatureDataUri() {
        val image = proofOfIssue != null ? proofOfIssue.getSignedImage() : null;
        if (image == null || image.length == 0) {
            return null;
        }
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(image);
    }

    private Person findPerson(final Long personId) {
        return personId != null ? personRepository.findById(personId).orElse(null) : null;
    }

    private List<ProofOfIssueLine> equipmentAsGoods(final Long equipmentId) {
        if (equipmentId == null) {
            return List.of();
        }

        val equipment = equipmentRepository.findById(equipmentId).orElse(null);
        if (equipment == null) {
            return List.of();
        }

        val description = Stream.of(equipment.getCategoryName(), equipment.getManufacturer(), equipment.getModel())
            .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
            .reduce((first, second) -> first + " " + second)
            .orElse("#" + equipmentId);

        return List.of(ProofOfIssueLine.builder()
            .description(description)
            .quantity(BigDecimal.ONE)
            .unit(UNIT)
            .build());
    }
}
