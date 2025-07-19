package com.estate.repository;

import com.estate.domain.enumaration.Status;
import com.estate.domain.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String>, JpaSpecificationExecutor<Payment> {
    Page<Payment> findAllByOrderByCreationDateDesc(Pageable pageable);
    long countAllByStatus(Status status);

    Page<Payment> findAllByStudentUserIdOrderByCreationDateDesc(String userId, Pageable pageable);
    List<Payment> findAllByStatusAndCreationDateBetweenOrderByCreationDateDesc(Status status, LocalDateTime startDate, LocalDateTime endDate);

    @Transactional
    @Modifying(clearAutomatically = true)
    void deleteAllByStandingId(String standingId);

    @Transactional
    @Modifying(clearAutomatically = true)
    void deleteAllByStudentId(String studentId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Payment p SET p.validator = null WHERE p.validator.id = ?1")
    void setValidatorToNullByUserId(String userId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Payment p SET p.desiderata = null WHERE p.desiderata.id = ?1")
    void setDesiderataToNullByHousingId(String housingId);

}
