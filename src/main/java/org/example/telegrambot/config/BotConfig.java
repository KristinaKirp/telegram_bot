package org.example.telegrambot.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.telegrambot.bot.Bot;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
public class BotConfig {

    private final Bot bot;

    @PostConstruct
    public void registerBot() {

        System.out.println("=== BOT CONFIG START ===");

        try {

            TelegramBotsApi botsApi =
                    new TelegramBotsApi(DefaultBotSession.class);

            System.out.println("=== API CREATED ===");

            botsApi.registerBot(bot);

            System.out.println("=== BOT REGISTERED ===");
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(60000);
                        System.out.println("BOT IS ALIVE " + java.time.LocalDateTime.now());
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}