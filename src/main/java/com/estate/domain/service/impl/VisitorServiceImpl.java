package com.estate.domain.service.impl;


import com.estate.domain.entity.User;
import com.estate.domain.entity.Visitor;
import com.estate.domain.enumaration.Profil;
import com.estate.domain.form.ContactForm;
import com.estate.domain.form.VisitorForm;
import com.estate.domain.helper.EmailHelper;
import com.estate.domain.service.face.VisitorService;
import com.estate.repository.UserRepository;
import com.estate.repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.estate.domain.entity.Notification;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitorServiceImpl implements VisitorService {
    private final VisitorRepository visitorRepository;
    private final UserRepository userRepository;
    private final EmailHelper emailHelper;

    @Override
    public Notification contact(ContactForm form){
        HashMap<String, Object> context = new HashMap<>();
        context.put("name",form.getName().trim());
        context.put("message",form.getMessage().trim());
        context.put("number",form.getPhone().trim());
        context.put("email",form.getEmail().trim());
        String receiver = userRepository.findByProfil(Profil.STAFF).stream().map(User::getEmail).collect(Collectors.joining(","));
        emailHelper.sendMail(form.getEmail().trim(), "", "PRISE DE CONTACT - MINI CITE CONCORDE", "contact_user.ftl", Locale.FRENCH, context, Collections.emptyList());
        emailHelper.sendMail(receiver,"", form.getSubject(),"contact_staff.ftl",Locale.FRENCH, context, Collections.emptyList());
        return Notification.info("Votre message a été envoyé");
    }

    @Override
    public Notification subscribe(VisitorForm form){
        Visitor visitor = visitorRepository.findByEmail(form.getEmail().trim()).orElse(new Visitor());
        boolean sendNotification = StringUtils.isBlank(visitor.getId());
        visitor.setEmail(form.getEmail().trim());
        visitor.setName(form.getName().trim());
        visitor.setPhone(form.getPhone().trim());
        visitorRepository.save(visitor);
        if(sendNotification) {
            HashMap<String, Object> context = new HashMap<>();
            context.put("name", visitor.getName());
            emailHelper.sendMail(visitor.getEmail(),"","SOUSCRIPTION - MINI CITE CONCORDE","visitor.ftl", Locale.FRENCH, context, Collections.emptyList());
        }
        return Notification.info("Votre souscription a été enregistrée.");
    }

}
