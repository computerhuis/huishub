package com.github.computerhuis.huishub.web.dataset.person;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.repository.PersonRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import static com.github.computerhuis.huishub.web.UtilBean.getRedirectUrl;

@Slf4j
@RequiredArgsConstructor
@Data
@RequestScope
@Component
public class PersonCrudBean {

    private final PersonRepository personRepository;

    private Person person;
    private Long id;

    @PostConstruct
    public void init() {
        person = new Person();
    }

    public void fetch() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            if (id != null) {
                person = personRepository.findById(id).orElse(new Person());
            } else {
                init();
            }
        }
    }

    public String save() {
        personRepository.saveAndFlush(person);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Save", "Wijzigingen opgeslagen"));
        return getRedirectUrl(person.getId());
    }
}
