package banbot.service.handlers;

import banbot.entity.common.HandleResult;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public interface UpdateHandler {

    HandleResult handle(Update update);

}
