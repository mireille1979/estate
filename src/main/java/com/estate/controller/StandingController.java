package com.estate.controller;

import com.estate.domain.entity.Notification;
import com.estate.domain.entity.Standing;
import com.estate.domain.form.StandingForm;
import com.estate.domain.service.face.StandingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/standing")
public class StandingController {
    private final StandingService standingService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @GetMapping(value="list")
    public String findAll(Model model){
        model.addAttribute("standings", standingService.findAll());
        return "admin/standing/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "save")
    public String findById(@RequestParam(required = false) String id, Model model, RedirectAttributes attributes){
        Standing standing = new Standing();
        if(StringUtils.isNotBlank(id))  standing = standingService.findById(id).orElse(null);
        if(standing == null){
            attributes.addFlashAttribute("notification", Notification.error("Standing introuvable"));
            return "redirect:/standing/list";
        }
        model.addAttribute("standing", standing.toForm());
        return "admin/standing/save";
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @GetMapping(value="view/{id}")
    public String viewById(@PathVariable String id, Model model, RedirectAttributes attributes){
        Standing standing = standingService.findById(id).orElse(null);
        if(standing == null){
            attributes.addFlashAttribute("notification", Notification.error("Standing introuvable"));
            return "redirect:/standing/list";
        }
        model.addAttribute("standing", standing);
        return "admin/standing/view";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @RequestMapping(value="toggle/{id}")
    public String toggle(@PathVariable String id, RedirectAttributes attributes){
        attributes.addFlashAttribute("notification", standingService.toggleById(id));
        return "redirect:/standing/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value="save")
    public String save(@Valid @ModelAttribute("standing") StandingForm standing, BindingResult result, @RequestParam(required = false, defaultValue = "false") boolean multiple, Model model, RedirectAttributes attributes){
        if(result.hasErrors()) return "admin/standing/save";
        Notification notification =  standingService.save(standing);
        if(multiple || notification.hasError()){
            model.addAttribute("notification", notification);
            model.addAttribute("standing", notification.hasError() ? standing : new StandingForm());
            return "admin/standing/save";
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/standing/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="delete")
    public String deleteById(@RequestParam String id, @RequestParam(required = false, defaultValue = "false") boolean force, RedirectAttributes attributes, HttpServletRequest request){
        Notification notification =  standingService.deleteById(id, force, request);
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/standing/list";
    }
}
