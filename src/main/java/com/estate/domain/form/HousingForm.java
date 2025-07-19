package com.estate.domain.form;

import com.estate.domain.enumaration.Category;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class HousingForm {
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String standingId;
    @NotNull
    private Category category;
    @NotNull
    private Boolean available;
}
