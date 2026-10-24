package com.github.computerhuis.huishub.web.dataset.person.timesheet;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.repository.TimesheetOverviewRepository;
import com.github.computerhuis.huishub.cdm.view.TimesheetOverview;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class TimesheetListBean {

    private final TimesheetOverviewRepository timesheetOverviewRepository;

    private List<TimesheetOverview> list;

    public void fetch() {
        list = timesheetOverviewRepository.findAll();
    }
}
