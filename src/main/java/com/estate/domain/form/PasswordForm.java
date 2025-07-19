package com.estate.domain.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PasswordForm {
    @NotBlank
    private String before;
    @NotBlank
    private String after;
}
