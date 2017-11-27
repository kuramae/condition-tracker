package com.github.pluraliseseverythings.conditio.core;

import com.github.pluraliseseverythings.conditio.db.ConditionStatsDAO;
import com.github.pluraliseseverythings.medi.api.PatientCondition;
import pluraliseseverythings.events.api.Event;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

public class StatsComputer {
    private ConditionStatsDAO conditionStatsDAO;

    public StatsComputer(ConditionStatsDAO conditionStatsDAO) {
        this.conditionStatsDAO = conditionStatsDAO;
    }

    /**
     * The consumer returned creates buckets of 1 second.
     * If the bucket exists, it adds the new condition and overrides.
     * This also uses optimistic concurrency control.
     * TODO: A clever move would be to have kafka partitions based on the timestamp
     * this way we'd avoid conflicts completely and we could cache a lot.
     * @return Returns a consumer that knows what to do with the events
     */
    public Consumer<Event> eventConsumer() {
        return (event) -> {
            // TODO make the interval configurable
            long bucket = Duration.of(event.getTimestamp(), ChronoUnit.MILLIS).getSeconds();
            PatientCondition content = (PatientCondition) event.getContent();
            conditionStatsDAO.addPatientCondition(content, bucket, event.getTimestamp());
        };
    }
}
