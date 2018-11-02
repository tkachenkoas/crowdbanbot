package banbot.service;

import banbot.config.BotConfig;
import banbot.config.MessagesConfig;
import banbot.entity.common.CallBackData;
import banbot.entity.trial.Trial;
import banbot.entity.trial.TrialAction;
import banbot.entity.trial.TrialKey;
import banbot.entity.trial.TrialState;
import banbot.exceptions.TGBotException;
import banbot.exceptions.TrialActionException;
import banbot.repository.TrialRepository;
import banbot.utils.TGBotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.KickChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.io.Serializable;
import java.util.Date;
import java.util.stream.Collectors;

import static banbot.utils.TGBotUtils.getMention;

@Service
public class BanBotService {

    private static final String LOG_TAG = "BanBotservice";

    @Autowired
    private TrialRepository trialRepository;
    @Autowired
    private CrowdBanBot bot;

    private final String START = "/start";
    private final String STATUS_ADMINISTRATOR = "administrator";

    public void processOwnchat(Message message) {
        if (!message.getText().trim().equals(START)) return;
        String text = String.format(MessagesConfig.getWelcomeMessage(), getMention(message.getFrom()), MessagesConfig.getTrialExpirationTime());
        processExecute(new SendMessage(message.getChatId(), text));
    }

    public void initTrial(Message message) {
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

    public void ensureThatBotIsAdministrator(long chatId) {
        ChatMember bot = (ChatMember) processExecute(new GetChatMember().setChatId(chatId).setUserId(BotConfig.getBotUserId()));
        if (!bot.getStatus().equalsIgnoreCase(STATUS_ADMINISTRATOR)) {
            processExecute(new SendMessage().setChatId(chatId)
                    .setText(String.format(MessagesConfig.getNotAdminMessage(), BotConfig.getBotUsername())));
            throw new TGBotException("Bot is not admin for chatId " + chatId);
        }
    }

    public void processButtonCallback(Update update) {
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

    public void processFinishedTrial(Trial trial) {
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
        return bot.processExecute(method);
    }

}
