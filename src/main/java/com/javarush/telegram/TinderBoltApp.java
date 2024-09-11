package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "my_firts_ai_bot";
    public static final String TELEGRAM_BOT_TOKEN = "TELEGRAM_BOT_TOKEN";
    public static final String OPEN_AI_TOKEN = "OPEN_AI_TOKEN";
    private final ChatGPTService chatGpt = new ChatGPTService(OPEN_AI_TOKEN);
    private UserInfo me;
    private UserInfo she;
    private int questionNumber;

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

        if (currentMode == DialogMode.DATE && !isMessageCommand()) {
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
            return;
        }

        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {
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

        //command Profile
        if (message.equals("/profile")) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");
            sendTextMessage(loadMessage("profile"));

            me = new UserInfo();
            questionNumber = 1;
            sendTextMessage("Сколько Вам лет?");
            return;
        }

        if (currentMode == DialogMode.PROFILE && !isMessageCommand()) {
            switch (questionNumber) {
                case 1:
                    me.age = message;
                    questionNumber = 2;
                    sendTextMessage("Кем Вы работаете?");
                    return;
                case 2:
                    me.occupation = message;
                    questionNumber = 3;
                    sendTextMessage("У Вас есть хобби");
                    return;
                case 3:
                    me.hobby = message;
                    questionNumber = 4;
                    sendTextMessage("Что Вам НЕ нравится в людях?");
                    return;
                case 4:
                    me.annoys = message;
                    questionNumber = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    me.goals = message;
                    String aboutUser = me.toString();
                    Message msg = sendTextMessage("Подождите немного, ChatGPT \uD83E\uDDE0 думает...");
                    String answer = chatGpt.sendMessage(loadPrompt("profile"), aboutUser);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }

        //command Opener
        if (message.equals("/opener")) {
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");
            sendTextMessage(loadMessage("opener"));

            she = new UserInfo();
            questionNumber = 1;
            sendTextMessage("Имя девушки?");
            return;
        }

        if (currentMode == DialogMode.OPENER && !isMessageCommand()) {
            switch (questionNumber) {
                case 1:
                    she.name = message;
                    questionNumber = 2;
                    sendTextMessage("Сколько ей лет?");
                    return;
                case 2:
                    she.age = message;
                    questionNumber = 3;
                    sendTextMessage("Есть ли у нее хобби и какие?");
                    return;
                case 3:
                    she.hobby = message;
                    questionNumber = 4;
                    sendTextMessage("Кем она работает?");
                    return;
                case 4:
                    she.occupation = message;
                    questionNumber = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    she.goals = message;
                    String aboutHer = she.toString();
                    Message msg = sendTextMessage("Подождите немного, ChatGPT \uD83E\uDDE0 думает...");
                    String answer = chatGpt.sendMessage(loadPrompt("opener"), aboutHer);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }

        //command GPT
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            sendTextMessage(loadMessage("gpt"));
            return;
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()) {
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
