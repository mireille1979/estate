package com.estate.domain.form;

import com.estate.domain.entity.Log;
import com.estate.domain.enumaration.Level;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class LogSearch {
    private Integer page = 1;
    private Level level;
    private String message;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    public Specification<Log> toSpecification() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (level != null) {
                predicates.add(cb.equal(root.get("level"), level));
            }
            if (StringUtils.isNotBlank(message)) {
                predicates.add(cb.like(cb.lower(root.get("message")), cb.lower(cb.literal("%" + message + "%"))));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("creationDate"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("creationDate"), startDate.atTime(LocalTime.MAX)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
