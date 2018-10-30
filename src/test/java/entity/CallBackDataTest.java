package entity;

import static org.junit.Assert.*;

import banbot.entity.common.CallBackData;
import banbot.entity.trial.TrialAction;
import banbot.entity.trial.TrialKey;
import org.junit.Test;

public class CallBackDataTest {

    @Test
    public void fromToString() throws Exception{
        CallBackData callBackData = new CallBackData(new TrialKey(123, 321), TrialAction.BAN);

        String serialized = callBackData.getStringCallbackData();

        CallBackData deserialized = CallBackData.fromString(serialized);

        assertEquals("chatId", 123, deserialized.getTrialKey().getChatId());
        assertEquals("userId", 321, deserialized.getTrialKey().getUserId());
        assertEquals("action", TrialAction.BAN, deserialized.getAction());

    }

}