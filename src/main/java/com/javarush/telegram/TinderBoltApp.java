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
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            sendTextMessage(loadMessage("gpt"));
            return;
        }
        if (currentMode.equals(DialogMode.GPT)) {
            String prompt = loadPrompt("gpt");
            String answer = chatGpt.sendMessage(prompt, message);
            sendTextMessage(answer);
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
