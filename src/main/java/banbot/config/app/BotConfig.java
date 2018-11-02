package banbot.config.app;

import org.apache.commons.lang3.BooleanUtils;

import java.util.Properties;

public class BotConfig {

    private static String API_KEY = System.getenv("API_KEY");
    private static String BOT_NAME = System.getenv("BOT_NAME");
    private static String TEST_MODE = System.getenv("TEST_MODE");
    private static String BOT_CHAT_ID = System.getenv("BOT_CHAT_ID");
    private static String BOT_USER_ID = System.getenv("BOT_USER_ID");
    private static String SUPER_ADMIN = System.getenv("SUPER_ADMIN");

    private static Properties properties = new Properties();

    private BotConfig(){}


    public static String getSuperAdmin(){return SUPER_ADMIN;}
    public static String getBotUsername() {
        return BOT_NAME;
    }
    public static String getApiKey(){
        return API_KEY;
    }
    public static Long getOwnChatId(){
        return Long.parseLong(BOT_CHAT_ID);
    }
    public static int getBotUserId() {return Integer.parseInt(BOT_USER_ID);}
    public static boolean isTestMode() {return BooleanUtils.toBoolean(TEST_MODE); }


}
