package banbot.config.app;

import java.io.FileInputStream;
import java.util.Properties;

public class MessagesConfig {

    private static final String WELCOME_MESSAGE = "botDesc";
    private static final String NOT_ADMIN_MESSAGE = "notAdminMessage";
    private static final String DECISION_VOTES_COUNT = "decisionVoteCount";
    private static final String BAN_MESSAGE ="banMessage";
    private static final String SPARE_MESSAGE ="spareMessage";
    private static final String TRIAL_EXPIRATION_TIME ="trialExpirationTimeInMinutes";
    private static final String TRIAL_EXPIRED_MESSAGE = "trialExpiredMessage";
    private static final String TRIAL_INIT_MESSAGE = "trialInitMessage";

    private static Properties properties = new Properties();

    private MessagesConfig(){}

    static {
        try (FileInputStream fis =  new FileInputStream("src/main/resources/messages.properties")) {
            properties.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static int getDecisionVotesCount(){
        return Integer.parseInt(getProperty(DECISION_VOTES_COUNT));
    }

    public static String getWelcomeMessage(){
        return getProperty(WELCOME_MESSAGE);
    }

    public static String getBanMessage() {
        return getProperty(BAN_MESSAGE);
    }

    public static String getSpareMessage() {
        return getProperty(SPARE_MESSAGE);
    }

    public static String getTrialExpiredMessage(){
        return getProperty(TRIAL_EXPIRED_MESSAGE);
    }

    public static int getTrialExpirationTime(){
        return Integer.parseInt(getProperty(TRIAL_EXPIRATION_TIME));
    }
    public static String getNotAdminMessage(){
        return getProperty(NOT_ADMIN_MESSAGE);
    }

    public static String getTrialInitMessage() {
        return getProperty(TRIAL_INIT_MESSAGE);
    }
}
