package com.estate.domain.service.face;

import com.estate.domain.entity.Lease;
import com.estate.domain.entity.Notification;
import com.estate.domain.form.LeaseSearch;
import com.estate.domain.form.MutationForm;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Optional;

public interface LeaseService {
    Page<Lease> findAll(int page);

    Page<Lease> findAll(LeaseSearch form);

    Page<Lease> findAllByUserId(String userId, int page);

    Optional<Lease> findById(String id);

    ResponseEntity<?> download(String id);

    long count();

    Notification disable(String id);

    Notification mutate(MutationForm mutation, Principal principal);
}
