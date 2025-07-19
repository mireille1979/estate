package com.estate.domain.service.face;

import com.estate.domain.form.ObitResponse;

public interface NotificationService {
    ObitResponse sendSMS(String sender, String destination, String message);
}
