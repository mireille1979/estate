package com.estate.domain.enumaration;

import com.estate.domain.converter.EnumConverter;
import lombok.Getter;

@Getter
public enum Grade {
    L1("Licence 1"),
    L2("Licence 2"),
    L3("Licence 3"),
    M1("Master 1"),
    M2("Master 2"),
    PHD("Doctorat");

    private final String name;

    Grade(String name) {
        this.name = name;
    }

    public static class Converter extends EnumConverter<Grade> {
        public Converter() {
            super(Grade.class);
        }
    }
}
