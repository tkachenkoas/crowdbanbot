package banbot.entity.common;

import banbot.entity.trial.TrialAction;
import banbot.entity.trial.TrialKey;

import java.io.Serializable;

public class CallBackData implements Serializable {

    private static String SERIALIZE_DELIMETER = "/";

    private TrialKey trialKey;
    private TrialAction action;

    public CallBackData(TrialKey trialKey, TrialAction action) {
        this.trialKey = trialKey;
        this.action = action;
    }

    public TrialKey getTrialKey() {
        return trialKey;
    }
    public TrialAction getAction() {
        return action;
    }

    public String getStringCallbackData(){
        return action.name() + SERIALIZE_DELIMETER + trialKey.getChatId() + SERIALIZE_DELIMETER + trialKey.getUserId();
    }

    public static CallBackData fromString(String data){
        try {
            String [] arr = data.split(SERIALIZE_DELIMETER);
            Long chatId = Long.parseLong(arr[1]);
            Long userId = Long.parseLong(arr[2]);
            TrialAction action = TrialAction.valueOf(arr[0]);
            return new CallBackData(new TrialKey(chatId, userId), action);
        } catch (Exception e) {
            return null;
        }
    }
}
