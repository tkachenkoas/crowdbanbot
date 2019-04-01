package banbot.config;

import banbot.config.dto.BotProps;
import banbot.entity.trial.Trial;
import banbot.entity.trial.TrialKey;
import banbot.utils.MessageTextProvider;
import banbot.utils.MockUpdateUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class Beans {

    @Value("${trial.expire.minutes}")
    private Long trialExpireTime;

    @Bean
    public Cache<TrialKey, Trial> trialCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(trialExpireTime, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public MessageTextProvider textProvider(MessageSource messageSource) {
        return new MessageTextProvider(messageSource);
    }

    @Value("${super-admin}")
    private String superAdmin;

    @Bean
    public MockUpdateUtils mockUpdateUtils() {
        return new MockUpdateUtils(superAdmin);
    }

    @Value("${api.key}")
    private String token;
    @Value("${bot.user.id}")
    private Integer botUserId;
    @Value("${bot.name}")
    private String botName;
    @Value("${test-mode}")
    private Boolean testMode;

    @Bean
    public BotProps botProps() {
        return new BotProps(token, botUserId, botName, testMode);
    }

}
