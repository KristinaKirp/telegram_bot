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
        try {
            TelegramBotsApi botsApi =
                    new TelegramBotsApi(DefaultBotSession.class);

            botsApi.registerBot(bot);

            System.out.println("Бот запущен");
            System.out.println("Username = " + bot.getBotUsername());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}