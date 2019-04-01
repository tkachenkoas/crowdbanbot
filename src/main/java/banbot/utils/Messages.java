package banbot.utils;

public enum Messages {

    WELCOME_MESSAGE("botDesc"),
    NOT_ADMIN_MESSAGE("notAdminMessage"),
    BAN_MESSAGE("banMessage"),
    SPARE_MESSAGE("spareMessage"),
    TRIAL_EXPIRED_MESSAGE("trialExpiredMessage"),
    TRIAL_INIT_MESSAGE ("trialInitMessage");

    private String code;

    Messages(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
