package banbot.service.handlers.trial;

import banbot.entity.common.CallBackData;
import banbot.entity.controller.CrowdBanBot;
import banbot.entity.trial.Trial;
import banbot.exceptions.TrialActionException;
import banbot.repository.TrialRepository;
import banbot.utils.MessageTextProvider;
import banbot.utils.Messages;
import banbot.utils.TGBotKeyboardUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import static banbot.utils.TGChatMessageUtils.getMention;

@Service
public class VoteButtonClickHandler extends AbstractTrialHandler {

    private TrialRepository trialRepository;
    private MessageTextProvider textProvider;
    private CrowdBanBot botBean;

    public VoteButtonClickHandler(TrialRepository trialRepository, MessageTextProvider textProvider, CrowdBanBot botBean) {
        this.trialRepository = trialRepository;
        this.textProvider = textProvider;
        this.botBean = botBean;
    }

    @Override
    TrialRepository getRepository() {
        return trialRepository;
    }

    @Override
    MessageTextProvider getTextProvider() {
        return textProvider;
    }

    @Override
    protected boolean canHandle(Update update) {
        return update.getCallbackQuery() != null;
    }

    @Override
    protected void process(Update update) {
        CallbackQuery callback = update.getCallbackQuery();
        CallBackData data = CallBackData.fromString(callback.getData());

        Trial trial = trialRepository.getTrial(data.getTrialKey());
        if (trial == null) {
            // Inform the voter that the trial has expired and remove outdated trial poll
            botBean.processExecute(new SendMessage(callback.getMessage().getChatId(), textProvider.getFormattedText(Messages.TRIAL_EXPIRED_MESSAGE,getMention(callback.getFrom()))));
            botBean.processExecute(new DeleteMessage(callback.getMessage().getChatId(), callback.getMessage().getMessageId()));
            return;
        }
        try {
            trial.performAction(data.getAction(), callback.getFrom());
        } catch (TrialActionException e) {
            botBean.processExecute(new AnswerCallbackQuery().setCallbackQueryId(callback.getId())
                    .setText(e.getMessage()));
            return;
        }
        if (!trial.isOnTrial()) {
            processFinishedTrial(trial);
            return;
        }

        trialRepository.saveTrial(trial);

        botBean.processExecute(new EditMessageReplyMarkup().setChatId(trial.getChatId())
                .setMessageId(callback.getMessage().getMessageId())
                .setReplyMarkup(TGBotKeyboardUtils.getTrialKeyboardMarkup(trial)));
    }

    @Override
    protected CrowdBanBot getBot() {
        return botBean;
    }

}
