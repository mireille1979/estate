package com.estate.domain.enumaration;

import com.estate.domain.converter.EnumConverter;
import lombok.Getter;

@Getter
public enum Relationship {
    FATHER("Père", "Fils", "Fille"),
    MOTHER("Mère", "Fils", "Fille"),
    UNCLE("Oncle", "Neveux", "Nièce"),
    AUNT("Tante", "Neveux", "Nièce"),
    TUTOR("Tuteur", "Fils", "Fille"),
    TUTOR2("Tutrice", "Fils", "Fille"),;

    private final String name;
    private final String boy;
    private final String girl;

    Relationship(String name, String boy, String girl) {
        this.name = name;
        this.boy = boy;
        this.girl = girl;
    }

    public static class Converter extends EnumConverter<Relationship> {
        public Converter() {
            super(Relationship.class);
        }
    }
}
