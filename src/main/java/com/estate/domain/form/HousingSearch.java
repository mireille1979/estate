package com.estate.domain.form;

import com.estate.domain.entity.Housing;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Data
public class HousingSearch {
    private String standingId;
    private Boolean available;

    public Specification<Housing> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(standingId)) {
                predicates.add(cb.equal(root.get("standing").get("id"), standingId));
            }
            if (available != null) {
                predicates.add(cb.equal(root.get("available"), available));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
