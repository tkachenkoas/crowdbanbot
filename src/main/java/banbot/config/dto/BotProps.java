package banbot.config.dto;

public class BotProps {

    private String token;
    private Integer botUserId;
    private String botName;
    private Boolean testMode;
    public BotProps(String token, Integer botUserId, String botName, Boolean testMode) {
        this.token = token;
        this.botUserId = botUserId;
        this.botName = botName;
        this.testMode = testMode;
    }

    public String getToken() {
        return token;
    }

    public Integer getBotUserId() {
        return botUserId;
    }

    public String getBotName() {
        return botName;
    }

    public Boolean getTestMode() {
        return testMode != null && testMode;
    }

}
