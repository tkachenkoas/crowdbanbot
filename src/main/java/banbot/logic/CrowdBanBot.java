package banbot.logic;

import banbot.config.MessagesConfig;
import banbot.config.BotConfig;
import banbot.entity.common.CallBackData;
import banbot.entity.trial.Trial;
import banbot.entity.trial.TrialAction;
import banbot.entity.trial.TrialKey;
import banbot.entity.trial.TrialState;
import banbot.exceptions.TGBotException;
import banbot.exceptions.TrialActionException;
import banbot.repository.TrialRepository;
import banbot.utils.MockUpdateUtils;
import banbot.utils.TGBotUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.KickChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import static banbot.utils.TGBotUtils.getMention;

@Component
public class CrowdBanBot extends TelegramLongPollingBot {

    private static final String LOG_TAG = "CrowdBanBot";

    @Autowired
    private TrialRepository trialRepository;
    private final String START = "/start";
    private final String STATUS_ADMINISTRATOR = "administrator";

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
                processOwnchat(message);
                return;
            }

            if (isMentioned(message)) {
                initTrial(message);
                return;
            }

            if (receivedButtonCallback(update)) {
                processButtonCallback(update);
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

    private void processOwnchat(Message message) {
        if (!message.getText().trim().equals(START)) return;
        String text = String.format(MessagesConfig.getWelcomeMessage(), getMention(message.getFrom()), MessagesConfig.getTrialExpirationTime());
        processExecute(new SendMessage(message.getChatId(), text));
    }

    private boolean isMentioned(Message message){
        return message != null && StringUtils.contains(message.getText(), getMention(BotConfig.getBotUsername()));
    }

    private void initTrial(Message message) {
        // Ignore bot mention without reply on some user's message
        if (message.getReplyToMessage() == null) return;

        long chatId = message.getChatId();
        ensureThatBotIsAdministrator(chatId);

        User userToTrial = message.getReplyToMessage().getFrom();

        // First mention -> init a new trial, second mention -> add a ban vote
        Trial trial = trialRepository.getTrial(new TrialKey(chatId, userToTrial));
        if (trial == null) trial = new Trial(chatId, userToTrial);

        try {
            trial.performAction(TrialAction.BAN, message.getFrom());
        } catch (TrialActionException e) {
            processExecute(new SendMessage().setChatId(chatId).setText(getMention(message.getFrom()) + ", " + e.getMessage()));
            return;
        }
        if (!trial.isOnTrial()) {
            processFinishedTrial(trial);
            return;
        }

        Message resultMessage = (Message) processExecute(new SendMessage().setChatId(message.getChatId())
                                    .setText(String.format(MessagesConfig.getTrialInitMessage(), getMention(message.getFrom()), getMention(userToTrial)))
                                    .setReplyMarkup(TGBotUtils.getTrialKeyboardMarkup(trial)));
        trial.setPollMessageId(resultMessage.getMessageId());
        trialRepository.saveTrial(trial);
    }

    private void ensureThatBotIsAdministrator(long chatId) {
        ChatMember bot = (ChatMember) processExecute(new GetChatMember().setChatId(chatId).setUserId(BotConfig.getBotUserId()));
        if (!bot.getStatus().equalsIgnoreCase(STATUS_ADMINISTRATOR)) {
            processExecute(new SendMessage().setChatId(chatId)
                            .setText(String.format(MessagesConfig.getNotAdminMessage(), BotConfig.getBotUsername())));
            throw new TGBotException("Bot is not admin for chatId " + chatId);
        }
    }

    private boolean receivedButtonCallback(Update update) {
        return update.getCallbackQuery() != null;
    }

    private void processButtonCallback(Update update) {
        CallbackQuery callback = update.getCallbackQuery();
        CallBackData data = CallBackData.fromString(callback.getData());

        Trial trial = trialRepository.getTrial(data.getTrialKey());
        if (trial == null) {
            // Inform the voter that the trial has expired and remove outdated trial poll
            processExecute(new SendMessage(callback.getMessage().getChatId(),
                                           String.format(MessagesConfig.getTrialExpiredMessage(), getMention(callback.getFrom()))));
            processExecute(new DeleteMessage(callback.getMessage().getChatId(), callback.getMessage().getMessageId()));
            return;
        }
        try {
            trial.performAction(data.getAction(), callback.getFrom());
        } catch (TrialActionException e) {
            processExecute(new AnswerCallbackQuery().setCallbackQueryId(callback.getId())
                                                    .setText(e.getMessage()));
            return;
        }
        if (!trial.isOnTrial()) {
            processFinishedTrial(trial);
            return;
        }

        trialRepository.saveTrial(trial);

        processExecute(new EditMessageReplyMarkup().setChatId(trial.getChatId())
                        .setMessageId(callback.getMessage().getMessageId())
                        .setReplyMarkup(TGBotUtils.getTrialKeyboardMarkup(trial)));
    }

    private void processFinishedTrial(Trial trial) {
        Long chatId = trial.getChatId();
        User userOnTrial = trial.getUserOnTrial();
        String voters;
        String messageText;

        if (trial.getState() == TrialState.BAN) {
            BotLogger.info(LOG_TAG,"Ban user " + userOnTrial.getUserName() + " from chat " + trial.getChatId());
            voters = trial.getBanVoters().stream().map(TGBotUtils::getMention).collect(Collectors.joining(", "));
            messageText = String.format(MessagesConfig.getBanMessage(), userOnTrial.getUserName(), voters);

            processExecute(new KickChatMember(chatId, (int) trial.getKey().getUserId()).setUntilDate( (int) new Date().getTime() + 60));
        } else {
            BotLogger.info(LOG_TAG,"Spare user " + userOnTrial.getUserName() + " in chat " + trial.getChatId());
            voters = trial.getSpareVoters().stream().map(TGBotUtils::getMention).collect(Collectors.joining(", "));
            messageText = String.format(MessagesConfig.getSpareMessage(), getMention(userOnTrial), voters);
        }

        processExecute(new DeleteMessage(chatId, trial.getPollMessageId()));
        processExecute(new SendMessage(trial.getChatId(), messageText));
        trialRepository.removeTrial(trial);
    }

    private Serializable processExecute(BotApiMethod method) {
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