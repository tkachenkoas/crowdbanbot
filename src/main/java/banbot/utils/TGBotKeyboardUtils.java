package banbot.utils;

import banbot.entity.common.CallBackData;
import banbot.entity.trial.Trial;
import banbot.entity.trial.TrialAction;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static banbot.utils.TGChatMessageUtils.EMOJI_ANGEL;
import static banbot.utils.TGChatMessageUtils.EMOJI_GHOST;

public class TGBotKeyboardUtils {

    public static InlineKeyboardMarkup getTrialKeyboardMarkup(Trial trial){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.getKeyboard().add(getActionButtonRow(TrialAction.BAN, trial, EMOJI_GHOST + " Ban"));
        markup.getKeyboard().add(getActionButtonRow(TrialAction.SPARE, trial, EMOJI_ANGEL + " Spare"));
        return markup;
    }

    private static  List<InlineKeyboardButton> getActionButtonRow(TrialAction action, Trial trial, String buttonText) {
        List<InlineKeyboardButton> result = new ArrayList<>();
        CallBackData callBackData = new CallBackData(trial.getKey(), action);
        String countStr = " (" + trial.getActionCount(action) + "/" + trial.getDecisionVoteCount() + ")";
        result.add(new InlineKeyboardButton(buttonText + countStr).setCallbackData(callBackData.getStringCallbackData()));
        return result;
    }

}
