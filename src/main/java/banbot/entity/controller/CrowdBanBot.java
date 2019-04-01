package banbot.entity.controller;

import banbot.config.dto.BotProps;
import banbot.entity.common.HandleResult;
import banbot.exceptions.TGBotException;
import banbot.service.handlers.UpdateHandler;
import banbot.utils.MockUpdateUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.io.Serializable;
import java.util.List;

@Component
public class CrowdBanBot extends TelegramLongPollingBot {

    private static final String LOG_TAG = "CrowdBanBot";

    private List<UpdateHandler> handlers;
    private MockUpdateUtils mockUtil;
    private BotProps props;

    public CrowdBanBot(@Lazy List<UpdateHandler> handlers, MockUpdateUtils mockUtil, BotProps props) {
        this.handlers = handlers;
        this.mockUtil = mockUtil;
        this.props = props;
    }

    @Override
    public String getBotUsername() {
        return props.getBotName();
    }

    public void onUpdateReceived(Update update) {
        BotLogger.info(LOG_TAG,"Incoming update: " + update);
        if (props.getTestMode() && update.getCallbackQuery() != null) {
            mockUtil.mockUpdate(update);
        }

        for (UpdateHandler handler: handlers) {
            try{
                if (handler.handle(update) == HandleResult.TERMINAL) break;
            } catch (TGBotException e) {
                BotLogger.info(LOG_TAG, "Caught bot process exception: " + e.getMessage());
            } catch (Exception e) {
                BotLogger.error(LOG_TAG, e);
            }
        }
    }

    public <T> T processExecute( Class<T> resultClass, BotApiMethod method) {
        Serializable result = processExecute(method);
        return resultClass.isInstance(result) ? (T) result : null;
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
        return props.getToken();
    }

    public Integer getBotUserId() {
        return props.getBotUserId();
    }

}