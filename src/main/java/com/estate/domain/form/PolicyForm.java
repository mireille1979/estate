package com.estate.domain.form;

import com.estate.domain.annotation.FileSize;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PolicyForm {
    @FileSize(extensions = {"pdf"}, max = 2 * 1024 * 1024)
    private MultipartFile file;
}
