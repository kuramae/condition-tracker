package pluraliseseverythings.events;

import pluraliseseverythings.events.api.Event;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public interface EventServiceConsumer {
    Runnable consume(String alert, Consumer<Event> consumer, ExecutorService executorService);
}
