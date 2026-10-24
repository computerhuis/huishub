package com.github.computerhuis.huishub.cdm.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EquipmentStatus {
    CUSTOMER_OWNED("Eigendom van de klant"),
    INCOMING_GIFT("Binnenkomend gift"),
    SUITABLE_FOR_GIFT("Geschikt voor uitgave"),
    RESERVED("Gereserveerd"),
    GIFT_ISSUED("Geschenk uitgegeven"),
    DEMOLITION("Sloop"),
    SOLD("Verkocht");

    private final String label;
}
