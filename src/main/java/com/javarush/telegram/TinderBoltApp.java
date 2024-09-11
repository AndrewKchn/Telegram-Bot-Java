package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "my_firts_ai_bot";
    public static final String TELEGRAM_BOT_TOKEN = "7219086303:AAHPabL0vO4PJ4cXO3XHpWHjIQ-WXNOGPac";
    public static final String OPEN_AI_TOKEN = "gpt:6MZuruLWYMt7BFAYy33hJFkblB3TrOQSkF7WUgsEFs26dToB";
    private final ChatGPTService chatGpt = new ChatGPTService(OPEN_AI_TOKEN);

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    private DialogMode currentMode = null;
    private final ArrayList<String> list = new ArrayList<>();

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();

        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            sendTextMessage(loadMessage("main"));
            showMainMenu("главное меню бота", "/start",
                    "генерация Tinder-профля \uD83D\uDE0E", "/profile",
                    "сообщение для знакомства \uD83E\uDD70", "/opener",
                    "переписка от вашего имени \uD83D\uDE08", "/message",
                    "переписка со звездами \uD83D\uDD25", "/date",
                    "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
            return;
        }

        //command Date
        if ((message.equals("/date"))) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            sendTextButtonsMessage(loadMessage("date"),
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendaya",
                    "Райан Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return;
        }

        if (currentMode == DialogMode.DATE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("Отличный выбор!");
                chatGpt.setPrompt(loadPrompt(query));
                return;
            }
            Message msg = sendTextMessage("Подождите, девушка набирает текст...");
            String answer = chatGpt.addMessage(message);
            updateTextMessage(msg, answer);
            return;
        }

        //command Message
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Слудующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
        }

        if (currentMode == DialogMode.MESSAGE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                sendPhotoMessage(query);
                String userChatHistory = String.join("\n\n", list);
                String answer = chatGpt.sendMessage(loadPrompt(query), userChatHistory);
                sendTextMessage(answer);
                return;
            }
            list.add(message);
            return;
        }

        //command GPT
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            sendTextMessage(loadMessage("gpt"));
            return;
        }

        if (currentMode == DialogMode.GPT) {
            String prompt = loadPrompt("gpt");
            Message msg = sendTextMessage("Подождите немного, ChatGPT думает...");
            String answer = chatGpt.sendMessage(prompt, message);
            updateTextMessage(msg, answer);
            return;
        }

        sendTextMessage("*Привет!*");
        sendTextMessage("_Привет!_");
        sendTextMessage("Вы написали : " + message);
        sendTextButtonsMessage("Выберите режим работы",
                "Старт", "start",
                "Стоп", "stop");


    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
