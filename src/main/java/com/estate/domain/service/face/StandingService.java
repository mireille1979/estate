package com.estate.domain.service.face;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Standing;
import com.estate.domain.form.StandingForm;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

public interface StandingService {
    Optional<Standing> findById(String id);

    List<Standing> findAll();

    List<Standing> findAllByActiveTrue();

    List<Standing> findAllByActiveTrueOrderByRentAsc();

    Notification deleteById(String id, boolean force, HttpServletRequest request);

    Notification save(StandingForm form);

    Notification toggleById(String id);
}
