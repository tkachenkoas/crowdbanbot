package banbot.repository;

import banbot.entity.trial.Trial;
import banbot.entity.trial.TrialKey;
import com.google.common.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CacheTrialRepository implements TrialRepository {

    @Autowired
    private Cache<TrialKey, Trial> trialCache;

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
