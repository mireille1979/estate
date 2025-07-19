package com.estate.repository;

import com.estate.domain.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String>, JpaSpecificationExecutor<Student> {

    Page<Student> findAllByOrderByCreationDateDesc(Pageable pageable);
    List<Student> findAllByDateOfBirthAndCurrentLeaseNotNull(LocalDate date);
    @Query("SELECT s FROM Student s WHERE s.currentLease.endDate < ?1 AND s.currentLease.nextLease IS NOT NULL")
    List<Student> findAllByHavingPendingLease(LocalDate date);

    Optional<Student> findByUserId(String userId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Student s SET s.currentLease = null WHERE s.id = ?1")
    void setCurrentLeaseToNullByStudentId(String studentId);
}
