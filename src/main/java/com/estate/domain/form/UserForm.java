package com.estate.domain.form;

import com.estate.domain.enumaration.Gender;
import com.estate.domain.enumaration.Mode;
import com.estate.domain.enumaration.Role;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Data
public class UserForm {
    private String id;
    @NotBlank
    private String firstName;
    private String lastName;
    @NotNull
    private Gender gender;
    @NotBlank
    @Email
    private String email;
    @NotNull
    @Valid
    private Phone phone = new Phone();
    @NotNull
    @Valid
    private Phone mobile = new Phone();
    private List<@NotNull Mode> modes;
    @NotEmpty
    private List<@NotNull Role> roles;
}
