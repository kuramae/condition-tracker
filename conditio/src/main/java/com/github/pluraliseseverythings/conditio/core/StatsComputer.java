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
     * See the addPatientCondition for the logic
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
