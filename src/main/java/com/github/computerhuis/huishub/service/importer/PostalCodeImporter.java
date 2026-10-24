package com.github.computerhuis.huishub.service.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.github.computerhuis.huishub.cdm.model.Postal;
import com.github.computerhuis.huishub.cdm.repository.PostalRepository;
import com.github.computerhuis.huishub.util.MapperUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostalCodeImporter {

    private final static ObjectMapper MAPPER = MapperUtils.createJsonMapper();
    private final PostalRepository postalRepository;

    @Async
    public void importPostalFile(@NonNull final String content) {
        try {
            val data = MAPPER.readValue(content, Map.class);

            val provincies = (Map<String, Map>) data.get("provincies");
            for (val provincie : provincies.entrySet()) {
                log.trace("Importing postal codes for province {}", provincie.getKey());
                val gemeenten = (Map<String, Map>) provincie.getValue().get("gemeenten");
                for (val gemeente : gemeenten.entrySet()) {
                    log.trace("Importing postal codes for gemeente {}", gemeente.getKey());
                    val plaatsen = (Map<String, Map>) gemeente.getValue().get("plaatsen");
                    for (val plaats : plaatsen.entrySet()) {
                        log.trace("Importing postal codes for plaats {}", plaats.getKey());
                        val postbussen = (Map<String, String>) plaats.getValue().get("postbussen");
                        for (val postbus : postbussen.entrySet()) {
                            if (!postalRepository.existsById(postbus.getKey())) {
                                postalRepository.saveAndFlush(Postal.builder()
                                    .code(postbus.getKey())
                                    .province(provincie.getKey())
                                    .municipality(gemeente.getKey())
                                    .city(plaats.getKey())
                                    .pobox(true)
                                    .url(postbus.getValue())
                                    .build());
                            }
                        }

                        val wijken = (Map<String, Map>) plaats.getValue().get("wijken");
                        for (val wijk : wijken.entrySet()) {
                            log.trace("Importing postal codes for wijk {}", wijk.getKey());
                            val buurten = (Map<String, Map>) wijk.getValue().get("buurten");
                            for (val buurt : buurten.entrySet()) {
                                log.trace("Importing postal codes for buurt {}", buurt.getKey());
                                val postcodes = (Map<String, Map>) buurt.getValue().get("postcodes");
                                for (val postcode : postcodes.entrySet()) {
                                    log.trace("Importing postal codes for postcode {}", postcode.getKey());
                                    if (!postalRepository.existsById(postcode.getKey())) {
                                        postalRepository.saveAndFlush(Postal.builder()
                                            .code(postcode.getKey())
                                            .province(gemeente.getKey())
                                            .municipality(gemeente.getKey())
                                            .city(plaats.getKey())
                                            .district(wijk.getKey())
                                            .neighbourhood(buurt.getKey())
                                            .street((String) postcode.getValue().get("straat"))
                                            .houseNumberMin(Integer.valueOf(((String) postcode.getValue().get("nummers")).split("-")[0].strip()))
                                            .houseNumberMax(Integer.valueOf(((String) postcode.getValue().get("nummers")).split("-")[1].strip()))
                                            .url((String) postcode.getValue().get("url"))
                                            .build());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Exception: {}", e.getMessage(), e);
        }
    }
}
