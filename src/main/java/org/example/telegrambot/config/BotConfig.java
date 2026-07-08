import jakarta.annotation.PostConstruct;
import org.example.telegrambot.bot.Bot;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BotConfig {

    private final Bot bot;

    public BotConfig(Bot bot) {
        this.bot = bot;
    }

    @PostConstruct
    public void registerWebhook() {
        String webhookUrl = "https://твой-сервис.onrender.com/webhook";

        // Отправляем запрос на установку webhook
        String url = "https://api.telegram.org/bot" + bot.getBotToken() + "/setWebhook?url=" + webhookUrl;

        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(url, null, String.class);
            System.out.println("Webhook registration response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}