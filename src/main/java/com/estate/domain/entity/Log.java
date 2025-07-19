package com.estate.domain.entity;

import com.estate.domain.enumaration.Level;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Log extends Auditable {
    @Id
    @GeneratedValue(generator = "uuid2", strategy = GenerationType.IDENTITY)
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;
    @Convert(converter = Level.Converter.class)
    private Level level = Level.INFO;
    @Column(columnDefinition = "TEXT")
    private String message = "";
    @Column(columnDefinition = "TEXT")
    private String details = "";

    public Log(Level level, String message) {
        this.level = level;
        this.message = message;
    }

    public Log(Level level, String message, String details) {
        this.level = level;
        this.message = message;
        this.details = details;
    }

    public static Log info(String message){
        return new Log(Level.INFO, message);
    }
    public static Log warn(String message){
        return new Log(Level.WARN, message);
    }
    public static Log error(String message, String details){
        return new Log(Level.ERROR, message, details);
    }
}
