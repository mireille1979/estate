package com.estate.domain.entity;

import com.estate.domain.enumaration.Category;
import com.estate.domain.form.HousingForm;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Optional;


@Setter
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UK_NAME", columnNames = {"name"}))
public class Housing extends Auditable {
    @Id
    @GeneratedValue(generator = "uuid2", strategy = GenerationType.IDENTITY)
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    @Column(nullable = false)
    private String name;
    @ManyToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_STANDING_ID"), nullable = false)
    private Standing standing;
    @OneToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_STUDENT_ID"))
    private Student resident;
    @Convert(converter = Category.Converter.class)
    private Category category = Category.ROOM;
    private boolean available = true;
    private boolean active = true;

    public Housing() {
    }

    public HousingForm toForm(){
        HousingForm form = new HousingForm();
        form.setId(id);
        form.setName(name);
        form.setStandingId(Optional.ofNullable(standing).map(Standing::getId).orElse(null));
        form.setCategory(category);
        form.setAvailable(available);
        return form;
    }

    @PrePersist
    @PreUpdate
    public void beforeSave(){
        if(this.name != null) this.name = this.name.trim().replaceAll("\\s+", " ").toUpperCase();
    }
}
