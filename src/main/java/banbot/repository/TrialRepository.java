package banbot.repository;

import banbot.entity.trial.Trial;
import banbot.entity.trial.TrialKey;

public interface TrialRepository {

    Trial getTrial(TrialKey key);
    void saveTrial(Trial trial);
    void removeTrial(Trial trial);

}
