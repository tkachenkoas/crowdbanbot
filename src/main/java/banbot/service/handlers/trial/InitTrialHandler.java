package banbot.service.handlers.trial;

import banbot.entity.common.HandleResult;
import banbot.entity.controller.CrowdBanBot;
import banbot.entity.trial.Trial;
import banbot.entity.trial.TrialAction;
import banbot.entity.trial.TrialKey;
import banbot.exceptions.TGBotException;
import banbot.exceptions.TrialActionException;
import banbot.repository.TrialRepository;
import banbot.utils.MessageTextProvider;
import banbot.utils.Messages;
import banbot.utils.TGBotKeyboardUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static banbot.utils.TGChatMessageUtils.getMention;

@Service
public class InitTrialHandler extends AbstractTrialHandler {

    @Value("${decision.count}")
    private Integer decisionVotesCount;

    private CrowdBanBot botBean;
    private MessageTextProvider textProvider;
    private TrialRepository trialRepository;

    public InitTrialHandler(CrowdBanBot botBean,
                            MessageTextProvider textProvider, TrialRepository repository) {
        this.botBean = botBean;
        this.textProvider = textProvider;
        this.trialRepository = repository;
    }

    @Override
    protected boolean canHandle(Update update) {
        return Optional.ofNullable(update.getMessage())
                       .map(mes -> StringUtils.contains(mes.getText(), getMention(botBean.getBotUsername())))
                       .orElse(false);
    }

    @Override
    protected void process(Update update) {
        Message message = update.getMessage();
        // Ignore bot mention without reply on some user's message
        if (message.getReplyToMessage() == null) return;

        long chatId = message.getChatId();
        ensureThatBotIsAdministrator(chatId);

        User userToTrial = message.getReplyToMessage().getFrom();

        // First mention -> init a new trial, second mention -> add a ban vote
        Trial trial = trialRepository.getTrial(new TrialKey(chatId, userToTrial));
        if (trial == null) trial = new Trial(chatId, userToTrial, decisionVotesCount);

        try {
            trial.performAction(TrialAction.BAN, message.getFrom());
        } catch (TrialActionException e) {
            getBot().processExecute(new SendMessage().setChatId(chatId).setText(getMention(message.getFrom()) + ", " + e.getMessage()));
            return;
        }
        if (!trial.isOnTrial()) {
            processFinishedTrial(trial);
            return;
        }

        SendMessage sendMessage = new SendMessage(message.getChatId(),
                                                  textProvider.getFormattedText(Messages.TRIAL_INIT_MESSAGE,
                                                                                getMention(message.getFrom()), getMention(userToTrial)))
                                        .setReplyMarkup(TGBotKeyboardUtils.getTrialKeyboardMarkup(trial));
        Message resultMessage = getBot().processExecute(Message.class, sendMessage);
        trial.setPollMessageId(resultMessage.getMessageId());
        trialRepository.saveTrial(trial);
    }

    @Override
    protected CrowdBanBot getBot() {
        return botBean;
    }

    @Override
    TrialRepository getRepository() {
        return trialRepository;
    }

    @Override
    MessageTextProvider getTextProvider() {
        return textProvider;
    }
}
