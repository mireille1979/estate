package com.estate.domain.service.face;

import com.estate.domain.entity.Notification;
import com.estate.domain.form.ContactForm;
import com.estate.domain.form.VisitorForm;

public interface VisitorService {
    Notification contact(ContactForm form);
    Notification subscribe(VisitorForm form);
}
