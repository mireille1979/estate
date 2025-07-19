package com.estate.domain.form;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class VisitorForm {
    @NotBlank(message = "nom requis")
    private String name;
    @NotBlank(message = "adresse e-mail requise")
    @Email(message = "adresse e-mail invalide")
    private String email;
    @NotBlank(message = "numéro de téléphone requis")
    private String phone;
}
