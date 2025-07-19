package com.estate.domain.service.face;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Student;
import com.estate.domain.form.StudentForm;
import com.estate.domain.form.StudentSearch;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface StudentService {
    long count();
    Page<Student> findAll(int page);
    Page<Student> findAll(StudentSearch form);
    Optional<Student> findById(String id);
    Optional<Student> findByUserId(String userId);
    Notification save(StudentForm form);
    Notification toggleById(String id);

    Notification deleteById(String id, boolean force, HttpServletRequest request);
}
