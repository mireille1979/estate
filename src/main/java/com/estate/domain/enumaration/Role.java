package com.estate.domain.enumaration;

import lombok.Getter;

@Getter
public enum Role {
    ROLE_ADMIN("Cadre administratif", true),
    ROLE_MANAGER("Gestionnaire", true),
    ROLE_JANITOR("Concierge", true),
    ROLE_STUDENT("Étudiant", false);

    private final String name;
    private final boolean staff;

    Role(String name, boolean staff) {
        this.name = name;
        this.staff = staff;
    }
}
