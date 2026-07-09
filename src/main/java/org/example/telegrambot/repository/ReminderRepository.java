package org.example.telegrambot.repository;

import lombok.NonNull;
import org.example.telegrambot.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByEventDateTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<Reminder> findByChatId(String chatId);

    List<Reminder> findByChatIdAndType(String chatId, String type);

    List<Reminder> findBySentFalseAndRemindAtBefore(LocalDateTime now);
}