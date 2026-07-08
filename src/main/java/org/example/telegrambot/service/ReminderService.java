package org.example.telegrambot.service;

import org.example.telegrambot.bot.Bot;
import org.example.telegrambot.entity.Reminder;
import org.example.telegrambot.repository.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReminderService {

    private final ReminderRepository repository;
    private final Bot bot;

    public ReminderService(ReminderRepository repository, Bot bot) {
        this.repository = repository;
        this.bot = bot;
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void checkYearlyReminders() {
        System.out.println("Проверка напоминаний...");
        List<Reminder> reminders = repository.findAll();

        for (Reminder r : reminders) {

            if ("yearly".equals(r.getType())) {

                String monthDay = r.getMonthDay();

                if (monthDay == null) {
                    continue;
                }

                String[] parts = monthDay.split("-");

                int month = Integer.parseInt(parts[0]);
                int day = Integer.parseInt(parts[1]);

                LocalDate today = LocalDate.now();

                LocalDate eventDate = LocalDate.of(
                        today.getYear(),
                        month,
                        day
                );

                if (eventDate.isBefore(today)) {
                    eventDate = eventDate.plusYears(1);
                }

                long daysBetween =
                        ChronoUnit.DAYS.between(today, eventDate);

                if (daysBetween == 7 ||
                        daysBetween == 3 ||
                        daysBetween == 1) {

                    send(
                            r,
                            "Напоминание через "
                                    + daysBetween
                                    + " дней: "
                                    + r.getTitle()
                    );
                }

                if (daysBetween == 0) {

                    send(
                            r,
                            "Сегодня: "
                                    + r.getTitle()
                    );
                }
            }
        }
    }
    @Scheduled(fixedRate = 60000)
    public void checkOnceReminders() {
        System.out.println("Проверка напоминаний...");
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> reminders = repository.findAll();
        for(Reminder r : reminders) {

            if ("once".equals(r.getType())) {
                LocalDateTime event = r.getEventDateTime();

                if (event == null) {
                    continue;
                }

                System.out.println("Сейчас: " + now);
                System.out.println("Напоминание: " + event);

                if (now.truncatedTo(ChronoUnit.MINUTES)
                        .equals(event.truncatedTo(ChronoUnit.MINUTES))) {

                    send(r, "Сегодня: " + r.getTitle());

                    repository.delete(r);
                }
            }
        }

    }

    private void send(Reminder r,String text) {

        System.out.println("Отправка: " + text);
        try {
            bot.execute(
                    new SendMessage(r.getChatId(), text)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}