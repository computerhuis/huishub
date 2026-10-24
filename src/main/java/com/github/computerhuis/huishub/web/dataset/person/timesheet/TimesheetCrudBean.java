package com.github.computerhuis.huishub.web.dataset.person.timesheet;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.model.Person;
import com.github.computerhuis.huishub.cdm.repository.PersonRepository;
import com.github.computerhuis.huishub.cdm.repository.TimesheetOverviewRepository;
import com.github.computerhuis.huishub.cdm.view.TimesheetOverview;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Data
@RequestScope
@Component
public class TimesheetCrudBean {

    private final TimesheetOverviewRepository timesheetOverviewRepository;
    private final PersonRepository personRepository;

    private List<TimesheetOverview> list;
    private Long personId;
    private Person person;

    public void fetch() {
        person = personRepository.findById(personId).orElse(new Person());
        list = timesheetOverviewRepository.findByPersonId(personId);
    }
}
