package com.estate.domain.service.impl;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Log;
import com.estate.domain.entity.User;
import com.estate.domain.enumaration.Level;
import com.estate.domain.enumaration.Profil;
import com.estate.domain.enumaration.Role;
import com.estate.domain.form.PasswordForm;
import com.estate.domain.form.ProfilForm;
import com.estate.domain.form.UserForm;
import com.estate.domain.helper.EmailHelper;
import com.estate.domain.service.face.UserService;
import com.estate.repository.LogRepository;
import com.estate.repository.PaymentRepository;
import com.estate.repository.UserRepository;
import com.estate.domain.helper.TextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Locale;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailHelper emailHelper;

    @Override
    public long countByProfil(Profil profil) {
        return userRepository.countByProfil(profil);
    }

    @Override
    public List<User> findAllByProfil(Profil profil) {
        return userRepository.findAllByProfilOrderByLastLoginDesc(profil);
    }

    @Override
    public Notification deleteById(String id, boolean force, HttpServletRequest request){
        Notification notification;
        User user = userRepository.findById(id).orElse(null);
        if(user == null) return Notification.error("Utilisateur introuvable");
        if(Profil.STUDENT.equals(user.getProfil())) return Notification.warn("Cet utilisateur n'est pas membre du personnel administratif");
        try {
            if(force) paymentRepository.setValidatorToNullByUserId(id);
            userRepository.deleteById(id);
            notification = Notification.info("L'utilisateur <b>" + user.getName() + "</b> a été supprimé");
            logRepository.save(Log.info(notification.getMessage()));
        }catch (Throwable e){
            notification = Notification.error("Erreur lors de la suppression de l'utilisateur <b>" + user.getName() + "</b>.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
            if(!force){
                String actions = "";
                if(user.isActive()) actions = "<a class='lazy-link' href='" + request.getContextPath() + "/user/toggle/" + id + "'><b>Désactiver</b></a> ou ";
                actions += "<a class='lazy-link text-danger' href='" + request.getRequestURI() + "?id=" + id + "&force=true" + "'><b>Forcer la suppression</b></a>.";
                notification = Notification.warn("Cet utilisateur est impliqué dans certains enregistrements. " + actions);
            }
        }
        return notification;
    }

    @Override
    public Notification toggleById(String id) {
        Notification notification = new Notification();
        User user = userRepository.findById(id).orElse(null);
        if(user == null) return Notification.error("Utilisateur introuvable");
        try {
            user.setActive(!user.isActive());
            userRepository.save(user);
            notification.setMessage("L'utilisateur <b>" + user.getName() + "</b> a été " + (user.isActive() ? "activé" : "désactivé") + " avec succès.");
            logRepository.save(Log.info(notification.getMessage()));
        } catch (Throwable e){
            notification = Notification.error("Erreur lors du changement de statut de l'utilisateur <b>" + user.getName() + "</b>.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }
        return notification;
    }

    @Override
    public Notification save(UserForm form, HttpSession session) {
        boolean creation = StringUtils.isBlank(form.getId());
        Notification notification = Notification.info();
        User user = creation ? new User() : userRepository.findById(form.getId()).orElse(null);
        if(user == null) return Notification.error("Utilisateur introuvable");
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setPhone(form.getPhone().format());
        user.setMobile(form.getMobile().format());
        user.setEmail(form.getEmail());
        user.setGender(form.getGender());
        user.setModes(form.getModes());
        user.setRoles(form.getRoles());
        try {
            if(creation){
                HashMap<String, Object> context = new HashMap<>();
                String password = TextUtils.generatePassword();
                user.setPassword(passwordEncoder.encode(password));
                context.put("name", user.getName());
                context.put("password", password);
                if(user.getRoles().contains(Role.ROLE_ADMIN)){
                    context.put("role", Role.ROLE_ADMIN.getName());
                }else if(user.getRoles().contains(Role.ROLE_JANITOR)) {
                    context.put("role", Role.ROLE_JANITOR.getName());
                } else {
                    context.put("role", Role.ROLE_MANAGER.getName());
                }
                context.put("link", ServletUriComponentsBuilder.fromCurrentContextPath().path("/237in").build());
                emailHelper.sendMail(user.getEmail(),"", "NOUVEAU COMPTE", "new_account.ftl", Locale.FRENCH, context, Collections.emptyList());
            }
            user = userRepository.save(user);
            notification.setMessage("L'utilisateur <b>" + user.getName() +"</b> a été " + (creation ? "ajouté." : "modifié."));
            logRepository.save(Log.info(notification.getMessage()));
            if(!creation){
                User client = (User) session.getAttribute("user");
                if(client != null && client.getId().equals(user.getId())){
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream().map(Enum::name).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                    auth = new UsernamePasswordAuthenticationToken(user.getEmail(), auth.getCredentials(), authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    session.setAttribute("user", user);
                }
            }
        } catch (Throwable e){
            notification.setType(Level.ERROR);
            String message = ExceptionUtils.getRootCauseMessage(e);
            if(StringUtils.containsIgnoreCase(message, "UK_EMAIL")){
                notification.setMessage("Adresse e-mail existante");
            } else {
                notification.setMessage("Erreur lors de la " + (creation ? "création" : "modification") + " de l'utilisateur <b>" + user.getName() + "</b>.");
                logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
            }
        }
        return notification;
    }

    @Override
    public Notification updateProfile(ProfilForm form, HttpSession session) {
        Notification notification = Notification.info();
        User user = userRepository.findById(form.getId()).orElse(null);
        if(user == null) return Notification.error("Utilisateur introuvable");
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setPhone(form.getPhone().format());
        user.setMobile(form.getMobile().format());
        user.setEmail(form.getEmail());
        user.setGender(form.getGender());
        try {
            user = userRepository.save(user);
            notification.setMessage("L'utilisateur <b>" + user.getName() +"</b> a été modifié.");
            logRepository.save(Log.info(notification.getMessage()));
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream().map(Enum::name).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            auth = new UsernamePasswordAuthenticationToken(user.getEmail(), auth.getCredentials(), authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            session.setAttribute("user", user);
        } catch (Throwable e){
            notification.setType(Level.ERROR);
            String message = ExceptionUtils.getRootCauseMessage(e);
            if(StringUtils.containsIgnoreCase(message, "UK_EMAIL")){
                notification.setMessage("Adresse e-mail existante");
            } else {
                notification.setMessage("Erreur lors de la modification de l'utilisateur <b>" + user.getName() + "</b>.");
                logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
            }
        }
        return notification;
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Notification changePassword(PasswordForm form, Principal principal) {
        Notification notification = Notification.info();
        User user = userRepository.findByEmail(Optional.ofNullable(principal).map(Principal::getName).orElse("")).orElse(null);
        if(user == null) return Notification.error("Utilisateur introuvable");
        if(!passwordEncoder.matches(form.getBefore(), user.getPassword())){
            notification = Notification.error("Mot de passe actuel incorrect");
        } else {
            user.setPassword(passwordEncoder.encode(form.getAfter()));
            userRepository.save(user);
            logRepository.save(Log.info("L'utilisateur <b>" + user.getName() + "</b> a modifié son mot de passe."));
        }
        return notification;
    }
}
