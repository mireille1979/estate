package com.estate.controller;

import com.estate.domain.entity.*;
import com.estate.domain.enumaration.Profil;
import com.estate.domain.form.LeaseSearch;
import com.estate.domain.form.MutationForm;
import com.estate.domain.service.face.HousingService;
import com.estate.domain.service.face.LeaseService;
import com.estate.domain.service.face.StandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lease")
public class LeaseController {
    private final LeaseService leaseService;
    private final StandingService standingService;
    private final HousingService housingService;

    @GetMapping("list")
    public String findAll(Model model, @RequestParam(required = false, defaultValue = "1") int page, HttpSession session, HttpServletRequest request){
        LeaseSearch form = new LeaseSearch();
        Page<Lease> leases;
        Map<String, ?> attributes = RequestContextUtils.getInputFlashMap(request);
        boolean search = attributes != null && attributes.containsKey("searchForm");
        if(search){
            form = (LeaseSearch) attributes.get("searchForm");
            leases = leaseService.findAll(form);
            if(leases.isEmpty()) model.addAttribute("notification", Notification.info("Aucun résultat"));
        } else {
            User user = (User) session.getAttribute("user");
            if(user != null && Profil.STUDENT.equals(user.getProfil())){
                leases = leaseService.findAllByUserId(user.getId(), page);
            } else {
                leases = leaseService.findAll(page);
            }
        }
        model.addAttribute("housings", housingService.findAll());
        model.addAttribute("standings", standingService.findAll());
        model.addAttribute("leases", leases.toList());
        model.addAttribute("totalPages", leases.getTotalPages());
        model.addAttribute("currentPage", leases.getNumber() + 1);
        model.addAttribute("startIndex", leases.getPageable().getOffset());
        model.addAttribute("searchForm", form);
        model.addAttribute("search", search);
        return "admin/lease/list";
    }
    
    @GetMapping("view/{id}")
    public String findById(@PathVariable String id, Model model, RedirectAttributes attributes){
        Lease lease = leaseService.findById(id).orElse(null);
        if(lease == null){
            attributes.addFlashAttribute("notification", Notification.error("Contrat de bail introuvable"));
            return "redirect:/lease/list";
        }
        model.addAttribute("lease", lease);
        return "admin/lease/view";
    }

    @ResponseBody
    @GetMapping("download/{id}")
    public ResponseEntity<?> download(@PathVariable String id){
        return leaseService.download(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'JANITOR')")
    @PostMapping("search")
    public String search(LeaseSearch form, RedirectAttributes attributes){
        attributes.addFlashAttribute("searchForm", form);
        return "redirect:/lease/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value="disable/{id}")
    public String disable(@PathVariable String id, RedirectAttributes attributes){
        Notification notification = leaseService.disable(id);
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/lease/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("save")
    public String mutation(@RequestParam String id, Model model, RedirectAttributes attributes){
        Lease lease = leaseService.findById(id).orElse(null);
        if(lease == null){
            attributes.addFlashAttribute("notification", Notification.error("Contrat de bail introuvable"));
            return "redirect:/lease/list";
        }
        MutationForm mutation = new MutationForm();
        mutation.setLeaseId(lease.getId());
        mutation.setStartDate(lease.getStartDate());
        model.addAttribute("lease", lease);
        model.addAttribute("mutation", mutation);
        model.addAttribute("housings", housingService.findAllByAvailableAndActiveTrue(true));
        return "admin/lease/save";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("save")
    public String save(@Valid @ModelAttribute("mutation") MutationForm mutation, BindingResult result, Model model, RedirectAttributes attributes, Principal principal){
        if(result.hasErrors()){
            model.addAttribute("housings", housingService.findAllByAvailableAndActiveTrue(true));
            return "admin/lease/save";
        }
        Notification notification =  leaseService.mutate(mutation, principal);
        if(notification.hasError()){
            attributes.addAttribute("id", mutation.getLeaseId());
            attributes.addFlashAttribute("notification", notification);
            return "redirect:/lease/save";
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/lease/list";
    }
}
