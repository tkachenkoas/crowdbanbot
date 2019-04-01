package banbot.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;

public class MessageTextProvider {

    private MessageSourceAccessor messageSourceAccessor;

    public MessageTextProvider(MessageSource messageSource) {
        this.messageSourceAccessor = new MessageSourceAccessor(messageSource);
    }

    public String getText(Messages message) {
        return messageSourceAccessor.getMessage(message.getCode());
    }

    public String getFormattedText(Messages message, Object ... args) {
        return String.format(getText(message), args);
    }

}
