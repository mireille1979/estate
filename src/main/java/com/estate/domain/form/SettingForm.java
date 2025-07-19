package com.estate.domain.form;

import com.estate.domain.enumaration.SettingCode;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SettingForm {
    @NotBlank
    private String id;
    @NotNull
    private SettingCode code;
    @NotBlank
    private String value;
}
