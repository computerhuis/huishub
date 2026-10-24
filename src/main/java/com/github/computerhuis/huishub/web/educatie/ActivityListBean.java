package com.github.computerhuis.huishub.web.educatie;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.model.Activity;
import com.github.computerhuis.huishub.cdm.repository.ActivityRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class ActivityListBean {

    private final ActivityRepository activityRepository;

    private List<Activity> list;

    public void fetch() {
        list = activityRepository.findAll();
    }
}
