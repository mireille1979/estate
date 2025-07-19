package com.estate.domain.annotation;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FileSizeValidator implements ConstraintValidator<FileSize, MultipartFile> {

    private long max;
    private String[] extensions;

    @Override
    public void initialize(FileSize constraintAnnotation) {
        this.max = constraintAnnotation.max();
        this.extensions = constraintAnnotation.extensions();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        boolean valid = true;
        String message = "";
        if(file != null && !file.isEmpty()) {
            if(file.getSize() > max) {
                valid = false;
                message = "taille maximale " + readableSize(max);
            } else if(extensions.length > 0 && Arrays.stream(extensions).noneMatch(extension -> StringUtils.defaultString(extension).equalsIgnoreCase(FilenameUtils.getExtension(file.getOriginalFilename())))) {
                valid = false;
                String formats = Arrays.stream(extensions).map(extension -> StringUtils.defaultString(extension, "pdf").toUpperCase()).distinct().collect(Collectors.joining(" , "));
                message = "accepte uniquement" + (formats.split(",").length > 1 ? " les formats " : " le format ") + formats;
            }
        }
        if(!valid){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        }
        return valid;
    }

    public static String readableSize(Long size) {
        if(size == null || size <= 0) return "0 Ko";
        final String[] units = new String[] { "o", "Ko", "Mo", "Go", "To" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
