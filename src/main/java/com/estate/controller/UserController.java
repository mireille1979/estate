package com.estate.controller;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.User;
import com.estate.domain.enumaration.Profil;
import com.estate.domain.exception.NotFoundException;
import com.estate.domain.form.PasswordForm;
import com.estate.domain.form.ProfilForm;
import com.estate.domain.form.UserForm;
import com.estate.domain.helper.SmsHelper;
import com.estate.domain.service.face.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @GetMapping(value="list")
    public String findAll(Model model){
        model.addAttribute("users", userService.findAllByProfil(Profil.STAFF));
        return "admin/user/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @GetMapping(value="view/{id}")
    public String viewById(@PathVariable String id, Model model, RedirectAttributes attributes){
        User user = userService.findById(id).orElse(null);
        if(user == null){
            attributes.addFlashAttribute("notification", Notification.error("Utilisateur introuvable."));
            return "redirect:/user/list";
        }
        model.addAttribute("user", user);
        return "admin/user/view";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "save")
    public String findById(@RequestParam(required = false) String id, Model model, RedirectAttributes attributes){
        User user = new User();
        if(StringUtils.isNotBlank(id))  user = userService.findById(id).orElse(null);
        if(user == null){
            attributes.addFlashAttribute("notification", Notification.error("Utilisateur introuvable"));
            return "redirect:/user/list";
        }
        model.addAttribute("user", user.toForm());
        model.addAttribute("countryCodes", SmsHelper.countryCodes);
        return "admin/user/save";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value="save")
    public String save(@Valid @ModelAttribute("user") UserForm user, BindingResult result, @RequestParam(required = false, defaultValue = "false") boolean multiple, Model model, RedirectAttributes attributes, HttpSession session){
        if(result.hasErrors()){
            model.addAttribute("countryCodes", SmsHelper.countryCodes);
            return "admin/user/save";
        }
        Notification notification =  userService.save(user, session);
        if(multiple || notification.hasError()){
            model.addAttribute("notification", notification);
            model.addAttribute("user", notification.hasError() ? user : new UserForm());
            model.addAttribute("countryCodes", SmsHelper.countryCodes);
            return "admin/user/save";
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/user/list";
    }

    @PostMapping(value="profile")
    public String saveProfile(@Valid @ModelAttribute("user") ProfilForm user, BindingResult result, Model model, HttpSession session){
        model.addAttribute("countryCodes", SmsHelper.countryCodes);
        if(result.hasErrors()){
            return "admin/user/profile";
        }
        Notification notification =  userService.updateProfile(user, session);
        model.addAttribute("notification", notification);
        model.addAttribute("user", ((User) session.getAttribute("user")).toProfilForm());
        return "admin/user/profile";
    }

    @GetMapping(value="profile")
    public String profile(Model model, HttpSession session){
        User user = (User) session.getAttribute("user");
        if(user == null) throw new NotFoundException();
        model.addAttribute("user", user.toProfilForm());
        model.addAttribute("countryCodes", SmsHelper.countryCodes);
        return "admin/user/profile";
    }


    @GetMapping(value="change/password")
    public String changePassword(Model model) {
        model.addAttribute("passwordForm", new PasswordForm());
        return "admin/user/password";
    }

    @PostMapping(value="change/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordForm form, BindingResult result, Model model, Principal principal){
        if(result.hasErrors()) return "admin/user/password";
        Notification notification = userService.changePassword(form, principal);
        model.addAttribute("notification", notification);
        model.addAttribute("passwordForm", notification.hasError() ? form : new PasswordForm());
        return "admin/user/password";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="toggle/{id}")
    public String toggle(@PathVariable String id, RedirectAttributes attributes){
        attributes.addFlashAttribute("notification", userService.toggleById(id));
        return "redirect:/user/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="delete")
    public String deleteById(@RequestParam String id, @RequestParam(required = false, defaultValue = "false") boolean force, RedirectAttributes attributes, HttpServletRequest request){
        Notification notification =  userService.deleteById(id, force, request);
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/user/list";
    }
}
