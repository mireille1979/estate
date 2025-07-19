package com.estate.controller;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Setting;
import com.estate.domain.form.PolicyForm;
import com.estate.domain.form.SettingForm;
import com.estate.domain.service.face.SettingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/setting")
public class SettingController {
    private final SettingService settingService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @GetMapping(value="list")
    public String findAll(Model model){
        List<Setting> settings = settingService.findAll();
        settings.sort(Comparator.comparing(Setting::getCode));
        model.addAttribute("settings", settings);
        return "admin/setting/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "save")
    public String findById(@RequestParam String id, Model model, RedirectAttributes attributes){
        System.out.println("Id = " + id);
        Setting setting = settingService.findById(id).orElse(null);
        if(setting == null){
            attributes.addFlashAttribute("notification", Notification.error("Paramètre introuvable"));
            return "redirect:/setting/list";
        }
        model.addAttribute("setting", setting.toForm());
        return "admin/setting/save";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value="save")
    public String save(@Valid @ModelAttribute("setting") SettingForm setting, BindingResult result, RedirectAttributes attributes){
        if(result.hasErrors()) return "admin/setting/save";
        attributes.addFlashAttribute("notification", settingService.update(setting));
        return "redirect:/setting/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value="policy")
    public String policy(@Valid @ModelAttribute("policy") PolicyForm policy, BindingResult result, RedirectAttributes attributes){
        Notification notification;
        if(result.hasErrors()){
            notification = Notification.error(StringUtils.capitalize(Optional.ofNullable(result.getFieldError()).map(DefaultMessageSourceResolvable::getDefaultMessage).orElse("Fichier invalide")));
        } else {
            notification = settingService.savePolicy(policy.getFile());
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/setting/list";
    }
}
