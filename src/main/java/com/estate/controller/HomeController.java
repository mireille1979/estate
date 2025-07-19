package com.estate.controller;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Setting;
import com.estate.domain.enumaration.SettingCode;
import com.estate.domain.form.ContactForm;
import com.estate.domain.form.VisitorForm;
import com.estate.domain.service.face.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final HousingService housingService;
    private final SettingService settingService;
    private final StandingService standingService;
    private final VisitorService visitorService;

    @Value("${spring.mail.username}")
    private String email;

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request){
        ContactForm contactForm = new ContactForm();
        VisitorForm visitorForm = new VisitorForm();
        Map<String, ?> attributes = RequestContextUtils.getInputFlashMap(request);
        if(attributes != null){
            if(attributes.containsKey("contact")) contactForm = (ContactForm) attributes.get("contact");
            if(attributes.containsKey("visitor")) visitorForm = (VisitorForm) attributes.get("visitor");
        }
        model.addAttribute("contact", contactForm);
        model.addAttribute("visitor", visitorForm);
        model.addAttribute("housings", housingService.findAll());
        model.addAttribute("standings", standingService.findAllByActiveTrueOrderByRentAsc());
        model.addAttribute("telephone", settingService.findByCode(SettingCode.TELEPHONE_PUBLIC).map(Setting::getValue).orElse(""));
        model.addAttribute("whatsapp", settingService.findByCode(SettingCode.WHATSAPP).map(Setting::getValue).orElse(""));
        model.addAttribute("email", email);
        return "index";
    }

    @PostMapping("contact")
    public String contact(@Valid @ModelAttribute("contact") ContactForm contact, BindingResult result, RedirectAttributes attributes){
        Notification notification;
        if(result.hasErrors()){
            notification = Notification.error(result.getFieldErrors().get(0).getDefaultMessage());
            attributes.addFlashAttribute("contact", contact);
        } else {
            notification = visitorService.contact(contact);
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/#contact";
    }

    @PostMapping("subscribe")
    public String subscribe(@Valid @ModelAttribute("visitor") VisitorForm visitor, BindingResult result, RedirectAttributes attributes){
        Notification notification;
        if(result.hasErrors()){
            notification = Notification.error(result.getFieldErrors().get(0).getDefaultMessage());
            attributes.addFlashAttribute("visitor", visitor);
        } else {
            notification = visitorService.subscribe(visitor);
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/";
    }
}
