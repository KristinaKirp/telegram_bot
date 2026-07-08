package org.example.telegrambot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDateTime eventDateTime;

    private String type;

    private String monthDay;

    private boolean sentToday = false;

    private String chatId; //сохранять айди чата в базе
}