package com.estate.domain.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PaymentReject {
    @NotBlank
    private String id;
    @NotBlank
    private String comment;
}
