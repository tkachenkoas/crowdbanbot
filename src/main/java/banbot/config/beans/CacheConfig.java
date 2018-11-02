package banbot.config.beans;

import banbot.config.app.MessagesConfig;
import banbot.entity.trial.Trial;
import banbot.entity.trial.TrialKey;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<TrialKey, Trial> trialCache() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(MessagesConfig.getTrialExpirationTime(), TimeUnit.MINUTES)
                .build();
    }
}
