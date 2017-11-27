package pluraliseseverythings.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pluraliseseverythings.events.api.Event;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class KafkaEventServiceConsumer implements EventServiceConsumer {
    private static Logger LOG = LoggerFactory.getLogger(KafkaEventServiceConsumer.class);
    private static ObjectMapper MAPPER = new ObjectMapper();
    private KafkaConsumer<String, String> consumer;

    public KafkaEventServiceConsumer(KafkaConsumer<String, String> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Runnable consume(String topic, Consumer<Event> consumerFunction, ExecutorService executorService) {
        return () -> {
            try {
                consumer.subscribe(Collections.singleton(topic));
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                    for (ConsumerRecord<String, String> record : records) {
                        try {
                            MAPPER.readValue(record.value(), Event.class);
                        } catch (IOException e) {
                            // TODO this is a classic data loss point. You change serialization
                            // and all the data is discarded here. We are stopping consuming here
                            // and we should raise an alert.
                            LOG.error("Could not deserialize {}", record.value(), e);
                            break;
                        }
                    }
                }
            } catch (WakeupException e) {
                // ignore for shutdown
            } finally {
                consumer.close();
            }
        };
    }


    public void shutdown() {
        consumer.wakeup();
    }
}
