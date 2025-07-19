package com.estate.domain.enumaration;

import com.estate.domain.converter.EnumConverter;
import lombok.Getter;

@Getter
public enum Status {
    INITIATED("Initié", "bg-warning", true),
    SUBMITTED("Soumis", "bg-info", false),
    CONFIRMED("Confirmé", "bg-success", false),
    CANCELLED("Annulé", "bg-danger", true);

    private final String name;
    private final String background;
    private final boolean removable;

    Status(String name, String background, boolean removable) {
        this.name = name;
        this.background = background;
        this.removable = removable;
    }

    public static class Converter extends EnumConverter<Status> {
        public Converter() {
            super(Status.class);
        }
    }

}
