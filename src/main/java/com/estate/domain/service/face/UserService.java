package com.estate.domain.service.face;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.User;
import com.estate.domain.enumaration.Profil;
import com.estate.domain.form.PasswordForm;
import com.estate.domain.form.ProfilForm;
import com.estate.domain.form.UserForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface UserService {

    long countByProfil(Profil profil);

    List<User> findAllByProfil(Profil profil);

    Optional<User> findById(String id);

    Notification save(UserForm form, HttpSession session);

    Notification updateProfile(ProfilForm form, HttpSession session);

    Notification toggleById(String id);

    Notification changePassword(PasswordForm form, Principal principal);

    Notification deleteById(String id, boolean force, HttpServletRequest request);
}
