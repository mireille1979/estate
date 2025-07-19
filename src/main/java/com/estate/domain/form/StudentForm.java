package com.estate.domain.form;

import com.estate.domain.annotation.FileSize;
import com.estate.domain.enumaration.Gender;
import com.estate.domain.enumaration.Grade;
import com.estate.domain.enumaration.Relationship;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class StudentForm {
    private String id;
    @NotBlank
    private String firstName;
    private String lastName;
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;
    @NotBlank
    private String placeOfBirth;
    @NotNull
    private Gender gender;
    @NotBlank
    private String cniNumber;
    @FileSize(extensions = {"pdf", "png", "jpg", "jpeg"}, max = 2 * 1024 * 1024)
    private MultipartFile birthCertificateFile;
    @FileSize(extensions = {"pdf", "png", "jpg", "jpeg"}, max = 2 * 1024 * 1024)
    private MultipartFile cniRectoFile;
    @NotNull
    @FileSize(extensions = {"pdf", "png", "jpg", "jpeg"}, max = 2 * 1024 * 1024)
    private MultipartFile cniVersoFile;


    @NotBlank
    private String school;
    @NotBlank
    private String specialities;
    @NotNull
    private Grade grade;
    @NotBlank
    private String matricule;
    @FileSize(extensions = {"pdf", "png", "jpg", "jpeg"}, max = 2 * 1024 * 1024)
    private MultipartFile studentCardFile;

    @NotBlank
    @Email
    private String email;
    @NotNull
    @Valid
    private Phone phone = new Phone();
    @NotNull
    @Valid
    private Phone mobile = new Phone();

    @NotNull
    private Relationship firstParentRelation;
    @NotBlank
    private String firstParentName;
    @NotBlank
    private String firstParentAddress;
    @NotNull
    @Valid
    private Phone firstParentPhone = new Phone();
    @NotNull
    @Valid
    private Phone firstParentMobile = new Phone();
    @NotBlank
    @Email
    private String firstParentEmail;

    @NotNull
    private Relationship secondParentRelation;
    @NotBlank
    private String secondParentName;
    @NotBlank
    private String secondParentAddress;
    @NotNull
    @Valid
    private Phone secondParentPhone = new Phone();
    @NotNull
    @Valid
    private Phone secondParentMobile = new Phone();
    @NotBlank
    @Email
    private String secondParentEmail;

}
