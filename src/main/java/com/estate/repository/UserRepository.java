package com.estate.repository;


import com.estate.domain.entity.User;
import com.estate.domain.enumaration.Profil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByToken(String token);
    List<User> findAllByProfilOrderByLastLoginDesc(Profil profil);

    List<User> findByProfil(Profil profil);

    long countByProfil(Profil profil);
}