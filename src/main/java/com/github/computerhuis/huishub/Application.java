package com.github.computerhuis.huishub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@Slf4j
@SpringBootApplication
public class Application {

    public static void main(final String... args) {
        Locale.setDefault(new Locale("nl", "NL"));
        SpringApplication.run(Application.class, args);
    }
}
