package com.estate.domain.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObitResponse {
    private Long id;
    private int code;
    private String message;
    private boolean success;
}
