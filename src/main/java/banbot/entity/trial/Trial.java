package banbot.entity.trial;


import banbot.config.MessagesConfig;
import banbot.exceptions.TGBotException;
import banbot.exceptions.TrialActionException;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** An entity representin trial on a user: contain information about user,
 *  votecount (pro/con) and ids of those who voted to ban/spare the suspect
 */
public class Trial {

    private final int DECISION_VOTES_COUNT = MessagesConfig.getDecisionVotesCount();

    private TrialKey key;
    private Integer pollMessageId;
    private User userOnTrial;
    private Set<User> banVoters = new HashSet<>();
    private Set<User> spareVoters = new HashSet<>();
    private TrialState state = TrialState.ON_TRIAL;

    public Trial(long chatId, User userOnTrial) {
        this.userOnTrial = userOnTrial;
        this.key = new TrialKey(chatId, userOnTrial.getId());
    }

    private void addBanVote(User user) {
        banVoters.add(user);
        spareVoters.remove(user);
        if (banVoters.size() == DECISION_VOTES_COUNT) state = TrialState.BAN;
    }

    private void addSpareVote(User user) {
        banVoters.remove(user);
        spareVoters.add(user);
        if (spareVoters.size() == DECISION_VOTES_COUNT) state = TrialState.SPARE;
    }

    public void performAction (TrialAction action, User user) {
        if (!isOnTrial()) throw new TGBotException("Trial" + key + " is already finished");
        if (user.equals(userOnTrial)) throw new TrialActionException("your own vote doesn't count");

        if (action == TrialAction.BAN) {
            if (banVoters.contains(user)) throw new TrialActionException("you have already voted to ban " + userOnTrial.getUserName());
            addBanVote(user);
        } else {
            if (spareVoters.contains(user)) throw new TrialActionException("you have already voted to spare" + userOnTrial.getUserName());
            addSpareVote(user);
        }
    }

    public long getChatId(){
        return getKey().getChatId();
    }

    public Integer getPollMessageId() {
        return pollMessageId;
    }

    public void setPollMessageId(Integer pollMessageId) {
        this.pollMessageId = pollMessageId;
    }

    public User getUserOnTrial() {
        return userOnTrial;
    }

    public TrialState getState() {
        return state;
    }

    public int getBanCount(){
        return banVoters.size();
    }

    public int getSpareCount(){
        return spareVoters.size();
    }

    public boolean isNew(){
        return getSpareCount() + getBanCount() == 0;
    }

    public Set<User> getBanVoters() {
        return Collections.unmodifiableSet(banVoters);
    }

    public Set<User> getSpareVoters() {
        return Collections.unmodifiableSet(spareVoters);
    }

    public TrialKey getKey() {
        return key;
    }

    public boolean isOnTrial(){
        return state == TrialState.ON_TRIAL;
    }



}
