package com.estate.domain.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FileSizeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface FileSize {

    String message() default "La taille du fichier doit être inférieure à 3 Mo";

    long max() default 3 * 1024 * 1024; // Default size is 3MB

    String[] extensions() default {};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}