package com.estate.controller;

import com.estate.domain.entity.Log;
import com.estate.domain.exception.NotFoundException;
import com.estate.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Setter
@Controller
@ControllerAdvice
@RequiredArgsConstructor
public class ErrorControllerImpl implements ErrorController {
    private final LogRepository logRepository;

    @ExceptionHandler(NotFoundException.class)
    @RequestMapping({"/error/{statusCode}","/error"})
    public String handleError(@PathVariable(name = "statusCode", required = false) Integer statusCode, HttpServletRequest request, Model model, Exception exception) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) return "redirect:/";
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null && statusCode == null) statusCode = Integer.valueOf(status.toString());
        String title = "Erreur";
        String details = "Une erreur s'est produite lors de cette opération. Veuillez contacter votre administrateur.";
        switch (statusCode) {
            case 401:
            case 403:
                title = "Accès refusé";
                details = "Vous n'avez pas les droits pour accéder à cette page. Veuillez contacter votre administrateur.";
                break;
            case 404:
                title = "Page Introuvable";
                details = "La page ou la ressource sollicitée est introuvable.";
                break;
            case 500:
                title = "Erreur Serveur";
                details = "Une erreur s'est produite sur le serveur.";
                logRepository.save(Log.error(details, ExceptionUtils.getStackTrace(exception)));
                break;
            default:
                break;
        }
        model.addAttribute("title", title);
        model.addAttribute("details", details);
        return "error";
    }
}