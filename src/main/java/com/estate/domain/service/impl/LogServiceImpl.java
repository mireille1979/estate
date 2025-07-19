package com.estate.domain.service.impl;

import com.estate.domain.entity.Log;
import com.estate.domain.entity.Notification;
import com.estate.domain.form.LogSearch;
import com.estate.domain.service.face.LogService;
import com.estate.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {
    private final LogRepository logRepository;

    @Override
    public Page<Log> findAll(int page){
        return logRepository.findAllByOrderByCreationDateDesc(PageRequest.of(page  - 1, 500));
    }

    @Override
    public Page<Log> findAll(LogSearch form){
        return logRepository.findAll(form.toSpecification(), PageRequest.of(form.getPage()  - 1, 500, Sort.by(Sort.Direction.DESC, "creationDate")));
    }

    @Override
    public Optional<Log> findById(String id){
        return logRepository.findById(id);
    }

    @Override
    public RedirectView deleteAllByIds(List<String> ids, RedirectAttributes attributes){
        Notification notification = Notification.info();
        try {
            int n = ids.size();
            logRepository.deleteAllById(ids);
            String plural = n > 1 ? "s" : "";
            notification.setMessage( n + " évènement" + plural + " supprimé" + plural +".");
        }catch (Exception e){
            notification = Notification.error("Erreur lors de la suppression des évènements.");
            logRepository.save(Log.error(notification.getMessage(), ExceptionUtils.getStackTrace(e)));
        }
        attributes.addFlashAttribute("notification", notification);
        return new RedirectView("/log/list", true);
    }
}
