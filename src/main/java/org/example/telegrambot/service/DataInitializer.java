package org.example.telegrambot.service;

import jakarta.annotation.PostConstruct;
import org.example.telegrambot.entity.Reminder;
import org.example.telegrambot.repository.ReminderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer {

    private final ReminderRepository repository;

    public DataInitializer(ReminderRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {

        if (repository.count() > 0) {
            return;
        }

        Reminder r1 = new Reminder();
        r1.setChatId("962544272"); // пока твой chatId
        r1.setType("yearly");
        r1.setTitle("День рождения");
        r1.setMonthDay("01-18");

        Reminder r2 = new Reminder();
        r2.setChatId("962544272");
        r2.setType("yearly");
        r2.setTitle("Новый год");
        r2.setMonthDay("01-01");

        Reminder r3 = new Reminder();
        r3.setChatId("962544272");
        r3.setType("yearly");
        r3.setTitle("Годовщина");
        r3.setMonthDay("07-20");

        Reminder r4 = new Reminder();
        r4.setChatId("962544272");
        r4.setType("yearly");
        r4.setTitle("Др Кристины");
        r4.setMonthDay("09-12");

        Reminder r5 = new Reminder();
        r5.setChatId("962544272");
        r5.setType("yearly");
        r5.setTitle("Татьянин день");
        r5.setMonthDay("01-25");

        Reminder r6 = new Reminder();
        r6.setChatId("962544272");
        r6.setType("yearly");
        r6.setTitle("День Валентина");
        r6.setMonthDay("02-14");

        Reminder r7 = new Reminder();
        r2.setChatId("962544272");
        r2.setType("yearly");
        r2.setTitle("Рождество");
        r2.setMonthDay("01-07");

        repository.save(r1);
        repository.save(r2);
        repository.save(r3);
        repository.save(r4);
        repository.save(r5);
        repository.save(r6);
        repository.save(r7);

    }
}