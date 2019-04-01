package banbot.utils;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.objects.User;

public class TGChatMessageUtils {

    public final static String EMOJI_GHOST = EmojiParser.parseToUnicode(":ghost:");
    public final static String EMOJI_ANGEL = EmojiParser.parseToUnicode(":angel:");

    public static String getMention(String userName) {
        return "@" + userName;
    }

    public static String getMention (User user) {
        return getMention(user.getUserName());
    }

}
