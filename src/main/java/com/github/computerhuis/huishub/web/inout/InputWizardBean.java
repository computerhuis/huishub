package com.github.computerhuis.huishub.web.inout;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.github.computerhuis.huishub.cdm.enumeration.EquipmentCategoryType;
import com.github.computerhuis.huishub.web.inout.helper.ReaderDigidromenLeergeld;
import com.github.computerhuis.huishub.web.inout.helper.WriterDigidromenLeergeld;
import org.primefaces.event.FilesUploadEvent;
import org.primefaces.event.FlowEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.computerhuis.huishub.web.inout.InputWizardType.DIGIDROMEN_LEERGELD;
import static com.github.computerhuis.huishub.web.inout.InputWizardType.GEZINNEN_LEERGELD;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
@Scope("view")
public class InputWizardBean {

    private static final List<EquipmentCategoryType> EQUIPMENT_CATEGORIES = Arrays.asList(EquipmentCategoryType.values());

    private final WriterDigidromenLeergeld writerDigidromenLeergeld;

    private InputWizardType importType;
    private Map<String, Boolean> serialNumberList;

    @Transactional
    public String onFlowProcess(final FlowEvent event) {
        if ("confirm".equals(event.getNewStep())) {
            if (isDigidromenLeergeld()) {
                importToDatabase();
            }
        }
        return event.getNewStep();
    }

    public boolean isDigidromenLeergeld() {
        return DIGIDROMEN_LEERGELD.equals(importType);
    }

    public boolean isGezinnenLeergeld() {
        return GEZINNEN_LEERGELD.equals(importType);
    }

    @Transactional
    public void importToDatabase() {
        if (isDigidromenLeergeld()) {
            for (val serialNumber : serialNumberList.entrySet()) {
                if (writerDigidromenLeergeld.notExist(serialNumber.getKey())) {
                    try {
                        writerDigidromenLeergeld.write(serialNumber.getKey());
                        serialNumber.setValue(true);
                    } catch (Exception e) {
                        log.warn("Exception: {}", e.getMessage(), e);
                        serialNumber.setValue(null);
                    }
                } else {
                    serialNumber.setValue(false);
                }
            }
        }
    }

    public void importFile(final FilesUploadEvent event) {
        for (val file : event.getFiles().getFiles()) {
            if (file != null) {
                try {
                    if (isDigidromenLeergeld()) {
                        serialNumberList = ReaderDigidromenLeergeld.read(file.getInputStream()).stream()
                            .collect(Collectors.toMap(
                                key -> key,
                                key -> false,
                                (existing, replacement) -> existing
                            ));
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Import bestand", "Bestand succesvol ingeladen"));
                    }
                } catch (Exception e) {
                    log.warn("Exception: {}", e.getMessage(), e);
                }
                return;
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Import bestand", "Bestand is leeg!"));
    }
}

