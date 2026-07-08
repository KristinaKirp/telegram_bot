//package org.example.telegrambot.controller;
//
//import org.example.telegrambot.bot.Bot;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//import org.telegram.telegrambots.meta.api.objects.Update;
//
//@RestController
//public class WebhookController {
//
//    private final Bot bot;
//
//    public WebhookController(Bot bot) {
//        this.bot = bot;
//    }
//
//    @PostMapping("/webhook")
//    public void handleWebhook(@RequestBody Update update) {
//        bot.onWebhookUpdateReceived(update);
//    }
//}