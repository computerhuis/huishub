package com.github.computerhuis.huishub.web.setting.volunteer;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.repository.PersonRepository;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.github.computerhuis.huishub.cdm.enumeration.PersonRoleType.ROLE_VOLUNTEER;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class VolunteerListBean {

    private final PersonRepository personRepository;

    private List<Person> list;

    public void fetch() {
        list = personRepository.findByRoles(ROLE_VOLUNTEER);
    }
}
