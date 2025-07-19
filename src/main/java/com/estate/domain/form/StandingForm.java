package com.estate.domain.form;

import com.estate.domain.annotation.FileSize;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

@Data
public class StandingForm {
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @Min(value = 1)
    private Integer rent;
    @PositiveOrZero
    private Integer caution;
    @PositiveOrZero
    private Integer repair;
    @FileSize(extensions = {"png", "jpg", "jpeg"}, max = 2 * 1024 * 1024)
    private MultipartFile picture;
}
