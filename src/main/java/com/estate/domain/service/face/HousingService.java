package com.estate.domain.service.face;

import com.estate.domain.entity.Housing;
import com.estate.domain.entity.Notification;
import com.estate.domain.form.HousingForm;
import com.estate.domain.form.HousingSearch;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

public interface HousingService {

    List<Housing> findAll();

    List<Housing> findAllByStandingIdAndActiveTrue(String standingId);

    List<Housing> findAllByAvailableAndActiveTrue(boolean available);

    List<Housing> findAll(HousingSearch form);

    Optional<Housing> findById(String id);

    Notification deleteById(String id, boolean force, HttpServletRequest request);

    long count();

    Notification save(HousingForm form);

    Notification toggleById(String id);

    Notification liberate(String id, HttpServletRequest request);
}
