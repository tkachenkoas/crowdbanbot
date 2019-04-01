package banbot.service.handlers;

import banbot.entity.common.HandleResult;
import banbot.entity.controller.CrowdBanBot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public abstract class AbstractUpdateHandler implements UpdateHandler {

    @Override
    public HandleResult handle(Update update) {
        if (!canHandle(update)) return HandleResult.NOT_APPLIED;
        process(update);
        return HandleResult.TERMINAL;
    }

    protected abstract boolean canHandle(Update update);

    protected abstract void process(Update update);

}