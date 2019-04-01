package banbot.service.handlers.trial;

import banbot.entity.controller.CrowdBanBot;
import banbot.entity.trial.Trial;
import banbot.entity.trial.TrialState;
import banbot.exceptions.TGBotException;
import banbot.repository.TrialRepository;
import banbot.service.handlers.AbstractUpdateHandler;
import banbot.utils.MessageTextProvider;
import banbot.utils.Messages;
import banbot.utils.TGChatMessageUtils;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.KickChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.util.Date;
import java.util.stream.Collectors;

import static banbot.utils.TGChatMessageUtils.getMention;

public abstract class AbstractTrialHandler extends AbstractUpdateHandler {

    protected final String STATUS_ADMINISTRATOR = "administrator";

    protected void ensureThatBotIsAdministrator(long chatId) {
        GetChatMember botChatMemberRequest = new GetChatMember().setChatId(chatId).setUserId(getBot().getBotUserId());
        ChatMember botChatMember = getBot().processExecute(ChatMember.class, botChatMemberRequest);
        if (!botChatMember.getStatus().equalsIgnoreCase(STATUS_ADMINISTRATOR)) {
            SendMessage notAdminMessage = new SendMessage(chatId, getTextProvider().getFormattedText(Messages.NOT_ADMIN_MESSAGE, getBot().getBotUsername()));
            getBot().processExecute(notAdminMessage);
            throw new TGBotException("Bot is not admin for chatId " + chatId);
        }
    }

    protected void processFinishedTrial(Trial trial) {
        Long chatId = trial.getChatId();
        ensureThatBotIsAdministrator(chatId);
        User userOnTrial = trial.getUserOnTrial();
        String messageText;

        if (trial.getState() == TrialState.BAN) {
            BotLogger.info(getLogTag(), "Ban user " + userOnTrial.getUserName() + " from chat " + trial.getChatId());
            String votersMention = trial.getBanVoters().stream().map(TGChatMessageUtils::getMention).collect(Collectors.joining(", "));
            messageText = getTextProvider().getFormattedText(Messages.BAN_MESSAGE, userOnTrial.getUserName(), votersMention);

            getBot().processExecute(new KickChatMember(chatId, (int) trial.getKey().getUserId()).setUntilDate((int) new Date().getTime() + 60));
        } else {
            BotLogger.info(getLogTag(), "Spare user " + userOnTrial.getUserName() + " in chat " + trial.getChatId());
            String votersMention = trial.getSpareVoters().stream().map(TGChatMessageUtils::getMention).collect(Collectors.joining(", "));
            messageText = getTextProvider().getFormattedText(Messages.SPARE_MESSAGE, getMention(userOnTrial), votersMention);
        }

        getBot().processExecute(new DeleteMessage(chatId, trial.getPollMessageId()));
        getBot().processExecute(new SendMessage(trial.getChatId(), messageText));
        getRepository().removeTrial(trial);
    }

    protected String getLogTag() {
        return getClass().getSimpleName();
    }

    abstract TrialRepository getRepository();

    abstract MessageTextProvider getTextProvider();

    abstract CrowdBanBot getBot();
}
