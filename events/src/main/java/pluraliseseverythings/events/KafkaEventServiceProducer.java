package pluraliseseverythings.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pluraliseseverythings.events.api.Event;

import java.util.concurrent.ExecutionException;

public class KafkaEventServiceProducer implements EventServiceProducer {
    private static Logger LOG = LoggerFactory.getLogger(KafkaEventServiceProducer.class);
    private static ObjectMapper MAPPER = new ObjectMapper();
    private KafkaProducer<String, String> producer;

    public KafkaEventServiceProducer(KafkaProducer<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public void saveEvent(Event event) throws JsonProcessingException, SaveEventException {
        try {
            producer.send(new ProducerRecord<>(event.getType(), event.getKey(), MAPPER.writeValueAsString(event)),
                    (metadata, e) -> {
                        if (e != null) {
                            LOG.error("Error while sending event {}", event, e);
                        } else {
                            LOG.info("Offset {} for event with type {} and key {}", metadata.offset(), event.getType(), event.getKey());
                        }
                    }).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new SaveEventException(e);
        }
    }

}
