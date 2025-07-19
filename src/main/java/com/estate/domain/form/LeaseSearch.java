package com.estate.domain.form;

import com.estate.domain.entity.Lease;
import com.estate.domain.enumaration.State;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class LeaseSearch {
    private Integer page = 1;
    private String firstName;
    private String lastName;
    private String housingId;
    private String standingId;
    private Boolean active;
    private State state;

    public Specification<Lease> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(housingId)) {
                predicates.add(cb.equal(root.get("housing").get("id"), housingId));
            }
            if (StringUtils.isNotBlank(standingId)) {
                predicates.add(cb.equal(root.get("housing").get("standing").get("id"), standingId));
            }
            if (StringUtils.isNotBlank(firstName)) {
                predicates.add(cb.like(cb.lower(root.get("payment").get("student").get("user").get("firstName")), cb.lower(cb.literal("%" + firstName + "%"))));
            }
            if (StringUtils.isNotBlank(lastName)) {
                predicates.add(cb.like(cb.lower(root.get("payment").get("student").get("user").get("lastName")), cb.lower(cb.literal("%" + lastName + "%"))));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (state != null) {
                LocalDate today = LocalDate.now();
                switch (state){
                    case PENDING:
                        predicates.add(cb.equal(root.get("active"), true));
                        predicates.add(cb.greaterThan(root.get("startDate"), today));
                        break;
                    case RUNNING:
                        predicates.add(cb.equal(root.get("active"), true));
                        predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), today));
                        predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), today));
                        break;
                    case EXPIRED:
                        predicates.add(cb.lessThan(root.get("endDate"), today));
                        break;
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
