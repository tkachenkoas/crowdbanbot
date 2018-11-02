package banbot.utils;

import banbot.config.app.BotConfig;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MockUpdateUtils {

    private static final String TAG = "MockUpdateUtils";
    private static Map<Integer, User> mockUserMap = new HashMap<>();

    static {
        for (int i = 0; i < 20; i++ ) {
            mockUserMap.put(mockUserMap.size(), getMockUser(new Random().nextInt(100)));
        }
    }

    private static User getMockUser(int id) {
        return new User(id, "name"+id, false, "lastname"+id, "username"+id, "en" );
    }

    public static void mockUpdate(Update update) {
        if (!BotConfig.isTestMode() || update.getCallbackQuery() == null
                || !BotConfig.getSuperAdmin().equalsIgnoreCase(update.getCallbackQuery().getFrom().getUserName())) return;

        CallbackQuery callback = update.getCallbackQuery();
        try {
            Field from = callback.getClass().getDeclaredField("from");
            from.setAccessible(true);

            User mockUser = mockUserMap.get(new Random().nextInt(mockUserMap.size()));
            from.set(callback, mockUser);

            BotLogger.info(TAG, "Assigned new user: " + callback.getFrom());
        } catch (Exception e) {
            BotLogger.error(TAG, e);
        }

    }

}
