package com.github.computerhuis.huishub.web.educatie;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.model.Activity;
import com.github.computerhuis.huishub.cdm.repository.ActivityRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import static com.github.computerhuis.huishub.web.UtilBean.getRedirectUrl;

@Slf4j
@RequiredArgsConstructor
@Data
@RequestScope
@Component
public class ActivityCrudBean {

    private final ActivityRepository activityRepository;

    private Activity activity;
    private Long id;

    @PostConstruct
    public void init() {
        activity = new Activity();
    }

    public void fetch() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (id != null) {
                activity = activityRepository.findById(id).orElse(new Activity());
            } else {
                init();
            }
        }
    }

    public String save() {
        activityRepository.saveAndFlush(activity);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Save", "Wijzigingen opgeslagen"));
        return getRedirectUrl(activity.getId());
    }
}
