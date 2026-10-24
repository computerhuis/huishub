package com.github.computerhuis.huishub.web.dataset.donor;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.model.Donor;
import com.github.computerhuis.huishub.cdm.repository.DonorRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import static com.github.computerhuis.huishub.web.UtilBean.getRedirectUrl;

@Slf4j
@RequiredArgsConstructor
@Data
@RequestScope
@Component
public class DonorCrudBean {

    private final DonorRepository donorRepository;

    private Donor donor;
    private Long id;

    @PostConstruct
    public void init() {
        donor = new Donor();
    }

    public void fetch() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (id != null) {
                donor = donorRepository.findById(id).orElse(new Donor());
            }
        } else {
            init();
        }
    }

    public String save() {
        donorRepository.saveAndFlush(donor);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Save", "Wijzigingen opgeslagen"));
        return getRedirectUrl(donor.getId());
    }
}
