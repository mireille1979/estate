package com.estate.domain.service.face;

import com.estate.domain.entity.Log;
import com.estate.domain.form.LogSearch;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;

public interface LogService {
    Page<Log> findAll(int page);

    Page<Log> findAll(LogSearch form);

    Optional<Log> findById(String id);

    RedirectView deleteAllByIds(List<String> ids, RedirectAttributes attributes);
}
