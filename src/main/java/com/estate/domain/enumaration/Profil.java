package com.estate.domain.enumaration;

import com.estate.domain.converter.EnumConverter;
import lombok.Getter;

@Getter
public enum Profil {
    STAFF,
    STUDENT;

    public static class Converter extends EnumConverter<Profil> {
        public Converter() {
            super(Profil.class);
        }
    }
}
