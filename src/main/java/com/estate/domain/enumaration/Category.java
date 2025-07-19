package com.estate.domain.enumaration;

import com.estate.domain.converter.EnumConverter;
import lombok.Getter;

@Getter
public enum Category {
    ROOM("Chambre"),
    STUDIO("Studio"),
    APARTMENT("Appartement");

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public static class Converter extends EnumConverter<Category> {
        public Converter() {
            super(Category.class);
        }
    }
}
