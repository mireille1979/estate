package com.estate.domain.service.face;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Payment;
import com.estate.domain.enumaration.Status;
import com.estate.domain.form.PaymentForm;
import com.estate.domain.form.PaymentReject;
import com.estate.domain.form.PaymentSearch;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

public interface PaymentService {

    Page<Payment> findAll(int page);

    Page<Payment> findAll(PaymentSearch form);

    Page<Payment> findAllByUserId(String userId, int page);

    Optional<Payment> findById(String id);

    long countByStatus(Status status);

    Notification save(PaymentForm form, HttpServletRequest request);

    List<Payment> findAllByStatusAndYear(Status status, Integer year);

    Notification validate(String id, HttpSession session);

    Notification submit(String id);

    Notification cancel(PaymentReject form, HttpSession session);

    Notification deleteById(String id, HttpServletRequest request);
}
