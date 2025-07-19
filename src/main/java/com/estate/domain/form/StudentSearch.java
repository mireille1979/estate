package com.estate.domain.form;

import com.estate.domain.entity.Student;
import com.estate.domain.enumaration.Gender;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Data
public class StudentSearch {
    private Integer page = 1;
    private String firstName;
    private String lastName;
    private Gender gender;
    private String housingId;

    public Specification<Student> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(firstName)) {
                predicates.add(cb.like(cb.lower(root.get("firstName")), cb.lower(cb.literal("%" + firstName + "%"))));
            }
            if (StringUtils.isNotBlank(lastName)) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), cb.lower(cb.literal("%" + lastName + "%"))));
            }
            if (StringUtils.isNotBlank(housingId)) {
                predicates.add(cb.equal(root.get("housing").get("id"), housingId));
            }
            if (gender != null) {
                predicates.add(cb.equal(root.get("gender"), gender));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
