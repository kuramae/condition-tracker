package com.github.pluraliseseverythings.conditio.core;

import com.github.pluraliseseverythings.conditio.db.ConditionStatsDAO;
import com.github.pluraliseseverythings.medi.api.PatientCondition;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pluraliseseverythings.events.KafkaEventServiceProducer;
import pluraliseseverythings.events.api.Event;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

public class StatsComputer {
    private static Logger LOG = LoggerFactory.getLogger(StatsComputer.class);

    private ConditionStatsDAO conditionStatsDAO;

    public StatsComputer(ConditionStatsDAO conditionStatsDAO) {
        this.conditionStatsDAO = conditionStatsDAO;
    }

    /**
     * See the addPatientCondition for the logic
     * @return Returns a consumer that knows what to do with the events
     */
    public Consumer<Event<PatientCondition>> eventConsumer() {
        return (event) -> {
            LOG.info("Processing event {}", event);
            // TODO make the interval configurable
            Object jsonContent = event.getContent();
            PatientCondition content;
            // TODO can simplify this by reading the documentation for Jackson on generics
            if (jsonContent instanceof PatientCondition) {
                content = (PatientCondition) jsonContent;
            } else {
                Map<String, String> jsonContentMap = (Map<String, String>) jsonContent;
                content = PatientCondition.builder().conditionId(jsonContentMap.get("condition_id")).patientId(jsonContentMap.get("patient_id")).build();
            }
            conditionStatsDAO.addPatientCondition(content, event.getTimestamp());
        };
    }
}
