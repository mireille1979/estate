package com.estate.domain.entity;

import com.estate.domain.enumaration.Gender;
import com.estate.domain.enumaration.Grade;
import com.estate.domain.enumaration.Relationship;
import com.estate.domain.form.Phone;
import com.estate.domain.form.StudentForm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@Entity
public class Student extends Auditable {
    @Id
    @GeneratedValue(generator = "uuid2", strategy = GenerationType.IDENTITY)
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_USER_ID"))
    private User user;
    @Column(nullable = false)
    private String placeOfBirth = "";
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;
    private String cniNumber = ""; // Numéro de la CNI
    @Column(nullable = false)
    private String cniRecto = ""; // CNI face recto
    private String cniVerso = ""; // CNI face verso
    private String birthCertificate = ""; // Acte de naissance
    private String studentCard = ""; // Carte d'étudiant
    @Column(nullable = false)
    private String school = "";
    @Convert(converter = Grade.Converter.class)
    private Grade grade = Grade.L1;
    private String matricule = "";
    @Column(nullable = false)
    private String specialities = "";

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_HOUSING_ID"))
    private Housing housing;

    @Column(nullable = false)
    @Convert(converter = Relationship.Converter.class)
    private Relationship firstParentRelation = Relationship.FATHER;
    @Column(nullable = false)
    private String firstParentName = "";
    @Column(nullable = false)
    private String firstParentAddress = "";
    @Column(nullable = false)
    private String firstParentPhone = "";
    private String firstParentMobile;
    @Column(nullable = false)
    private String firstParentEmail = "";

    @Column(nullable = false)
    @Convert(converter = Relationship.Converter.class)
    private Relationship secondParentRelation = Relationship.MOTHER;
    @Column(nullable = false)
    private String secondParentName = "";
    @Column(nullable = false)
    private String secondParentAddress = "";
    @Column(nullable = false)
    private String secondParentPhone = "";
    private String secondParentMobile;
    @Column(nullable = false)
    private String secondParentEmail = "";


    @Column(nullable = false)
    private String otp = "";
    @Column(nullable = false)
    private LocalDateTime otpExpiredAt = LocalDateTime.now();
    @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Payment> payments = new ArrayList<>();

    public Student(){
        this.user = new User();
    }

    @OneToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_LEASE_ID"))
    private Lease currentLease;

    public StudentForm toForm(){
        StudentForm form = new StudentForm();
        form.setId(id);
        form.setFirstName(Optional.ofNullable(user).map(User::getFirstName).orElse(""));
        form.setLastName(Optional.ofNullable(user).map(User::getLastName).orElse(""));
        form.setDateOfBirth(dateOfBirth);
        form.setPlaceOfBirth(placeOfBirth);
        form.setGender(Optional.ofNullable(user).map(User::getGender).orElse(Gender.MALE));
        form.setCniNumber(cniNumber);

        form.setSchool(school);
        form.setSpecialities(specialities);
        form.setGrade(grade);
        form.setMatricule(matricule);
        form.setPhone(Phone.parse(Optional.ofNullable(user).map(User::getPhone).orElse("")));
        form.setMobile(Phone.parse(Optional.ofNullable(user).map(User::getMobile).orElse("")));
        form.setEmail(Optional.ofNullable(user).map(User::getEmail).orElse(""));

        form.setFirstParentRelation(firstParentRelation);
        form.setFirstParentName(firstParentName);
        form.setFirstParentAddress(firstParentAddress);
        form.setFirstParentPhone(Phone.parse(firstParentPhone));
        form.setFirstParentMobile(Phone.parse(firstParentMobile));
        form.setFirstParentEmail(firstParentEmail);

        form.setSecondParentRelation(secondParentRelation);
        form.setSecondParentName(secondParentName);
        form.setSecondParentAddress(secondParentAddress);
        form.setSecondParentPhone(Phone.parse(secondParentPhone));
        form.setSecondParentMobile(Phone.parse(secondParentMobile));
        form.setSecondParentEmail(secondParentEmail);

        return form;
    }

    @PrePersist
    @PreUpdate
    public void beforeSave(){
        if(this.placeOfBirth != null) this.placeOfBirth = this.placeOfBirth.trim().replaceAll("\\s+", " ");
        if(this.school != null) this.school = this.school.toUpperCase().trim().replaceAll("\\s+", " ");
        if(this.specialities != null) this.specialities = this.specialities.trim().replaceAll("\\s+", " ");
        if(this.firstParentName != null) this.firstParentName = this.firstParentName.toUpperCase().trim().replaceAll("\\s+", " ");
        if(this.firstParentAddress != null) this.firstParentAddress = this.firstParentAddress.trim().replaceAll("\\s+", " ");
        if(this.firstParentEmail != null) this.firstParentEmail = this.firstParentEmail.trim();
        if(this.secondParentName != null) this.secondParentName = this.secondParentName.toUpperCase().trim().replaceAll("\\s+", " ");
        if(this.secondParentAddress != null) this.secondParentAddress = this.secondParentAddress.trim().replaceAll("\\s+", " ");
        if(this.secondParentEmail != null) this.secondParentEmail = this.secondParentEmail.trim();
        if(this.cniNumber != null) this.cniNumber = this.cniNumber.toUpperCase().replaceAll(" ", "");
        if(this.matricule != null) this.matricule = this.matricule.toUpperCase().replaceAll(" ", "");
    }

}
