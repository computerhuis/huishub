package com.github.computerhuis.huishub.web.dataset.person;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.repository.PersonRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class PersonListBean {

    private final PersonRepository personRepository;

    private List<Person> list;

    public void fetch() {
        list = personRepository.findAll();
    }
}
