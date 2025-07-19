package com.estate.domain.entity;

import com.estate.domain.form.StandingForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_NAME", columnNames = { "name"})})
public class Standing extends Auditable {
    @Id
    @GeneratedValue(generator = "uuid2", strategy = GenerationType.IDENTITY)
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    @Column(nullable = false)
    private String name = "";
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description = "";
    private String picture;
    @Column(nullable = false)
    private int rent;
    @Column(nullable = false)
    private int caution;
    @Column(nullable = false)
    private int repair;
    private boolean active = true;

    public StandingForm toForm(){
        StandingForm form = new StandingForm();
        form.setId(id);
        form.setName(name);
        form.setDescription(description);
        form.setRent(rent);
        form.setCaution(caution);
        form.setRepair(repair);
        return form;
    }

    @PrePersist
    @PreUpdate
    public void beforeSave(){
        if(this.name != null) this.name = this.name.trim().replaceAll("\\s+", " ").toUpperCase();
    }
}
