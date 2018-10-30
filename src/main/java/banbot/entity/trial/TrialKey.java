package banbot.entity.trial;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;

public class TrialKey implements Serializable {

    private long chatId;
    private long userId;

    public TrialKey(long chatId, long userId) {
        this.chatId = chatId;
        this.userId = userId;
    }

    public TrialKey(long chatId, User user) {
        this.chatId = chatId;
        this.userId = user.getId();
    }

    public long getUserId() {
        return userId;
    }

    public long getChatId() {
        return chatId;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                    .append("chatId", chatId)
                    .append("userId", userId)
                    .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrialKey trialKey = (TrialKey) o;
        return new EqualsBuilder()
                .append(chatId, trialKey.chatId)
                .append(userId, trialKey.userId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(chatId)
                .append(userId)
                .toHashCode();
    }

}
