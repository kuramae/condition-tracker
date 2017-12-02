package pluraliseseverythings.events;

import com.fasterxml.jackson.core.type.TypeReference;
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
    public <T> Runnable consume(String topic, Consumer<Event<T>> consumerFunction, ExecutorService executorService, Class<T> klass) {
        return () -> {
            try {
                LOG.info("Subscribing to topic {}", topic);
                LOG.info("Topics available {}", consumer.listTopics());
                consumer.subscribe(Collections.singleton(topic));

                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                    LOG.info("Consuming {} records", records.count());
                    for (ConsumerRecord<String, String> record : records) {
                        try {
                            if (record.value() == null) {
                                LOG.error("Null value for record {}", record);
                            } else {
                                Event<T> value = MAPPER.readValue(record.value(),
                                        MAPPER.getTypeFactory().constructType(Event.class, klass)
                                );
                                consumerFunction.accept(value);
                            }
                        } catch (IOException e) {
                            // TODO this is a classic data loss point. You change serialization
                            // and all the data is discarded here. We are stopping consuming here
                            // and we should raise an alert.
                            LOG.error("Could not deserialize {}", record.value(), e);
                            break;
                        }
                    }
                    consumer.commitAsync();
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
