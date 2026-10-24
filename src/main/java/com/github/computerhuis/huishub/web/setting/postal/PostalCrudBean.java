package com.github.computerhuis.huishub.web.setting.postal;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.computerhuis.huishub.cdm.model.Postal;
import com.github.computerhuis.huishub.cdm.repository.PostalRepository;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Data
@Component
public class PostalCrudBean {

    private final PostalRepository postalRepository;

    private Postal postal;
    private String code;

    @PostConstruct
    public void init() {
        postal = new Postal();
    }

    public void fetch() {
        postal = postalRepository.findById(code).orElse(new Postal());
    }
}

