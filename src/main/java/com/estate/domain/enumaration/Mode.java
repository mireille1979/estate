package com.estate.domain.enumaration;

import com.estate.domain.converter.EnumConverter;
import lombok.Getter;

@Getter
public enum Mode {
    BANK("Banque"),
    CASH("Cash"),
    MTN("MTN Mobile Money"),
    ORANGE("Orange Money"),
    PAYPAL("PayPal");

    private final String name;

    Mode(String name) {
        this.name = name;
    }

    public static class Converter extends EnumConverter<Mode> {
        public Converter() {
            super(Mode.class);
        }
    }
}
