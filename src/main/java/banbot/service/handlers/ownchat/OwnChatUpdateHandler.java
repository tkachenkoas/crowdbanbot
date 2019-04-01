package banbot.service.handlers.ownchat;

import banbot.entity.common.HandleResult;
import banbot.entity.controller.CrowdBanBot;
import banbot.service.handlers.AbstractUpdateHandler;
import banbot.utils.MessageTextProvider;
import banbot.utils.Messages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

import static banbot.utils.TGChatMessageUtils.getMention;

@Service
public class OwnChatUpdateHandler extends AbstractUpdateHandler {

    private Long ownChatid;
    private Integer trialExpireTime;

    private final String START_COMMAND = "/start";

    private MessageTextProvider textProvider;
    private CrowdBanBot botBean;

    public OwnChatUpdateHandler(MessageTextProvider textProvider,
                                CrowdBanBot botBean,
                                @Value("${bot.chat.id}") Long ownChatid,
                                @Value("${trial.expire.minutes}") Integer trialExpireTime) {
        this.textProvider = textProvider;
        this.botBean = botBean;
        this.ownChatid = ownChatid;
        this.trialExpireTime = trialExpireTime;
    }

    @Override
    protected boolean canHandle(Update update) {
        Long targetChatId = Optional.ofNullable(update).map(Update::getMessage)
                                    .map(Message::getChatId).orElse(null);
        return ownChatid.equals(targetChatId);
    }

    @Override
    protected void process(Update update) {
        Message message = update.getMessage();
        if (!message.getText().trim().equals(START_COMMAND)) return;
        String text = textProvider.getFormattedText(Messages.WELCOME_MESSAGE,
                                    getMention(message.getFrom()),
                                    trialExpireTime);
        botBean.processExecute(new SendMessage(message.getChatId(), text));
    }

}
