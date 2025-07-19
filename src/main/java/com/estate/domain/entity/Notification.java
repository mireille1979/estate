package com.estate.domain.entity;

import com.estate.domain.enumaration.Level;
import lombok.Data;

@Data
public class Notification {
    private Level type = Level.INFO;
    private String message = "";

    public void setMessage(String message) {
        this.message = message;
        if(!this.message.endsWith(".")) this.message = this.message + ".";
    }

    public static Notification info() {
        return Notification.info("Opération terminée avec succès.");
    }

    public static Notification info(String message) {
        Notification notification = new Notification();
        notification.setType(Level.INFO);
        notification.setMessage(message);
        return notification;
    }

    public static Notification error(String message) {
        Notification notification = new Notification();
        notification.setType(Level.ERROR);
        notification.setMessage(message);
        return notification;
    }

    public static Notification warn(String message) {
        Notification notification = new Notification();
        notification.setType(Level.WARN);
        notification.setMessage(message);
        return notification;
    }

    public boolean hasError(){
        return Level.ERROR.equals(this.type);
    }
}
