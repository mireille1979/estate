package com.estate.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "UK_PAYMENT_ID", columnNames = { "payment_id"})})
public class Lease extends Auditable {
    @Id
    @GeneratedValue(generator = "uuid2", strategy = GenerationType.IDENTITY)
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate lastRememberDate;
    @OneToOne(optional = false)
    private Payment payment;
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_HOUSING_ID"))
    private Housing housing;
    @OneToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_LEASE_ID"))
    private Lease nextLease;
    private boolean active = true;

    @Transient
    public String getBackground(){
        LocalDate date = getRealEndDate();
        if(date != null) {
            LocalDate today = LocalDate.now();
            if(date.isAfter(today)) {
                long n = ChronoUnit.DAYS.between(today, date);
                if(n < 30){
                    return "bg-danger";
                } else if(n < 60){
                    return "bg-warning";
                } else if(startDate != null && (today.equals(startDate) || startDate.isBefore(today))){
                    return "bg-success";
                }
            } else {
                return "bg-danger";
            }
        }
        return "";
    }

    public LocalDate getRealEndDate(){
        return nextLease != null && nextLease.getEndDate() != null && (endDate == null || endDate.isBefore(nextLease.getEndDate())) ? nextLease.getEndDate() : endDate;
    }

    public boolean isPending(){
        LocalDate today = LocalDate.now();
        return startDate != null && endDate != null && !(today.isBefore(startDate) || today.isAfter(endDate));
    }

    public boolean isMutable(){
        return active && isPending() && nextLease == null;
    }
}
