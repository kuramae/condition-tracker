package pluraliseseverythings.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import pluraliseseverythings.events.api.Event;

public interface EventServiceProducer {
    void saveEvent(Event event) throws JsonProcessingException, SaveEventException;
}
