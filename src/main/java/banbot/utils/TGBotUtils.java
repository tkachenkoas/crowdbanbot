package banbot.utils;

import banbot.entity.trial.Trial;
import com.vdurmont.emoji.EmojiParser;
import banbot.config.MessagesConfig;
import banbot.entity.common.CallBackData;
import banbot.entity.trial.TrialAction;
import banbot.entity.trial.TrialKey;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class TGBotUtils {

    private final static String EMOJI_GHOST = EmojiParser.parseToUnicode(":ghost:");
    private final static String EMOJI_ANGEL = EmojiParser.parseToUnicode(":angel:");

    private TGBotUtils() {
    }

    public static String getMention(String userName) {
        return "@" + userName;
    }

    public static String getMention (User user) {
        return getMention(user.getUserName());
    }

    public static InlineKeyboardMarkup getTrialKeyboardMarkup(Trial trial){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.getKeyboard().add(getActionButtonRow(TrialAction.BAN, trial.getBanCount(), trial.getKey(), EMOJI_GHOST + " Ban"));
        markup.getKeyboard().add(getActionButtonRow(TrialAction.SPARE, trial.getSpareCount(), trial.getKey(), EMOJI_ANGEL + " Spare"));
        return markup;
    }

    private static List<InlineKeyboardButton> getActionButtonRow(TrialAction action, int count, TrialKey key, String buttonText) {
        List<InlineKeyboardButton> result = new ArrayList<>();
        CallBackData callBackData = new CallBackData(key, action);
        String countStr = " (" + count + "/" + MessagesConfig.getDecisionVotesCount() + ")";
        result.add(new InlineKeyboardButton(buttonText + countStr).setCallbackData(callBackData.getStringCallbackData()));
        return result;
    }

}
