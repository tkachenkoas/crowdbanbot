package banbot.service;

import banbot.config.BotConfig;
import banbot.exceptions.TGBotException;
import banbot.repository.TrialRepository;
import banbot.utils.MockUpdateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.io.Serializable;
import java.util.Optional;

import static banbot.utils.TGBotUtils.getMention;

@Component
public class CrowdBanBot extends TelegramLongPollingBot {

    private static final String LOG_TAG = "CrowdBanBot";

    @Autowired
    private BanBotService banService;

    @Override
    public String getBotUsername() {
        return BotConfig.getBotUsername();
    }

    public void onUpdateReceived(Update update) {
        BotLogger.info(LOG_TAG,"Incoming update: " + update);
        MockUpdateUtils.mockUpdate(update);

        try{
            Message message = update.getMessage();
            if (messageFromOwnChat(update)) {
                banService.processOwnchat(message);
                return;
            }

            if (isMentioned(message)) {
                banService.initTrial(message);
                return;
            }

            if (receivedButtonCallback(update)) {
                banService.processButtonCallback(update);
                return;
            }
        } catch (TGBotException e) {
            BotLogger.info(LOG_TAG, "Caught bot process exception: " + e.getMessage());
        } catch (Exception e) {
            BotLogger.error(LOG_TAG, e);
        }

    }

    private boolean messageFromOwnChat(Update update) {
        return BotConfig.getOwnChatId().equals(Optional.ofNullable(update)
                                                        .map(Update::getMessage)
                                                        .map(Message::getChatId)
                                                        .orElse(null));
    }

    private boolean isMentioned(Message message){
        return message != null && StringUtils.contains(message.getText(), getMention(BotConfig.getBotUsername()));
    }

    private boolean receivedButtonCallback(Update update) {
        return update.getCallbackQuery() != null;
    }

    public Serializable processExecute(BotApiMethod method) {
        try {
            BotLogger.info(LOG_TAG,"Outgoing method:\n" + method);
            return execute(method);
        } catch (TelegramApiException e) {
            BotLogger.error(LOG_TAG, e);
            return null;
        }
    }

    public String getBotToken() {
        return BotConfig.getApiKey();
    }


}