package com.estate.domain.enumaration;

import com.estate.domain.converter.EnumConverter;
import lombok.Getter;

@Getter
public enum Gender {
    MALE("Masculin"),
    FEMALE("Féminin");

    private final String name;

    Gender(String name) {
        this.name = name;
    }

    public static class Converter extends EnumConverter<Gender> {
        public Converter() {
            super(Gender.class);
        }
    }
}
