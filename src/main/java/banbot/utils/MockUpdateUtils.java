package banbot.utils;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockUpdateUtils {

    private final String TAG = getClass().getSimpleName();
    private List<User> mockUserList = new ArrayList<>(20);
    private String superAdmin;

    public MockUpdateUtils(String superAdmin) {
        mockUserList = new ArrayList<>(20);
        for (int i = 0; i < 20; i++ ) {
            mockUserList.add(getMockUser(new Random().nextInt(100)));
        }
        this.superAdmin = superAdmin;
    }

    private User getMockUser(int id) {
        return new User(id, "name"+id, false, "lastname"+id, "username"+id, "en" );
    }

    public void mockUpdate(Update update) {
        if (!superAdmin.equalsIgnoreCase(update.getCallbackQuery().getFrom().getUserName())) return;

        CallbackQuery callback = update.getCallbackQuery();
        try {
            Field from = callback.getClass().getDeclaredField("from");
            from.setAccessible(true);

            User mockUser = mockUserList.get(new Random().nextInt(mockUserList.size()));
            from.set(callback, mockUser);

            BotLogger.info(TAG, "Assigned new user: " + callback.getFrom());
        } catch (Exception e) {
            BotLogger.error(TAG, e);
        }

    }

}
