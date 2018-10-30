package banbot.repository;

import banbot.config.MessagesConfig;
import banbot.entity.trial.Trial;
import banbot.entity.trial.TrialKey;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheTrialRepository implements TrialRepository {

    private Cache<TrialKey, Trial> trialCache;

    public CacheTrialRepository() {
        trialCache = CacheBuilder.newBuilder()
                .expireAfterAccess(MessagesConfig.getTrialExpirationTime(), TimeUnit.MINUTES)
                .build();
    }

    @Override
    public Trial getTrial(TrialKey key) {
        return trialCache.getIfPresent(key);
    }

    @Override
    public void saveTrial(Trial trial) {
        trialCache.put(trial.getKey(), trial);
    }

    @Override
    public void removeTrial(Trial trial) {
        trialCache.invalidate(trial.getKey());
    }
}
