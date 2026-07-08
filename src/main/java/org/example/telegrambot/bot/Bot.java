package org.example.telegrambot.bot;

import org.example.telegrambot.entity.Reminder;
import org.example.telegrambot.model.DraftReminder;
import org.example.telegrambot.repository.ReminderRepository;
import org.example.telegrambot.state.UserState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class Bot extends TelegramWebhookBot {

    private final ReminderRepository repository;

    private final Map<String, UserState> userState = new HashMap<>();
    private final Map<String, DraftReminder> drafts = new HashMap<>();

    public Bot(ReminderRepository repository) {
        this.repository = repository;
    }

    @Value("${bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return "napominalka07_bot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return "/webhook";  // ← путь, куда Telegram будет стучаться
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update);
        }
        return null;
    }


    private void handleMessage(Update update) {

        String text = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();

        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        UserState state = userState.get(chatId);

        // СЮДА переносим ВСЕ if(text.equals(...))

        if (text.equals("/start")) {

            message.setText("Добро пожаловать!\nВыберите действие.");
            message.setReplyMarkup(createMainKeyboard());
        }
        else if (text.equals("Добавить")) {

            SendMessage msg = new SendMessage();
            msg.setChatId(chatId);

            msg.setText("Какое напоминание создадим?");
            msg.setReplyMarkup(createTypeKeyboard());

            sendMessage(msg);
            return;
        }
        else if (text.equals("Мои напоминания")) {
            List<Reminder> reminders = repository.findByChatId(chatId);

            if (reminders.isEmpty()) {
                message.setText("Упси, у тебя нет никаких напоминаний(");
            }
            else {

                StringBuilder br = new StringBuilder();
                br.append("Напоминания :3\n\n");
                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                boolean hasOnce = reminders.stream()
                        .anyMatch(r -> "once".equals(r.getType()));
                if (hasOnce) {
                    br.append("Одноразовые:\n\n");
                    for (Reminder r : reminders) {

                        if ("once".equals(r.getType())) {

                            br.append(r.getTitle())
                                    .append("\n");


                            br.append(r.getEventDateTime().format(formatter))
                                    .append("\n\n");
                        }
                    }
                }
                boolean hasYearly = reminders.stream()
                        .anyMatch(r -> "yearly".equals(r.getType()));
                if (hasYearly) {
                    if (hasOnce) {
                        br.append("━━━━━━━━━━━━━━\n");
                    }
                    br.append("Ежегодные: \n\n");
                    for (Reminder r : reminders) {
                        if ("yearly".equals(r.getType())) {

                            br.append(r.getTitle())
                                    .append("\n");
                            String[] date = r.getMonthDay().split("-");
                            br.append(date[1]).append(".").append(date[0])
                                    .append("\n\n");
                        }
                    }
                }

                message.setText(br.toString());
                message.setReplyMarkup(createReminderMenuKeyboard());

                userState.put(chatId, UserState.VIEW_REMINDERS);


            }
        }
        else if (text.equals("Назад")) {
            System.out.println("НАЗАД. STATE = " + state);
            if (state == UserState.VIEW_REMINDERS) {

                message.setText("Главное меню");
                message.setReplyMarkup(createMainKeyboard());

                userState.put(chatId, UserState.NONE);
            }

            else if (state == UserState.DELETE_TYPE
                    || state == UserState.EDIT_TYPE) {

                message.setText("Главное меню");
                message.setReplyMarkup(createMainKeyboard());

                userState.put(chatId, UserState.NONE);
            }

            else if (state == UserState.EDIT_SELECT) {

                message.setText("Выберите тип напоминаний:");
                message.setReplyMarkup(createReminderTypeKeyboard());

                userState.put(chatId, UserState.EDIT_TYPE);
            }

            else if (state == UserState.EDIT_MENU) {

                List<Reminder> reminders = repository.findByChatIdAndType(
                        chatId,
                        drafts.get(chatId).getType()
                );

                message.setText("Выберите напоминание:");
                message.setReplyMarkup(createReminderInlineKeyboard(reminders, "EDIT"));

                userState.put(chatId, UserState.EDIT_SELECT);
            }

            else if (state == UserState.EDIT_TITLE
                    || state == UserState.EDIT_TIME
                    || state == UserState.EDIT_MONTH
                    || state == UserState.EDIT_DAY) {

                DraftReminder draft = drafts.get(chatId);

                message.setText("Что хотите изменить?");
                message.setReplyMarkup(createEditKeyboard(draft.getType()));

                userState.put(chatId, UserState.EDIT_MENU);
            }
            else if (state == UserState.EDIT_SELECT) {

                message.setText("Выберите тип напоминаний:");
                message.setReplyMarkup(createReminderTypeKeyboard());

                userState.put(chatId, UserState.EDIT_TYPE);
            }
        }
        else if (text.equals("Удалить")
                && state == UserState.VIEW_REMINDERS) {
            message.setText("Выберите тип напоминаний:");

            message.setReplyMarkup(createReminderTypeKeyboard());

            userState.put(chatId, UserState.DELETE_TYPE);
        }
        else if (text.equals("Редактировать")) {
            message.setText("Выберите тип напоминаний:");

            message.setReplyMarkup(createReminderTypeKeyboard());

            userState.put(chatId, UserState.EDIT_TYPE);
        }
        else if (text.equals("Название")) {

            message.setText("Введите новое название:");

            userState.put(chatId, UserState.EDIT_TITLE);
        }
        else if (text.equals("Дата")) {

            message.setText("Выберите новый месяц:");

            userState.put(chatId, UserState.EDIT_MONTH);

            message.setReplyMarkup(createMonthsInline());
        }
        else if (text.equals("Время")) {
            userState.put(chatId, UserState.EDIT_HOUR);

            message.setText("Выберите новый час:");
            message.setReplyMarkup(createHoursKeyboard());
        }
        else if (text.equals("Одноразовые")
                && state == UserState.DELETE_TYPE) {

            DraftReminder draft =
                    drafts.getOrDefault(chatId, new DraftReminder());

            draft.setType("once");

            drafts.put(chatId, draft);

            showRemindersForDelete(chatId, "once");

            return;
        }

        else if (text.equals("Ежегодные")
                && state == UserState.DELETE_TYPE) {

            DraftReminder draft =
                    drafts.getOrDefault(chatId, new DraftReminder());

            draft.setType("yearly");

            drafts.put(chatId, draft);

            showRemindersForDelete(chatId, "yearly");

            return;
        }
        else if (text.equals("Одноразовые")
                && state == UserState.EDIT_TYPE) {

            DraftReminder draft =
                    drafts.getOrDefault(chatId, new DraftReminder());

            draft.setType("once");

            drafts.put(chatId, draft);

            List<Reminder> reminders =
                    repository.findByChatIdAndType(chatId, "once");

            message.setText("Выберите напоминание:");

            message.setReplyMarkup(
                    createReminderInlineKeyboard(reminders, "EDIT")
            );
            userState.put(chatId, UserState.EDIT_SELECT);
        }
        else if (text.equals("Ежегодные")
                && state == UserState.EDIT_TYPE) {

            DraftReminder draft =
                    drafts.getOrDefault(chatId, new DraftReminder());

            draft.setType("yearly");

            drafts.put(chatId, draft);

            List<Reminder> reminders =
                    repository.findByChatIdAndType(chatId, "yearly");

            message.setText("Выберите напоминание:");

            message.setReplyMarkup(
                    createReminderInlineKeyboard(reminders, "EDIT")
            );
            userState.put(chatId, UserState.EDIT_SELECT);
        }
        else if (state == UserState.TIME) {

            try {

                int minute = Integer.parseInt(text);

                if (minute < 0 || minute > 59) {

                    message.setText("Минуты должны быть от 00 до 59.");
                    sendMessage(message);
                    return;
                }

                DraftReminder draft = drafts.get(chatId);

                String fullTime = draft.getTime() + ":" + String.format("%02d", minute);

                draft.setTime(fullTime);

                userState.put(chatId, UserState.TITLE);

                message.setText("Введите текст напоминания:");

            }
            catch (NumberFormatException e) {

                message.setText("Введите число от 00 до 59.");
            }
        }

        else if (state == UserState.TITLE) {

            try {
                DraftReminder draft = drafts.get(chatId);

                if (draft == null) {
                    message.setText("Ошибка: черновик пуст");
                    return;
                }

                String date = draft.getDate();   // "06-06"
                String time = draft.getTime();   // "23:42"
                String type = draft.getType();

                Reminder r = new Reminder();

                r.setChatId(chatId);
                r.setTitle(text);
                r.setSentToday(false);

                if ("once".equals(type)) {

                    String[] dm = date.split("-");

                    int day = Integer.parseInt(dm[1]);
                    int month = Integer.parseInt(dm[0]);
                    int year = LocalDateTime.now(ZoneId.of("Europe/Samara")).getYear();

                    LocalDateTime dt = LocalDateTime.of(
                            year,
                            month,
                            day,
                            Integer.parseInt(time.split(":")[0]),
                            Integer.parseInt(time.split(":")[1])
                    );

                    r.setType("once");
                    r.setEventDateTime(dt);

                }
                else if ("yearly".equals(type)) {

                    r.setType("yearly");
                    r.setMonthDay(date);
                }


                repository.save(r);

                drafts.remove(chatId);
                userState.remove(chatId);

                SendMessage ok = new SendMessage();
                ok.setChatId(chatId);
                ok.setText("Напоминание сохранено");

                execute(ok);

                return;

            } catch (Exception e) {
                e.printStackTrace();

                SendMessage err = new SendMessage();
                err.setChatId(chatId);
                err.setText("Ошибка формата даты/времени");


                return;
            }
        }
        else if (state == UserState.EDIT_TITLE) {

            DraftReminder draft = drafts.get(chatId);

            if (draft == null) {
                message.setText("Ошибка");
                sendMessage(message);
                return;
            }

            Reminder reminder = repository
                    .findById(draft.getReminderId())
                    .orElse(null);

            if (reminder == null) {
                message.setText("Напоминание не найдено");
                sendMessage(message);
                return;
            }

            reminder.setTitle(text);

            repository.save(reminder);

            message.setText("Название изменено.");

            message.setReplyMarkup(createMainKeyboard());

            userState.put(chatId, UserState.NONE);

            drafts.remove(chatId);
        }
        else if (state == UserState.EDIT_TIME) {
            try {

                int minute = Integer.parseInt(text);

                if (minute < 0 || minute > 59) {

                    message.setText("Минуты должны быть от 00 до 59.");
                    sendMessage(message);
                    return;
                }

                DraftReminder draft = drafts.get(chatId);

                Reminder reminder = repository
                        .findById(draft.getReminderId())
                        .orElse(null);

                if (reminder == null) {

                    message.setText("Напоминание не найдено.");
                    sendMessage(message);
                    return;
                }

                int hour = Integer.parseInt(draft.getTime());

                LocalDateTime oldDate = reminder.getEventDateTime().atZone(ZoneId.of("Europe/Samara"))
                        .toLocalDateTime();;

                LocalDateTime newDate = LocalDateTime.of(
                        oldDate.getYear(),
                        oldDate.getMonth(),
                        oldDate.getDayOfMonth(),
                        hour,
                        minute
                );

                reminder.setEventDateTime(newDate);

                repository.save(reminder);

                drafts.remove(chatId);
                userState.put(chatId, UserState.NONE);

                message.setText("Время изменено.");
                message.setReplyMarkup(createMainKeyboard());

            } catch (NumberFormatException e) {

                message.setText("Введите число от 00 до 59.");
            }
        }
        sendMessage(message);
    }

    private void handleCallback(Update update) {

        CallbackQuery cq = update.getCallbackQuery();

        String data = cq.getData();
        System.out.println(data);
        String chatId =
                cq.getMessage().getChatId().toString();
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        if (data.equals("BACK")) {
            if (userState.get(chatId) == UserState.MONTH) {

                userState.put(chatId, UserState.NONE);

                drafts.remove(chatId);

                msg.setText("Какое напоминание создадим?");
                msg.setReplyMarkup(createTypeKeyboard());

                sendMessage(msg);
                return;
            }
            if (userState.get(chatId) == UserState.DAY) {

                userState.put(chatId, UserState.MONTH);

                DraftReminder draft = drafts.get(chatId);

                draft.setDate(null);

                msg.setText("Выберите месяц:");
                msg.setReplyMarkup(createMonthsInline());

                sendMessage(msg);
                return;
            }
            if (userState.get(chatId) == UserState.HOUR) {

                userState.put(chatId, UserState.DAY);

                DraftReminder draft = drafts.get(chatId);

                String month = draft.getDate().split("-")[0];

                draft.setDate(month);

                msg.setText("Выберите день:");
                msg.setReplyMarkup(createCalendarDays(Integer.parseInt(month)));

                sendMessage(msg);
                return;
            }

        }
        if (data.equals("TYPE_ONCE")) {

            DraftReminder draft =
                    drafts.getOrDefault(chatId, new DraftReminder());

            draft.setType("once");

            drafts.put(chatId, draft);

            userState.put(chatId, UserState.MONTH);

            msg.setText("Выбери месяц:");
            msg.setReplyMarkup(createMonthsInline());
        }
        else if (data.equals("TYPE_YEARLY")) {

            DraftReminder draft =
                    drafts.getOrDefault(chatId, new DraftReminder());

            draft.setType("yearly");

            drafts.put(chatId, draft);

            userState.put(chatId, UserState.MONTH);

            msg.setText("Выбери месяц:");
            msg.setReplyMarkup(createMonthsInline());
        }
        else if (data.startsWith("MONTH_")) {

            String month = data.replace("MONTH_", "");

            DraftReminder draft =
                    drafts.getOrDefault(chatId, new DraftReminder());

            draft.setDate(month);

            drafts.put(chatId, draft);

            if (userState.get(chatId) == UserState.EDIT_MONTH) {

                userState.put(chatId, UserState.EDIT_DAY);

            } else {

                userState.put(chatId, UserState.DAY);
            }

            msg.setText("Выберите день:");
            int monthNumber = Integer.parseInt(month);

            msg.setReplyMarkup(
                    createCalendarDays(monthNumber)
            );
        }

        else if (data.startsWith("DAY_")) {

            String day = data.replace("DAY_", "");

            DraftReminder draft = drafts.get(chatId);

            draft.setDate(draft.getDate() + "-" + day);

            if (userState.get(chatId) == UserState.EDIT_DAY) {

                Reminder reminder = repository
                        .findById(draft.getReminderId())
                        .orElse(null);

                if (reminder == null) {

                    msg.setText("Напоминание не найдено.");

                    sendMessage(msg);
                    return;
                }

                if ("once".equals(reminder.getType())) {

                    String[] parts = draft.getDate().split("-");

                    int month = Integer.parseInt(parts[0]);
                    int dayNumber = Integer.parseInt(parts[1]);

                    LocalDateTime oldDate = reminder.getEventDateTime().atZone(ZoneId.of("Europe/Samara"))
                            .toLocalDateTime();;

                    LocalDateTime newDate = LocalDateTime.of(
                            oldDate.getYear(),
                            month,
                            dayNumber,
                            oldDate.getHour(),
                            oldDate.getMinute()
                    );

                    reminder.setEventDateTime(newDate);

                } else {

                    reminder.setMonthDay(draft.getDate());
                }

                repository.save(reminder);

                msg.setText("Дата изменена.");

                msg.setReplyMarkup(createMainKeyboard());

                userState.put(chatId, UserState.NONE);

                drafts.remove(chatId);

                sendMessage(msg);
                return;
            }

            // ---------- создание нового напоминания ----------

            if ("yearly".equals(draft.getType())) {

                userState.put(chatId, UserState.TITLE);

                msg.setText("Введите текст напоминания:");

            } else {

                userState.put(chatId, UserState.HOUR);

                msg.setText("Выберите час:");

                msg.setReplyMarkup(createHoursKeyboard());
            }
        }
        else if (data.startsWith("HOUR_")) {

            String hour = data.replace("HOUR_", "");

            DraftReminder draft = drafts.get(chatId);

            draft.setTime(hour);

            drafts.put(chatId, draft);

            UserState state = userState.get(chatId);

            if (state == UserState.HOUR) {

                userState.put(chatId, UserState.TIME);

            } else if (state == UserState.EDIT_HOUR) {

                userState.put(chatId, UserState.EDIT_TIME);

            }

            msg.setText("Введите минуты (00-59):");
        }
        else if (data.startsWith("DELETE_")) {

            Long id = Long.parseLong(data.replace("DELETE_", ""));

            repository.deleteById(id);

            String type = drafts.get(chatId).getType();

            showRemindersForDelete(chatId, type);

            return;
        }
        else if (data.startsWith("EDIT_")) {

            Long id = Long.parseLong(data.replace("EDIT_", ""));

            DraftReminder draft =
                    drafts.getOrDefault(chatId, new DraftReminder());

            draft.setReminderId(id);
            userState.put(chatId, UserState.EDIT_MENU);
            drafts.put(chatId, draft);

            msg.setText("Что хотите изменить?");
            msg.setReplyMarkup(
                    createEditKeyboard(draft.getType())
            );

            sendMessage(msg);
            return;
        }

        sendMessage(msg);
        return;
    }

    /* ================= CALENDAR ================= */

    private InlineKeyboardMarkup createCalendarDays(int month) {

        int year = LocalDate.now(ZoneId.of("Europe/Samara")).getYear();
        int maxDays = YearMonth.of(year, month).lengthOfMonth();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (int i = 1; i <= maxDays; i++) {

            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(String.valueOf(i));
            btn.setCallbackData("DAY_" + i);

            row.add(btn);

            if (row.size() == 7) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }

        if (!row.isEmpty()) {
            rows.add(row);
        }

        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("Назад");
        back.setCallbackData("BACK");

        rows.add(List.of(back));

        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup createMonthsInline() {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        String[] months = {
                "01","02","03","04","05","06",
                "07","08","09","10","11","12"
        };

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        for (int i = 0; i < months.length; i++) {

            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(months[i]);
            btn.setCallbackData("MONTH_" + months[i]);

            if (i < 6) {
                row1.add(btn);
            } else {
                row2.add(btn);
            }
        }

        rows.add(row1);
        rows.add(row2);
        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("Назад");
        back.setCallbackData("BACK");

        rows.add(List.of(back));
        markup.setKeyboard(rows);

        return markup;
    }

    private InlineKeyboardMarkup createTypeKeyboard() {

        InlineKeyboardButton yearly = new InlineKeyboardButton();
        yearly.setText("Ежегодное");
        yearly.setCallbackData("TYPE_YEARLY");

        InlineKeyboardButton once = new InlineKeyboardButton();
        once.setText("Одноразовое");
        once.setCallbackData("TYPE_ONCE");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        markup.setKeyboard(
                List.of(
                        List.of(yearly),
                        List.of(once)
                )
        );

        return markup;
    }


    private ReplyKeyboardMarkup createMainKeyboard() {

        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("Добавить"));
        row.add(new KeyboardButton("Мои напоминания"));

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

        keyboard.setKeyboard(List.of(row));

        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        return keyboard;
    }

    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup createReminderMenuKeyboard() {

        KeyboardButton edit = new KeyboardButton("Редактировать");
        KeyboardButton delete = new KeyboardButton("Удалить");
        KeyboardButton back = new KeyboardButton("Назад");

        KeyboardRow row1 = new KeyboardRow();
        row1.add(edit);
        row1.add(delete);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(back);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(List.of(row1, row2));
        keyboard.setResizeKeyboard(true);

        return keyboard;
    }

    private ReplyKeyboardMarkup createReminderTypeKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Одноразовые"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Ежегодные"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Назад"));

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setKeyboard(List.of(row1, row2, row3));

        return keyboard;
    }

    private InlineKeyboardMarkup createReminderInlineKeyboard(List<Reminder> reminders, String action) {

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Reminder reminder : reminders) {

            InlineKeyboardButton button = new InlineKeyboardButton();

            button.setText(reminder.getTitle());
            button.setCallbackData(action + "_" + reminder.getId());

            rows.add(List.of(button));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);

        return markup;
    }

    private void showRemindersForDelete(String chatId, String type) {

        List<Reminder> reminders =
                repository.findByChatIdAndType(chatId, type);

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);

        if (reminders.isEmpty()) {

            msg.setText("Напоминаний больше нет.");

        } else {

            msg.setText("Выберите напоминание:");

            msg.setReplyMarkup(
                    createReminderInlineKeyboard(reminders, "DELETE")
            );
        }

        sendMessage(msg);
    }

    private ReplyKeyboardMarkup createEditKeyboard(String type) {

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Название");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Дата");

        List<KeyboardRow> rows = new ArrayList<>();

        rows.add(row1);
        rows.add(row2);


        if ("once".equals(type)) {
            KeyboardRow row3 = new KeyboardRow();
            row3.add("Время");
            rows.add(row3);
        }

        KeyboardRow row4 = new KeyboardRow();
        row4.add("Назад");
        rows.add(row4);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();

        markup.setKeyboard(rows);
        markup.setResizeKeyboard(true);

        return markup;
    }

    private InlineKeyboardMarkup createHoursKeyboard() {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        int hour = 0;

        for (int i = 0; i < 6; i++) {

            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int j = 0; j < 4; j++) {

                InlineKeyboardButton btn = new InlineKeyboardButton();

                String text = String.format("%02d", hour);

                btn.setText(text);
                btn.setCallbackData("HOUR_" + text);

                row.add(btn);

                hour++;
            }

            rows.add(row);
        }
        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("Назад");
        back.setCallbackData("BACK");

        rows.add(List.of(back));

        markup.setKeyboard(rows);

        return markup;
    }

    private void handleUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update);
        }
    }
}

