package com.github.computerhuis.huishub.web.setting.postal;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.github.computerhuis.huishub.cdm.model.Postal;
import com.github.computerhuis.huishub.cdm.repository.PostalRepository;
import com.github.computerhuis.huishub.service.importer.PostalCodeImporter;
import org.primefaces.event.FilesUploadEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Getter
@Setter
@Component
@Scope("view")
public class PostalListBean {

    private final PostalRepository postalRepository;
    private final PostalCodeImporter postalCodeImporter;

    private List<Postal> list;

    public void fetch() {
        list = postalRepository.findAll();
    }

    public void importPostalCodes(final FilesUploadEvent event) {
        for (val file : event.getFiles().getFiles()) {
            if (file != null) {
                try {
                    val content = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    postalCodeImporter.importPostalFile(content);
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Save", "Wijzigingen opgeslagen"));
                } catch (Exception e) {
                    log.warn("Exception: {}", e.getMessage(), e);
                }
                return;
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Save", "Bestand is leeg!"));
    }
}
