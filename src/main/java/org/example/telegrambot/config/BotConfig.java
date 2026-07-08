package org.example.telegrambot.config;

import jakarta.annotation.PostConstruct;
import org.example.telegrambot.bot.Bot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BotConfig {

    private final Bot bot;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.webhook.url}")
    private String webhookUrl;

    public BotConfig(Bot bot) {
        this.bot = bot;
    }

    @PostConstruct
    public void registerWebhook() {
        try {
            String url = "https://api.telegram.org/bot" + botToken +
                    "/setWebhook?url=" + webhookUrl;

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(url, null, String.class);

            System.out.println("Webhook registration response: " + response);
        } catch (Exception e) {
            System.err.println("Webhook registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.example.telegrambot.bot.Bot;
//import org.springframework.context.annotation.Configuration;
//import org.telegram.telegrambots.meta.TelegramBotsApi;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
//
//@Configuration
//@RequiredArgsConstructor
//public class BotConfig {
//
//    private final Bot bot;
//
//    @PostConstruct
//    public void registerBot() {
//        try {
//            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
//            botsApi.registerBot(bot);
//            System.out.println("Бот зарегистрирован в polling режиме!");
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//            System.err.println("Ошибка регистрации бота: " + e.getMessage());
//        }
//    }
//}