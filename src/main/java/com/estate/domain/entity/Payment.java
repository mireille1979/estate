package com.estate.domain.entity;

import com.estate.domain.enumaration.Mode;
import com.estate.domain.enumaration.Status;
import com.estate.domain.form.PaymentForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Payment extends Auditable {
    @Id
    @GeneratedValue(generator = "uuid2", strategy = GenerationType.IDENTITY)
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    private int rent;
    private int months = 12;
    private int caution;
    private int repair;
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_STANDING_ID"), nullable = false)
    private Standing standing;
    @Convert(converter = Mode.Converter.class)
    private Mode mode = Mode.CASH;
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_USER_ID"))
    private User validator;
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_STUDENT_ID"), nullable = false)
    private Student student;
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_HOUSING_ID"))
    private Housing desiderata;
    private String proof;
    private String comment;

    @Convert(converter = Status.Converter.class)
    private Status status = Status.INITIATED;

    @Transient
    public int getAmount(){
        return rent * months + caution + repair;
    }


    public PaymentForm toForm(){
        PaymentForm form = new PaymentForm();
        form.setId(id);
        form.setRent(rent);
        form.setMonths(months);
        form.setCaution(caution);
        form.setRepair(repair);
        form.setMode(mode);
        form.setStandingId(Optional.ofNullable(standing).map(Standing::getId).orElse(null));
        form.setStudentId(Optional.ofNullable(student).map(Student::getId).orElse(null));
        form.setDesiderataId(Optional.ofNullable(desiderata).map(Housing::getId).orElse(null));
        return form;
    }
}
