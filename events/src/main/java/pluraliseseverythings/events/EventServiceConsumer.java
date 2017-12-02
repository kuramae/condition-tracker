package pluraliseseverythings.events;

import pluraliseseverythings.events.api.Event;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public interface EventServiceConsumer {
    <T> Runnable consume(String alert, Consumer<Event<T>> consumer, ExecutorService executorService, Class<T> klass);
}
